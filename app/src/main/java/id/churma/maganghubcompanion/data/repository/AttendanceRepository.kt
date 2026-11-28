package id.churma.maganghubcompanion.data.repository

import id.churma.maganghubcompanion.data.model.AttendanceCheckResult
import id.churma.maganghubcompanion.data.model.AttendanceHomeData
import id.churma.maganghubcompanion.data.model.AttendanceStatus
import id.churma.maganghubcompanion.data.model.FeedbackRequest
import id.churma.maganghubcompanion.data.model.FeedbackResponse
import id.churma.maganghubcompanion.data.model.ReminderSettings
import id.churma.maganghubcompanion.data.model.ThemeMode
import id.churma.maganghubcompanion.data.model.UpdateCheckState
import id.churma.maganghubcompanion.data.model.UserProfileData
import id.churma.maganghubcompanion.data.model.UserSession
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
     * Submits user feedback (bug report, feature request, general feedback).
     */
    suspend fun submitFeedback(request: FeedbackRequest): Result<FeedbackResponse>

    /**
     * Postpones mentor review reminder notifications for 24 hours.
     */
    suspend fun snoozeMentorReminderOneDay()

    /**
     * Retrieves cached pending mentor review count without network call.
     */
    fun getCachedPendingMentorReviewCount(): Int

    /**
     * Returns diagnostic sync info from the last mentor attendance check.
     */
    fun getLastMentorSyncDebug(): String?

    /**
     * Checks GitHub releases for a newer app version.
     */
    suspend fun checkForUpdates(): Result<UpdateCheckState>

    /**
     * Returns timestamp of the last update check in millis (0 if never checked).
     */
    fun getLastUpdateCheckTimestamp(): Long
}
