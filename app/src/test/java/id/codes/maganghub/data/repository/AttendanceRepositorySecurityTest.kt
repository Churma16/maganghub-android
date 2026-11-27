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
        // Realistic epoch millis (1.7 trillion ms) so now - ttl is strictly positive
        val now = 1_700_000_000_000L

        // Never checked before (0L) -> due immediately
        assertTrue(AttendanceRepositoryImpl.isMentorCheckDue(now, 0L))

        // Less than 3 hours (e.g. 1 hour, 2.5 hours, or 1 ms before ttl) -> not due
        assertFalse(AttendanceRepositoryImpl.isMentorCheckDue(now, now - (60 * 60 * 1000L)))
        assertFalse(AttendanceRepositoryImpl.isMentorCheckDue(now, now - (150 * 60 * 1000L)))
        assertFalse(AttendanceRepositoryImpl.isMentorCheckDue(now, now - (ttl - 1L)))

        // Exactly 3 hours -> due
        assertTrue(AttendanceRepositoryImpl.isMentorCheckDue(now, now - ttl))

        // More than 3 hours (e.g. 4 hours) -> due
        assertTrue(AttendanceRepositoryImpl.isMentorCheckDue(now, now - (4 * 60 * 60 * 1000L)))

        // Clock adjusted backwards (lastCheck is in the future relative to now) -> due
        assertTrue(AttendanceRepositoryImpl.isMentorCheckDue(now, now + 1000L))
    }

    @Test
    fun testResolveEffectiveEndDate() {
        // Today earlier than internship end date -> uses today
        assertEquals(
            "2026-11-27",
            AttendanceRepositoryImpl.resolveEffectiveEndDate(
                todayDate = "2026-11-27",
                internshipEndDate = "2027-02-09"
            )
        )

        // Today later than internship end date -> caps at internship end date
        assertEquals(
            "2027-02-09",
            AttendanceRepositoryImpl.resolveEffectiveEndDate(
                todayDate = "2027-03-01",
                internshipEndDate = "2027-02-09"
            )
        )

        // Today missing -> fallback to internship end date
        assertEquals(
            "2027-02-09",
            AttendanceRepositoryImpl.resolveEffectiveEndDate(
                todayDate = null,
                internshipEndDate = "2027-02-09"
            )
        )

        // Internship end date missing -> fallback to today
        assertEquals(
            "2026-11-27",
            AttendanceRepositoryImpl.resolveEffectiveEndDate(
                todayDate = "2026-11-27",
                internshipEndDate = null
            )
        )

        // Both missing -> null
        assertEquals(
            null,
            AttendanceRepositoryImpl.resolveEffectiveEndDate(
                todayDate = null,
                internshipEndDate = null
            )
        )
    }

    @Test
    fun testIsRecoverableWithFallback() {
        // Coroutine cancellation must never be swallowed or retried
        assertFalse(AttendanceRepositoryImpl.isRecoverableWithFallback(kotlinx.coroutines.CancellationException("Job cancelled")))

        // Expired auth cannot be recovered by changing query params
        assertFalse(AttendanceRepositoryImpl.isRecoverableWithFallback(id.codes.maganghub.data.api.UnauthorizedSessionException("Session expired")))

        // Network connection / timeout failures cannot be recovered by changing query params
        assertFalse(AttendanceRepositoryImpl.isRecoverableWithFallback(java.net.SocketTimeoutException("Read timed out")))
        assertFalse(AttendanceRepositoryImpl.isRecoverableWithFallback(java.net.UnknownHostException("Unable to resolve host")))
        assertFalse(AttendanceRepositoryImpl.isRecoverableWithFallback(java.net.ConnectException("Connection refused")))

        // Server query errors (e.g. 400 Bad Request, 422 Unprocessable, 500 internal error) ARE candidates for fallback
        assertTrue(AttendanceRepositoryImpl.isRecoverableWithFallback(java.io.IOException("HTTP 400 Bad Request")))
        assertTrue(AttendanceRepositoryImpl.isRecoverableWithFallback(java.io.IOException("HTTP 422 Unprocessable Entity")))
        assertTrue(AttendanceRepositoryImpl.isRecoverableWithFallback(java.io.IOException("HTTP 500 Internal Server Error")))
        assertTrue(AttendanceRepositoryImpl.isRecoverableWithFallback(com.google.gson.JsonParseException("Malformed JSON")))
    }
}
