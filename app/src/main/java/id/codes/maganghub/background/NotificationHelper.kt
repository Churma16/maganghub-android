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
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import id.codes.maganghub.R
import id.codes.maganghub.ui.MainActivity

object NotificationHelper {

    const val CHANNEL_REMINDER_ID = "channel_maganghub_reminder"
    const val CHANNEL_CONFIRMATION_ID = "channel_maganghub_confirmation"
    const val CHANNEL_SYSTEM_ID = "channel_maganghub_system"

    const val NOTIFICATION_ID_REMINDER = 1001
    const val NOTIFICATION_ID_CONFIRMATION = 1002
    const val NOTIFICATION_ID_AUTH_ALERT = 1003

    const val ACTION_CHECK_NOW = "id.codes.maganghub.ACTION_CHECK_NOW"
    const val ACTION_SNOOZE_30 = "id.codes.maganghub.ACTION_SNOOZE_30"

    const val URL_MONEV_DASHBOARD = "https://monev.maganghub.kemnaker.go.id/dashboard"

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

        // 1. High Priority Reminder Channel (with sound & prominent vibration)
        val reminderChannel = NotificationChannel(
            CHANNEL_REMINDER_ID,
            "Pengingat Presensi MagangHub",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Pengingat berkala pukul 16:00 - 23:59 WIB jika belum presensi & isi logbook"
            enableLights(true)
            lightColor = Color.RED
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 500, 200, 500, 200, 500)
            setSound(soundUri, audioAttributes)
            lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
        }

        // 2. Default Priority Confirmation Channel
        val confirmationChannel = NotificationChannel(
            CHANNEL_CONFIRMATION_ID,
            "Konfirmasi Presensi Tercatat",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Konfirmasi sukses setelah presensi & logbook harian berhasil tercatat"
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

        notificationManager.createNotificationChannels(
            listOf(reminderChannel, confirmationChannel, systemChannel)
        )
    }

    /**
     * Shows high-priority heads-up reminder notification with action buttons.
     */
    fun showReminderNotification(
        context: Context,
        reminderIndex: Int,
        currentTimeWib: String,
        dateFormatted: String,
        soundEnabled: Boolean = true,
        vibrationEnabled: Boolean = true
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

        // Quick Action 2: "Snooze 30m"
        val snoozeIntent = Intent(context, AttendanceAlarmReceiver::class.java).apply {
            action = ACTION_SNOOZE_30
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            3,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = "🚨 PERINGATAN: BELUM ABSEN MAGANGHUB!"
        val bigText = StringBuilder()
            .append("Batas waktu pengisian: 23:59 WIB\n\n")
            .append("• Status: 🔴 BELUM ABSEN (Peringatan #$reminderIndex)\n")
            .append("• Waktu Sekarang: $currentTimeWib WIB\n")
            .append("• Tanggal: $dateFormatted\n\n")
            .append("Presensi & logbook harian Anda belum terisi di Monev Kemnaker. Ketuk 'Buka Monev' untuk mengisi sekarang!")
            .toString()

        val builder = NotificationCompat.Builder(context, CHANNEL_REMINDER_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText("Batas 23:59 WIB! Presensi & logbook hari ini belum terisi (#$reminderIndex).")
            .setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(false)
            .setOngoing(false)
            .setContentIntent(openAppPendingIntent)
            .addAction(0, "Buka Monev", monevWebPendingIntent)
            .addAction(0, "Snooze 30 Menit", snoozePendingIntent)

        if (!soundEnabled) {
            builder.setSilent(true)
        }
        if (vibrationEnabled) {
            builder.setVibrate(longArrayOf(0, 500, 200, 500, 200, 500))
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
     * Shows daily confirmation notification when attendance has been fulfilled.
     */
    fun showSuccessNotification(context: Context, dateFormatted: String) {
        createNotificationChannels(context)

        // Cancel existing reminder notification
        cancelReminder(context)

        val monevWebIntent = Intent(Intent.ACTION_VIEW, Uri.parse(URL_MONEV_DASHBOARD)).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val monevWebPendingIntent = PendingIntent.getActivity(
            context,
            4,
            monevWebIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_CONFIRMATION_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("🎉 Presensi MagangHub Tercatat!")
            .setContentText("Presensi dan logbook harian Anda untuk tanggal $dateFormatted sudah tercatat.")
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    "✅ Status Kehadiran: SUDAH ABSEN\n" +
                    "📅 Tanggal: $dateFormatted\n" +
                    "🎯 Status Magang: Aktif 🟢\n\n" +
                    "Presensi dan logbook harian Anda telah berhasil tercatat di sistem Monev MagangHub Kemnaker. Kerja bagus hari ini!"
                )
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .addAction(0, "Buka Monev", monevWebPendingIntent)

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
     * Cancels any active reminder notification (e.g. after successful check or user snooze).
     */
    fun cancelReminder(context: Context) {
        try {
            NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID_REMINDER)
        } catch (e: Exception) {}
    }
}
