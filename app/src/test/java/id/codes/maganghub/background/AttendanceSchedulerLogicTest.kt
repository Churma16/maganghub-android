package id.codes.maganghub.background

import id.codes.maganghub.data.model.AttendanceHomeData
import id.codes.maganghub.data.model.ReminderSettings
import org.junit.Assert.assertEquals
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
}
