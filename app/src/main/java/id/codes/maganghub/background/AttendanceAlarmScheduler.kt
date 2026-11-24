package id.codes.maganghub.background

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import id.codes.maganghub.MagangHubApp
import id.codes.maganghub.data.model.ReminderSettings
import id.codes.maganghub.ui.MainActivity
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * Manages exact alarm scheduling for MagangHub spam reminders
 * between 16:00 and 23:59 WIB (Asia/Jakarta, UTC+7).
 */
object AttendanceAlarmScheduler {

    private const val TAG = "AlarmScheduler"

    val ZONE_WIB: ZoneId = ZoneId.of("Asia/Jakarta")

    const val ACTION_ALARM_TRIGGER = "id.codes.maganghub.ACTION_ALARM_TRIGGER"
    const val REQUEST_CODE_ALARM = 2001

    private const val PREFS_SCHEDULER_STATE = "maganghub_scheduler_state"
    private const val KEY_STATE_DATE = "state_date"
    private const val KEY_STATE_REMINDER_COUNT = "state_reminder_count"
    private const val KEY_STATE_LAST_REMINDER_TIME = "state_last_reminder_time"
    private const val KEY_STATE_CONGRATS_SENT = "state_congrats_sent"
    private const val KEY_STATE_AUTH_ALERT_SENT = "state_auth_alert_sent"

    /**
     * Resets or rolls over daily scheduler state when a new calendar day in WIB starts.
     */
    fun checkAndResetDailyRollover(context: Context, todayDateString: String) {
        val prefs = context.getSharedPreferences(PREFS_SCHEDULER_STATE, Context.MODE_PRIVATE)
        val savedDate = prefs.getString(KEY_STATE_DATE, null)
        if (savedDate != todayDateString) {
            Log.i(TAG, "Daily rollover detected ($savedDate -> $todayDateString). Resetting daily reminder counters.")
            prefs.edit()
                .putString(KEY_STATE_DATE, todayDateString)
                .putInt(KEY_STATE_REMINDER_COUNT, 0)
                .putLong(KEY_STATE_LAST_REMINDER_TIME, 0L)
                .putBoolean(KEY_STATE_CONGRATS_SENT, false)
                .putBoolean(KEY_STATE_AUTH_ALERT_SENT, false)
                .apply()
        }
    }

