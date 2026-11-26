package id.codes.maganghub.data.storage

import android.content.SharedPreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class EncryptedPreferencesSecurityTest {

    private class FakeEditor(private val data: MutableMap<String, Any?>) : SharedPreferences.Editor {
        private val temp = mutableMapOf<String, Any?>()
        private val toRemove = mutableSetOf<String>()
        private var clear = false

        override fun putString(key: String, value: String?): SharedPreferences.Editor {
            temp[key] = value
            return this
        }
        override fun putStringSet(key: String, values: Set<String>?): SharedPreferences.Editor = this
        override fun putInt(key: String, value: Int): SharedPreferences.Editor {
            temp[key] = value
            return this
        }
        override fun putLong(key: String, value: Long): SharedPreferences.Editor {
            temp[key] = value
            return this
        }
        override fun putFloat(key: String, value: Float): SharedPreferences.Editor = this
        override fun putBoolean(key: String, value: Boolean): SharedPreferences.Editor {
            temp[key] = value
            return this
        }
        override fun remove(key: String): SharedPreferences.Editor {
            toRemove.add(key)
            return this
        }
        override fun clear(): SharedPreferences.Editor {
            clear = true
            return this
        }
        override fun commit(): Boolean {
            apply()
            return true
        }
        override fun apply() {
            if (clear) data.clear()
            for (key in toRemove) data.remove(key)
            data.putAll(temp)
        }
    }

    private class FakeSharedPreferences : SharedPreferences {
        val data = mutableMapOf<String, Any?>()

        override fun getAll(): Map<String, *> = data
        override fun getString(key: String, defValue: String?): String? = data[key] as? String ?: defValue
        override fun getStringSet(key: String, defValues: Set<String>?): Set<String>? = null
        override fun getInt(key: String, defValue: Int): Int = (data[key] as? Int) ?: defValue
        override fun getLong(key: String, defValue: Long): Long = (data[key] as? Long) ?: defValue
        override fun getFloat(key: String, defValue: Float): Float = defValue
        override fun getBoolean(key: String, defValue: Boolean): Boolean = (data[key] as? Boolean) ?: defValue
        override fun contains(key: String): Boolean = data.containsKey(key)
        override fun edit(): SharedPreferences.Editor = FakeEditor(data)
        override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {}
        override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {}
    }

    @Test
    fun testFallbackModeRefusesToSaveCredentialsAndToken() {
        val fakePrefs = FakeSharedPreferences()
        val prefs = EncryptedPreferences(
            context = null,
            prefsOverride = fakePrefs,
            isFallbackOverride = true
        )

        assertTrue(prefs.isFallback)

        // Attempt to persist credentials & token
        prefs.saveCredentials("admin_user", "super_secret_password")
        prefs.saveAccessToken("monev_bearer_token_xyz")

        // Assert they were NOT persisted to unencrypted prefs
        assertNull(prefs.getUsername())
        assertNull(prefs.getPassword())
        assertNull(prefs.getAccessToken())
        assertFalse(prefs.hasCredentials())
        assertTrue(fakePrefs.data.isEmpty())
    }

    @Test
    fun testNormalModePersistsCredentialsAndToken() {
        val fakePrefs = FakeSharedPreferences()
        val prefs = EncryptedPreferences(
            context = null,
            prefsOverride = fakePrefs,
            isFallbackOverride = false
        )

        assertFalse(prefs.isFallback)

        prefs.saveCredentials("admin_user", "super_secret_password")
        prefs.saveAccessToken("monev_bearer_token_xyz")

        assertEquals("admin_user", prefs.getUsername())
        assertEquals("super_secret_password", prefs.getPassword())
        assertEquals("monev_bearer_token_xyz", prefs.getAccessToken())
        assertTrue(prefs.hasCredentials())
    }
}
