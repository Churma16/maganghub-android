package id.codes.maganghub.background

import android.content.Context
import android.util.Log
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import id.codes.maganghub.MagangHubApp
import id.codes.maganghub.data.api.UnauthorizedSessionException
import id.codes.maganghub.data.model.AttendanceStatus
import id.codes.maganghub.data.model.ReminderSettings
import id.codes.maganghub.widget.AttendanceWidgetHelper
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

/**
 * Background CoroutineWorker executing attendance checks against MagangHub Monev API.
 * Integrates directly with AttendanceRepository, NotificationHelper, and AttendanceAlarmScheduler.
 */
class AttendanceCheckWorker(
    private val appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        private const val TAG = "AttendanceCheckWorker"
        const val PERIODIC_WORK_NAME = "maganghub_periodic_attendance_check"
        const val IMMEDIATE_WORK_NAME = "maganghub_immediate_attendance_check"

        /**
         * Enqueues a periodic background check (every 15-30 minutes) as a durable safety net.
         */
        fun enqueuePeriodicCheck(context: Context, intervalMinutes: Long = 30) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val workRequest = PeriodicWorkRequestBuilder<AttendanceCheckWorker>(
                intervalMinutes.coerceAtLeast(15), TimeUnit.MINUTES,
                5, TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                PERIODIC_WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                workRequest
            )
            Log.i(TAG, "Enqueued periodic WorkManager check every $intervalMinutes minutes.")
        }

        /**
         * Enqueues an immediate one-time check.
         */
        fun enqueueImmediateCheck(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val workRequest = OneTimeWorkRequestBuilder<AttendanceCheckWorker>()
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                IMMEDIATE_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                workRequest
            )
            Log.i(TAG, "Enqueued immediate OneTimeWorkRequest for attendance check.")
        }

        /**
         * Pure function to determine whether network attendance check should be skipped.
         * Skips network call only if today's attendance has ALREADY been confirmed.
         * Does NOT skip outside reminder window if not attended yet, allowing daytime attendance sync.
         */
        fun shouldSkipAttendanceCheck(
            todayDateString: String,
            cachedDate: String?,
            cachedHasAttendance: Boolean
        ): Boolean {
            return cachedDate == todayDateString && cachedHasAttendance
        }
    }

    override suspend fun doWork(): Result {
        Log.i(TAG, "Starting background attendance verification cycle...")

        val nowWib = ZonedDateTime.now(AttendanceAlarmScheduler.ZONE_WIB)
        val todayDateString = nowWib.toLocalDate().toString()
        val currentTimeString = nowWib.format(DateTimeFormatter.ofPattern("HH:mm"))

        AttendanceAlarmScheduler.checkAndResetDailyRollover(appContext, todayDateString)

        val cachedHomeData = try {
            MagangHubApp.instance.encryptedPreferences.getLastHomeData()
        } catch (_: Exception) {
            null
        }

        // Early return to conserve battery if today's attendance has already been confirmed
        if (shouldSkipAttendanceCheck(
                todayDateString = todayDateString,
                cachedDate = cachedHomeData?.date,
                cachedHasAttendance = cachedHomeData?.hasAttendance == true
            )
        ) {
            Log.i(TAG, "Today's attendance already confirmed ($todayDateString). Early return to conserve battery.")
            if (!AttendanceAlarmScheduler.isCongratulationsSentToday(appContext)) {
                NotificationHelper.showSuccessNotification(appContext)
                AttendanceAlarmScheduler.markCongratulationsSent(appContext)
            } else {
                NotificationHelper.cancelReminder(appContext)
            }
            AttendanceAlarmScheduler.scheduleNextAlarm(appContext)
            AttendanceWidgetHelper.updateWidget(appContext)
            return Result.success()
        }

        val repository = try {
            MagangHubApp.instance.attendanceRepository
        } catch (e: Exception) {
            Log.e(TAG, "Failed to obtain AttendanceRepository instance", e)
            return Result.retry()
        }

        val settings = try {
            MagangHubApp.instance.encryptedPreferences.getReminderSettings()
        } catch (e: Exception) {
            ReminderSettings()
        }

        val checkResult = repository.checkAttendance(forceRefresh = true)

        if (checkResult.isFailure) {
            val exception = checkResult.exceptionOrNull()
            Log.e(TAG, "Attendance verification failed", exception)

            if (exception is id.codes.maganghub.data.api.RateLimitedException) {
                val backoffSec = exception.retryAfterSeconds.coerceAtLeast(60L)
                val resumeEpochMillis = System.currentTimeMillis() + (backoffSec * 1000L)
                Log.w(TAG, "MagangHub API rate limited (HTTP 429). Rescheduling next alarm in $backoffSec seconds.")
                AttendanceAlarmScheduler.scheduleNextAlarm(appContext, overrideTriggerMillis = resumeEpochMillis)
                return Result.success()
            }

            if (exception is UnauthorizedSessionException && !AttendanceAlarmScheduler.isAuthAlertSentToday(appContext)) {
                NotificationHelper.showSessionExpiredNotification(appContext)
                AttendanceAlarmScheduler.setAuthAlertSent(appContext, true)
            }

            // Reschedule next alarm attempt
            AttendanceAlarmScheduler.scheduleNextAlarm(appContext)
            AttendanceWidgetHelper.updateWidget(appContext)
            return if (runAttemptCount >= 3) {
                Log.w(TAG, "Attendance check failed after $runAttemptCount attempts. Abandoning worker retry.")
                Result.failure()
            } else {
                Result.retry()
            }
        }

        val resultData = checkResult.getOrNull()

        // Mentor review reminder verification
        if (settings.mentorReminderEnabled) {
            val pendingCount = resultData?.pendingMentorReviewCount?.takeIf { it > 0 }
                ?: try {
                    repository.getCachedPendingMentorReviewCount()
                } catch (e: Throwable) {
                    try {
                        MagangHubApp.instance.encryptedPreferences.getPendingMentorReviewCount()
                    } catch (e2: Throwable) {
                        0
                    }
                }

            val nowMillis = System.currentTimeMillis()
            val lastMentorNotificationTime = AttendanceAlarmScheduler.getLastMentorReminderTimeMillis(appContext)

            if (AttendanceAlarmScheduler.shouldTriggerMentorReminder(
                    mentorReminderEnabled = settings.mentorReminderEnabled,
                    pendingCount = pendingCount,
                    thresholdDays = settings.mentorReviewThresholdDays,
                    snoozedUntil = settings.mentorSnoozedUntil,
                    lastMentorReminderTime = lastMentorNotificationTime,
                    repeatIntervalMinutes = settings.mentorRepeatIntervalMinutes,
                    currentTimeMillis = nowMillis
                )
            ) {
                Log.i(TAG, "Dispatching mentor review reminder (pendingCount=$pendingCount, threshold=${settings.mentorReviewThresholdDays})")
                NotificationHelper.showMentorReviewReminderNotification(
                    context = appContext,
                    pendingCount = pendingCount,
                    thresholdDays = settings.mentorReviewThresholdDays,
                    soundEnabled = settings.soundEnabled,
                    vibrationEnabled = settings.vibrationEnabled
                )
                AttendanceAlarmScheduler.updateLastMentorReminderTimeMillis(appContext, nowMillis)
            }
        }

        val homeData = resultData?.data
        AttendanceWidgetHelper.updateWidget(appContext)

        if (homeData == null) {
            AttendanceAlarmScheduler.scheduleNextAlarm(appContext)
            return Result.success()
        }

        Log.i(TAG, "Attendance check succeeded. Date: ${homeData.date}, HasAttendance: ${homeData.hasAttendance}")

        if (homeData.hasAttendance) {
            // Attendance is confirmed
            if (!AttendanceAlarmScheduler.isCongratulationsSentToday(appContext)) {
                NotificationHelper.showSuccessNotification(appContext)
                AttendanceAlarmScheduler.markCongratulationsSent(appContext)
            } else {
                NotificationHelper.cancelReminder(appContext)
            }
            AttendanceAlarmScheduler.scheduleNextAlarm(appContext)
            return Result.success()
        }

        // Attendance NOT completed
        if (homeData.isHoliday || homeData.isScheduledOffDay) {
            Log.i(TAG, "Today is holiday or scheduled off-day. Skipping reminder.")
            NotificationHelper.cancelReminder(appContext)
            AttendanceAlarmScheduler.scheduleNextAlarm(appContext)
            return Result.success()
        }

        if (!homeData.activeInternship) {
            Log.i(TAG, "Internship inactive. Skipping reminder.")
            NotificationHelper.cancelReminder(appContext)
            AttendanceAlarmScheduler.scheduleNextAlarm(appContext)
            return Result.success()
        }

        // Active workday & not attended yet: check if current hour is within spam reminder window
        val currentHour = nowWib.hour
        val isWithinWindow = currentHour >= settings.startHour && currentHour < settings.endHour && settings.isEnabled
        if (!isWithinWindow) {
            Log.i(TAG, "Current hour ($currentHour WIB) outside reminder window [${settings.startHour}-${settings.endHour}) or reminder disabled.")
            AttendanceAlarmScheduler.scheduleNextAlarm(appContext)
            return Result.success()
        }

        val reminderCount = AttendanceAlarmScheduler.incrementDailyReminderCount(appContext)
        Log.i(TAG, "Dispatching spam reminder #$reminderCount at $currentTimeString WIB")

        NotificationHelper.showReminderNotification(
            context = appContext,
            soundEnabled = settings.soundEnabled,
            vibrationEnabled = settings.vibrationEnabled,
            intervalMinutes = settings.intervalMinutes
        )

        // Reschedule next exact alarm
        AttendanceAlarmScheduler.scheduleNextAlarm(appContext)
        return Result.success()
    }
}
