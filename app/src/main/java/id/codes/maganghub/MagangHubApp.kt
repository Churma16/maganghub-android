package id.codes.maganghub

import android.app.Application
import id.codes.maganghub.data.api.MagangHubApiClient
import id.codes.maganghub.data.repository.AttendanceRepository
import id.codes.maganghub.data.repository.AttendanceRepositoryImpl
import id.codes.maganghub.data.storage.EncryptedPreferences

/**
 * Main application class providing thread-safe dependencies to ViewModels,
 * Background Workers, and Broadcast Receivers.
 */
class MagangHubApp : Application() {

    lateinit var encryptedPreferences: EncryptedPreferences
        private set

    lateinit var apiClient: MagangHubApiClient
        private set

    lateinit var attendanceRepository: AttendanceRepository
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        encryptedPreferences = EncryptedPreferences(applicationContext)
        apiClient = MagangHubApiClient()
        attendanceRepository = AttendanceRepositoryImpl(
            apiClient = apiClient,
            storage = encryptedPreferences
        )
    }

    companion object {
        lateinit var instance: MagangHubApp
            private set
    }
}
