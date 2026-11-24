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
        private const val KEY_REMINDER_START_HOUR = "pref_reminder_start_hour"
        private const val KEY_REMINDER_END_HOUR = "pref_reminder_end_hour"
        private const val KEY_REMINDER_INTERVAL = "pref_reminder_interval"
        private const val KEY_REMINDER_SOUND = "pref_reminder_sound"
        private const val KEY_REMINDER_VIBRATION = "pref_reminder_vibration"

        private const val KEY_LOGIN_FAILURE_COUNT = "pref_login_failure_count"
        private const val KEY_DEVICE_JITTER_SEED = "pref_device_jitter_seed_minutes"
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

    fun getUserSession(): UserSession {
        val username = getUsername().orEmpty()
        val token = getAccessToken().orEmpty()
        val isLoggedIn = token.isNotEmpty() || hasCredentials()
        val lastCheck = getLastCheckTimestamp()
        return UserSession(
            username = username,
            monevAccessToken = token,
            isLoggedIn = isLoggedIn,
            lastLoginTimestamp = lastCheck
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
            .putInt(KEY_REMINDER_START_HOUR, settings.startHour)
            .putInt(KEY_REMINDER_END_HOUR, settings.endHour)
            .putInt(KEY_REMINDER_INTERVAL, settings.intervalMinutes)
            .putBoolean(KEY_REMINDER_SOUND, settings.soundEnabled)
            .putBoolean(KEY_REMINDER_VIBRATION, settings.vibrationEnabled)
            .apply()
    }

    fun getReminderSettings(): ReminderSettings {
        return ReminderSettings(
            isEnabled = prefs.getBoolean(KEY_REMINDER_ENABLED, true),
            startHour = prefs.getInt(KEY_REMINDER_START_HOUR, 16),
            endHour = prefs.getInt(KEY_REMINDER_END_HOUR, 24),
            intervalMinutes = prefs.getInt(KEY_REMINDER_INTERVAL, 30),
            soundEnabled = prefs.getBoolean(KEY_REMINDER_SOUND, true),
            vibrationEnabled = prefs.getBoolean(KEY_REMINDER_VIBRATION, true)
        )
    }

    // Reset / Logout
    fun clearCredentials() {
        prefs.edit()
            .remove(KEY_USERNAME)
            .remove(KEY_PASSWORD)
            .remove(KEY_ACCESS_TOKEN)
            .apply()
    }

    fun clearAll() {
        prefs.edit().clear().apply()
    }
}
