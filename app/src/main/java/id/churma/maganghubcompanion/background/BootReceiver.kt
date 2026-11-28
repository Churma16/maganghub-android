package id.churma.maganghubcompanion.background

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * Reschedules AlarmManager exact alarms and registers WorkManager periodic safety net
 * when the Android device boots or when the application package is updated.
 */
class BootReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "BootReceiver"
    }

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        Log.i(TAG, "Boot / Package replaced event received: $action")

        when (action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            "android.intent.action.QUICKBOOT_POWERON",
            "com.htc.intent.action.QUICKBOOT_POWERON" -> {
                Log.i(TAG, "Initializing channels, exact alarms, and WorkManager periodic checks...")
                NotificationHelper.createNotificationChannels(context)
                AttendanceAlarmScheduler.scheduleNextAlarm(context)
                AttendanceCheckWorker.enqueuePeriodicCheck(context)
            }
            else -> {
                Log.d(TAG, "Ignored broadcast action: $action")
            }
        }
    }
}
