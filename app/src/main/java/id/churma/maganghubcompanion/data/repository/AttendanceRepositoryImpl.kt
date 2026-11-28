package id.churma.maganghubcompanion.data.repository

import android.util.Log
import id.churma.maganghubcompanion.BuildConfig
import id.churma.maganghubcompanion.data.api.AttendanceItem
import id.churma.maganghubcompanion.data.api.FeedbackApiClient
import id.churma.maganghubcompanion.data.api.FeedbackApiClientImpl
import id.churma.maganghubcompanion.data.api.MagangHubApiClient
import id.churma.maganghubcompanion.data.api.UnauthorizedSessionException
import id.churma.maganghubcompanion.data.api.UpdateApiClient
import id.churma.maganghubcompanion.data.api.UpdateApiClientImpl
import id.churma.maganghubcompanion.data.model.AttendanceCheckResult
import id.churma.maganghubcompanion.data.model.AttendanceHomeData
import id.churma.maganghubcompanion.data.model.AttendanceStatus
import id.churma.maganghubcompanion.data.model.FeedbackRequest
import id.churma.maganghubcompanion.data.model.FeedbackResponse
import id.churma.maganghubcompanion.data.model.ReminderSettings
import id.churma.maganghubcompanion.data.model.ThemeMode
import id.churma.maganghubcompanion.data.model.UpdateCheckState
import id.churma.maganghubcompanion.data.model.UserProfileData
import id.churma.maganghubcompanion.data.model.UserSession
import id.churma.maganghubcompanion.data.storage.EncryptedPreferences
import id.churma.maganghubcompanion.util.VersionComparator
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

private data class AttendanceFetchTier(
    val name: String,
    val participantId: String?,
    val startDate: String?,
    val endDate: String?,
    val limit: Int
)

/**
 * Concrete implementation of AttendanceRepository managing network sync,
 * secure credential storage, and automatic session recovery.
 */
