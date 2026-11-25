package id.codes.maganghub.widget

import androidx.compose.ui.graphics.Color
import id.codes.maganghub.R
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
}
