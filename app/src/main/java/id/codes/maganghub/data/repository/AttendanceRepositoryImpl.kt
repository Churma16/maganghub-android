package id.codes.maganghub.data.repository

import android.util.Log
import id.codes.maganghub.data.api.MagangHubApiClient
import id.codes.maganghub.data.api.UnauthorizedSessionException
import id.codes.maganghub.data.model.AttendanceCheckResult
import id.codes.maganghub.data.model.AttendanceHomeData
import id.codes.maganghub.data.model.AttendanceStatus
import id.codes.maganghub.data.model.ReminderSettings
import id.codes.maganghub.data.model.UserProfileData
import id.codes.maganghub.data.model.UserSession
import id.codes.maganghub.data.storage.EncryptedPreferences
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * Concrete implementation of AttendanceRepository managing network sync,
 * secure credential storage, and automatic session recovery.
 */
class AttendanceRepositoryImpl(
    private val apiClient: MagangHubApiClient,
    private val storage: EncryptedPreferences,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : AttendanceRepository {

    companion object {
        private const val TAG = "AttendanceRepo"
    }

    private val authMutex = Mutex()

    private val _sessionFlow = MutableStateFlow(storage.getUserSession())
    override val sessionFlow: StateFlow<UserSession> = _sessionFlow.asStateFlow()

    private val _reminderSettingsFlow = MutableStateFlow(storage.getReminderSettings())
    override val reminderSettingsFlow: StateFlow<ReminderSettings> = _reminderSettingsFlow.asStateFlow()

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
                        storage.saveUserProfile(profile.name, profile.photoUrl)
                    } catch (pe: Exception) {
                        Log.w(TAG, "Best-effort profile fetch after login failed: ${pe.message}")
                    }
                    _sessionFlow.value = storage.getUserSession()
                    Log.i(TAG, "Successfully authenticated user $username with Kemnaker SSO")
                    Result.success(token)
                } catch (e: Exception) {
                    if (e is id.codes.maganghub.data.api.KemnakerAuthException) {
                        storage.incrementLoginFailureCount()
                    }
                    Log.e(TAG, "Login failed for user $username (failure count: ${storage.getLoginFailureCount()})", e)
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
                    apiClient.fetchTodayAttendanceStatus(refreshedToken)
                }

                val status = homeData.toAttendanceStatus()
                val now = System.currentTimeMillis()
                storage.saveAttendanceCache(homeData, status, now)
                if (storage.getFullName().isNullOrBlank() || storage.getPhotoUrl().isNullOrBlank()) {
                    try {
                        val profile = apiClient.fetchUserProfile(currentToken.orEmpty())
                        storage.saveUserProfile(profile.name, profile.photoUrl)
                    } catch (pe: Exception) {
                        Log.w(TAG, "Best-effort profile fetch during check failed: ${pe.message}")
                    }
                }
                _sessionFlow.value = storage.getUserSession()

                val result = AttendanceCheckResult(
                    status = status,
                    data = homeData,
                    checkedAt = now,
                    isSuccess = true
                )
                Result.success(result)
            } catch (e: Exception) {
                Log.e(TAG, "Attendance check failed", e)
                val cachedStatus = storage.getLastAttendanceStatus()
                val cachedData = storage.getLastHomeData()
                val cachedTime = storage.getLastCheckTimestamp()

                val fallbackResult = AttendanceCheckResult(
                    status = cachedStatus,
                    data = cachedData,
                    checkedAt = if (cachedTime > 0) cachedTime else System.currentTimeMillis(),
                    isSuccess = false,
                    errorMessage = e.message ?: "Gagal mengambil data kehadiran"
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

    override suspend fun refreshUserProfile(): Result<UserProfileData> =
        withContext(ioDispatcher) {
            try {
                val token = storage.getAccessToken()
                if (token.isNullOrBlank()) {
                    return@withContext Result.failure(UnauthorizedSessionException("Belum login"))
                }
                val profile = apiClient.fetchUserProfile(token)
                storage.saveUserProfile(profile.name, profile.photoUrl)
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
}
