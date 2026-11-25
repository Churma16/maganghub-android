package id.codes.maganghub.data.repository

import id.codes.maganghub.data.model.AttendanceCheckResult
import id.codes.maganghub.data.model.AttendanceHomeData
import id.codes.maganghub.data.model.AttendanceStatus
import id.codes.maganghub.data.model.ReminderSettings
import id.codes.maganghub.data.model.ThemeMode
import id.codes.maganghub.data.model.UserProfileData
import id.codes.maganghub.data.model.UserSession
import kotlinx.coroutines.flow.StateFlow

/**
 * Contract for MagangHub attendance checks, authentication, and session handling.
 */
interface AttendanceRepository {

    /**
     * Observable stream of current user session.
     */
    val sessionFlow: StateFlow<UserSession>

    /**
     * Observable stream of reminder settings.
     */
    val reminderSettingsFlow: StateFlow<ReminderSettings>

    /**
     * Observable stream of theme display mode.
     */
    val themeModeFlow: StateFlow<ThemeMode>

    /**
     * Authenticates with Kemnaker credentials and stores monev access token.
     */
    suspend fun login(username: String, password: String): Result<String>

    /**
     * Performs an attendance check against MagangHub API.
     * Automatically attempts relogin with stored credentials if token is expired (401).
     */
    suspend fun checkAttendance(forceRefresh: Boolean = false): Result<AttendanceCheckResult>

    /**
     * Clears all session credentials, tokens, and cached state.
     */
    suspend fun logout()

    /**
     * Updates background reminder configurations.
     */
    suspend fun updateReminderSettings(settings: ReminderSettings)

    /**
     * Updates theme display mode preference.
     */
    suspend fun setThemeMode(themeMode: ThemeMode)

    /**
     * Retrieves cached home attendance data without network request.
     */
    fun getCachedHomeData(): AttendanceHomeData?

    /**
     * Fetches user profile from Monev API and updates cached session.
     */
    suspend fun refreshUserProfile(): Result<UserProfileData>

    /**
     * Retrieves cached attendance status.
     */
    fun getCachedStatus(): AttendanceStatus

    /**
     * Timestamp in millis of the last attendance check.
     */
    fun getLastCheckTimestamp(): Long

    /**
     * Whether valid username and password credentials are saved locally.
     */
    fun hasSavedCredentials(): Boolean

    /**
     * Postpones mentor review reminder notifications for 24 hours.
     */
    suspend fun snoozeMentorReminderOneDay()

    /**
     * Retrieves cached pending mentor review count without network call.
     */
    fun getCachedPendingMentorReviewCount(): Int
}
