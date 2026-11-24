package id.codes.maganghub.background

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

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
            else -> {
                Log.w(TAG, "Unhandled broadcast action: $action")
            }
        }
    }
}
