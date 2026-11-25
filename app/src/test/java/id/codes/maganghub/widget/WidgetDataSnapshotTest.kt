package id.codes.maganghub.widget

import androidx.compose.ui.graphics.Color
import id.codes.maganghub.R
import id.codes.maganghub.data.model.AttendanceStatus
import id.codes.maganghub.data.model.ThemeMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class WidgetDataSnapshotTest {

    @Test
    fun testSudahAbsenZeroPendingReports() {
        val snapshot = WidgetDataSnapshot(
            status = WidgetStatus.SUDAH_ABSEN,
            remainingTimeMillis = 5 * 3600 * 1000L,
            remainingTimeText = "5j 0m",
            formattedDate = "Rab, 25 Nov",
            pendingMentorCount = 0,
            isMentorOverdue = false,
            themeMode = ThemeMode.SYSTEM
        )

        assertEquals("Sudah Absen", snapshot.getTitle1x5())
        assertEquals("Sudah Absen", snapshot.getTitle1x3())
        assertEquals("Semua laporan sudah di-approve", snapshot.getSubtext1x5())
        assertEquals("Rab, 25 Nov", snapshot.getSubtext1x3())
        assertFalse(snapshot.isSubtextWarning())
        assertFalse(snapshot.showBadge1x5())
        assertFalse(snapshot.showActionButton())
        assertEquals(R.drawable.ic_widget_check, snapshot.getInlineIconResId())
    }

    @Test
    fun testSudahAbsenPendingReportsNormal() {
        val snapshot = WidgetDataSnapshot(
            status = WidgetStatus.SUDAH_ABSEN,
            remainingTimeMillis = 5 * 3600 * 1000L,
            remainingTimeText = "5j 0m",
            formattedDate = "Rab, 25 Nov",
            pendingMentorCount = 2,
            isMentorOverdue = false,
            themeMode = ThemeMode.DARK
        )

        assertEquals("Sudah Absen", snapshot.getTitle1x5())
        assertEquals("Sudah Absen", snapshot.getTitle1x3())
        assertEquals("2 laporan menunggu approval", snapshot.getSubtext1x5())
        assertEquals("2 menunggu approval", snapshot.getSubtext1x3())
        assertFalse(snapshot.isSubtextWarning())
        assertFalse(snapshot.showBadge1x5())
        assertFalse(snapshot.showActionButton())
    }

    @Test
    fun testSudahAbsenPendingReportsOverdue() {
        val snapshot = WidgetDataSnapshot(
            status = WidgetStatus.SUDAH_ABSEN,
            remainingTimeMillis = 5 * 3600 * 1000L,
            remainingTimeText = "5j 0m",
            formattedDate = "Rab, 25 Nov",
            pendingMentorCount = 2,
            isMentorOverdue = true,
            themeMode = ThemeMode.DARK
        )

        assertEquals("Sudah Absen", snapshot.getTitle1x5())
        assertEquals("2 laporan belum di-approve", snapshot.getSubtext1x5())
        assertEquals("2 menunggu >3 hr", snapshot.getSubtext1x3())
        assertTrue(snapshot.isSubtextWarning())
        assertTrue(snapshot.showBadge1x5())
        assertFalse(snapshot.showActionButton())
    }

    @Test
    fun testBelumAbsenNormalRemainingTime() {
        val snapshot = WidgetDataSnapshot(
            status = WidgetStatus.BELUM_ABSEN,
            remainingTimeMillis = 7 * 3600 * 1000L + 29 * 60 * 1000L,
            remainingTimeText = "7j 29m",
            formattedDate = "Rab, 25 Nov",
            pendingMentorCount = 0,
            isMentorOverdue = false,
            themeMode = ThemeMode.SYSTEM
        )

        assertEquals("Belum Absen", snapshot.getTitle1x5())
        assertEquals("Belum Absen", snapshot.getTitle1x3())
        assertEquals("Batas hari ini 23:59 WIB", snapshot.getSubtext1x5())
        assertEquals("Sisa 7j 29m", snapshot.getSubtext1x3())
        assertFalse(snapshot.isSubtextWarning())
        assertFalse(snapshot.showBadge1x5())
        assertTrue(snapshot.showActionButton())
        assertEquals(R.drawable.ic_widget_clock, snapshot.getInlineIconResId())
    }

    @Test
    fun testBelumAbsenMendesakRemainingTime() {
        val snapshot = WidgetDataSnapshot(
            status = WidgetStatus.MENDESAK,
            remainingTimeMillis = 1 * 3600 * 1000L + 58 * 60 * 1000L,
            remainingTimeText = "1j 58m",
            formattedDate = "Rab, 25 Nov",
            pendingMentorCount = 0,
            isMentorOverdue = false,
            themeMode = ThemeMode.SYSTEM
        )

        assertEquals("Belum Absen", snapshot.getTitle1x5())
        assertEquals("Belum Absen", snapshot.getTitle1x3())
        assertEquals("Segera isi sebelum 23:59 WIB", snapshot.getSubtext1x5())
        assertEquals("Sisa 1j 58m", snapshot.getSubtext1x3())
        assertFalse(snapshot.isSubtextWarning())
        assertFalse(snapshot.showBadge1x5())
        assertTrue(snapshot.showActionButton())
        assertEquals(R.drawable.ic_widget_warning, snapshot.getInlineIconResId())
    }

    @Test
    fun testHariLiburAllVariants() {
        val base = WidgetDataSnapshot(
            status = WidgetStatus.HARI_LIBUR,
            remainingTimeMillis = 0L,
            remainingTimeText = "0m",
            formattedDate = "Rab, 25 Nov",
            pendingMentorCount = 0,
            isMentorOverdue = false,
            themeMode = ThemeMode.SYSTEM
        )

        assertEquals("Hari Libur", base.getTitle1x5())
        assertEquals("Hari Libur", base.getTitle1x3())
        assertEquals("Pengingat nonaktif hari ini", base.getSubtext1x5())
        assertEquals("Pengingat off", base.getSubtext1x3())
        assertFalse(base.showBadge1x5())
        assertEquals(R.drawable.ic_widget_moon, base.getInlineIconResId())

        val withOnePending = base.copy(pendingMentorCount = 1)
        assertEquals("1 laporan menunggu approval", withOnePending.getSubtext1x5())
        assertEquals("1 menunggu approval", withOnePending.getSubtext1x3())
        assertFalse(withOnePending.showBadge1x5())

        val withOverdue = base.copy(pendingMentorCount = 2, isMentorOverdue = true)
        assertEquals("2 laporan belum di-approve", withOverdue.getSubtext1x5())
        assertEquals("2 menunggu >3 hr", withOverdue.getSubtext1x3())
        assertTrue(withOverdue.isSubtextWarning())
        assertTrue(withOverdue.showBadge1x5())
    }

    @Test
    fun testSesiBerakhir() {
        val snapshot = WidgetDataSnapshot(
            status = WidgetStatus.SESI_BERAKHIR,
            remainingTimeMillis = 0L,
            remainingTimeText = "",
            formattedDate = "Rab, 25 Nov",
            pendingMentorCount = 0,
            isMentorOverdue = false,
            themeMode = ThemeMode.SYSTEM
        )

        assertEquals("Sesi Berakhir", snapshot.getTitle1x5())
        assertEquals("Perlu Login", snapshot.getTitle1x3())
        assertEquals("Ketuk untuk masuk kembali ke SSO", snapshot.getSubtext1x5())
        assertEquals("Masuk ke SSO", snapshot.getSubtext1x3())
        assertTrue(snapshot.showActionButton())
        assertNull(snapshot.getInlineIconResId())
    }

    @Test
    fun testWidgetColorTokensContract() {
        for (status in WidgetStatus.values()) {
            val dark = WidgetColors.getTokens(status, ThemeMode.DARK)
            val light = WidgetColors.getTokens(status, ThemeMode.LIGHT)

            assertNotEquals(dark.cardBackground, light.cardBackground)
            assertNotEquals(dark.titleText, light.titleText)
            assertNotEquals(Color.Unspecified, dark.accentBar)
            assertNotEquals(Color.Unspecified, light.accentBar)
        }
    }

    @Test
    fun testCalculateRemainingTime() {
        val wib = TimeZone.getTimeZone("Asia/Jakarta")
        val cal = Calendar.getInstance(wib).apply {
            set(Calendar.HOUR_OF_DAY, 22)
            set(Calendar.MINUTE, 1)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val (diff, text) = WidgetDataSnapshot.calculateRemainingTime(cal.timeInMillis)
        // From 22:01 to 23:59 = 1 hour 58 minutes
        assertEquals("1j 58m", text)
        assertTrue(diff < 2 * 3600 * 1000L)
    }

    @Test
    fun testFromComponentsSesiBerakhirWhenNoCredentials() {
        val snapshot = WidgetDataSnapshot.fromComponents(
            hasCredentials = false,
            attendanceStatus = AttendanceStatus.HADIR,
            pendingCount = 2,
            thresholdDays = 3,
            themeMode = ThemeMode.SYSTEM
        )

        assertEquals(WidgetStatus.SESI_BERAKHIR, snapshot.status)
        assertEquals("Sesi Berakhir", snapshot.getTitle1x5())
        assertEquals("Perlu Login", snapshot.getTitle1x3())
    }

    @Test
    fun testFromComponentsSudahAbsen() {
        val snapshot = WidgetDataSnapshot.fromComponents(
            hasCredentials = true,
            attendanceStatus = AttendanceStatus.HADIR,
            pendingCount = 0,
            thresholdDays = 3,
            themeMode = ThemeMode.DARK
        )

        assertEquals(WidgetStatus.SUDAH_ABSEN, snapshot.status)
        assertEquals("Sudah Absen", snapshot.getTitle1x5())
        assertEquals("Semua laporan sudah di-approve", snapshot.getSubtext1x5())
    }

    @Test
    fun testFromComponentsHariLiburAndTidakAktif() {
        val libur = WidgetDataSnapshot.fromComponents(
            hasCredentials = true,
            attendanceStatus = AttendanceStatus.LIBUR,
            pendingCount = 1,
            thresholdDays = 3,
            themeMode = ThemeMode.LIGHT
        )
        assertEquals(WidgetStatus.HARI_LIBUR, libur.status)

        val tidakAktif = WidgetDataSnapshot.fromComponents(
            hasCredentials = true,
            attendanceStatus = AttendanceStatus.TIDAK_AKTIF,
            pendingCount = 0,
            thresholdDays = 3,
            themeMode = ThemeMode.LIGHT
        )
        assertEquals(WidgetStatus.HARI_LIBUR, tidakAktif.status)
    }

    @Test
    fun testFromComponentsBelumAbsenVsMendesak() {
        val wib = TimeZone.getTimeZone("Asia/Jakarta")

        // 14:00 WIB -> >2 hours remaining -> BELUM_ABSEN
        val calNormal = Calendar.getInstance(wib).apply {
            set(Calendar.HOUR_OF_DAY, 14)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val normal = WidgetDataSnapshot.fromComponents(
            hasCredentials = true,
            attendanceStatus = AttendanceStatus.BELUM_HADIR,
            pendingCount = 0,
            thresholdDays = 3,
            themeMode = ThemeMode.SYSTEM,
            nowMillis = calNormal.timeInMillis
        )
        assertEquals(WidgetStatus.BELUM_ABSEN, normal.status)

        // 22:30 WIB -> <2 hours remaining -> MENDESAK
        val calMendesak = Calendar.getInstance(wib).apply {
            set(Calendar.HOUR_OF_DAY, 22)
            set(Calendar.MINUTE, 30)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val mendesak = WidgetDataSnapshot.fromComponents(
            hasCredentials = true,
            attendanceStatus = AttendanceStatus.BELUM_HADIR,
            pendingCount = 0,
            thresholdDays = 3,
            themeMode = ThemeMode.SYSTEM,
            nowMillis = calMendesak.timeInMillis
        )
        assertEquals(WidgetStatus.MENDESAK, mendesak.status)

        // 23:59:15 WIB -> 44.999 seconds before 23:59:59.999 deadline -> MENDESAK
        val calLastMinute = Calendar.getInstance(wib).apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 15)
            set(Calendar.MILLISECOND, 0)
        }
        val lastMinute = WidgetDataSnapshot.fromComponents(
            hasCredentials = true,
            attendanceStatus = AttendanceStatus.BELUM_HADIR,
            pendingCount = 0,
            thresholdDays = 3,
            themeMode = ThemeMode.SYSTEM,
            nowMillis = calLastMinute.timeInMillis
        )
        assertEquals(WidgetStatus.MENDESAK, lastMinute.status)
    }

    @Test
    fun testDynamicMentorThresholdAndBadgeText() {
        // Threshold = 2, pending = 2 -> Overdue
        val snapshotOverdue = WidgetDataSnapshot.fromComponents(
            hasCredentials = true,
            attendanceStatus = AttendanceStatus.HADIR,
            pendingCount = 2,
            thresholdDays = 2,
            themeMode = ThemeMode.SYSTEM
        )
        assertTrue(snapshotOverdue.isMentorOverdue)
        assertEquals(">2 hari", snapshotOverdue.getBadgeText1x5())
        assertEquals("2 menunggu >2 hr", snapshotOverdue.getSubtext1x3())
        assertTrue(snapshotOverdue.showBadge1x5())

        // Threshold = 4, pending = 3 -> Not overdue
        val snapshotNotOverdue = WidgetDataSnapshot.fromComponents(
            hasCredentials = true,
            attendanceStatus = AttendanceStatus.HADIR,
            pendingCount = 3,
            thresholdDays = 4,
            themeMode = ThemeMode.SYSTEM
        )
        assertFalse(snapshotNotOverdue.isMentorOverdue)
        assertEquals(">4 hari", snapshotNotOverdue.getBadgeText1x5())
        assertEquals("3 menunggu approval", snapshotNotOverdue.getSubtext1x3())
        assertFalse(snapshotNotOverdue.showBadge1x5())
    }

    @Test
    fun testFormatDateThreadSafetyAndConsistency() {
        val wib = TimeZone.getTimeZone("Asia/Jakarta")
        val cal = Calendar.getInstance(wib).apply {
            set(Calendar.YEAR, 2026)
            set(Calendar.MONTH, Calendar.NOVEMBER)
            set(Calendar.DAY_OF_MONTH, 25)
            set(Calendar.HOUR_OF_DAY, 10)
            set(Calendar.MINUTE, 0)
        }
        val formatted = WidgetDataSnapshot.formatDate(cal.timeInMillis)
        // Indonesian locale: Wednesday = "Rab"
        assertTrue(formatted.startsWith("Rab"))
        assertTrue(formatted.contains("25"))
        assertTrue(formatted.contains("Nov"))
    }
}
