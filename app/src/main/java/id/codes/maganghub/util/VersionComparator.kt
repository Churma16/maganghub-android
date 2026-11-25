package id.codes.maganghub.util

/**
 * Compares semver-like version strings to determine if a remote release is newer.
 *
 * Supported formats:
 *   - "1.0.0", "v1.0.0", "V0.1.0"
 *   - "0.1.0-snapshot.1c44245" (pre-release suffix after first '-')
 *   - "v1.1.0-snapshot.7f3f79f"
 */
object VersionComparator {

    /**
     * Returns true if [remoteVersion] represents a release that should be offered as an update
     * over [currentVersion].
     *
     * Rules:
     *  1. Strip leading 'v'/'V' from both.
     *  2. Split on '-' to separate the core semver from the pre-release label.
     *  3. Compare major.minor.patch numerically.
     *  4. If core versions are equal:
     *     - current is pre-release AND remote is stable  → true (upgrade to stable)
     *     - current is pre-release AND remote is pre-release with the same label → false
     *     - both stable and equal → false
     *  5. If remote core > current core → true.
     *  6. If remote core < current core → false.
     */
    fun isUpdateAvailable(currentVersion: String, remoteVersion: String): Boolean {
        val (currentCore, currentPreRelease) = parse(currentVersion)
        val (remoteCore, remotePreRelease) = parse(remoteVersion)

        val cmp = compareCores(currentCore, remoteCore)
        return when {
            cmp < 0 -> true   // remote is strictly newer
            cmp > 0 -> false  // current is somehow newer than remote
            else -> {
                // Core versions are equal; decide based on pre-release
                val currentIsPreRelease = currentPreRelease != null
                val remoteIsPreRelease = remotePreRelease != null
                when {
                    // current snapshot → remote stable: update available
                    currentIsPreRelease && !remoteIsPreRelease -> true
                    // both pre-release with same suffix: identical snapshot build
                    currentIsPreRelease && remoteIsPreRelease -> currentPreRelease != remotePreRelease
                    // both stable and equal
                    else -> false
                }
            }
        }
    }

    // Returns (core: List<Int>, preRelease: String?) after stripping 'v'/'V'
    private fun parse(version: String): Pair<List<Int>, String?> {
        val stripped = version.trimStart('v', 'V')
        val dashIdx = stripped.indexOf('-')
        val (coreStr, preRelease) = if (dashIdx == -1) {
            stripped to null
        } else {
            stripped.substring(0, dashIdx) to stripped.substring(dashIdx + 1)
        }
        val parts = coreStr.split('.').map { it.toIntOrNull() ?: 0 }
        // Normalise to exactly 3 components
        val core = when {
            parts.size >= 3 -> parts.take(3)
            else -> parts + List(3 - parts.size) { 0 }
        }
        return core to preRelease
    }

    /** Negative if a < b, 0 if equal, positive if a > b. */
    private fun compareCores(a: List<Int>, b: List<Int>): Int {
        for (i in 0..2) {
            val diff = a[i] - b[i]
            if (diff != 0) return diff
        }
        return 0
    }
}
