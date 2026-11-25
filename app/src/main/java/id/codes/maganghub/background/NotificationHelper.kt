package id.codes.maganghub.background

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import id.codes.maganghub.R
import id.codes.maganghub.ui.MainActivity
import java.time.Duration
import java.time.ZonedDateTime

object NotificationHelper {

    const val CHANNEL_REMINDER_ID = "channel_maganghub_reminder"
    const val CHANNEL_CONFIRMATION_ID = "channel_maganghub_confirmation"
    const val CHANNEL_SYSTEM_ID = "channel_maganghub_system"
    const val CHANNEL_MENTOR_REMINDER_ID = "channel_maganghub_mentor_reminder"

    const val NOTIFICATION_ID_REMINDER = 1001
    const val NOTIFICATION_ID_CONFIRMATION = 1002
    const val NOTIFICATION_ID_AUTH_ALERT = 1003
    const val NOTIFICATION_ID_MENTOR_REMINDER = 1004

    const val ACTION_CHECK_NOW = "id.codes.maganghub.ACTION_CHECK_NOW"
    const val ACTION_SNOOZE_30 = "id.codes.maganghub.ACTION_SNOOZE_30"
    const val ACTION_SNOOZE_MENTOR_ONE_DAY = "id.codes.maganghub.ACTION_SNOOZE_MENTOR_ONE_DAY"

    const val URL_MONEV_DASHBOARD = "https://monev.maganghub.kemnaker.go.id/dashboard"
    const val URL_PRIVACY_POLICY = "https://github.com/Churma16/maganghub-logbook-companion/blob/main/PRIVACY_POLICY.md"
    const val URL_TERMS = "https://github.com/Churma16/maganghub-logbook-companion/blob/main/TERMS.md"

    /**
     * Safely opens an external web URL, handling ActivityNotFoundException if no browser is installed.
     */
    fun openWebUrl(context: Context, url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            Toast.makeText(context, "Tidak dapat membuka tautan web", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Initializes notification channels required for Android O+ (API 26+).
     */
    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            ?: return

        val soundUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
            .build()

        // 1. High Priority Reminder Channel (with sound & vibration)
        val reminderChannel = NotificationChannel(
            CHANNEL_REMINDER_ID,
            "Pengingat Harian Presensi",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Pengingat berkala jika presensi & logbook hari ini belum lengkap"
            enableLights(true)
            lightColor = Color.RED
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 400, 200, 400)
            setSound(soundUri, audioAttributes)
            lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
        }

        // 2. Default Priority Confirmation Channel
        val confirmationChannel = NotificationChannel(
            CHANNEL_CONFIRMATION_ID,
            "Status Presensi Selesai",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Pemberitahuan singkat ketika presensi & logbook hari ini sudah lengkap"
            enableLights(true)
            lightColor = Color.GREEN
            enableVibration(false)
        }

        // 3. System Alert Channel
        val systemChannel = NotificationChannel(
            CHANNEL_SYSTEM_ID,
            "Peringatan Sistem & Sesi",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Peringatan sesi otorisasi kedaluwarsa atau masalah sinkronisasi"
            enableLights(true)
            lightColor = Color.YELLOW
        }

        // 4. Mentor Review Reminder Channel
        val mentorReminderChannel = NotificationChannel(
            CHANNEL_MENTOR_REMINDER_ID,
            "Pengingat Peninjauan Mentor",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Pengingat jika kehadiran dan laporan menunggu peninjauan mentor"
            enableLights(true)
            lightColor = Color.YELLOW
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 400, 200, 400)
            setSound(soundUri, audioAttributes)
            lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
        }

