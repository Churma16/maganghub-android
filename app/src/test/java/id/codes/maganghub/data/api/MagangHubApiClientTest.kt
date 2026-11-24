package id.codes.maganghub.data.api

import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test

class MagangHubApiClientTest {

    private lateinit var mockServer: MockWebServer
    private lateinit var client: MagangHubApiClient

    @Before
    fun setUp() {
        mockServer = MockWebServer()
        mockServer.start()
        val baseUrl = mockServer.url("/api/v1").toString()
        client = MagangHubApiClient(apiBaseUrl = baseUrl)
    }

    @After
    fun tearDown() {
        mockServer.shutdown()
    }

    @Test
    fun testEnsureHttps() {
        assertEquals("https://account.kemnaker.go.id/auth", MagangHubApiClient.ensureHttps("http://account.kemnaker.go.id/auth"))
        assertEquals("https://account.kemnaker.go.id/auth", MagangHubApiClient.ensureHttps("https://account.kemnaker.go.id/auth"))
        assertEquals("/auth/login", MagangHubApiClient.ensureHttps("/auth/login"))
    }

    @Test
    fun testCleanBearerToken() {
        assertEquals("sample_token_123", MagangHubApiClient.cleanBearerToken("Bearer sample_token_123"))
        assertEquals("sample_token_123", MagangHubApiClient.cleanBearerToken("bearer sample_token_123"))
        assertEquals("sample_token_123", MagangHubApiClient.cleanBearerToken("\"sample_token_123\""))
        assertEquals("sample_token_123", MagangHubApiClient.cleanBearerToken("'Bearer sample_token_123'"))
        assertEquals("sample_token_123", MagangHubApiClient.cleanBearerToken("  sample_token_123  "))
    }

    @Test
    fun testExtractCsrfTokenFromHtml() {
        val html1 = """<html><head><meta name="csrf-token" content="secret-csrf-token-123"></head></html>"""
        assertEquals("secret-csrf-token-123", client.extractCsrfTokenFromHtml(html1))

        val html2 = """<html><head><meta content="reversed-csrf-456" name="csrf-token"></head></html>"""
        assertEquals("reversed-csrf-456", client.extractCsrfTokenFromHtml(html2))
    }

    @Test
    fun testParseSsoAuthorizationUrl() {
        // Plain URL
        val plain = "https://sso.kemnaker.go.id/auth?client_id=123"
        assertEquals(plain, client.parseSsoAuthorizationUrl(plain))

        // Quoted JSON string
        val quoted = "\"https://sso.kemnaker.go.id/auth?client_id=123\""
        assertEquals(plain, client.parseSsoAuthorizationUrl(quoted))

        // JSON envelope with url
        val jsonEnvelope = """{"code":200,"data":{"url":"https://sso.kemnaker.go.id/auth?client_id=123"}}"""
        assertEquals(plain, client.parseSsoAuthorizationUrl(jsonEnvelope))

        // JSON envelope with direct data string
        val jsonDirect = """{"data":"https://sso.kemnaker.go.id/auth?client_id=123"}"""
        assertEquals(plain, client.parseSsoAuthorizationUrl(jsonDirect))
    }

    @Test
    fun testExtractTokenFromCallbackResponse() {
        // Nested data map
        val jsonWithData = """{"data":{"monev_access_token":"token_abc_123"}}"""
        assertEquals("token_abc_123", client.extractTokenFromCallbackResponse(jsonWithData))

        // Top level access_token
        val jsonTopLevel = """{"access_token":"token_xyz_456"}"""
        assertEquals("token_xyz_456", client.extractTokenFromCallbackResponse(jsonTopLevel))

        // Plain string
        val plain = "simple_token_789"
        assertEquals("simple_token_789", client.extractTokenFromCallbackResponse(plain))
    }

    @Test
    fun testFetchTodayAttendanceStatusSuccess() = runBlocking {
        val responseBody = """
            {
                "data": {
                    "date": "2026-11-24",
                    "active_internship": true,
                    "is_holiday": false,
                    "is_scheduled_off_day": false,
                    "has_attendance": true
                }
            }
        """.trimIndent()

        mockServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(responseBody)
        )

        val result = client.fetchTodayAttendanceStatus("valid_mock_token")
        assertNotNull(result)
        assertEquals("2026-11-24", result.date)
        assertEquals(true, result.activeInternship)
        assertEquals(true, result.hasAttendance)
        assertEquals(false, result.isHoliday)

        val recorded = mockServer.takeRequest()
        assertEquals("/api/v1/users/me/home", recorded.path)
        assertEquals("Bearer valid_mock_token", recorded.getHeader("Authorization"))
    }

    @Test
    fun testFetchTodayAttendanceStatusUnauthorized() {
        val errorBody = """
            {
                "code": 401,
                "error_code": "UNAUTHORIZED",
                "message": "Token expired or invalid"
            }
        """.trimIndent()

        mockServer.enqueue(
            MockResponse()
                .setResponseCode(401)
                .setHeader("Content-Type", "application/json")
                .setBody(errorBody)
        )

        assertThrows(UnauthorizedSessionException::class.java) {
            runBlocking {
                client.fetchTodayAttendanceStatus("expired_token")
            }
        }
    }

    @Test
    fun testLoginWithCredentials4StepFlow() = runBlocking {
        val ssoBaseUrl = mockServer.url("/sso")
        val authLoginUrl = "${ssoBaseUrl}auth/login"

        // Step 1: GET /api/v1/auth/login returns SSO URL
        mockServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("""{"data":{"url":"$authLoginUrl"}}""")
        )

        // Step 2: GET SSO authorization page returns HTML with CSRF
        mockServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "text/html")
                .setBody("""
                    <html>
                    <head><meta name="csrf-token" content="mock-csrf-token-999"></head>
                    <body>Login Page</body>
                    </html>
                """.trimIndent())
        )

        // Step 3: POST /auth/login returns 302 redirect with code and state
        val callbackUrl = mockServer.url("/api/v1/auth/login/callback?code=mock_auth_code_123&state=mock_state_456").toString()
        mockServer.enqueue(
            MockResponse()
                .setResponseCode(302)
                .setHeader("Location", callbackUrl)
        )

        // Step 4: GET /api/v1/auth/login/callback returns new token
        mockServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("""{"data":{"monev_access_token":"final_granted_token_777"}}""")
        )

        val grantedToken = client.loginWithCredentials("testuser@example.com", "SecretPass123!")
        assertEquals("final_granted_token_777", grantedToken)

        // Verify requests executed in proper sequence
        val req1 = mockServer.takeRequest()
        assertEquals("/api/v1/auth/login", req1.path)

        val req2 = mockServer.takeRequest()
        assertEquals("/sso/auth/login", req2.path)

        val req3 = mockServer.takeRequest()
        assertEquals("/auth/login", req3.path)
        assertEquals("mock-csrf-token-999", req3.getHeader("X-CSRF-TOKEN"))

        val req4 = mockServer.takeRequest()
        assertTrue(req4.path?.startsWith("/api/v1/auth/login/callback") == true)
        assertTrue(req4.path?.contains("code=mock_auth_code_123") == true)
    }
}
