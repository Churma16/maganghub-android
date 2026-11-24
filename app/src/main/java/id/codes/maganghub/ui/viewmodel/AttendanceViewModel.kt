package id.codes.maganghub.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import id.codes.maganghub.background.NotificationHelper
import id.codes.maganghub.data.model.AttendanceStatus
import id.codes.maganghub.data.model.ReminderSettings
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
            attendanceStatus = repository.getCachedStatus(),
            attendanceData = repository.getCachedHomeData(),
            lastSyncTimestamp = repository.getLastCheckTimestamp(),
            reminderSettings = repository.reminderSettingsFlow.value
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
                        username = session.username
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
     * Fires a local test notification to verify channel and sound configurations.
     */
    fun testNotification(context: Context) {
        val now = ZonedDateTime.now(wibZoneId)
        val timeWib = now.format(timeFormatter)
        val dateFormatted = now.format(DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy", Locale("id", "ID")))

        val currentStatus = _uiState.value.attendanceStatus
        if (currentStatus == AttendanceStatus.HADIR) {
            NotificationHelper.showSuccessNotification(context, dateFormatted)
        } else {
            val settings = _uiState.value.reminderSettings
            NotificationHelper.showReminderNotification(
                context = context,
                reminderIndex = 1,
                currentTimeWib = timeWib,
                dateFormatted = dateFormatted,
                soundEnabled = settings.soundEnabled,
                vibrationEnabled = settings.vibrationEnabled
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

    private fun updateNextReminderString() {
        val now = ZonedDateTime.now(wibZoneId)
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
                val interval = settings.intervalMinutes
                val nextMinute = ((now.minute / interval) + 1) * interval
                val nextTime = now.plusMinutes((nextMinute - now.minute).toLong()).withSecond(0)
                if (nextTime.hour >= settings.endHour) {
                    "Batas akhir presensi 23:59 WIB"
                } else {
                    "Aktif pukul ${nextTime.format(timeFormatter)} WIB (setiap $interval m)"
                }
            }
            else -> "Batas presensi 23:59 WIB telah lewat"
        }

        _uiState.update { it.copy(nextReminderFormatted = text) }
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
