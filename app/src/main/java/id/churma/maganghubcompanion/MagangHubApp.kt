package id.churma.maganghubcompanion

import android.app.Application
import id.churma.maganghubcompanion.data.api.MagangHubApiClient
import id.churma.maganghubcompanion.data.repository.AttendanceRepository
import id.churma.maganghubcompanion.data.repository.AttendanceRepositoryImpl
import id.churma.maganghubcompanion.data.storage.EncryptedPreferences

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
