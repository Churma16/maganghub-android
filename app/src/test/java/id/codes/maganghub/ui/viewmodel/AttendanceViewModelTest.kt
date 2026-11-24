package id.codes.maganghub.ui.viewmodel

import id.codes.maganghub.data.model.AttendanceCheckResult
import id.codes.maganghub.data.model.AttendanceHomeData
import id.codes.maganghub.data.model.AttendanceStatus
import id.codes.maganghub.data.model.ReminderSettings
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

        private var cachedHomeData: AttendanceHomeData? = null
        private var cachedStatus: AttendanceStatus = AttendanceStatus.UNKNOWN
        private var cachedTimestamp: Long = 0L
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
            return Result.success(AttendanceCheckResult(status = status, data = data, checkedAt = cachedTimestamp))
        }

        override suspend fun logout() {
            _sessionFlow.value = UserSession()
            hasCredentialsSaved = false
            cachedHomeData = null
            cachedStatus = AttendanceStatus.UNKNOWN
            cachedTimestamp = 0L
        }

        override suspend fun updateReminderSettings(settings: ReminderSettings) {
            _reminderSettingsFlow.value = settings
        }

        override fun getCachedHomeData(): AttendanceHomeData? = cachedHomeData
        override fun getCachedStatus(): AttendanceStatus = cachedStatus
        override fun getLastCheckTimestamp(): Long = cachedTimestamp
        override fun hasSavedCredentials(): Boolean = hasCredentialsSaved
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
}
