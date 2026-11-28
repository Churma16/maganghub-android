package id.churma.maganghubcompanion.background

import id.churma.maganghubcompanion.data.model.AttendanceHomeData
import id.churma.maganghubcompanion.data.model.ReminderSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

class AttendanceSchedulerLogicTest {

    private val zoneWib = ZoneId.of("Asia/Jakarta")

    @Test
    fun testWibTimezoneHandling() {
        val nowWib = ZonedDateTime.now(zoneWib)
        assertEquals("Asia/Jakarta", nowWib.zone.id)
    }

    @Test
    fun testNextAlarmCalculation_Before16Wib() {
        val today = LocalDate.of(2026, 11, 24)
        val morningTime = ZonedDateTime.of(today, LocalTime.of(9, 30), zoneWib)
        val settings = ReminderSettings(startHour = 16, endHour = 24, intervalMinutes = 30)

        val nextTrigger = AttendanceAlarmScheduler.calculateNextTriggerTime(morningTime, false, settings)

        assertEquals(16, nextTrigger.hour)
        assertEquals(0, nextTrigger.minute)
        assertEquals(today, nextTrigger.toLocalDate())
    }

    @Test
    fun testNextAlarmCalculation_InsideWindow() {
        val today = LocalDate.of(2026, 11, 24)
        val eveningTime = ZonedDateTime.of(today, LocalTime.of(17, 15), zoneWib)
        val settings = ReminderSettings(startHour = 16, endHour = 24, intervalMinutes = 30)

        val nextTrigger = AttendanceAlarmScheduler.calculateNextTriggerTime(eveningTime, false, settings)

        assertEquals(17, nextTrigger.hour)
        assertEquals(45, nextTrigger.minute)
        assertEquals(today, nextTrigger.toLocalDate())
    }

    @Test
    fun testNextAlarmCalculation_AttendedScheduleTomorrow() {
        val today = LocalDate.of(2026, 11, 24)
        val afternoonTime = ZonedDateTime.of(today, LocalTime.of(14, 0), zoneWib)
        val settings = ReminderSettings(startHour = 16, endHour = 24, intervalMinutes = 30)

        val nextTrigger = AttendanceAlarmScheduler.calculateNextTriggerTime(afternoonTime, true, settings)

        assertEquals(today.plusDays(1), nextTrigger.toLocalDate())
        assertEquals(16, nextTrigger.hour)
        assertEquals(0, nextTrigger.minute)
    }

    @Test
    fun testNextAlarmCalculation_EndOfWindowRollsToTomorrow() {
        val today = LocalDate.of(2026, 11, 24)
        val lateNightTime = ZonedDateTime.of(today, LocalTime.of(23, 45), zoneWib)
        val settings = ReminderSettings(startHour = 16, endHour = 24, intervalMinutes = 30)

        val nextTrigger = AttendanceAlarmScheduler.calculateNextTriggerTime(lateNightTime, false, settings)

        assertEquals(today.plusDays(1), nextTrigger.toLocalDate())
        assertEquals(16, nextTrigger.hour)
        assertEquals(0, nextTrigger.minute)
    }

    @Test
    fun testNextAlarmCalculation_WithJitter() {
        val today = LocalDate.of(2026, 11, 24)
        val eveningTime = ZonedDateTime.of(today, LocalTime.of(17, 0), zoneWib)
        val settings = ReminderSettings(startHour = 16, endHour = 24, intervalMinutes = 30)

        // Positive jitter (+2 minutes -> 32 minute interval)
        val triggerPos = AttendanceAlarmScheduler.calculateNextTriggerTime(
            nowWib = eveningTime,
            hasAttendedToday = false,
            settings = settings,
            jitterMinutes = 2
        )
        assertEquals(17, triggerPos.hour)
        assertEquals(32, triggerPos.minute)

        // Negative jitter (-3 minutes -> 27 minute interval)
        val triggerNeg = AttendanceAlarmScheduler.calculateNextTriggerTime(
            nowWib = eveningTime,
            hasAttendedToday = false,
            settings = settings,
            jitterMinutes = -3
        )
        assertEquals(17, triggerNeg.hour)
        assertEquals(27, triggerNeg.minute)
    }

    @Test
    fun testNextAlarmCalculation_WithDeviceSeedBeforeStartHour() {
        val today = LocalDate.of(2026, 11, 24)
        val morningTime = ZonedDateTime.of(today, LocalTime.of(10, 0), zoneWib)
        val settings = ReminderSettings(startHour = 16, endHour = 24, intervalMinutes = 30)

        val triggerSeed = AttendanceAlarmScheduler.calculateNextTriggerTime(
            nowWib = morningTime,
            hasAttendedToday = false,
            settings = settings,
            deviceSeedMinutes = 5
        )
        assertEquals(16, triggerSeed.hour)
        assertEquals(5, triggerSeed.minute)
    }

    @Test
    fun testNextAlarmCalculation_ClampedJitterAndSeed() {
        val today = LocalDate.of(2026, 11, 24)
        val morningTime = ZonedDateTime.of(today, LocalTime.of(8, 0), zoneWib)
        val settings = ReminderSettings(startHour = 16, endHour = 24, intervalMinutes = 30)

        // Device seed > 7 should clamp to 7
        val triggerClampedSeed = AttendanceAlarmScheduler.calculateNextTriggerTime(
            nowWib = morningTime,
            hasAttendedToday = false,
            settings = settings,
            deviceSeedMinutes = 20
        )
        assertEquals(16, triggerClampedSeed.hour)
        assertEquals(7, triggerClampedSeed.minute)
    }

