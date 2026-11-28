package id.churma.maganghubcompanion.data.model

/**
 * Information regarding an available app update release.
 */
data class UpdateInfo(
    val versionName: String,
    val changelog: String,
    val downloadUrl: String
)

/**
 * UI and domain states for in-app update checks matching the 4 mockup conditions:
 * 1. Awal (Idle)
 * 2. Memeriksa (Checking)
 * 3. Sudah terbaru (UpToDate)
 * 4. Pembaruan tersedia (Available)
 * Plus Error state for failure resilience.
 */
sealed class UpdateCheckState {
    object Idle : UpdateCheckState()
    object Checking : UpdateCheckState()
    object UpToDate : UpdateCheckState()
    data class Available(val updateInfo: UpdateInfo) : UpdateCheckState()
    data class Error(val message: String) : UpdateCheckState()
}