    fun getDailyReminderCount(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_SCHEDULER_STATE, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_STATE_REMINDER_COUNT, 0)
    }

    fun incrementDailyReminderCount(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_SCHEDULER_STATE, Context.MODE_PRIVATE)
        val newCount = prefs.getInt(KEY_STATE_REMINDER_COUNT, 0) + 1
        prefs.edit()
            .putInt(KEY_STATE_REMINDER_COUNT, newCount)
            .putLong(KEY_STATE_LAST_REMINDER_TIME, System.currentTimeMillis())
            .apply()
        return newCount
    }

    fun isCongratulationsSentToday(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_SCHEDULER_STATE, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_STATE_CONGRATS_SENT, false)
    }

    fun markCongratulationsSent(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_SCHEDULER_STATE, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_STATE_CONGRATS_SENT, true).apply()
    }

    fun isAuthAlertSentToday(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_SCHEDULER_STATE, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_STATE_AUTH_ALERT_SENT, false)
    }

    fun setAuthAlertSent(context: Context, sent: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_SCHEDULER_STATE, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_STATE_AUTH_ALERT_SENT, sent).apply()
    }

    fun getLastReminderSentTimeMillis(context: Context): Long {
        val prefs = context.getSharedPreferences(PREFS_SCHEDULER_STATE, Context.MODE_PRIVATE)
        return prefs.getLong(KEY_STATE_LAST_REMINDER_TIME, 0L)
    }

    /**
     * Schedules the next exact alarm based on current WIB time and reminder settings.
     *
     * @param context Application or receiver context.
     * @param overrideTriggerMillis Optional specific epoch millisecond timestamp (e.g. for snooze).
     */
    fun scheduleNextAlarm(context: Context, overrideTriggerMillis: Long? = null) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        // Read user settings
        val settings = try {
            MagangHubApp.instance.encryptedPreferences.getReminderSettings()
        } catch (e: Exception) {
            ReminderSettings()
        }

        if (!settings.isEnabled && overrideTriggerMillis == null) {
            Log.i(TAG, "Reminders are disabled in settings. Skipping alarm scheduling.")
            cancelAlarm(context)
            return
        }

        val triggerEpochMillis: Long = if (overrideTriggerMillis != null) {
            overrideTriggerMillis
        } else {
            calculateNextTriggerMillis(context, settings)
        }

        val intent = Intent(context, AttendanceAlarmReceiver::class.java).apply {
            action = ACTION_ALARM_TRIGGER
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_ALARM,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerWib = ZonedDateTime.ofInstant(
            java.time.Instant.ofEpochMilli(triggerEpochMillis),
            ZONE_WIB
        )
        Log.i(TAG, "Scheduling next exact alarm for: $triggerWib (${triggerEpochMillis - System.currentTimeMillis()} ms from now)")

        try {
            // Check exact alarm permissions on Android 12+ (API 31+)
            val canScheduleExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                alarmManager.canScheduleExactAlarms()
            } else {
                true
            }

            if (canScheduleExact) {
                // Use setAlarmClock for highest reliability through aggressive Doze mode & OEM task killers
                val showIntent = Intent(context, MainActivity::class.java)
                val showPendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    showIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                val alarmClockInfo = AlarmManager.AlarmClockInfo(triggerEpochMillis, showPendingIntent)
                alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
            } else {
                Log.w(TAG, "Cannot schedule exact alarm (permission missing). Falling back to setAndAllowWhileIdle.")
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerEpochMillis, pendingIntent)
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException while scheduling exact alarm, falling back to set()", e)
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerEpochMillis, pendingIntent)
        }
    }

    /**
     * Calculates next alarm trigger epoch millis based on WIB business logic:
     * - If today's attendance already completed: schedule for 16:00 WIB tomorrow.
     * - If before 16:00 WIB: schedule for 16:00 WIB today.
     * - If between 16:00 and 23:59 WIB: schedule for next interval (e.g. +30 mins).
     * - If past 23:59 WIB: schedule for 16:00 WIB today (or tomorrow).
     */
    fun calculateNextTriggerMillis(context: Context, settings: ReminderSettings): Long {
        val nowWib = ZonedDateTime.now(ZONE_WIB)
        val todayDate = nowWib.toLocalDate()
        val todayDateString = todayDate.toString()

        checkAndResetDailyRollover(context, todayDateString)

        val cachedHomeData = try {
            MagangHubApp.instance.encryptedPreferences.getLastHomeData()
        } catch (e: Exception) {
            null
        }

        // If attendance for today is already completed, schedule for tomorrow at startHour
        if (cachedHomeData != null && cachedHomeData.date == todayDateString && cachedHomeData.hasAttendance) {
            val tomorrowStart = ZonedDateTime.of(
                todayDate.plusDays(1),
                LocalTime.of(settings.startHour, 0),
                ZONE_WIB
            )
            return tomorrowStart.toInstant().toEpochMilli()
        }

        val currentHour = nowWib.hour
        val startHour = settings.startHour
        val endHour = settings.endHour
        val intervalMinutes = if (settings.intervalMinutes > 0) settings.intervalMinutes else 30

        return when {
            // Before reminder window (e.g. 08:00 WIB): schedule for 16:00 WIB today
            currentHour < startHour -> {
                val todayStart = ZonedDateTime.of(
                    todayDate,
                    LocalTime.of(startHour, 0),
                    ZONE_WIB
                )
                todayStart.toInstant().toEpochMilli()
            }
            // Inside active spam reminder window (16:00 - 23:59 WIB)
            currentHour < endHour -> {
                // Align to next interval (e.g. 30 minutes from now, with minimum 60s delay)
                val nextTrigger = nowWib.plusMinutes(intervalMinutes.toLong())
                if (nextTrigger.hour >= endHour) {
                    // Falls past end of window tonight; schedule for tomorrow at startHour
                    val tomorrowStart = ZonedDateTime.of(
                        todayDate.plusDays(1),
                        LocalTime.of(startHour, 0),
                        ZONE_WIB
                    )
                    tomorrowStart.toInstant().toEpochMilli()
                } else {
                    nextTrigger.toInstant().toEpochMilli()
                }
            }
            // Past reminder window (after midnight / >= 24:00): schedule for 16:00 WIB today/tomorrow
            else -> {
                val nextStart = ZonedDateTime.of(
                    todayDate.plusDays(1),
                    LocalTime.of(startHour, 0),
                    ZONE_WIB
                )
                nextStart.toInstant().toEpochMilli()
            }
        }
    }

    /**
     * Postpones the alarm by specified minutes (default: 30 minutes) and cancels notification.
     */
    fun snooze(context: Context, minutes: Int = 30) {
        NotificationHelper.cancelReminder(context)
        val snoozeMillis = System.currentTimeMillis() + (minutes * 60 * 1000L)
        Log.i(TAG, "Snoozing alarm for $minutes minutes (trigger at $snoozeMillis)")
        scheduleNextAlarm(context, overrideTriggerMillis = snoozeMillis)
    }

    /**
     * Cancels any pending scheduled alarm.
     */
    fun cancelAlarm(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, AttendanceAlarmReceiver::class.java).apply {
            action = ACTION_ALARM_TRIGGER
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_ALARM,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
            Log.i(TAG, "Cancelled existing AttendanceAlarmReceiver pending alarm.")
        }
    }
}
