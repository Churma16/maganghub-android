package id.codes.maganghub.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

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
}
