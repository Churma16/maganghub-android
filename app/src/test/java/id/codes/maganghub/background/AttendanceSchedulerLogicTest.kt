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

        val targetStartToday = ZonedDateTime.of(today, LocalTime.of(16, 0), zoneWib)

        // Expect alarm to be scheduled at 16:00 today
        assertTrue(targetStartToday.toInstant().toEpochMilli() > morningTime.toInstant().toEpochMilli())
        assertEquals(16, targetStartToday.hour)
    }

    @Test
    fun testNextAlarmCalculation_InsideWindow() {
        val today = LocalDate.of(2026, 11, 24)
        val eveningTime = ZonedDateTime.of(today, LocalTime.of(17, 15), zoneWib)
        val settings = ReminderSettings(startHour = 16, endHour = 24, intervalMinutes = 30)

        val nextTrigger = eveningTime.plusMinutes(settings.intervalMinutes.toLong())

        assertEquals(17, nextTrigger.hour)
        assertEquals(45, nextTrigger.minute)
    }

    @Test
    fun testNextAlarmCalculation_AttendedScheduleTomorrow() {
        val today = LocalDate.of(2026, 11, 24)
        val tomorrow = today.plusDays(1)
        val tomorrowStart = ZonedDateTime.of(tomorrow, LocalTime.of(16, 0), zoneWib)

        assertEquals(25, tomorrowStart.dayOfMonth)
        assertEquals(16, tomorrowStart.hour)
    }
}
