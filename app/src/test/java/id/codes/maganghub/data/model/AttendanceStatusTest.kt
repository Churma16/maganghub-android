package id.codes.maganghub.data.model

import id.codes.maganghub.ui.viewmodel.AttendanceUiState
import id.codes.maganghub.ui.viewmodel.DailyWorkPhase
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

        // 5. Reporting Window (17:30 WIB, after 16:00)
        val timeReporting = ZonedDateTime.of(today, LocalTime.of(17, 30), wib)
        assertEquals(DailyWorkPhase.REPORTING_WINDOW, statePending.getDailyWorkPhase(timeReporting))
    }
}
