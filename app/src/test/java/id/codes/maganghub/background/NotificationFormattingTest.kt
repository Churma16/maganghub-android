package id.codes.maganghub.background

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

class NotificationFormattingTest {

    private val zoneWib: ZoneId = ZoneId.of("Asia/Jakarta")
    private val testDate: LocalDate = LocalDate.of(2026, 11, 25)

    @Test
    fun testFormatRemainingTimeToDeadline_HoursAndMinutes() {
        // 21:00 WIB -> 2 hours 59 minutes until 23:59 WIB
        val time = ZonedDateTime.of(testDate, LocalTime.of(21, 0, 0), zoneWib)
        val result = NotificationHelper.formatRemainingTimeToDeadline(time)
        assertEquals("2 jam 59 menit", result)
    }

    @Test
    fun testFormatRemainingTimeToDeadline_OnlyMinutes() {
        // 23:15 WIB -> 44 minutes until 23:59 WIB
        val time = ZonedDateTime.of(testDate, LocalTime.of(23, 15, 0), zoneWib)
        val result = NotificationHelper.formatRemainingTimeToDeadline(time)
        assertEquals("44 menit", result)
    }

    @Test
    fun testFormatRemainingTimeToDeadline_ExactHours() {
        // 21:59 WIB -> exactly 2 hours 0 minutes until 23:59 WIB
        val time = ZonedDateTime.of(testDate, LocalTime.of(21, 59, 0), zoneWib)
        val result = NotificationHelper.formatRemainingTimeToDeadline(time)
        assertEquals("2 jam", result)
    }

    @Test
    fun testFormatRemainingTimeToDeadline_OneMinute() {
        // 23:58:15 WIB -> 45 seconds -> rounded to 1 minute
        val time = ZonedDateTime.of(testDate, LocalTime.of(23, 58, 15), zoneWib)
        val result = NotificationHelper.formatRemainingTimeToDeadline(time)
        assertEquals("1 menit", result)
    }

    @Test
    fun testFormatRemainingTimeToDeadline_LessThanOneMinute() {
        // 23:58:45 WIB -> 15 seconds left -> less than 1 minute
        val time = ZonedDateTime.of(testDate, LocalTime.of(23, 58, 45), zoneWib)
        val result = NotificationHelper.formatRemainingTimeToDeadline(time)
        assertEquals("kurang dari 1 menit", result)
    }

    @Test
    fun testFormatRemainingTimeToDeadline_PastDeadline() {
        // 23:59:30 WIB -> past deadline -> less than 1 minute
        val time = ZonedDateTime.of(testDate, LocalTime.of(23, 59, 30), zoneWib)
        val result = NotificationHelper.formatRemainingTimeToDeadline(time)
        assertEquals("kurang dari 1 menit", result)
    }

    @Test
    fun testFormatRemainingTimeToDeadline_StartOfReminderWindow() {
        // 16:00 WIB -> 7 hours 59 minutes until 23:59 WIB
        val time = ZonedDateTime.of(testDate, LocalTime.of(16, 0, 0), zoneWib)
        val result = NotificationHelper.formatRemainingTimeToDeadline(time)
        assertEquals("7 jam 59 menit", result)
    }

    @Test
    fun testFormatRemainingTimeToDeadline_NonWibTimezone() {
        // 00:15 WIT on Nov 26 is equivalent to 22:15 WIB on Nov 25 -> 1 hour 44 minutes until 23:59 WIB
        val witZone = ZoneId.of("Asia/Jayapura")
        val timeWit = ZonedDateTime.of(LocalDate.of(2026, 11, 26), LocalTime.of(0, 15, 0), witZone)
        val result = NotificationHelper.formatRemainingTimeToDeadline(timeWit)
        assertEquals("1 jam 44 menit", result)
    }
}
