package id.codes.maganghub.data.model

/**
 * Active authenticated user session state.
 */
data class UserSession(
    val username: String = "",
    val monevAccessToken: String = "",
    val isLoggedIn: Boolean = false,
    val lastLoginTimestamp: Long = 0L,
    val fullName: String = "",
    val photoUrl: String? = null
)

/**
 * Snapshot of manual or automated attendance check.
 */
data class AttendanceCheckResult(
    val status: AttendanceStatus,
    val data: AttendanceHomeData? = null,
    val checkedAt: Long = System.currentTimeMillis(),
    val isSuccess: Boolean = true,
    val errorMessage: String? = null
)

/**
 * Configuration for spam reminders between 16:00 and 23:59 WIB.
 */
data class ReminderSettings(
    val isEnabled: Boolean = true,
    val morningReminderEnabled: Boolean = true,
    val skipHolidays: Boolean = true,
    val startHour: Int = 16,
    val endHour: Int = 24,
    val intervalMinutes: Int = 30,
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true
)
