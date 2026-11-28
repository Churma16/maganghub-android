package id.churma.maganghubcompanion.data.api

import com.google.gson.Gson
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
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
            releasesUrl = server.url("/repos/test/test/releases/latest").toString(),
            client = OkHttpClient(),
            gson = Gson()
        )
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    private fun enqueueResponse(body: String, code: Int = 200) {
        server.enqueue(MockResponse().setResponseCode(code).setBody(body))
    }

    // ------------------------------------------------------------------
    // Success paths (Strictly Stable Releases)
    // ------------------------------------------------------------------

    @Test
    fun `returns UpdateInfo from stable release with apk asset`() = runTest {
        enqueueResponse(
            """
            {
              "tag_name": "v1.1.0",
              "name": "Release 1.1.0",
              "body": "- Perbaikan bug notifikasi\n- Tambah fitur baru",
              "draft": false,
              "prerelease": false,
              "html_url": "https://github.com/example/releases/tag/v1.1.0",
              "assets": [
                {
                  "name": "maganghub-companion-v1.1.0.apk",
                  "browser_download_url": "https://github.com/example/releases/download/v1.1.0/maganghub-companion-v1.1.0.apk"
                }
              ]
            }
            """.trimIndent()
        )

        val result = client.getLatestRelease()
        assertTrue(result.isSuccess)
        val info = result.getOrThrow()
        assertNotNull(info)
        assertEquals("v1.1.0", info?.versionName)
        assertEquals("https://github.com/example/releases/download/v1.1.0/maganghub-companion-v1.1.0.apk", info?.downloadUrl)
        assertTrue(info?.changelog?.contains("Perbaikan bug notifikasi") == true)
    }

    @Test
    fun `falls back to html_url when no apk asset`() = runTest {
        enqueueResponse(
            """
            {
              "tag_name": "v1.0.0",
              "name": "Release 1.0.0",
              "body": null,
              "draft": false,
              "prerelease": false,
              "html_url": "https://github.com/example/releases/tag/v1.0.0",
              "assets": []
            }
            """.trimIndent()
        )

        val result = client.getLatestRelease()
        assertTrue(result.isSuccess)
        val info = result.getOrThrow()
        assertNotNull(info)
        assertEquals("https://github.com/example/releases/tag/v1.0.0", info?.downloadUrl)
        assertEquals("Release 1.0.0", info?.changelog)
    }

    @Test
    fun `returns success null when no stable release published yet (HTTP 404)`() = runTest {
        enqueueResponse("{\"message\":\"Not Found\"}", code = 404)

        val result = client.getLatestRelease()
        assertTrue(result.isSuccess)
        assertNull(result.getOrThrow())
    }

    @Test
    fun `returns success null when release is marked prerelease`() = runTest {
        enqueueResponse(
            """
            {
              "tag_name": "v1.2.0-snapshot.abc1234",
              "name": "Snapshot Build",
              "body": "Development build",
              "draft": false,
              "prerelease": true,
              "html_url": "https://github.com/example/releases/tag/v1.2.0-snapshot.abc1234",
              "assets": []
            }
            """.trimIndent()
        )

        val result = client.getLatestRelease()
        assertTrue(result.isSuccess)
        assertNull(result.getOrThrow())
    }

    @Test
    fun `returns success null when release is marked draft`() = runTest {
        enqueueResponse(
            """
            {
              "tag_name": "v1.2.0",
              "name": "Draft Build",
              "body": null,
              "draft": true,
              "prerelease": false,
              "html_url": "https://github.com/example/releases/tag/v1.2.0",
              "assets": []
            }
            """.trimIndent()
        )

        val result = client.getLatestRelease()
        assertTrue(result.isSuccess)
        assertNull(result.getOrThrow())
    }

    @Test
    fun `extracts Yang baru changelog line without prefix`() = runTest {
        enqueueResponse(
            """
            {
              "tag_name": "v1.2.0",
              "name": "Release 1.2.0",
              "body": "Release notes\nYang baru: Fitur mode gelap\nPerbaikan bug",
              "draft": false,
              "prerelease": false,
              "html_url": "https://github.com/example/releases/tag/v1.2.0",
              "assets": []
            }
            """.trimIndent()
        )

        val info = client.getLatestRelease().getOrThrow()
        assertNotNull(info)
        assertEquals("Fitur mode gelap", info?.changelog)
    }

    @Test
    fun `empty Yang baru header falls through to bullet points`() = runTest {
        enqueueResponse(
            """
            {
              "tag_name": "v1.3.0",
              "name": "Release 1.3.0",
              "body": "Yang baru:\n- Integrasi laporan presensi mentor\n- Mode gelap",
              "draft": false,
              "prerelease": false,
              "html_url": "https://github.com/example/releases/tag/v1.3.0",
              "assets": []
            }
            """.trimIndent()
        )

        val info = client.getLatestRelease().getOrThrow()
        assertNotNull(info)
        assertEquals("Integrasi laporan presensi mentor", info?.changelog)
    }

    // ------------------------------------------------------------------
    // Failure paths
    // ------------------------------------------------------------------

    @Test
    fun `returns failure on HTTP 403 rate limit`() = runTest {
        enqueueResponse("{\"message\":\"API rate limit exceeded\"}", code = 403)
        val result = client.getLatestRelease()
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("403") == true)
    }

    @Test
    fun `returns failure on malformed JSON`() = runTest {
        enqueueResponse("<html>Not JSON</html>")
        val result = client.getLatestRelease()
        assertTrue(result.isFailure)
    }
}
