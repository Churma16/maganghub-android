package id.codes.maganghub.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import id.codes.maganghub.BuildConfig
import id.codes.maganghub.MagangHubApp
import id.codes.maganghub.background.AttendanceAlarmScheduler
import id.codes.maganghub.background.NotificationHelper
import id.codes.maganghub.data.model.AttendanceCheckResult
import id.codes.maganghub.data.model.AttendanceStatus
import id.codes.maganghub.data.model.FeedbackRequest
import id.codes.maganghub.data.model.FeedbackType
import id.codes.maganghub.data.model.LegalDocumentType
import id.codes.maganghub.data.model.ReminderSettings
import id.codes.maganghub.data.model.ThemeMode
import id.codes.maganghub.data.model.UpdateCheckState
import id.codes.maganghub.data.repository.AttendanceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class AttendanceViewModel(
    private val repository: AttendanceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        AttendanceUiState(
            isLoggedIn = repository.hasSavedCredentials(),
            fullName = repository.sessionFlow.value.fullName,
            photoUrl = repository.sessionFlow.value.photoUrl,
            attendanceStatus = repository.getCachedStatus(),
            attendanceData = repository.getCachedHomeData(),
            pendingMentorReviewCount = repository.getCachedPendingMentorReviewCount(),
            lastSyncTimestamp = repository.getLastCheckTimestamp(),
            reminderSettings = repository.reminderSettingsFlow.value,
            themeMode = repository.themeModeFlow.value,
            lastUpdateCheckTimestamp = repository.getLastUpdateCheckTimestamp()
        )
    )
    val uiState: StateFlow<AttendanceUiState> = _uiState.asStateFlow()

    private val wibZoneId = ZoneId.of("Asia/Jakarta")
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())
    private val fullDateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm 'WIB'", Locale("id", "ID"))

    init {
        // Collect repository session
        viewModelScope.launch {
            repository.sessionFlow.collect { session ->
                _uiState.update { current ->
                    current.copy(
                        isLoggedIn = session.isLoggedIn,
                        username = session.username,
                        fullName = session.fullName,
                        photoUrl = session.photoUrl
                    )
                }
                updateNextReminderString()
            }
        }

        // Collect reminder settings
        viewModelScope.launch {
            repository.reminderSettingsFlow.collect { settings ->
                _uiState.update { current ->
                    current.copy(reminderSettings = settings)
                }
                updateNextReminderString()
            }
        }

        // Collect theme display mode
        viewModelScope.launch {
            repository.themeModeFlow.collect { mode ->
                _uiState.update { it.copy(themeMode = mode) }
            }
        }

        // Initial check if credentials are saved
        if (repository.hasSavedCredentials()) {
            checkAttendanceNow(silent = true)
        } else {
            updateNextReminderString()
        }
    }

    /**
     * Triggers manual or automated attendance check.
     */
    fun checkAttendanceNow(silent: Boolean = false) {
        if (!silent) {
            refreshUserProfile()
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isChecking = true, errorMessage = null, successMessage = null) }

            val result = repository.checkAttendance(forceRefresh = true)
            result.fold(
                onSuccess = { checkResult ->
                    _uiState.update { current ->
                        current.copy(
                            isChecking = false,
                            attendanceStatus = checkResult.status,
                            attendanceData = checkResult.data,
                            pendingMentorReviewCount = checkResult.pendingMentorReviewCount,
                            lastSyncTimestamp = checkResult.checkedAt,
                            successMessage = if (!silent) "Presensi berhasil dicek!" else null,
                            errorMessage = null
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update { current ->
                        current.copy(
                            isChecking = false,
                            errorMessage = error.message ?: "Gagal memeriksa status presensi"
                        )
                    }
                }
            )
            updateNextReminderString()
        }
    }

    /**
     * Logs in with Kemnaker credentials and initiates immediate attendance sync.
     */
    fun login(username: String, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }

            val loginResult = repository.login(username, password)
            loginResult.fold(
                onSuccess = {
                    _uiState.update { current ->
                        current.copy(
                            isLoading = false,
                            isLoginDialogOpen = false,
                            successMessage = "Login berhasil tersambung ke SSO Kemnaker!"
                        )
                    }
                    // Immediately check attendance
                    checkAttendanceNow(silent = false)
                },
                onFailure = { error ->
                    _uiState.update { current ->
                        current.copy(
                            isLoading = false,
                            errorMessage = "Login gagal: ${error.message ?: "Periksa username/password"}"
                        )
                    }
                }
            )
        }
    }

    /**
     * Clears credentials and active session.
     */
    fun logout() {
        viewModelScope.launch {
            repository.logout()
            _uiState.update {
                AttendanceUiState(
                    isLoggedIn = false,
                    username = "",
                    attendanceStatus = AttendanceStatus.UNKNOWN,
                    attendanceData = null,
                    pendingMentorReviewCount = 0,
                    lastSyncTimestamp = 0L,
                    reminderSettings = repository.reminderSettingsFlow.value,
                    successMessage = "Berhasil logout dari sistem"
                )
            }
            updateNextReminderString()
        }
    }

    /**
     * Updates reminder settings in repository and persistent storage.
     */
    fun updateReminderSettings(settings: ReminderSettings) {
        viewModelScope.launch {
            repository.updateReminderSettings(settings)
            _uiState.update { it.copy(reminderSettings = settings) }
            updateNextReminderString()
            try {
                AttendanceAlarmScheduler.scheduleNextAlarm(MagangHubApp.instance.applicationContext)
            } catch (_: Exception) {
                // Fallback for pure JVM unit tests where Application context is unavailable
            }
        }
    }

    /**
     * Refreshes user profile (name, photo) from Monev API.
     */
    fun refreshUserProfile() {
        viewModelScope.launch {
            repository.refreshUserProfile()
        }
    }

    /**
     * Opens or closes the login dialog.
     */
    fun setLoginDialogOpen(open: Boolean) {
        _uiState.update { it.copy(isLoginDialogOpen = open, errorMessage = null) }
    }

    /**
     * Clears banner / snackbar messages.
     */
    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }

    /**
     * Sets a transient success message.
     */
    fun setSuccessMessage(message: String?) {
        _uiState.update { it.copy(successMessage = message, errorMessage = null) }
    }

    /**
     * Sets a transient error message.
     */
    fun setErrorMessage(message: String?) {
        _uiState.update { it.copy(errorMessage = message, successMessage = null) }
    }

    /**
     * Opens or closes the feedback bottom sheet.
     */
    fun setFeedbackSheetOpen(open: Boolean) {
        _uiState.update { it.copy(isFeedbackSheetOpen = open, errorMessage = null) }
    }

    /**
     * Shows the in-app legal document bottom sheet (Privacy Policy or Terms of Service).
     */
    fun showLegalDocument(type: LegalDocumentType) {
        _uiState.update { it.copy(activeLegalDocument = type) }
    }

    /**
     * Dismisses the active legal document bottom sheet.
     */
    fun dismissLegalDocument() {
        _uiState.update { it.copy(activeLegalDocument = null) }
    }

    /**
     * Submits user feedback (bug report, feature suggestion, general feedback).
     */
    fun submitFeedback(
        type: FeedbackType,
        message: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        if (message.isBlank()) {
            val error = "Masukan tidak boleh kosong"
            setErrorMessage(error)
            onError(error)
            return
        }

        val request = FeedbackRequest(
            type = type,
            message = message.trim(),
            appVersion = "v${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
            deviceModel = "${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}",
            osVersion = "Android ${android.os.Build.VERSION.RELEASE} (SDK ${android.os.Build.VERSION.SDK_INT})",
            timestamp = System.currentTimeMillis()
        )

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmittingFeedback = true) }
            val result = repository.submitFeedback(request)
            _uiState.update { it.copy(isSubmittingFeedback = false) }

            result.onSuccess {
                setFeedbackSheetOpen(false)
                setSuccessMessage("Terima kasih! Masukanmu berhasil dikirim.")
                onSuccess()
            }.onFailure { err ->
                val errorMsg = err.message ?: "Gagal mengirim masukan. Coba lagi nanti."
                setErrorMessage(errorMsg)
                onError(errorMsg)
            }
        }
    }

    val isPreviewOrDebugBuild: Boolean
        get() = try {
            BuildConfig.DEBUG || MagangHubApp.instance.packageName.endsWith(".preview")
        } catch (_: Throwable) {
            BuildConfig.DEBUG
        }

    /**
     * Toggles the preview QA test notification bottom sheet.
     */
    fun setTestNotificationSheetOpen(open: Boolean) {
        _uiState.update { it.copy(isTestNotificationSheetOpen = open) }
    }

    /**
     * Triggers the daily reminder notification mockup ("Presensi dan logbook belum lengkap").
     */
    fun testDailyReminderNotification(context: Context) {
        val settings = _uiState.value.reminderSettings
        NotificationHelper.showReminderNotification(
            context = context,
            soundEnabled = settings.soundEnabled,
            vibrationEnabled = settings.vibrationEnabled,
            intervalMinutes = settings.intervalMinutes
        )
        _uiState.update { it.copy(successMessage = "Notifikasi pengingat harian dikirim!") }
    }

    /**
     * Triggers the mentor review reminder notification mockup ("Peninjauan mentor tertunda").
     */
    fun testMentorReviewNotification(context: Context) {
        val settings = _uiState.value.reminderSettings
        NotificationHelper.showMentorReviewReminderNotification(
            context = context,
            pendingCount = 5,
            thresholdDays = settings.mentorReviewThresholdDays,
            soundEnabled = settings.soundEnabled,
            vibrationEnabled = settings.vibrationEnabled
        )
        _uiState.update { it.copy(successMessage = "Notifikasi peninjauan mentor dikirim!") }
    }

    /**
     * Triggers the completion notification mockup ("Semua beres hari ini").
     */
    fun testSuccessNotification(context: Context) {
        NotificationHelper.showSuccessNotification(context)
        _uiState.update { it.copy(successMessage = "Notifikasi 'Semua beres hari ini' dikirim (auto-dismiss 12s)!") }
    }

    /**
     * Fires a local test notification to verify channel and sound configurations.
     */
    fun testNotification(context: Context) {
        val currentStatus = _uiState.value.attendanceStatus
        if (currentStatus == AttendanceStatus.HADIR) {
            NotificationHelper.showSuccessNotification(context)
        } else {
            val settings = _uiState.value.reminderSettings
            NotificationHelper.showReminderNotification(
                context = context,
                soundEnabled = settings.soundEnabled,
                vibrationEnabled = settings.vibrationEnabled,
                intervalMinutes = settings.intervalMinutes
            )
        }

        _uiState.update { it.copy(successMessage = "Notifikasi pengujian berhasil dikirim!") }
    }

    /**
     * Formats last sync timestamp to readable Indonesian string.
     */
    fun formatLastSync(timestamp: Long): String {
        if (timestamp <= 0L) return "Belum pernah disinkronkan"
        val zdt = ZonedDateTime.ofInstant(Instant.ofEpochMilli(timestamp), wibZoneId)
        return zdt.format(fullDateTimeFormatter)
    }

    internal fun updateNextReminderString(now: ZonedDateTime = ZonedDateTime.now(wibZoneId)) {
        val settings = _uiState.value.reminderSettings
        val status = _uiState.value.attendanceStatus

        val text = when {
            !settings.isEnabled -> "Pengingat dinonaktifkan di pengaturan"
            status == AttendanceStatus.HADIR -> "Tuntas hari ini (Tidak ada pengingat)"
            status == AttendanceStatus.LIBUR -> "Hari libur / non-aktif (Tidak ada pengingat)"
            status == AttendanceStatus.TIDAK_AKTIF -> "Magang tidak aktif"
            now.hour < settings.startHour -> {
                val reminderTime = now.withHour(settings.startHour).withMinute(0).withSecond(0)
                "Hari ini pukul ${reminderTime.format(timeFormatter)} WIB"
            }
            now.hour >= settings.startHour && now.hour < settings.endHour -> {
                val interval = if (settings.intervalMinutes > 0) settings.intervalMinutes else 30
                val nextMinute = ((now.minute / interval) + 1) * interval
                val nextTime = now.plusMinutes((nextMinute - now.minute).toLong()).withSecond(0)
                if (nextTime.toLocalDate() != now.toLocalDate() || nextTime.hour >= settings.endHour) {
                    "Batas akhir presensi 23:59 WIB"
                } else {
                    "Aktif pukul ${nextTime.format(timeFormatter)} WIB (setiap $interval m)"
                }
            }
            else -> "Batas presensi 23:59 WIB telah lewat"
        }

        _uiState.update { it.copy(nextReminderFormatted = text) }
    }

    /**
     * Updates theme mode override selection (System, Light, Dark).
     */
    fun setThemeMode(themeMode: ThemeMode) {
        viewModelScope.launch {
            repository.setThemeMode(themeMode)
        }
    }

    /**
     * Postpones mentor review reminders for 24 hours.
     */
    fun snoozeMentorReminderOneDay() {
        viewModelScope.launch {
            repository.snoozeMentorReminderOneDay()
            _uiState.update { it.copy(successMessage = "Pengingat mentor ditunda 1 hari") }
        }
    }

    /**
     * Checks GitHub releases for a new version and updates the updateCheckState in UI state.
     */
    fun checkForUpdates() {
        if (_uiState.value.updateCheckState is UpdateCheckState.Checking) return
        viewModelScope.launch {
            _uiState.update { it.copy(updateCheckState = UpdateCheckState.Checking) }
            val result = repository.checkForUpdates()
            result.fold(
                onSuccess = { state ->
                    _uiState.update {
                        it.copy(
                            updateCheckState = state,
                            lastUpdateCheckTimestamp = repository.getLastUpdateCheckTimestamp()
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(
                            updateCheckState = UpdateCheckState.Error(e.message ?: "Gagal memeriksa pembaruan"),
                            lastUpdateCheckTimestamp = repository.getLastUpdateCheckTimestamp()
                        )
                    }
                }
            )
        }
    }

    /**
     * Formats the last update check timestamp to a human-readable Indonesian string.
     * Returns "Terakhir dicek hari ini, HH:mm WIB" or "Terakhir dicek dd MMM, HH:mm WIB".
     */
    fun formatLastUpdateCheck(timestamp: Long): String {
        if (timestamp <= 0L) return ""
        val zdt = ZonedDateTime.ofInstant(Instant.ofEpochMilli(timestamp), wibZoneId)
        val now = ZonedDateTime.now(wibZoneId)
        val timeStr = zdt.format(timeFormatter)
        return if (zdt.toLocalDate() == now.toLocalDate()) {
            "Terakhir dicek hari ini, $timeStr WIB"
        } else {
            val dateStr = zdt.format(DateTimeFormatter.ofPattern("dd MMM", Locale("id", "ID")))
            "Terakhir dicek $dateStr, $timeStr WIB"
        }
    }
}

/**
 * Factory for creating AttendanceViewModel instances with dependency injection.
 */
class AttendanceViewModelFactory(
    private val repository: AttendanceRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AttendanceViewModel::class.java)) {
            return AttendanceViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
