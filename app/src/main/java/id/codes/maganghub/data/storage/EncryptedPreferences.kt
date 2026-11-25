package id.codes.maganghub.data.storage

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.gson.Gson
import id.codes.maganghub.data.model.AttendanceHomeData
import id.codes.maganghub.data.model.AttendanceStatus
import id.codes.maganghub.data.model.ReminderSettings
import id.codes.maganghub.data.model.ThemeMode
import id.codes.maganghub.data.model.UserSession

/**
 * Manages encrypted and secure key-value persistence for credentials,
 * Monev access tokens, attendance cache, and scheduler configurations.
 */
class EncryptedPreferences(context: Context) {

    companion object {
        private const val TAG = "EncryptedPreferences"
        private const val PREFS_FILE = "maganghub_secure_prefs"
        private const val FALLBACK_PREFS_FILE = "maganghub_fallback_prefs"

        private const val KEY_USERNAME = "pref_username"
        private const val KEY_PASSWORD = "pref_password"
        private const val KEY_ACCESS_TOKEN = "pref_access_token"
        private const val KEY_LAST_STATUS = "pref_last_status"
        private const val KEY_LAST_HOME_DATA = "pref_last_home_data"
        private const val KEY_LAST_CHECK_TIME = "pref_last_check_timestamp"

        private const val KEY_REMINDER_ENABLED = "pref_reminder_enabled"
        private const val KEY_REMINDER_MORNING = "pref_reminder_morning"
        private const val KEY_REMINDER_SKIP_HOLIDAYS = "pref_reminder_skip_holidays"
        private const val KEY_REMINDER_START_HOUR = "pref_reminder_start_hour"
        private const val KEY_REMINDER_END_HOUR = "pref_reminder_end_hour"
        private const val KEY_REMINDER_INTERVAL = "pref_reminder_interval"
        private const val KEY_REMINDER_SOUND = "pref_reminder_sound"
        private const val KEY_REMINDER_VIBRATION = "pref_reminder_vibration"

        private const val KEY_LOGIN_FAILURE_COUNT = "pref_login_failure_count"
        private const val KEY_DEVICE_JITTER_SEED = "pref_device_jitter_seed_minutes"
        private const val KEY_FULL_NAME = "pref_user_full_name"
        private const val KEY_PHOTO_URL = "pref_user_photo_url"
        private const val KEY_THEME_MODE = "pref_theme_mode"

        const val KEY_MENTOR_REMINDER_ENABLED = "pref_mentor_reminder_enabled"
        const val KEY_MENTOR_THRESHOLD = "pref_mentor_review_threshold_days"
        const val KEY_MENTOR_INTERVAL = "pref_mentor_repeat_interval_minutes"
        const val KEY_MENTOR_SNOOZED_UNTIL = "pref_mentor_snoozed_until"
        const val KEY_MENTOR_PENDING_COUNT = "pref_mentor_pending_count"
    }

    private val gson = Gson()

