package id.codes.maganghub.data.model

import com.google.gson.annotations.SerializedName

/**
 * Details returned by GET /api/v1/users/me/home on MagangHub Monev API.
 */
data class AttendanceHomeData(
    @SerializedName("date")
    val date: String = "",

    @SerializedName("active_internship")
    val activeInternship: Boolean = false,

    @SerializedName("is_holiday")
    val isHoliday: Boolean = false,

    @SerializedName("is_scheduled_off_day")
    val isScheduledOffDay: Boolean = false,

    @SerializedName("has_attendance")
    val hasAttendance: Boolean = false
) {
    fun toAttendanceStatus(): AttendanceStatus = AttendanceStatus.fromHomeData(this)
}

/**
 * Envelope response wrapper for /users/me/home endpoint.
 */
data class AttendanceHomeResponse(
    @SerializedName("data")
    val data: AttendanceHomeData = AttendanceHomeData()
)

/**
 * Standard error response envelope from MagangHub backend.
 */
data class ApiErrorResponse(
    @SerializedName("code")
    val code: Int = 0,

    @SerializedName("error_code")
    val errorCode: String = "",

    @SerializedName("message")
    val message: String = ""
)
