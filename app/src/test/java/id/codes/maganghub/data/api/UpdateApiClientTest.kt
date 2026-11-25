package id.codes.maganghub.data.api

import com.google.gson.Gson
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class UpdateApiClientTest {

    private lateinit var server: MockWebServer
    private lateinit var client: UpdateApiClientImpl

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        client = UpdateApiClientImpl(
            releasesUrl = server.url("/repos/test/test/releases").toString(),
            client = OkHttpClient(),
            gson = Gson()
        )
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private fun enqueueReleasesResponse(body: String, code: Int = 200) {
        server.enqueue(MockResponse().setResponseCode(code).setBody(body))
    }

    // ------------------------------------------------------------------
    // Success paths
    // ------------------------------------------------------------------

    @Test
    fun `returns UpdateInfo from first non-draft release with apk asset`() = runTest {
        enqueueReleasesResponse(
            """
            [
              {
                "tag_name": "v1.1.0",
                "name": "Release 1.1.0",
                "body": "- Perbaikan bug notifikasi\n- Tambah fitur baru",
                "draft": false,
                "prerelease": false,
                "html_url": "https://github.com/example/releases/tag/v1.1.0",
                "published_at": "2026-11-01T00:00:00Z",
                "assets": [
                  {
                    "name": "maganghub-v1.1.0.apk",
                    "browser_download_url": "https://github.com/example/releases/download/v1.1.0/maganghub-v1.1.0.apk",
                    "size": 12345678
                  }
                ]
              }
            ]
            """.trimIndent()
        )

        val result = client.getLatestRelease()
        assertTrue(result.isSuccess)
        val info = result.getOrThrow()
        assertEquals("v1.1.0", info.versionName)
        assertEquals("https://github.com/example/releases/download/v1.1.0/maganghub-v1.1.0.apk", info.downloadUrl)
        assertEquals(12345678L, info.apkSize)
        assertEquals("2026-11-01T00:00:00Z", info.publishedAt)
        // First bullet should be extracted
        assertTrue(info.changelog.contains("Perbaikan bug notifikasi"))
    }

    @Test
    fun `falls back to html_url when no apk asset`() = runTest {
        enqueueReleasesResponse(
            """
            [
              {
                "tag_name": "v1.0.0",
                "name": "Release 1.0.0",
                "body": null,
                "draft": false,
                "prerelease": false,
                "html_url": "https://github.com/example/releases/tag/v1.0.0",
                "published_at": "2026-10-01T00:00:00Z",
                "assets": []
              }
            ]
            """.trimIndent()
        )

        val result = client.getLatestRelease()
        assertTrue(result.isSuccess)
        val info = result.getOrThrow()
        assertEquals("https://github.com/example/releases/tag/v1.0.0", info.downloadUrl)
        assertEquals(0L, info.apkSize)
        // Fallback changelog = release name
        assertEquals("Release 1.0.0", info.changelog)
    }

    @Test
    fun `skips draft releases`() = runTest {
        enqueueReleasesResponse(
            """
            [
              {
                "tag_name": "v2.0.0-draft",
                "name": "Draft",
                "body": null,
                "draft": true,
                "prerelease": false,
                "html_url": "https://github.com/example/releases/tag/v2.0.0-draft",
                "published_at": "",
                "assets": []
              },
              {
                "tag_name": "v1.1.0",
                "name": "Stable",
                "body": "Yang baru: Bug fix penting",
                "draft": false,
                "prerelease": false,
                "html_url": "https://github.com/example/releases/tag/v1.1.0",
                "published_at": "2026-11-01T00:00:00Z",
                "assets": []
              }
            ]
            """.trimIndent()
        )

        val result = client.getLatestRelease()
        assertTrue(result.isSuccess)
        assertEquals("v1.1.0", result.getOrThrow().versionName)
    }

    @Test
    fun `extracts Yang baru changelog line`() = runTest {
        enqueueReleasesResponse(
            """
            [
              {
                "tag_name": "v1.2.0",
                "name": "Release",
                "body": "Release notes\nYang baru: Fitur keren ditambahkan\nBug lainnya diperbaiki",
                "draft": false,
                "prerelease": false,
                "html_url": "https://github.com/example/releases/tag/v1.2.0",
                "published_at": "2026-11-15T00:00:00Z",
                "assets": []
              }
            ]
            """.trimIndent()
        )

        val info = client.getLatestRelease().getOrThrow()
        assertEquals("Yang baru: Fitur keren ditambahkan", info.changelog)
    }

    @Test
    fun `prerelease non-draft is accepted as update`() = runTest {
        enqueueReleasesResponse(
            """
            [
              {
                "tag_name": "v1.2.0-snapshot.abc1234",
                "name": "Snapshot build",
                "body": null,
                "draft": false,
                "prerelease": true,
                "html_url": "https://github.com/example/releases/tag/v1.2.0-snapshot.abc1234",
                "published_at": "2026-11-20T00:00:00Z",
                "assets": []
              }
            ]
            """.trimIndent()
        )

        val result = client.getLatestRelease()
        assertTrue(result.isSuccess)
        assertEquals("v1.2.0-snapshot.abc1234", result.getOrThrow().versionName)
    }

    // ------------------------------------------------------------------
    // Failure paths
    // ------------------------------------------------------------------

    @Test
    fun `returns failure when all releases are drafts`() = runTest {
        enqueueReleasesResponse(
            """
            [
              {
                "tag_name": "v2.0.0",
                "name": "Draft Only",
                "body": null,
                "draft": true,
                "prerelease": false,
                "html_url": "https://github.com/example/releases/tag/v2.0.0",
                "published_at": "",
                "assets": []
              }
            ]
            """.trimIndent()
        )

        val result = client.getLatestRelease()
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("No published releases") == true)
    }

    @Test
    fun `returns failure on HTTP 403`() = runTest {
        enqueueReleasesResponse("{\"message\":\"API rate limit exceeded\"}", code = 403)
        val result = client.getLatestRelease()
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("403") == true)
    }

    @Test
    fun `returns failure on malformed JSON`() = runTest {
        enqueueReleasesResponse("<html>Not JSON</html>")
        val result = client.getLatestRelease()
        assertTrue(result.isFailure)
    }

    @Test
    fun `returns failure on empty array`() = runTest {
        enqueueReleasesResponse("[]")
        val result = client.getLatestRelease()
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("No published releases") == true)
    }
}
