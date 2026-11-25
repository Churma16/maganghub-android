package id.codes.maganghub.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VersionComparatorTest {

    // ------------------------------------------------------------------
    // Basic newer / older / equal comparisons
    // ------------------------------------------------------------------

    @Test
    fun `older major returns true`() {
        assertTrue(VersionComparator.isUpdateAvailable("0.9.0", "1.0.0"))
    }

    @Test
    fun `older minor returns true`() {
        assertTrue(VersionComparator.isUpdateAvailable("1.0.0", "1.1.0"))
    }

    @Test
    fun `older patch returns true`() {
        assertTrue(VersionComparator.isUpdateAvailable("1.0.0", "1.0.1"))
    }

    @Test
    fun `equal stable versions returns false`() {
        assertFalse(VersionComparator.isUpdateAvailable("1.0.0", "1.0.0"))
    }

    @Test
    fun `current is newer than remote returns false`() {
        assertFalse(VersionComparator.isUpdateAvailable("1.1.0", "1.0.0"))
    }

    // ------------------------------------------------------------------
    // Leading 'v' / 'V' prefix handling
    // ------------------------------------------------------------------

    @Test
    fun `v-prefixed current and remote equal returns false`() {
        assertFalse(VersionComparator.isUpdateAvailable("v1.0.0", "v1.0.0"))
    }

    @Test
    fun `V-prefix stripped and compared correctly`() {
        assertTrue(VersionComparator.isUpdateAvailable("V1.0.0", "v1.1.0"))
    }

    @Test
    fun `mixed prefix still compares correctly`() {
        assertTrue(VersionComparator.isUpdateAvailable("1.0.0", "v1.1.0"))
    }

    @Test
    fun `v-prefix on current no prefix on remote`() {
        assertFalse(VersionComparator.isUpdateAvailable("v1.0.0", "1.0.0"))
    }

    // ------------------------------------------------------------------
    // Snapshot / pre-release suffix handling
    // ------------------------------------------------------------------

    @Test
    fun `snapshot current remote is stable same core returns true`() {
        // current is a snapshot build; stable remote at same version is an upgrade
        assertTrue(VersionComparator.isUpdateAvailable("0.1.0-snapshot.1c44245", "v0.1.0"))
    }

    @Test
    fun `snapshot current remote is newer stable returns true`() {
        assertTrue(VersionComparator.isUpdateAvailable("0.1.0-snapshot.1c44245", "v1.0.0"))
    }

    @Test
    fun `snapshot current remote is same snapshot returns false`() {
        // exactly the same snapshot hash → no update
        assertFalse(
            VersionComparator.isUpdateAvailable(
                "0.1.0-snapshot.1c44245",
                "v0.1.0-snapshot.1c44245"
            )
        )
    }

    @Test
    fun `snapshot current remote is different snapshot same core returns true`() {
        // different hash on the same core = a newer snapshot build
        assertTrue(
            VersionComparator.isUpdateAvailable(
                "0.1.0-snapshot.1c44245",
                "v0.1.0-snapshot.7f3f79f"
            )
        )
    }

    @Test
    fun `snapshot current remote snapshot newer core returns true`() {
        assertTrue(
            VersionComparator.isUpdateAvailable(
                "0.1.0-snapshot.1c44245",
                "v0.2.0-snapshot.7f3f79f"
            )
        )
    }

    @Test
    fun `stable current remote is snapshot same core returns false`() {
        // stable is considered at least equal to any snapshot of the same core
        assertFalse(VersionComparator.isUpdateAvailable("1.0.0", "v1.0.0-snapshot.abc"))
    }

    @Test
    fun `stable current remote is older snapshot returns false`() {
        assertFalse(VersionComparator.isUpdateAvailable("1.1.0", "v1.0.0-snapshot.abc"))
    }

    @Test
    fun `stable current remote is newer snapshot returns false`() {
        // Stable users must NEVER be offered unstable development snapshot builds
        assertFalse(VersionComparator.isUpdateAvailable("1.0.0", "v1.1.0-snapshot.abc"))
    }
}
