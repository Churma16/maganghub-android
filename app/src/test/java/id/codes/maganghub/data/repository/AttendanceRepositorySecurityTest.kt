package id.codes.maganghub.data.repository

import org.junit.Assert.assertEquals
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
}