        notificationManager.createNotificationChannels(
            listOf(reminderChannel, confirmationChannel, systemChannel, mentorReminderChannel)
        )
    }

    /**
     * Calculates remaining time until 23:59 WIB deadline for today in human-readable Indonesian text.
     * Example: "2 jam 59 menit", "45 menit", or "kurang dari 1 menit".
     */
    fun formatRemainingTimeToDeadline(nowWib: ZonedDateTime = ZonedDateTime.now(AttendanceAlarmScheduler.ZONE_WIB)): String {
        val wib = nowWib.withZoneSameInstant(AttendanceAlarmScheduler.ZONE_WIB)
        val deadline = wib.toLocalDate().atTime(23, 59).atZone(AttendanceAlarmScheduler.ZONE_WIB)
        val diffSeconds = Duration.between(wib, deadline).seconds
        if (diffSeconds <= 0) return "kurang dari 1 menit"
        val totalMinutes = (diffSeconds + 30) / 60
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        return when {
            hours > 0 && minutes > 0 -> "$hours jam $minutes menit"
            hours > 0 -> "$hours jam"
            minutes > 0 -> "$minutes menit"
            else -> "kurang dari 1 menit"
        }
    }

    /**
     * Shows high-priority heads-up reminder notification with action buttons.
     */
    fun showReminderNotification(
        context: Context,
        soundEnabled: Boolean = true,
        vibrationEnabled: Boolean = true,
        intervalMinutes: Int = 30,
        nowWib: ZonedDateTime = ZonedDateTime.now(AttendanceAlarmScheduler.ZONE_WIB)
    ) {
        createNotificationChannels(context)

        // Open MainActivity when tapping notification body
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            context,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Quick Action 1: "Buka Monev" in Browser
        val monevWebIntent = Intent(Intent.ACTION_VIEW, Uri.parse(URL_MONEV_DASHBOARD)).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val monevWebPendingIntent = PendingIntent.getActivity(
            context,
            1,
            monevWebIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Quick Action 2: "Tunda [X] menit"
        val snoozeIntent = Intent(context, AttendanceAlarmReceiver::class.java).apply {
            action = ACTION_SNOOZE_30
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            3,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Quick Action 3: "Sudah isi" (triggers immediate re-verification)
        val checkNowIntent = Intent(context, AttendanceAlarmReceiver::class.java).apply {
            action = ACTION_CHECK_NOW
        }
        val checkNowPendingIntent = PendingIntent.getBroadcast(
            context,
            2,
            checkNowIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val remainingText = formatRemainingTimeToDeadline(nowWib)
        val title = "Presensi dan logbook belum lengkap"
        val contentText = "Sisa $remainingText sampai 23.59 WIB. Buka Monev untuk mengisi laporan hari ini."
        val bigText = "Sisa $remainingText sampai 23.59 WIB.\nBuka Monev untuk mengisi laporan hari ini."

        val snoozeLabel = if (intervalMinutes > 0) "Tunda $intervalMinutes menit" else "Tunda 30 menit"

        val builder = NotificationCompat.Builder(context, CHANNEL_REMINDER_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(false)
            .setOngoing(false)
            .setContentIntent(openAppPendingIntent)
            .addAction(0, "Buka Monev", monevWebPendingIntent)
            .addAction(0, snoozeLabel, snoozePendingIntent)
            .addAction(0, "Sudah isi", checkNowPendingIntent)

        if (!soundEnabled) {
            builder.setSilent(true)
        }
        if (vibrationEnabled) {
            builder.setVibrate(longArrayOf(0, 400, 200, 400))
        } else {
            builder.setVibrate(longArrayOf(0))
        }

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_REMINDER, builder.build())
        } catch (e: SecurityException) {
            // POST_NOTIFICATIONS permission not granted yet by user
        }
    }

    /**
     * Shows brief confirmation notification when attendance has been fulfilled,
     * which auto-cancels after a few seconds.
     */
    fun showSuccessNotification(context: Context) {
        createNotificationChannels(context)

        // Cancel existing reminder notification
        cancelReminder(context)

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            context,
            4,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = "Semua beres hari ini"
        val message = "Presensi dan logbook sudah lengkap. Pengingat berhenti sampai besok."

        val builder = NotificationCompat.Builder(context, CHANNEL_CONFIRMATION_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setTimeoutAfter(12_000L)
            .setContentIntent(openAppPendingIntent)

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_CONFIRMATION, builder.build())
        } catch (e: SecurityException) {
            // POST_NOTIFICATIONS permission not granted yet
        }
    }

    /**
     * Shows warning when session token is unauthorized (HTTP 401) and auto-login is needed.
     */
    fun showSessionExpiredNotification(context: Context) {
        createNotificationChannels(context)

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            context,
            5,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_SYSTEM_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("🚨 Sesi MagangHub Kedaluwarsa")
            .setContentText("Token otorisasi tidak valid. Buka aplikasi untuk login kembali.")
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    "⚠️ HTTP 401 Unauthorized Session\n\n" +
                    "Sesi MagangHub Anda telah kedaluwarsa dan auto-login belum dapat diselesaikan. " +
                    "Buka aplikasi MagangHub untuk memperbarui kredensial akun Kemnaker Anda."
                )
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(openAppPendingIntent)

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_AUTH_ALERT, builder.build())
        } catch (e: SecurityException) {
            // Permission not granted
        }
    }

    /**
     * Shows notification when attendance reports are pending mentor review.
     */
    fun showMentorReviewReminderNotification(
        context: Context,
        pendingCount: Int,
        thresholdDays: Int = 5,
        soundEnabled: Boolean = true,
        vibrationEnabled: Boolean = true
    ) {
        createNotificationChannels(context)

        // Quick Action 1: "Buka Monev" in Browser
        val monevWebIntent = Intent(Intent.ACTION_VIEW, Uri.parse(URL_MONEV_DASHBOARD)).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val monevWebPendingIntent = PendingIntent.getActivity(
            context,
            6,
            monevWebIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Quick Action 2: "Tunda 1 hari"
        val snoozeIntent = Intent(context, AttendanceAlarmReceiver::class.java).apply {
            action = ACTION_SNOOZE_MENTOR_ONE_DAY
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            7,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = "Peninjauan mentor tertunda"
        val body = "$pendingCount kehadiran dan laporan sudah menunggu peninjauan. Saatnya mengingatkan mentor."

        val builder = NotificationCompat.Builder(context, CHANNEL_MENTOR_REMINDER_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(monevWebPendingIntent)
            .addAction(0, "Buka Monev", monevWebPendingIntent)
            .addAction(0, "Tunda 1 hari", snoozePendingIntent)

        if (!soundEnabled) {
            builder.setSilent(true)
        }
        if (vibrationEnabled) {
            builder.setVibrate(longArrayOf(0, 400, 200, 400))
        } else {
            builder.setVibrate(longArrayOf(0))
        }

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_MENTOR_REMINDER, builder.build())
        } catch (e: SecurityException) {
            // POST_NOTIFICATIONS permission not granted yet
        }
    }

    /**
     * Cancels any active mentor review reminder notification.
     */
    fun cancelMentorReminder(context: Context) {
        try {
            NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID_MENTOR_REMINDER)
        } catch (e: Exception) {}
    }

    /**
     * Cancels any active reminder notification (e.g. after successful check or user snooze).
     */
    fun cancelReminder(context: Context) {
        try {
            NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID_REMINDER)
        } catch (e: Exception) {}
    }
}
