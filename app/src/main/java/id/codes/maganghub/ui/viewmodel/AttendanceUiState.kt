package id.codes.maganghub.ui.viewmodel

import id.codes.maganghub.data.model.AttendanceHomeData
import id.codes.maganghub.data.model.AttendanceStatus
import id.codes.maganghub.data.model.DailyWorkPhase
import id.codes.maganghub.data.model.LegalDocumentType
import id.codes.maganghub.data.model.ReminderSettings
import id.codes.maganghub.data.model.ThemeMode
import id.codes.maganghub.data.model.UpdateCheckState
import id.codes.maganghub.data.model.WorkPhaseResolver

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
    val pendingMentorReviewCount: Int = 0,
    val mentorSyncDebug: String? = null,
    val lastSyncTimestamp: Long = 0L,
    val nextReminderFormatted: String = "Menghitung jadwal...",
    val reminderSettings: ReminderSettings = ReminderSettings(),
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val isLoginDialogOpen: Boolean = false,
    val resumeLoginOnLegalDismiss: Boolean = false,
    val isFeedbackSheetOpen: Boolean = false,
    val isSubmittingFeedback: Boolean = false,
    val activeLegalDocument: LegalDocumentType? = null,
    val isTestNotificationSheetOpen: Boolean = false,
    val updateCheckState: UpdateCheckState = UpdateCheckState.Idle,
    val lastUpdateCheckTimestamp: Long = 0L
) {
    val isAttended: Boolean
        get() = attendanceStatus == AttendanceStatus.HADIR

    val isHoliday: Boolean
        get() = attendanceStatus == AttendanceStatus.LIBUR

    val isPending: Boolean
        get() = attendanceStatus == AttendanceStatus.BELUM_HADIR

    fun getDailyWorkPhase(nowWib: java.time.ZonedDateTime = java.time.ZonedDateTime.now(WorkPhaseResolver.ZONE_WIB)): DailyWorkPhase {
        val todayStr = nowWib.toLocalDate().toString()
        val isDataStale = if (attendanceData != null && attendanceData.date.isNotBlank()) {
            attendanceData.date != todayStr
        } else if (lastSyncTimestamp > 0L) {
            val syncDate = java.time.Instant.ofEpochMilli(lastSyncTimestamp).atZone(WorkPhaseResolver.ZONE_WIB).toLocalDate().toString()
            syncDate != todayStr
        } else {
            false
        }

        val hasAttendance = if (isDataStale) {
            false
        } else {
            attendanceData?.hasAttendance == true || isAttended
        }

        val holiday = if (isDataStale) {
            val dayOfWeek = nowWib.dayOfWeek
            dayOfWeek == java.time.DayOfWeek.SATURDAY || dayOfWeek == java.time.DayOfWeek.SUNDAY
        } else {
            attendanceData?.isHoliday == true || attendanceData?.isScheduledOffDay == true || isHoliday
        }

        return WorkPhaseResolver.resolve(
            hasAttendance = hasAttendance,
            isHoliday = holiday,
            workStartHour = reminderSettings.workStartHour,
            workStartMinute = reminderSettings.workStartMinute,
            reportStartHour = reminderSettings.startHour,
            nowWib = nowWib
        )
    }
}
