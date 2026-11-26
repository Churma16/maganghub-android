package id.codes.maganghub.data.repository

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AttendanceRepositorySecurityTest {

    @Test
    fun testMaskUsername() {
        assertEquals("***", AttendanceRepositoryImpl.maskUsername(""))
        assertEquals("***", AttendanceRepositoryImpl.maskUsername("   "))
        assertEquals("***", AttendanceRepositoryImpl.maskUsername("a"))
        assertEquals("***", AttendanceRepositoryImpl.maskUsername("abc"))
        assertEquals("***", AttendanceRepositoryImpl.maskUsername("user"))
        assertEquals("***5678", AttendanceRepositoryImpl.maskUsername("12345678"))
        assertEquals("***0001", AttendanceRepositoryImpl.maskUsername("3171012345670001"))
        assertEquals("***.com", AttendanceRepositoryImpl.maskUsername("john.doe@example.com"))
    }

    @Test
    fun testIsMentorCheckDue_3HourTtl() {
        val ttl = AttendanceRepositoryImpl.MENTOR_CHECK_TTL_MILLIS
        val now = 10_000_000L

        // Never checked before (0L) -> due immediately
        assertTrue(AttendanceRepositoryImpl.isMentorCheckDue(now, 0L))

        // Less than 3 hours (e.g. 1 hour, 2.5 hours) -> not due
        assertFalse(AttendanceRepositoryImpl.isMentorCheckDue(now, now - (60 * 60 * 1000L)))
        assertFalse(AttendanceRepositoryImpl.isMentorCheckDue(now, now - (150 * 60 * 1000L)))

        // Exactly 3 hours -> due
        assertTrue(AttendanceRepositoryImpl.isMentorCheckDue(now, now - ttl))

        // More than 3 hours (e.g. 4 hours) -> due
        assertTrue(AttendanceRepositoryImpl.isMentorCheckDue(now, now - (4 * 60 * 60 * 1000L)))

        // Clock adjusted backwards -> due
        assertTrue(AttendanceRepositoryImpl.isMentorCheckDue(now, now + 1000L))
    }
}