    @Test
    fun testShouldTriggerMentorReminder_DisabledInSettings() {
        val shouldTrigger = AttendanceAlarmScheduler.shouldTriggerMentorReminder(
            mentorReminderEnabled = false,
            pendingCount = 6,
            thresholdDays = 5,
            snoozedUntil = 0L,
            lastMentorReminderTime = 0L,
            repeatIntervalMinutes = 180,
            currentTimeMillis = 1000000L
        )
        assertFalse(shouldTrigger)
    }

    @Test
    fun testShouldTriggerMentorReminder_BelowThreshold() {
        val shouldTrigger = AttendanceAlarmScheduler.shouldTriggerMentorReminder(
            mentorReminderEnabled = true,
            pendingCount = 4,
            thresholdDays = 5,
            snoozedUntil = 0L,
            lastMentorReminderTime = 0L,
            repeatIntervalMinutes = 180,
            currentTimeMillis = 1000000L
        )
        assertFalse(shouldTrigger)
    }

    @Test
    fun testShouldTriggerMentorReminder_AtOrAboveThreshold() {
        val shouldTrigger = AttendanceAlarmScheduler.shouldTriggerMentorReminder(
            mentorReminderEnabled = true,
            pendingCount = 5,
            thresholdDays = 5,
            snoozedUntil = 0L,
            lastMentorReminderTime = 0L,
            repeatIntervalMinutes = 180,
            currentTimeMillis = 1000000L
        )
        assertTrue(shouldTrigger)
    }

    @Test
    fun testShouldTriggerMentorReminder_ActiveSnoozeSuppressesNotification() {
        val now = 1000000L
        val activeSnooze = now + (24 * 60 * 60 * 1000L) // Snoozed 24h into future

        val shouldTrigger = AttendanceAlarmScheduler.shouldTriggerMentorReminder(
            mentorReminderEnabled = true,
            pendingCount = 7,
            thresholdDays = 5,
            snoozedUntil = activeSnooze,
            lastMentorReminderTime = 0L,
            repeatIntervalMinutes = 180,
            currentTimeMillis = now
        )
        assertFalse(shouldTrigger)
    }

    @Test
    fun testShouldTriggerMentorReminder_ExpiredSnoozeAllowsNotification() {
        val now = 2000000L
        val expiredSnooze = now - 1000L // Snooze ended in past

        val shouldTrigger = AttendanceAlarmScheduler.shouldTriggerMentorReminder(
            mentorReminderEnabled = true,
            pendingCount = 7,
            thresholdDays = 5,
            snoozedUntil = expiredSnooze,
            lastMentorReminderTime = 0L,
            repeatIntervalMinutes = 180,
            currentTimeMillis = now
        )
        assertTrue(shouldTrigger)
    }

    @Test
    fun testShouldTriggerMentorReminder_RepeatIntervalThrottling() {
        val lastReminderTime = 1000000L
        val intervalMinutes = 180 // 3 hours
        val intervalMillis = intervalMinutes * 60 * 1000L

        // Before interval expires (e.g. 2 hours after last reminder)
        val beforeInterval = lastReminderTime + (120 * 60 * 1000L)
        val shouldTriggerBefore = AttendanceAlarmScheduler.shouldTriggerMentorReminder(
            mentorReminderEnabled = true,
            pendingCount = 6,
            thresholdDays = 5,
            snoozedUntil = 0L,
            lastMentorReminderTime = lastReminderTime,
            repeatIntervalMinutes = intervalMinutes,
            currentTimeMillis = beforeInterval
        )
        assertFalse(shouldTriggerBefore)

        // Exactly when or after interval expires
        val afterInterval = lastReminderTime + intervalMillis
        val shouldTriggerAfter = AttendanceAlarmScheduler.shouldTriggerMentorReminder(
            mentorReminderEnabled = true,
            pendingCount = 6,
            thresholdDays = 5,
            snoozedUntil = 0L,
            lastMentorReminderTime = lastReminderTime,
            repeatIntervalMinutes = intervalMinutes,
            currentTimeMillis = afterInterval
        )
        assertTrue(shouldTriggerAfter)
    }

    @Test
    fun testShouldSkipAttendanceCheck_EarlyReturnWhenAttendedToday() {
        val today = "2026-11-26"
        val yesterday = "2026-11-25"

        // Today attended -> skip
        assertTrue(AttendanceCheckWorker.shouldSkipAttendanceCheck(today, today, true))

        // Today NOT attended yet -> do not skip (allows user daytime attendance sync)
        assertFalse(AttendanceCheckWorker.shouldSkipAttendanceCheck(today, today, false))

        // Yesterday attended, today not checked yet -> do not skip
        assertFalse(AttendanceCheckWorker.shouldSkipAttendanceCheck(today, yesterday, true))

        // Cached date null -> do not skip
        assertFalse(AttendanceCheckWorker.shouldSkipAttendanceCheck(today, null, true))
        assertFalse(AttendanceCheckWorker.shouldSkipAttendanceCheck(today, null, false))
    }
}
