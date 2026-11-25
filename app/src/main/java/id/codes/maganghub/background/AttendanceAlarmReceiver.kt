package id.codes.maganghub.background

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import id.codes.maganghub.MagangHubApp

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
                val interval = try {
                    MagangHubApp.instance.encryptedPreferences.getReminderSettings().intervalMinutes
                } catch (e: Exception) { 30 }
                val effectiveInterval = if (interval > 0) interval else 30
                Log.i(TAG, "User triggered 'Snooze' from notification action ($effectiveInterval minutes)")
                AttendanceAlarmScheduler.snooze(context, effectiveInterval)
            }
            else -> {
                Log.w(TAG, "Unhandled broadcast action: $action")
            }
        }
    }
}
