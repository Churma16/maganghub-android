package id.codes.maganghub.widget

import android.content.Context
import id.codes.maganghub.R
import id.codes.maganghub.data.model.AttendanceStatus
import id.codes.maganghub.data.model.DailyWorkPhase
import id.codes.maganghub.data.model.ThemeMode
import id.codes.maganghub.data.model.WorkPhaseResolver
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
    SESI_BERAKHIR,
    MENUNGGU_KERJA,
    SEDANG_BEKERJA
}

data class WidgetDataSnapshot(
    val status: WidgetStatus,
    val remainingTimeMillis: Long,
    val remainingTimeText: String, // e.g. "7j 29m" or "1j 58m"
    val formattedDate: String,    // e.g. "Rab, 25 Nov"
    val pendingMentorCount: Int,
    val isMentorOverdue: Boolean,
    val thresholdDays: Int = 3,
    val lastCheckTimestamp: Long = 0L,
    val reminderEnabled: Boolean = true,
    val reminderStartHour: Int = 16,
    val reminderIntervalMinutes: Int = 15,
    val themeMode: ThemeMode,
    val workStartHour: Int = 8,
    val workStartMinute: Int = 0
) {

    fun getTitle1x5(): String = when (status) {
        WidgetStatus.SUDAH_ABSEN -> "Lengkap"
        WidgetStatus.BELUM_ABSEN -> "Belum Lengkap"
        WidgetStatus.MENDESAK -> "Segera Isi"
        WidgetStatus.HARI_LIBUR -> "Hari Libur"
        WidgetStatus.SESI_BERAKHIR -> "Sesi Berakhir"
        WidgetStatus.MENUNGGU_KERJA -> "Menunggu jam kerja"
        WidgetStatus.SEDANG_BEKERJA -> "Selamat bekerja"
    }

    fun getTitle1x3(): String = when (status) {
        WidgetStatus.SUDAH_ABSEN -> "Lengkap"
        WidgetStatus.BELUM_ABSEN -> "Belum Lengkap"
        WidgetStatus.MENDESAK -> "Segera Isi"
        WidgetStatus.HARI_LIBUR -> "Hari Libur"
        WidgetStatus.SESI_BERAKHIR -> "Perlu Login"
        WidgetStatus.MENUNGGU_KERJA -> "Menunggu"
        WidgetStatus.SEDANG_BEKERJA -> "Bekerja"
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
                pendingMentorCount == 0 -> "Tidak wajib isi logbook"
                isMentorOverdue -> "$pendingMentorCount laporan belum di-approve"
                else -> "$pendingMentorCount laporan menunggu approval"
            }
        }
        WidgetStatus.SESI_BERAKHIR -> "Ketuk untuk masuk kembali ke SSO"
        WidgetStatus.MENUNGGU_KERJA -> {
            val workTime = String.format(Locale.ROOT, "%02d:%02d", workStartHour, workStartMinute)
            "Jam kerja dimulai pukul $workTime WIB"
        }
        WidgetStatus.SEDANG_BEKERJA -> {
            "Selamat menjalani aktivitas magang"
        }
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
                pendingMentorCount == 0 -> "Tidak wajib logbook"
                isMentorOverdue -> "$pendingMentorCount menunggu >$thresholdDays hr"
                else -> "$pendingMentorCount menunggu approval"
            }
        }
        WidgetStatus.SESI_BERAKHIR -> "Masuk ke SSO"
        WidgetStatus.MENUNGGU_KERJA -> {
            val workTime = String.format(Locale.ROOT, "%02d:%02d", workStartHour, workStartMinute)
            "Mulai $workTime"
        }
        WidgetStatus.SEDANG_BEKERJA -> {
            formattedDate
        }
    }

    fun getRightValue1x5(): String = when (status) {
        WidgetStatus.SUDAH_ABSEN, WidgetStatus.HARI_LIBUR -> formattedDate
        WidgetStatus.MENUNGGU_KERJA, WidgetStatus.SEDANG_BEKERJA -> formattedDate
        WidgetStatus.BELUM_ABSEN, WidgetStatus.MENDESAK -> remainingTimeText
        WidgetStatus.SESI_BERAKHIR -> ""
    }

    fun getRightLabel1x5(): String = when (status) {
        WidgetStatus.SUDAH_ABSEN -> "Lengkap"
        WidgetStatus.HARI_LIBUR -> "Hari Off"
        WidgetStatus.MENUNGGU_KERJA -> "Menunggu"
        WidgetStatus.SEDANG_BEKERJA -> "Bekerja"
        WidgetStatus.BELUM_ABSEN, WidgetStatus.MENDESAK -> "tersisa"
        WidgetStatus.SESI_BERAKHIR -> ""
    }

    fun getFooterLeft1x5(nowHour: Int = -1): String = when {
        status == WidgetStatus.SESI_BERAKHIR -> "Sinkronisasi terhenti"
        !reminderEnabled -> "Notifikasi nonaktif"
        status == WidgetStatus.HARI_LIBUR -> "Notifikasi dijeda hari ini"
        status == WidgetStatus.SUDAH_ABSEN -> "Tidak ada notifikasi lagi"
        status == WidgetStatus.MENUNGGU_KERJA || status == WidgetStatus.SEDANG_BEKERJA -> {
            val timeStr = String.format(Locale.ROOT, "%02d:00", reminderStartHour)
            "Notifikasi mulai $timeStr • tiap ${reminderIntervalMinutes}m"
        }
        else -> {
            // BELUM_ABSEN or MENDESAK
            val hour = if (nowHour >= 0) nowHour else Calendar.getInstance(WIB_TIMEZONE).get(Calendar.HOUR_OF_DAY)
            if (hour < reminderStartHour) {
                val timeStr = String.format(Locale.ROOT, "%02d:00", reminderStartHour)
                "Notifikasi mulai $timeStr • tiap ${reminderIntervalMinutes}m"
            } else {
                "Notifikasi aktif • tiap $reminderIntervalMinutes menit"
            }
        }
    }

    fun getFooterLeft1x3(nowHour: Int = -1): String = when {
        status == WidgetStatus.SESI_BERAKHIR -> "Perlu login"
        !reminderEnabled -> "Nonaktif"
        status == WidgetStatus.HARI_LIBUR -> "Dijeda hari ini"
        status == WidgetStatus.SUDAH_ABSEN -> "Notif berhenti"
        status == WidgetStatus.MENUNGGU_KERJA || status == WidgetStatus.SEDANG_BEKERJA -> {
            val timeStr = String.format(Locale.ROOT, "%02d:00", reminderStartHour)
            "Mulai $timeStr"
        }
        else -> {
            // BELUM_ABSEN or MENDESAK
            val hour = if (nowHour >= 0) nowHour else Calendar.getInstance(WIB_TIMEZONE).get(Calendar.HOUR_OF_DAY)
            if (hour < reminderStartHour) {
                val timeStr = String.format(Locale.ROOT, "%02d:00", reminderStartHour)
                "Mulai $timeStr"
            } else {
                "Tiap $reminderIntervalMinutes menit"
            }
        }
    }

    fun getFooterRight1x5(): String {
        if (lastCheckTimestamp <= 0L) return ""
        val timeStr = formatTime(lastCheckTimestamp)
        return "Dicek $timeStr"
    }

    fun getFooterRight1x3(): String {
        if (lastCheckTimestamp <= 0L) return ""
        return formatTime(lastCheckTimestamp)
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
        WidgetStatus.MENUNGGU_KERJA -> R.drawable.ic_widget_clock
        WidgetStatus.SEDANG_BEKERJA -> R.drawable.ic_widget_clock
        WidgetStatus.SESI_BERAKHIR -> null
    }

    companion object {

        private val WIB_TIMEZONE = TimeZone.getTimeZone("Asia/Jakarta")
        private val WIB_ZONE_ID = WorkPhaseResolver.ZONE_WIB
        private val DATE_FORMATTER = DateTimeFormatter.ofPattern("EEE, d MMM", Locale("id", "ID"))
        private val TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm", Locale("id", "ID"))

        fun fromPreferences(
            context: Context,
            nowMillis: Long = System.currentTimeMillis()
        ): WidgetDataSnapshot {
            val prefs = EncryptedPreferences(context)
            val settings = prefs.getReminderSettings()
            return fromComponents(
                hasCredentials = prefs.hasCredentials(),
                attendanceStatus = prefs.getLastAttendanceStatus(),
                pendingCount = prefs.getPendingMentorReviewCount(),
                thresholdDays = settings.mentorReviewThresholdDays,
                lastCheckTimestamp = prefs.getLastCheckTimestamp(),
                reminderEnabled = settings.isEnabled,
                reminderStartHour = settings.startHour,
                reminderIntervalMinutes = settings.intervalMinutes,
                themeMode = prefs.getThemeMode(),
                nowMillis = nowMillis,
                workStartHour = settings.workStartHour,
                workStartMinute = settings.workStartMinute
            )
        }

        fun fromComponents(
            hasCredentials: Boolean,
            attendanceStatus: AttendanceStatus,
            pendingCount: Int,
            thresholdDays: Int,
            lastCheckTimestamp: Long = 0L,
            reminderEnabled: Boolean = true,
            reminderStartHour: Int = 16,
            reminderIntervalMinutes: Int = 15,
            themeMode: ThemeMode,
            nowMillis: Long = System.currentTimeMillis(),
            workStartHour: Int = 8,
            workStartMinute: Int = 0
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
                    lastCheckTimestamp = lastCheckTimestamp,
                    reminderEnabled = reminderEnabled,
                    reminderStartHour = reminderStartHour,
                    reminderIntervalMinutes = reminderIntervalMinutes,
                    themeMode = themeMode,
                    workStartHour = workStartHour,
                    workStartMinute = workStartMinute
                )
            }

            val effectiveThreshold = thresholdDays.coerceIn(1, 5)
            val isOverdue = pendingCount >= effectiveThreshold

            val (remainingMillis, remainingText) = calculateRemainingTime(nowMillis)
            val nowWib = Instant.ofEpochMilli(nowMillis).atZone(WIB_ZONE_ID)

            val workPhase = WorkPhaseResolver.resolve(
                hasAttendance = attendanceStatus == AttendanceStatus.HADIR,
                isHoliday = attendanceStatus == AttendanceStatus.LIBUR || attendanceStatus == AttendanceStatus.TIDAK_AKTIF,
                workStartHour = workStartHour,
                workStartMinute = workStartMinute,
                reportStartHour = reminderStartHour,
                nowWib = nowWib,
                remainingMillis = remainingMillis
            )

            val widgetStatus = when (workPhase) {
                DailyWorkPhase.COMPLETED -> WidgetStatus.SUDAH_ABSEN
                DailyWorkPhase.HOLIDAY -> WidgetStatus.HARI_LIBUR
                DailyWorkPhase.BEFORE_WORK -> WidgetStatus.MENUNGGU_KERJA
                DailyWorkPhase.WORKING -> WidgetStatus.SEDANG_BEKERJA
                DailyWorkPhase.REPORTING_NORMAL -> WidgetStatus.BELUM_ABSEN
                DailyWorkPhase.REPORTING_URGENT -> WidgetStatus.MENDESAK
            }

            return WidgetDataSnapshot(
                status = widgetStatus,
                remainingTimeMillis = remainingMillis,
                remainingTimeText = remainingText,
                formattedDate = formatDate(nowMillis),
                pendingMentorCount = pendingCount,
                isMentorOverdue = isOverdue,
                thresholdDays = effectiveThreshold,
                lastCheckTimestamp = lastCheckTimestamp,
                reminderEnabled = reminderEnabled,
                reminderStartHour = reminderStartHour,
                reminderIntervalMinutes = reminderIntervalMinutes,
                themeMode = themeMode,
                workStartHour = workStartHour,
                workStartMinute = workStartMinute
            )
        }

        fun formatTime(millis: Long): String {
            val zonedDateTime = Instant.ofEpochMilli(millis).atZone(WIB_ZONE_ID)
            return TIME_FORMATTER.format(zonedDateTime)
        }

        fun calculateRemainingTime(nowMillis: Long): Pair<Long, String> {
            val nowWib = Instant.ofEpochMilli(nowMillis).atZone(WIB_ZONE_ID)
            val diff = WorkPhaseResolver.calculateRemainingMillis(nowWib)

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
