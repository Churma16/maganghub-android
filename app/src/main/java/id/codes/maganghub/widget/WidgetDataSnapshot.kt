package id.codes.maganghub.widget

import android.content.Context
import id.codes.maganghub.R
import id.codes.maganghub.data.model.AttendanceStatus
import id.codes.maganghub.data.model.ThemeMode
import id.codes.maganghub.data.storage.EncryptedPreferences
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

enum class WidgetStatus {
    SUDAH_ABSEN,
    BELUM_ABSEN,
    MENDESAK,
    HARI_LIBUR,
    SESI_BERAKHIR
}

data class WidgetDataSnapshot(
    val status: WidgetStatus,
    val remainingTimeMillis: Long,
    val remainingTimeText: String, // e.g. "7j 29m" or "1j 58m"
    val formattedDate: String,    // e.g. "Rab, 25 Nov"
    val pendingMentorCount: Int,
    val isMentorOverdue: Boolean,
    val thresholdDays: Int = 3,
    val themeMode: ThemeMode
) {

    fun getTitle1x5(): String = when (status) {
        WidgetStatus.SUDAH_ABSEN -> "Sudah Absen"
        WidgetStatus.BELUM_ABSEN -> "Belum Absen"
        WidgetStatus.MENDESAK -> "Belum Absen"
        WidgetStatus.HARI_LIBUR -> "Hari Libur"
        WidgetStatus.SESI_BERAKHIR -> "Sesi Berakhir"
    }

    fun getTitle1x3(): String = when (status) {
        WidgetStatus.SUDAH_ABSEN -> "Sudah Absen"
        WidgetStatus.BELUM_ABSEN -> "Belum Absen"
        WidgetStatus.MENDESAK -> "Belum Absen"
        WidgetStatus.HARI_LIBUR -> "Hari Libur"
        WidgetStatus.SESI_BERAKHIR -> "Perlu Login"
    }

    fun getSubtext1x5(): String = when (status) {
        WidgetStatus.SUDAH_ABSEN -> {
            when {
                pendingMentorCount == 0 -> "Semua laporan sudah di-approve"
                isMentorOverdue -> "$pendingMentorCount laporan belum di-approve"
                else -> "$pendingMentorCount laporan menunggu approval"
            }
        }
        WidgetStatus.BELUM_ABSEN -> "Batas hari ini 23:59 WIB"
        WidgetStatus.MENDESAK -> "Segera isi sebelum 23:59 WIB"
        WidgetStatus.HARI_LIBUR -> {
            when {
                pendingMentorCount == 0 -> "Pengingat nonaktif hari ini"
                isMentorOverdue -> "$pendingMentorCount laporan belum di-approve"
                else -> "$pendingMentorCount laporan menunggu approval"
            }
        }
        WidgetStatus.SESI_BERAKHIR -> "Ketuk untuk masuk kembali ke SSO"
    }

    fun getSubtext1x3(): String = when (status) {
        WidgetStatus.SUDAH_ABSEN -> {
            when {
                pendingMentorCount == 0 -> formattedDate
                isMentorOverdue -> "$pendingMentorCount menunggu >$thresholdDays hr"
                else -> "$pendingMentorCount menunggu approval"
            }
        }
        WidgetStatus.BELUM_ABSEN -> "Sisa $remainingTimeText"
        WidgetStatus.MENDESAK -> "Sisa $remainingTimeText"
        WidgetStatus.HARI_LIBUR -> {
            when {
                pendingMentorCount == 0 -> "Pengingat off"
                isMentorOverdue -> "$pendingMentorCount menunggu >$thresholdDays hr"
                else -> "$pendingMentorCount menunggu approval"
            }
        }
        WidgetStatus.SESI_BERAKHIR -> "Masuk ke SSO"
    }

    fun isSubtextWarning(): Boolean {
        return (status == WidgetStatus.SUDAH_ABSEN || status == WidgetStatus.HARI_LIBUR) && isMentorOverdue
    }

    fun showBadge1x5(): Boolean {
        return (status == WidgetStatus.SUDAH_ABSEN || status == WidgetStatus.HARI_LIBUR) && isMentorOverdue
    }

    fun getBadgeText1x5(): String = ">$thresholdDays hari"

    fun showActionButton(): Boolean {
        return status == WidgetStatus.BELUM_ABSEN ||
                status == WidgetStatus.MENDESAK ||
                status == WidgetStatus.SESI_BERAKHIR
    }

    fun getInlineIconResId(): Int? = when (status) {
        WidgetStatus.SUDAH_ABSEN -> R.drawable.ic_widget_check
        WidgetStatus.BELUM_ABSEN -> R.drawable.ic_widget_clock
        WidgetStatus.MENDESAK -> R.drawable.ic_widget_warning
        WidgetStatus.HARI_LIBUR -> R.drawable.ic_widget_moon
        WidgetStatus.SESI_BERAKHIR -> null
    }

    companion object {

        private val WIB_TIMEZONE = TimeZone.getTimeZone("Asia/Jakarta")
        private val WIB_ZONE_ID = ZoneId.of("Asia/Jakarta")
        private val DATE_FORMATTER = DateTimeFormatter.ofPattern("EEE, d MMM", Locale("id", "ID"))

        fun fromPreferences(
            context: Context,
            nowMillis: Long = System.currentTimeMillis()
        ): WidgetDataSnapshot {
            val prefs = EncryptedPreferences(context)
            return fromComponents(
                hasCredentials = prefs.hasCredentials(),
                attendanceStatus = prefs.getLastAttendanceStatus(),
                pendingCount = prefs.getPendingMentorReviewCount(),
                thresholdDays = prefs.getReminderSettings().mentorReviewThresholdDays,
                themeMode = prefs.getThemeMode(),
                nowMillis = nowMillis
            )
        }

        fun fromComponents(
            hasCredentials: Boolean,
            attendanceStatus: AttendanceStatus,
            pendingCount: Int,
            thresholdDays: Int,
            themeMode: ThemeMode,
            nowMillis: Long = System.currentTimeMillis()
        ): WidgetDataSnapshot {
            if (!hasCredentials) {
                return WidgetDataSnapshot(
                    status = WidgetStatus.SESI_BERAKHIR,
                    remainingTimeMillis = 0L,
                    remainingTimeText = "",
                    formattedDate = formatDate(nowMillis),
                    pendingMentorCount = 0,
                    isMentorOverdue = false,
                    thresholdDays = thresholdDays.coerceIn(1, 5),
                    themeMode = themeMode
                )
            }

            val effectiveThreshold = thresholdDays.coerceIn(1, 5)
            val isOverdue = pendingCount >= effectiveThreshold

            val (remainingMillis, remainingText) = calculateRemainingTime(nowMillis)

            val widgetStatus = when {
                attendanceStatus == AttendanceStatus.LIBUR || attendanceStatus == AttendanceStatus.TIDAK_AKTIF -> {
                    WidgetStatus.HARI_LIBUR
                }
                attendanceStatus == AttendanceStatus.HADIR -> {
                    WidgetStatus.SUDAH_ABSEN
                }
                else -> {
                    // Belum absen
                    if (remainingMillis < 2 * 60 * 60 * 1000L) {
                        WidgetStatus.MENDESAK
                    } else {
                        WidgetStatus.BELUM_ABSEN
                    }
                }
            }

            return WidgetDataSnapshot(
                status = widgetStatus,
                remainingTimeMillis = remainingMillis,
                remainingTimeText = remainingText,
                formattedDate = formatDate(nowMillis),
                pendingMentorCount = pendingCount,
                isMentorOverdue = isOverdue,
                thresholdDays = effectiveThreshold,
                themeMode = themeMode
            )
        }

        fun calculateRemainingTime(nowMillis: Long): Pair<Long, String> {
            val cal = Calendar.getInstance(WIB_TIMEZONE).apply {
                timeInMillis = nowMillis
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }

            val deadlineMillis = cal.timeInMillis
            val diff = (deadlineMillis - nowMillis).coerceAtLeast(0L)

            val hours = diff / (1000 * 60 * 60)
            val minutes = (diff / (1000 * 60)) % 60

            val text = if (hours > 0) "${hours}j ${minutes}m" else "${minutes}m"
            return Pair(diff, text)
        }

        fun formatDate(millis: Long): String {
            val zonedDateTime = Instant.ofEpochMilli(millis).atZone(WIB_ZONE_ID)
            return DATE_FORMATTER.format(zonedDateTime)
        }
    }
}
