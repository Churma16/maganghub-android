package id.codes.maganghub.data.model

import id.codes.maganghub.ui.viewmodel.AttendanceUiState
import id.codes.maganghub.data.model.DailyWorkPhase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

class AttendanceStatusTest {

    @Test
    fun testAttendanceStatusMapping() {
        // Inactive internship
        val inactive = AttendanceHomeData(activeInternship = false)
        assertEquals(AttendanceStatus.TIDAK_AKTIF, inactive.toAttendanceStatus())
        assertTrue(inactive.toAttendanceStatus().isAttended)

        // Holiday
        val holiday = AttendanceHomeData(activeInternship = true, isHoliday = true)
        assertEquals(AttendanceStatus.LIBUR, holiday.toAttendanceStatus())
        assertTrue(holiday.toAttendanceStatus().isAttended)

        // Scheduled off day
        val offDay = AttendanceHomeData(activeInternship = true, isScheduledOffDay = true)
        assertEquals(AttendanceStatus.LIBUR, offDay.toAttendanceStatus())
        assertTrue(offDay.toAttendanceStatus().isAttended)

        // Attended
        val attended = AttendanceHomeData(activeInternship = true, hasAttendance = true)
        assertEquals(AttendanceStatus.HADIR, attended.toAttendanceStatus())
        assertTrue(attended.toAttendanceStatus().isAttended)

        // Missing attendance (Belum Hadir)
        val missing = AttendanceHomeData(activeInternship = true, hasAttendance = false)
        assertEquals(AttendanceStatus.BELUM_HADIR, missing.toAttendanceStatus())
        assertFalse(missing.toAttendanceStatus().isAttended)

        // Null data
        assertEquals(AttendanceStatus.UNKNOWN, AttendanceStatus.fromHomeData(null))
    }

    @Test
    fun testDailyWorkPhaseTransitions() {
        val wib = ZoneId.of("Asia/Jakarta")
        val today = LocalDate.of(2026, 11, 26)
        val settings = ReminderSettings(
            workStartHour = 10,
            workStartMinute = 0,
            startHour = 16
        )

        // 1. Completed
        val stateCompleted = AttendanceUiState(
            attendanceStatus = AttendanceStatus.HADIR,
            reminderSettings = settings
        )
        assertEquals(DailyWorkPhase.COMPLETED, stateCompleted.getDailyWorkPhase())

        // 2. Holiday
        val stateHoliday = AttendanceUiState(
            attendanceStatus = AttendanceStatus.LIBUR,
            reminderSettings = settings
        )
        assertEquals(DailyWorkPhase.HOLIDAY, stateHoliday.getDailyWorkPhase())

        // 3. Before work (07:30 WIB, before 10:00)
        val statePending = AttendanceUiState(
            attendanceStatus = AttendanceStatus.BELUM_HADIR,
            reminderSettings = settings
        )
        val timeBeforeWork = ZonedDateTime.of(today, LocalTime.of(7, 30), wib)
        assertEquals(DailyWorkPhase.BEFORE_WORK, statePending.getDailyWorkPhase(timeBeforeWork))

        // 4. Working (11:00 WIB, between 10:00 and 16:00)
        val timeWorking = ZonedDateTime.of(today, LocalTime.of(11, 0), wib)
        assertEquals(DailyWorkPhase.WORKING, statePending.getDailyWorkPhase(timeWorking))

        // 5. Reporting Window Normal (17:30 WIB, after 16:00, > 4h left to 23:59)
        val timeReportingNormal = ZonedDateTime.of(today, LocalTime.of(17, 30), wib)
        assertEquals(DailyWorkPhase.REPORTING_NORMAL, statePending.getDailyWorkPhase(timeReportingNormal))

        // 6. Reporting Window Urgent (20:30 WIB, after 20:00, < 4h left to 23:59)
        val timeReportingUrgent = ZonedDateTime.of(today, LocalTime.of(20, 30), wib)
        assertEquals(DailyWorkPhase.REPORTING_URGENT, statePending.getDailyWorkPhase(timeReportingUrgent))
    }

    @Test
    fun testMidnightRolloverToWeekendHoliday() {
        val wib = ZoneId.of("Asia/Jakarta")
        // Friday data with completed attendance
        val fridayDate = "2026-11-27"
        val stateFridayCompleted = AttendanceUiState(
            attendanceStatus = AttendanceStatus.HADIR,
            attendanceData = AttendanceHomeData(date = fridayDate, activeInternship = true, hasAttendance = true),
            reminderSettings = ReminderSettings(workStartHour = 8, startHour = 16)
        )

        // At 01:11 WIB on Saturday (2026-11-28) -> should automatically roll over to HOLIDAY
        val saturdayMidnight = ZonedDateTime.of(LocalDate.of(2026, 11, 28), LocalTime.of(1, 11), wib)
        assertEquals(DailyWorkPhase.HOLIDAY, stateFridayCompleted.getDailyWorkPhase(saturdayMidnight))
    }

    @Test
    fun testMidnightRolloverToWeekdayBeforeWork() {
        val wib = ZoneId.of("Asia/Jakarta")
        // Sunday data marked as off-day
        val sundayDate = "2026-11-29"
        val stateSundayOff = AttendanceUiState(
            attendanceStatus = AttendanceStatus.LIBUR,
            attendanceData = AttendanceHomeData(date = sundayDate, activeInternship = true, isScheduledOffDay = true),
            reminderSettings = ReminderSettings(workStartHour = 8, startHour = 16)
        )

        // At 01:11 WIB on Monday (2026-11-30) -> should automatically roll over to BEFORE_WORK
        val mondayMidnight = ZonedDateTime.of(LocalDate.of(2026, 11, 30), LocalTime.of(1, 11), wib)
        assertEquals(DailyWorkPhase.BEFORE_WORK, stateSundayOff.getDailyWorkPhase(mondayMidnight))
    }
}
