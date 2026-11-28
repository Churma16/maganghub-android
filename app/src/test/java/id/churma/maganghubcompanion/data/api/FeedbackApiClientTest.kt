package id.churma.maganghubcompanion.data.api

import com.google.gson.Gson
import id.churma.maganghubcompanion.data.model.FeedbackRequest
import id.churma.maganghubcompanion.data.model.FeedbackResponse
import id.churma.maganghubcompanion.data.model.FeedbackType
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

class FeedbackApiClientTest {

    private lateinit var server: MockWebServer
    private lateinit var client: FeedbackApiClientImpl
    private val gson = Gson()

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        val url = server.url("/api/v1/feedback").toString()
        client = FeedbackApiClientImpl(
            endpointUrl = url,
            client = OkHttpClient(),
            gson = gson
        )
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun testSerializationMatchesTelegramBridgeContract() {
        val request = FeedbackRequest(
            type = FeedbackType.BUG,
            message = "Notifikasi tidak muncul saat deep sleep",
            appVersion = "v1.0.0 (1)",
            deviceModel = "Xiaomi 2201116SG",
            osVersion = "Android 13 (SDK 33)",
            timestamp = 1732532400000L
        )

        val json = gson.toJson(request)
        assertTrue(json.contains("\"type\":\"bug\""))
        assertTrue(json.contains("\"message\":\"Notifikasi tidak muncul saat deep sleep\""))
        assertTrue(json.contains("\"app_version\":\"v1.0.0 (1)\""))
        assertTrue(json.contains("\"device_model\":\"Xiaomi 2201116SG\""))
        assertTrue(json.contains("\"os_version\":\"Android 13 (SDK 33)\""))
        assertTrue(json.contains("\"timestamp\":1732532400000"))
    }

    @Test
    fun testSendFeedbackSuccess() = runTest {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("""{"success":true,"message":"Feedback received"}""")
        )

        val request = FeedbackRequest(
            type = FeedbackType.FEATURE_REQUEST,
            message = "Tambahkan widget homescreen",
            appVersion = "v1.0.0",
            deviceModel = "Pixel 7",
            osVersion = "Android 14"
        )

        val result = client.sendFeedback(request)
        assertTrue(result.isSuccess)
        val response = result.getOrThrow()
        assertTrue(response.success)
        assertEquals("Feedback received", response.message)

        val recordedRequest = server.takeRequest()
        assertEquals("/api/v1/feedback", recordedRequest.path)
        assertEquals("POST", recordedRequest.method)
        assertEquals(FeedbackApiClientImpl.DEFAULT_FORWARDER_API_KEY, recordedRequest.getHeader("X-Forwarder-Key"))
        assertTrue(recordedRequest.body.readUtf8().contains("Tambahkan widget homescreen"))
    }

    @Test
    fun testSendFeedbackHttpError() = runTest {
        server.enqueue(
            MockResponse()
                .setResponseCode(500)
                .setBody("""{"success":false,"message":"Internal bot bridge error"}""")
        )

        val request = FeedbackRequest(
            type = FeedbackType.OTHER,
            message = "Tanya sesuatu",
            appVersion = "v1.0.0",
            deviceModel = "Pixel 7",
            osVersion = "Android 14"
        )

        val result = client.sendFeedback(request)
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Internal bot bridge error") == true)
    }

    @Test
    fun testSendFeedbackHttpError_WithErrorField() = runTest {
        server.enqueue(
            MockResponse()
                .setResponseCode(400)
                .setBody("""{"error":"request timestamp expired","message":"request timestamp expired"}""")
        )

        val request = FeedbackRequest(
            type = FeedbackType.OTHER,
            message = "Tanya sesuatu",
            appVersion = "v1.0.0",
            deviceModel = "Pixel 7",
            osVersion = "Android 14"
        )

        val result = client.sendFeedback(request)
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("request timestamp expired") == true)
    }

    @Test
    fun testSendFeedbackMalformedJsonResponse() = runTest {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("<html>Login portal required</html>")
        )

        val request = FeedbackRequest(
            type = FeedbackType.BUG,
            message = "Test captive portal",
            appVersion = "v1.0.0",
            deviceModel = "Pixel 7",
            osVersion = "Android 14"
        )

        val result = client.sendFeedback(request)
        assertTrue(result.isFailure)
    }
}
