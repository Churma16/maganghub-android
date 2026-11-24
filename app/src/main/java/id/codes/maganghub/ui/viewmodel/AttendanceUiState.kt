package id.codes.maganghub.ui.viewmodel

import id.codes.maganghub.data.model.AttendanceHomeData
import id.codes.maganghub.data.model.AttendanceStatus
import id.codes.maganghub.data.model.ReminderSettings

/**
 * State representing current UI presentation in MagangHub.
 */
data class AttendanceUiState(
    val isLoggedIn: Boolean = false,
    val username: String = "",
    val fullName: String = "",
    val photoUrl: String? = null,
    val isLoading: Boolean = false,
    val isChecking: Boolean = false,
    val attendanceStatus: AttendanceStatus = AttendanceStatus.UNKNOWN,
    val attendanceData: AttendanceHomeData? = null,
    val lastSyncTimestamp: Long = 0L,
    val nextReminderFormatted: String = "Menghitung jadwal...",
    val reminderSettings: ReminderSettings = ReminderSettings(),
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val isLoginDialogOpen: Boolean = false
) {
    val isAttended: Boolean
        get() = attendanceStatus == AttendanceStatus.HADIR

    val isHoliday: Boolean
        get() = attendanceStatus == AttendanceStatus.LIBUR

    val isPending: Boolean
        get() = attendanceStatus == AttendanceStatus.BELUM_HADIR
}