class AttendanceRepositoryImpl(
    private val apiClient: MagangHubApiClient,
    private val storage: EncryptedPreferences,
    private val feedbackApiClient: FeedbackApiClient = FeedbackApiClientImpl(),
    private val updateApiClient: UpdateApiClient = UpdateApiClientImpl(),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : AttendanceRepository {

    companion object {
        private const val TAG = "AttendanceRepo"
        const val MENTOR_CHECK_TTL_MILLIS = 3 * 60 * 60 * 1000L // 3 hours

        /**
         * Pure function to determine whether mentor review fetch should be executed.
         * Throttled to at most once per 3 hours to conserve battery and API bandwidth.
         */
        fun isMentorCheckDue(
            currentTimeMillis: Long,
            lastCheckMillis: Long,
            ttlMillis: Long = MENTOR_CHECK_TTL_MILLIS
        ): Boolean {
            if (lastCheckMillis <= 0L) return true
            val elapsed = currentTimeMillis - lastCheckMillis
            if (elapsed < 0L) return true // Clock drifted or reset
            return elapsed >= ttlMillis
        }

        /**
         * Resolves effective end date for fetching attendances:
         * Uses earlier of todayDate or internshipEndDate (matching Nuxt frontend logic: min(today, end_date)).
         */
        fun resolveEffectiveEndDate(
            todayDate: String?,
            internshipEndDate: String?
        ): String? {
            val cleanToday = todayDate?.trim()?.take(10)?.takeIf { it.isNotBlank() }
            val cleanEnd = internshipEndDate?.trim()?.take(10)?.takeIf { it.isNotBlank() }
            return when {
                cleanToday != null && cleanEnd != null -> minOf(cleanToday, cleanEnd)
                cleanToday != null -> cleanToday
                else -> cleanEnd
            }
        }

        fun isRecoverableWithFallback(e: Exception): Boolean {
            if (e is CancellationException || e is UnauthorizedSessionException) return false
            if (e is java.net.SocketTimeoutException || e is java.net.UnknownHostException || e is java.net.ConnectException) {
                return false
            }
            return true
        }

        fun maskUsername(raw: String): String {
            val trimmed = raw.trim()
            if (trimmed.isEmpty()) return "***"
            return if (trimmed.length <= 4) {
                "***"
            } else {
                "***" + trimmed.takeLast(4)
            }
        }
    }

    private val authMutex = Mutex()

    private val _sessionFlow = MutableStateFlow(storage.getUserSession())
    override val sessionFlow: StateFlow<UserSession> = _sessionFlow.asStateFlow()

    private val _reminderSettingsFlow = MutableStateFlow(storage.getReminderSettings())
    override val reminderSettingsFlow: StateFlow<ReminderSettings> = _reminderSettingsFlow.asStateFlow()

    private val _themeModeFlow = MutableStateFlow(storage.getThemeMode())
    override val themeModeFlow: StateFlow<ThemeMode> = _themeModeFlow.asStateFlow()

    override suspend fun login(username: String, password: String): Result<String> =
        withContext(ioDispatcher) {
            authMutex.withLock {
                try {
                    val token = apiClient.loginWithCredentials(username, password)
                    storage.saveCredentials(username, password)
                    storage.saveAccessToken(token)
                    storage.resetLoginFailureCount()
                    try {
                        val profile = apiClient.fetchUserProfile(token)
                        storage.saveUserProfile(
                            fullName = profile.name,
                            photoUrl = profile.photoUrl,
                            participantId = profile.id.takeIf { it.isNotBlank() },
                            internshipStartDate = profile.internshipStartDate?.takeIf { it.isNotBlank() },
                            internshipEndDate = profile.internshipEndDate?.takeIf { it.isNotBlank() }
                        )
                    } catch (pe: Exception) {
                        Log.w(TAG, "Best-effort profile fetch after login failed: ${pe.message}")
                    }
                    _sessionFlow.value = storage.getUserSession()
                    Log.i(TAG, "Successfully authenticated user ${maskUsername(username)} with Kemnaker SSO")
                    Result.success(token)
                } catch (e: Exception) {
                    if (e is id.churma.maganghubcompanion.data.api.KemnakerAuthException) {
                        storage.incrementLoginFailureCount()
                    }
                    Log.e(TAG, "Login failed for user ${maskUsername(username)} (failure count: ${storage.getLoginFailureCount()})", e)
                    Result.failure(e)
                }
            }
        }

    override suspend fun checkAttendance(forceRefresh: Boolean): Result<AttendanceCheckResult> =
        withContext(ioDispatcher) {
            try {
                var currentToken = storage.getAccessToken()

                // If no token exists, attempt auto-login with stored credentials
                if (currentToken.isNullOrBlank()) {
                    val savedUsername = storage.getUsername()
                    val savedPassword = storage.getPassword()
                    if (savedUsername.isNullOrBlank() || savedPassword.isNullOrBlank()) {
                        return@withContext Result.failure(
                            UnauthorizedSessionException("Belum login. Masukkan kredensial Kemnaker terlebih dahulu.")
                        )
                    }

                    if (storage.isLoginCircuitOpen()) {
                        Log.w(TAG, "SSO auto-login circuit breaker is open. Halting auto-login.")
                        return@withContext Result.failure(
                            UnauthorizedSessionException("Auto-login ditangguhkan sementara demi keamanan akun Kemnaker. Silakan login manual di aplikasi.")
                        )
                    }

                    Log.i(TAG, "No active token found; attempting auto-login with stored credentials")
                    val loginResult = login(savedUsername, savedPassword)
                    if (loginResult.isFailure) {
                        return@withContext Result.failure(
                            loginResult.exceptionOrNull()
                                ?: UnauthorizedSessionException("Auto-login failed")
                        )
                    }
                    currentToken = storage.getAccessToken()
                }

                // Attempt to fetch attendance status with current token
                val homeData = try {
                    apiClient.fetchTodayAttendanceStatus(currentToken.orEmpty())
                } catch (e: UnauthorizedSessionException) {
                    // Token expired: attempt automatic re-login if credentials exist
                    val savedUsername = storage.getUsername()
                    val savedPassword = storage.getPassword()
                    if (savedUsername.isNullOrBlank() || savedPassword.isNullOrBlank()) {
                        throw e
                    }

                    if (storage.isLoginCircuitOpen()) {
                        Log.w(TAG, "SSO auto-login circuit breaker is open. Halting re-authentication.")
                        throw UnauthorizedSessionException("Auto-login ditangguhkan sementara demi keamanan akun Kemnaker. Silakan login manual di aplikasi.")
                    }

                    Log.w(TAG, "Access token expired (401). Attempting re-authentication with Kemnaker SSO...")
                    val reloginResult = login(savedUsername, savedPassword)
                    if (reloginResult.isFailure) {
                        throw reloginResult.exceptionOrNull() ?: e
                    }

                    val refreshedToken = storage.getAccessToken().orEmpty()
                    currentToken = refreshedToken
                    apiClient.fetchTodayAttendanceStatus(refreshedToken)
                }

                val status = homeData.toAttendanceStatus()
                val now = System.currentTimeMillis()
                storage.saveAttendanceCache(homeData, status, now)

                var participantId = storage.getParticipantId()
                var startDate = storage.getInternshipStartDate()
                var endDate = storage.getInternshipEndDate()

                if (forceRefresh || participantId.isNullOrBlank() || startDate.isNullOrBlank() || endDate.isNullOrBlank() || storage.getFullName().isNullOrBlank()) {
                    try {
                        val validToken = storage.getAccessToken().orEmpty()
                        if (validToken.isNotBlank()) {
                            val profile = apiClient.fetchUserProfile(validToken)
                            storage.saveUserProfile(
                                fullName = profile.name,
                                photoUrl = profile.photoUrl,
                                participantId = profile.id.takeIf { it.isNotBlank() },
                                internshipStartDate = profile.internshipStartDate?.takeIf { it.isNotBlank() },
                                internshipEndDate = profile.internshipEndDate?.takeIf { it.isNotBlank() }
                            )
                            if (profile.id.isNotBlank()) participantId = profile.id
                            if (!profile.internshipStartDate.isNullOrBlank()) startDate = profile.internshipStartDate
                            if (!profile.internshipEndDate.isNullOrBlank()) endDate = profile.internshipEndDate
                            Log.i(TAG, "Profile refreshed: pId=$participantId, start=$startDate, end=$endDate")
                        }
                    } catch (pe: Exception) {
                        Log.w(TAG, "Best-effort profile fetch during check failed: ${pe.message}")
                    }
                }

                // Best-effort fetch attendances to calculate and cache pending mentor review count.
                // Throttled to at most once every 3 hours to conserve battery and API bandwidth,
                // but respects forceRefresh = true for explicit manual user refreshes.
                var pendingCount = storage.getPendingMentorReviewCount()
                var approvedCount = 0
                val lastMentorCheck = storage.getLastMentorCheckTimestamp()
                val isDue = isMentorCheckDue(
                    currentTimeMillis = now,
                    lastCheckMillis = lastMentorCheck,
                    ttlMillis = MENTOR_CHECK_TTL_MILLIS
                )

                var debugMsg = storage.getLastMentorSyncDebug()
                if (forceRefresh || isDue) {
                    try {
                        val validToken = storage.getAccessToken().orEmpty()
                        if (validToken.isNotBlank()) {
                            val effectiveEndDate = resolveEffectiveEndDate(
                                todayDate = homeData.date,
                                internshipEndDate = endDate
                            )
                            val tiers = listOf(
                                AttendanceFetchTier("full_500", participantId, startDate?.take(10), effectiveEndDate, 500),
                                AttendanceFetchTier("full_100", participantId, startDate?.take(10), effectiveEndDate, 100),
                                AttendanceFetchTier("pid_only", participantId, null, null, 100),
                                AttendanceFetchTier("bare", null, null, null, 100)
                            )
                            var attendances = emptyList<AttendanceItem>()
                            var fetchError: String? = null

                            for ((index, tier) in tiers.withIndex()) {
                                try {
                                    attendances = apiClient.fetchAttendances(
                                        monevAccessToken = validToken,
                                        participantId = tier.participantId,
                                        startDate = tier.startDate,
                                        endDate = tier.endDate,
                                        limit = tier.limit
                                    )
                                    fetchError = null
                                    break
                                } catch (e: Exception) {
                                    if (e is CancellationException) throw e
                                    if (e is UnauthorizedSessionException) throw e
                                    fetchError = e.message ?: e.javaClass.simpleName
                                    if (!isRecoverableWithFallback(e) || index == tiers.lastIndex) {
                                        Log.w(TAG, "fetchAttendances aborted on tier ${tier.name}: $fetchError")
                                        break
                                    }
                                    Log.w(TAG, "fetchAttendances tier ${tier.name} failed ($fetchError), attempting next tier...")
                                }
                            }

                            if (fetchError == null) {
                                val previousPending = storage.getPendingMentorReviewCount()
                                pendingCount = apiClient.calculatePendingMentorReviewCount(attendances)
                                if (previousPending > 0 && pendingCount < previousPending) {
                                    approvedCount = previousPending - pendingCount
                                    Log.i(TAG, "Detected mentor approval: previous=$previousPending, current=$pendingCount, approved=$approvedCount")
                                }
                                storage.savePendingMentorReviewCount(pendingCount)
                                storage.saveLastMentorCheckTimestamp(now)
                                val pIdMask = if (!participantId.isNullOrBlank()) "${participantId.take(8)}.." else "null"
                                debugMsg = "pId=$pIdMask, ${startDate?.take(10)}..$effectiveEndDate, fetched=${attendances.size}, pending=$pendingCount"
                                storage.saveLastMentorSyncDebug(debugMsg)
                                Log.i(TAG, "Refreshed pending mentor review count: $pendingCount ($debugMsg)")
                            } else {
                                val pIdMask = if (!participantId.isNullOrBlank()) "${participantId.take(8)}.." else "null"
                                debugMsg = "pId=$pIdMask, err=$fetchError"
                                storage.saveLastMentorSyncDebug(debugMsg)
                            }
                        }
                    } catch (ae: Exception) {
                        if (ae is CancellationException) throw ae
                        if (ae is UnauthorizedSessionException) throw ae
                        Log.w(TAG, "Best-effort attendances fetch for mentor review count failed: ${ae.message}")
                    }
                } else {
                    Log.d(TAG, "Skipping mentor review fetch (throttled, last check: ${(now - lastMentorCheck) / 60000} mins ago)")
                }

                _sessionFlow.value = storage.getUserSession()

                val result = AttendanceCheckResult(
                    status = status,
                    data = homeData,
                    checkedAt = now,
                    isSuccess = true,
                    pendingMentorReviewCount = pendingCount,
                    approvedMentorReviewCount = approvedCount,
                    mentorSyncDebug = debugMsg
                )
                Result.success(result)
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Log.e(TAG, "Attendance check failed", e)
                val cachedStatus = storage.getLastAttendanceStatus()
                val cachedData = storage.getLastHomeData()
                val cachedTime = storage.getLastCheckTimestamp()
                val cachedPendingCount = storage.getPendingMentorReviewCount()
                val cachedDebug = storage.getLastMentorSyncDebug()

                val fallbackResult = AttendanceCheckResult(
                    status = cachedStatus,
                    data = cachedData,
                    checkedAt = if (cachedTime > 0) cachedTime else System.currentTimeMillis(),
                    isSuccess = false,
                    errorMessage = e.message ?: "Gagal mengambil data kehadiran",
                    pendingMentorReviewCount = cachedPendingCount,
                    mentorSyncDebug = cachedDebug
                )
                Result.failure(e)
            }
        }

    override suspend fun logout() {
        withContext(ioDispatcher) {
            storage.clearAll()
            _sessionFlow.value = storage.getUserSession()
            Log.i(TAG, "Logged out and cleared all session data")
        }
    }

    override suspend fun updateReminderSettings(settings: ReminderSettings) {
        withContext(ioDispatcher) {
            storage.saveReminderSettings(settings)
            _reminderSettingsFlow.value = settings
        }
    }

    override suspend fun setThemeMode(themeMode: ThemeMode) {
        withContext(ioDispatcher) {
            storage.saveThemeMode(themeMode)
            _themeModeFlow.value = themeMode
        }
    }

    override suspend fun refreshUserProfile(): Result<UserProfileData> =
        withContext(ioDispatcher) {
            try {
                val token = storage.getAccessToken()
                if (token.isNullOrBlank()) {
                    return@withContext Result.failure(UnauthorizedSessionException("Belum login"))
                }
                val profile = apiClient.fetchUserProfile(token)
                storage.saveUserProfile(
                    fullName = profile.name,
                    photoUrl = profile.photoUrl,
                    participantId = profile.id.takeIf { it.isNotBlank() },
                    internshipStartDate = profile.internshipStartDate?.takeIf { it.isNotBlank() },
                    internshipEndDate = profile.internshipEndDate?.takeIf { it.isNotBlank() }
                )
                _sessionFlow.value = storage.getUserSession()
                Result.success(profile)
            } catch (e: Exception) {
                Log.w(TAG, "Failed to refresh user profile: ${e.message}")
                Result.failure(e)
            }
        }

    override fun getCachedHomeData(): AttendanceHomeData? = storage.getLastHomeData()

    override fun getCachedStatus(): AttendanceStatus = storage.getLastAttendanceStatus()

    override fun getLastCheckTimestamp(): Long = storage.getLastCheckTimestamp()

    override fun hasSavedCredentials(): Boolean = storage.hasCredentials()

    override suspend fun submitFeedback(request: FeedbackRequest): Result<FeedbackResponse> =
        withContext(ioDispatcher) {
            feedbackApiClient.sendFeedback(request)
        }

    override suspend fun snoozeMentorReminderOneDay() {
        withContext(ioDispatcher) {
            val snoozeUntil = System.currentTimeMillis() + 24 * 60 * 60 * 1000L
            storage.saveMentorSnoozedUntil(snoozeUntil)
            val updatedSettings = storage.getReminderSettings().copy(mentorSnoozedUntil = snoozeUntil)
            storage.saveReminderSettings(updatedSettings)
            _reminderSettingsFlow.value = updatedSettings
            Log.i(TAG, "Snoozed mentor review reminder until $snoozeUntil")
        }
    }

    override fun getCachedPendingMentorReviewCount(): Int = storage.getPendingMentorReviewCount()

    override fun getLastMentorSyncDebug(): String? = storage.getLastMentorSyncDebug()

    override suspend fun checkForUpdates(): Result<UpdateCheckState> = withContext(ioDispatcher) {
        val releaseResult = updateApiClient.getLatestRelease()
        releaseResult.fold(
            onSuccess = { info ->
                storage.saveLastUpdateCheckTimestamp(System.currentTimeMillis())
                if (info == null) {
                    // No newer stable release published yet -> current build is up to date
                    Result.success(UpdateCheckState.UpToDate)
                } else {
                    val updateAvailable = VersionComparator.isUpdateAvailable(BuildConfig.VERSION_NAME, info.versionName)
                    if (updateAvailable) {
                        Result.success(UpdateCheckState.Available(info))
                    } else {
                        Result.success(UpdateCheckState.UpToDate)
                    }
                }
            },
            onFailure = { e ->
                // Do not update timestamp on network/API failure so last successful check is preserved
                Result.failure(e)
            }
        )
    }

    override fun getLastUpdateCheckTimestamp(): Long = storage.getLastUpdateCheckTimestamp()
}