    private val prefs: SharedPreferences = try {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            context,
            PREFS_FILE,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (e: Exception) {
        Log.w(TAG, "Failed to initialize EncryptedSharedPreferences, falling back to private prefs", e)
        context.getSharedPreferences(FALLBACK_PREFS_FILE, Context.MODE_PRIVATE)
    }

    // Credentials & Session
    fun saveCredentials(username: String, password: String) {
        prefs.edit()
            .putString(KEY_USERNAME, username.trim())
            .putString(KEY_PASSWORD, password.trim())
            .apply()
    }

    fun getUsername(): String? = prefs.getString(KEY_USERNAME, null)?.takeIf { it.isNotBlank() }

    fun getPassword(): String? = prefs.getString(KEY_PASSWORD, null)?.takeIf { it.isNotBlank() }

    fun hasCredentials(): Boolean = !getUsername().isNullOrBlank() && !getPassword().isNullOrBlank()

    fun saveAccessToken(token: String) {
        prefs.edit()
            .putString(KEY_ACCESS_TOKEN, token.trim())
            .apply()
    }

    fun getAccessToken(): String? = prefs.getString(KEY_ACCESS_TOKEN, null)?.takeIf { it.isNotBlank() }

    // Auth Circuit Breaker & Rate Limit Protection
    fun getLoginFailureCount(): Int = prefs.getInt(KEY_LOGIN_FAILURE_COUNT, 0)

    fun incrementLoginFailureCount(): Int {
        val next = getLoginFailureCount() + 1
        prefs.edit().putInt(KEY_LOGIN_FAILURE_COUNT, next).apply()
        return next
    }

    fun resetLoginFailureCount() {
        prefs.edit().putInt(KEY_LOGIN_FAILURE_COUNT, 0).apply()
    }

    fun isLoginCircuitOpen(): Boolean = getLoginFailureCount() >= 3

    // Device Jitter Seed (anti-thundering herd on daily 16:00 window)
    fun getDeviceJitterSeedMinutes(): Int {
        val existing = prefs.getInt(KEY_DEVICE_JITTER_SEED, -1)
        if (existing in 0..7) return existing
        val newSeed = kotlin.random.Random.nextInt(0, 8)
        prefs.edit().putInt(KEY_DEVICE_JITTER_SEED, newSeed).apply()
        return newSeed
    }

    // User Profile Cache (Name & Avatar Photo)
    fun saveUserProfile(fullName: String, photoUrl: String?) {
        val editor = prefs.edit().putString(KEY_FULL_NAME, fullName.trim())
        if (!photoUrl.isNullOrBlank()) {
            editor.putString(KEY_PHOTO_URL, photoUrl.trim())
        } else {
            editor.remove(KEY_PHOTO_URL)
        }
        editor.apply()
    }

    fun getFullName(): String? = prefs.getString(KEY_FULL_NAME, null)?.takeIf { it.isNotBlank() }

    fun getPhotoUrl(): String? = prefs.getString(KEY_PHOTO_URL, null)?.takeIf { it.isNotBlank() }

    fun getUserSession(): UserSession {
        val username = getUsername().orEmpty()
        val token = getAccessToken().orEmpty()
        val isLoggedIn = token.isNotEmpty() || hasCredentials()
        val lastCheck = getLastCheckTimestamp()
        val fullName = getFullName().orEmpty()
        val photoUrl = getPhotoUrl()
        return UserSession(
            username = username,
            monevAccessToken = token,
            isLoggedIn = isLoggedIn,
            lastLoginTimestamp = lastCheck,
            fullName = fullName,
            photoUrl = photoUrl
        )
    }

    // Attendance Cache
    fun saveAttendanceCache(data: AttendanceHomeData?, status: AttendanceStatus, timestamp: Long) {
        val dataJson = if (data != null) gson.toJson(data) else null
        prefs.edit()
            .putString(KEY_LAST_STATUS, status.name)
            .putString(KEY_LAST_HOME_DATA, dataJson)
            .putLong(KEY_LAST_CHECK_TIME, timestamp)
            .apply()
    }

    fun getLastAttendanceStatus(): AttendanceStatus {
        val statusName = prefs.getString(KEY_LAST_STATUS, null) ?: return AttendanceStatus.UNKNOWN
        return try {
            AttendanceStatus.valueOf(statusName)
        } catch (e: Exception) {
            AttendanceStatus.UNKNOWN
        }
    }

    fun getLastHomeData(): AttendanceHomeData? {
        val json = prefs.getString(KEY_LAST_HOME_DATA, null) ?: return null
        return try {
            gson.fromJson(json, AttendanceHomeData::class.java)
        } catch (e: Exception) {
            null
        }
    }

    fun getLastCheckTimestamp(): Long = prefs.getLong(KEY_LAST_CHECK_TIME, 0L)

    // Reminder Settings
    fun saveReminderSettings(settings: ReminderSettings) {
        prefs.edit()
            .putBoolean(KEY_REMINDER_ENABLED, settings.isEnabled)
            .putBoolean(KEY_REMINDER_MORNING, settings.morningReminderEnabled)
            .putBoolean(KEY_REMINDER_SKIP_HOLIDAYS, settings.skipHolidays)
            .putInt(KEY_REMINDER_START_HOUR, settings.startHour)
            .putInt(KEY_REMINDER_END_HOUR, settings.endHour)
            .putInt(KEY_REMINDER_INTERVAL, settings.intervalMinutes)
            .putBoolean(KEY_REMINDER_SOUND, settings.soundEnabled)
            .putBoolean(KEY_REMINDER_VIBRATION, settings.vibrationEnabled)
            .putBoolean(KEY_MENTOR_REMINDER_ENABLED, settings.mentorReminderEnabled)
            .putInt(KEY_MENTOR_THRESHOLD, settings.mentorReviewThresholdDays)
            .putInt(KEY_MENTOR_INTERVAL, settings.mentorRepeatIntervalMinutes)
            .putLong(KEY_MENTOR_SNOOZED_UNTIL, settings.mentorSnoozedUntil)
            .apply()
    }

    fun getReminderSettings(): ReminderSettings {
        return ReminderSettings(
            isEnabled = prefs.getBoolean(KEY_REMINDER_ENABLED, true),
            morningReminderEnabled = prefs.getBoolean(KEY_REMINDER_MORNING, true),
            skipHolidays = prefs.getBoolean(KEY_REMINDER_SKIP_HOLIDAYS, true),
            startHour = prefs.getInt(KEY_REMINDER_START_HOUR, 16),
            endHour = prefs.getInt(KEY_REMINDER_END_HOUR, 24),
            intervalMinutes = prefs.getInt(KEY_REMINDER_INTERVAL, 30),
            soundEnabled = prefs.getBoolean(KEY_REMINDER_SOUND, true),
            vibrationEnabled = prefs.getBoolean(KEY_REMINDER_VIBRATION, true),
            mentorReminderEnabled = prefs.getBoolean(KEY_MENTOR_REMINDER_ENABLED, true),
            mentorReviewThresholdDays = prefs.getInt(KEY_MENTOR_THRESHOLD, 5).coerceIn(1, 5),
            mentorRepeatIntervalMinutes = prefs.getInt(KEY_MENTOR_INTERVAL, 180),
            mentorSnoozedUntil = prefs.getLong(KEY_MENTOR_SNOOZED_UNTIL, 0L)
        )
    }

    // Mentor Review Reminder Cache
    fun savePendingMentorReviewCount(count: Int) {
        prefs.edit().putInt(KEY_MENTOR_PENDING_COUNT, count).apply()
    }

    fun getPendingMentorReviewCount(): Int = prefs.getInt(KEY_MENTOR_PENDING_COUNT, 0)

    fun saveMentorSnoozedUntil(snoozedUntil: Long) {
        prefs.edit().putLong(KEY_MENTOR_SNOOZED_UNTIL, snoozedUntil).apply()
    }

    fun setMentorSnoozedUntil(timestampMillis: Long) = saveMentorSnoozedUntil(timestampMillis)

    fun getMentorSnoozedUntil(): Long = prefs.getLong(KEY_MENTOR_SNOOZED_UNTIL, 0L)

    // Theme display mode
    fun getThemeMode(): ThemeMode {
        val raw = prefs.getString(KEY_THEME_MODE, ThemeMode.SYSTEM.value)
        return ThemeMode.fromValue(raw)
    }

    fun saveThemeMode(mode: ThemeMode) {
        prefs.edit().putString(KEY_THEME_MODE, mode.value).apply()
    }

    // Reset / Logout
    fun clearCredentials() {
        prefs.edit()
            .remove(KEY_USERNAME)
            .remove(KEY_PASSWORD)
            .remove(KEY_ACCESS_TOKEN)
            .remove(KEY_FULL_NAME)
            .remove(KEY_PHOTO_URL)
            .apply()
    }

    fun clearAll() {
        val currentTheme = getThemeMode()
        prefs.edit().clear().putString(KEY_THEME_MODE, currentTheme.value).apply()
    }
}
