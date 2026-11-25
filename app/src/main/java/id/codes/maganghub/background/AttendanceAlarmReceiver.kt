package id.codes.maganghub.background

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import id.codes.maganghub.MagangHubApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Handles exact alarms dispatched by AlarmManager and quick action clicks from notifications.
 */
class AttendanceAlarmReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "AttendanceAlarmReceiver"
    }

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        Log.i(TAG, "Received broadcast action: $action")

        when (action) {
            AttendanceAlarmScheduler.ACTION_ALARM_TRIGGER -> {
                Log.i(TAG, "Alarm triggered: enqueuing immediate attendance verification...")
                AttendanceCheckWorker.enqueueImmediateCheck(context)
            }
            NotificationHelper.ACTION_CHECK_NOW -> {
                Log.i(TAG, "User triggered 'Cek Ulang' from notification action")
                AttendanceCheckWorker.enqueueImmediateCheck(context)
            }
            NotificationHelper.ACTION_SNOOZE_30 -> {
                Log.i(TAG, "User triggered 'Snooze 30 Menit' from notification action")
                AttendanceAlarmScheduler.snooze(context, 30)
            }
            NotificationHelper.ACTION_SNOOZE_MENTOR_ONE_DAY -> {
                Log.i(TAG, "User triggered 'Tunda 1 Hari' mentor reminder snooze")
                NotificationHelper.cancelMentorReminder(context)
                val snoozedUntil = System.currentTimeMillis() + (24 * 60 * 60 * 1000L)
                try {
                    MagangHubApp.instance.encryptedPreferences.saveMentorSnoozedUntil(snoozedUntil)
                } catch (e: NoSuchMethodError) {
                    val prefs = MagangHubApp.instance.encryptedPreferences
                    val settings = prefs.getReminderSettings()
                    prefs.saveReminderSettings(settings.copy(mentorSnoozedUntil = snoozedUntil))
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to persist mentor snooze timestamp", e)
                }

                try {
                    val repo = MagangHubApp.instance.attendanceRepository
                    val pendingResult = goAsync()
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            repo.snoozeMentorReminderOneDay()
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to execute repo.snoozeMentorReminderOneDay()", e)
                        } finally {
                            pendingResult.finish()
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Could not launch repo snooze coroutine", e)
                }
            }
            else -> {
                Log.w(TAG, "Unhandled broadcast action: $action")
            }
        }
    }
}
