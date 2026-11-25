package id.codes.maganghub.ui.viewmodel

import id.codes.maganghub.data.model.AttendanceCheckResult
import id.codes.maganghub.data.model.AttendanceHomeData
import id.codes.maganghub.data.model.AttendanceStatus
import id.codes.maganghub.data.model.ReminderSettings
import id.codes.maganghub.data.model.ThemeMode
import id.codes.maganghub.data.model.UserProfileData
import id.codes.maganghub.data.model.UserSession
import id.codes.maganghub.data.repository.AttendanceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AttendanceViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private class FakeAttendanceRepository : AttendanceRepository {
        private val _sessionFlow = MutableStateFlow(UserSession())
        override val sessionFlow: StateFlow<UserSession> = _sessionFlow

        private val _reminderSettingsFlow = MutableStateFlow(ReminderSettings())
        override val reminderSettingsFlow: StateFlow<ReminderSettings> = _reminderSettingsFlow

        private val _themeModeFlow = MutableStateFlow(ThemeMode.SYSTEM)
        override val themeModeFlow: StateFlow<ThemeMode> = _themeModeFlow

        private var cachedHomeData: AttendanceHomeData? = null
        private var cachedStatus: AttendanceStatus = AttendanceStatus.UNKNOWN
        private var cachedTimestamp: Long = 0L
        private var cachedPendingMentorCount: Int = 0
        private var hasCredentialsSaved: Boolean = false

        override suspend fun login(username: String, password: String): Result<String> {
            _sessionFlow.value = UserSession(username = username, monevAccessToken = "token_abc", isLoggedIn = true)
            hasCredentialsSaved = true
            return Result.success("token_abc")
        }

        override suspend fun checkAttendance(forceRefresh: Boolean): Result<AttendanceCheckResult> {
            val data = AttendanceHomeData(activeInternship = true, hasAttendance = true, date = "2026-11-24")
            val status = AttendanceStatus.HADIR
            cachedHomeData = data
            cachedStatus = status
            cachedTimestamp = 1732454400000L
            return Result.success(
                AttendanceCheckResult(
                    status = status,
                    data = data,
                    checkedAt = cachedTimestamp,
                    pendingMentorReviewCount = cachedPendingMentorCount
                )
            )
        }

        override suspend fun logout() {
            _sessionFlow.value = UserSession()
            hasCredentialsSaved = false
            cachedHomeData = null
            cachedStatus = AttendanceStatus.UNKNOWN
            cachedTimestamp = 0L
            cachedPendingMentorCount = 0
        }

        override suspend fun updateReminderSettings(settings: ReminderSettings) {
            _reminderSettingsFlow.value = settings
        }

        override suspend fun setThemeMode(themeMode: ThemeMode) {
            _themeModeFlow.value = themeMode
        }

        override suspend fun refreshUserProfile(): Result<UserProfileData> {
            val profile = UserProfileData(name = "Fathan", photoUrl = "https://example.com/photo.jpg")
            _sessionFlow.value = _sessionFlow.value.copy(fullName = profile.name, photoUrl = profile.photoUrl)
            return Result.success(profile)
        }

        override fun getCachedHomeData(): AttendanceHomeData? = cachedHomeData
        override fun getCachedStatus(): AttendanceStatus = cachedStatus
        override fun getLastCheckTimestamp(): Long = cachedTimestamp
        override fun hasSavedCredentials(): Boolean = hasCredentialsSaved

        override suspend fun snoozeMentorReminderOneDay() {
            val snoozeUntil = System.currentTimeMillis() + 24 * 60 * 60 * 1000L
            _reminderSettingsFlow.value = _reminderSettingsFlow.value.copy(mentorSnoozedUntil = snoozeUntil)
        }

        override fun getCachedPendingMentorReviewCount(): Int = cachedPendingMentorCount

        fun setCachedPendingMentorReviewCount(count: Int) {
            cachedPendingMentorCount = count
        }
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testInitialUiState() = runTest {
        val repo = FakeAttendanceRepository()
        val viewModel = AttendanceViewModel(repo)

        assertFalse(viewModel.uiState.value.isLoggedIn)
        assertEquals(AttendanceStatus.UNKNOWN, viewModel.uiState.value.attendanceStatus)
    }

    @Test
    fun testLoginAndAttendanceCheck() = runTest {
        val repo = FakeAttendanceRepository()
        val viewModel = AttendanceViewModel(repo)

        viewModel.login("test_intern", "password123")
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isLoggedIn)
        assertEquals("test_intern", viewModel.uiState.value.username)
        assertEquals(AttendanceStatus.HADIR, viewModel.uiState.value.attendanceStatus)
        assertTrue(viewModel.uiState.value.isAttended)
    }

    @Test
    fun testLogoutClearsState() = runTest {
        val repo = FakeAttendanceRepository()
        val viewModel = AttendanceViewModel(repo)

        viewModel.login("test_user", "pass")
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.uiState.value.isLoggedIn)

        viewModel.logout()
        testDispatcher.scheduler.advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isLoggedIn)
        assertEquals(AttendanceStatus.UNKNOWN, viewModel.uiState.value.attendanceStatus)
    }

    @Test
    fun testUpdateReminderSettings() = runTest {
        val repo = FakeAttendanceRepository()
        val viewModel = AttendanceViewModel(repo)

        val updated = ReminderSettings(startHour = 17, intervalMinutes = 15, isEnabled = true)
        viewModel.updateReminderSettings(updated)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(17, viewModel.uiState.value.reminderSettings.startHour)
        assertEquals(15, viewModel.uiState.value.reminderSettings.intervalMinutes)
        assertNotNull(viewModel.uiState.value.nextReminderFormatted)
    }

    @Test
    fun testUpdateReminderSettingsZeroIntervalFallback() = runTest {
        val repo = FakeAttendanceRepository()
        val viewModel = AttendanceViewModel(repo)

        val zeroInterval = ReminderSettings(startHour = 0, endHour = 24, intervalMinutes = 0, isEnabled = true)
        viewModel.updateReminderSettings(zeroInterval)
        testDispatcher.scheduler.advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.nextReminderFormatted)
    }

    @Test
    fun testUpdateReminderSettingsMidnightRollover() = runTest {
        val repo = FakeAttendanceRepository()
        val viewModel = AttendanceViewModel(repo)

        val settings = ReminderSettings(startHour = 16, endHour = 24, intervalMinutes = 30, isEnabled = true)
        viewModel.updateReminderSettings(settings)
        testDispatcher.scheduler.advanceUntilIdle()

        // Simulate 23:45 WIB
        val nearMidnight = java.time.LocalDate.of(2026, 11, 25)
            .atTime(23, 45)
            .atZone(java.time.ZoneId.of("Asia/Jakarta"))
        viewModel.updateNextReminderString(nearMidnight)

        assertEquals("Batas akhir presensi 23:59 WIB", viewModel.uiState.value.nextReminderFormatted)
    }

    @Test
    fun testFormatLastSync() {
        val repo = FakeAttendanceRepository()
        val viewModel = AttendanceViewModel(repo)

        val formattedZero = viewModel.formatLastSync(0L)
        assertEquals("Belum pernah disinkronkan", formattedZero)

        val formattedEpoch = viewModel.formatLastSync(1732454400000L)
        assertTrue(formattedEpoch.contains("WIB"))
    }

    @Test
    fun testUserProfileEmissionUpdatesUiState() = runTest {
        val repo = FakeAttendanceRepository()
        val viewModel = AttendanceViewModel(repo)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("", viewModel.uiState.value.fullName)
        assertEquals(null, viewModel.uiState.value.photoUrl)

        viewModel.refreshUserProfile()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("Fathan", viewModel.uiState.value.fullName)
        assertEquals("https://example.com/photo.jpg", viewModel.uiState.value.photoUrl)
    }

    @Test
    fun testThemeModeSelectionUpdatesUiState() = runTest {
        val repo = FakeAttendanceRepository()
        val viewModel = AttendanceViewModel(repo)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(ThemeMode.SYSTEM, viewModel.uiState.value.themeMode)

        viewModel.setThemeMode(ThemeMode.DARK)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(ThemeMode.DARK, viewModel.uiState.value.themeMode)

        viewModel.setThemeMode(ThemeMode.LIGHT)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(ThemeMode.LIGHT, viewModel.uiState.value.themeMode)
    }

    @Test
    fun testThemeModeFromValueParsingAndFallbacks() {
        assertEquals(ThemeMode.SYSTEM, ThemeMode.fromValue("system"))
        assertEquals(ThemeMode.LIGHT, ThemeMode.fromValue("light"))
        assertEquals(ThemeMode.DARK, ThemeMode.fromValue("dark"))
        assertEquals(ThemeMode.LIGHT, ThemeMode.fromValue("LIGHT"))
        assertEquals(ThemeMode.DARK, ThemeMode.fromValue("DaRk"))
        assertEquals(ThemeMode.SYSTEM, ThemeMode.fromValue("unknown_mode"))
        assertEquals(ThemeMode.SYSTEM, ThemeMode.fromValue(null))
        assertEquals(ThemeMode.SYSTEM, ThemeMode.fromValue(""))
    }

    @Test
    fun testPendingMentorReviewCountUpdatedOnCheck() = runTest {
        val repo = FakeAttendanceRepository()
        repo.setCachedPendingMentorReviewCount(6)
        val viewModel = AttendanceViewModel(repo)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.checkAttendanceNow(silent = true)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(6, viewModel.uiState.value.pendingMentorReviewCount)
    }

    @Test
    fun testSnoozeMentorReminderOneDay() = runTest {
        val repo = FakeAttendanceRepository()
        val viewModel = AttendanceViewModel(repo)
        testDispatcher.scheduler.advanceUntilIdle()

        val beforeSnooze = System.currentTimeMillis()
        viewModel.snoozeMentorReminderOneDay()
        testDispatcher.scheduler.advanceUntilIdle()

        val snoozedUntil = viewModel.uiState.value.reminderSettings.mentorSnoozedUntil
        assertTrue(snoozedUntil >= beforeSnooze + 23 * 60 * 60 * 1000L)
        assertEquals("Pengingat mentor ditunda 1 hari", viewModel.uiState.value.successMessage)
    }

    @Test
    fun testUpdateMentorReminderSettings() = runTest {
        val repo = FakeAttendanceRepository()
        val viewModel = AttendanceViewModel(repo)
        testDispatcher.scheduler.advanceUntilIdle()

        val updated = ReminderSettings(
            mentorReminderEnabled = true,
            mentorReviewThresholdDays = 5,
            mentorRepeatIntervalMinutes = 60
        )
        viewModel.updateReminderSettings(updated)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(true, viewModel.uiState.value.reminderSettings.mentorReminderEnabled)
        assertEquals(5, viewModel.uiState.value.reminderSettings.mentorReviewThresholdDays)
        assertEquals(60, viewModel.uiState.value.reminderSettings.mentorRepeatIntervalMinutes)
    }
}
