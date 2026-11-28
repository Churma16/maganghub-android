package id.churma.maganghubcompanion.data.api

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import id.churma.maganghubcompanion.BuildConfig
import id.churma.maganghubcompanion.data.model.FeedbackRequest
import id.churma.maganghubcompanion.data.model.FeedbackResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * API contract and client for sending feedback to the telegram bridge microservice.
 */
interface FeedbackApiClient {
    suspend fun sendFeedback(request: FeedbackRequest): Result<FeedbackResponse>
}

/**
 * Implementation using OkHttp and Gson serialization.
 */
class FeedbackApiClientImpl(
    private val endpointUrl: String = DEFAULT_FEEDBACK_ENDPOINT,
    private val apiKey: String = DEFAULT_FORWARDER_API_KEY,
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build(),
    private val gson: Gson = Gson()
) : FeedbackApiClient {

    companion object {
        private const val TAG = "FeedbackApiClient"
        const val DEFAULT_FEEDBACK_ENDPOINT = "https://feedback-mctf.churma.codes/api/v1/feedback"
        const val DEFAULT_FORWARDER_API_KEY = "mctf_4e02cfa934ecbff0149a213607575fea"
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
    }

    override suspend fun sendFeedback(request: FeedbackRequest): Result<FeedbackResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val jsonPayload = gson.toJson(request)
                val httpRequestBuilder = Request.Builder()
                    .url(endpointUrl)
                    .post(jsonPayload.toRequestBody(JSON_MEDIA_TYPE))
                    .header("Accept", "application/json")

                if (apiKey.isNotBlank()) {
                    httpRequestBuilder.header("X-Forwarder-Key", apiKey)
                }

                val httpRequest = httpRequestBuilder.build()

                client.newCall(httpRequest).execute().use { response ->
                    val responseBody = response.body?.string().orEmpty()
                    if (!response.isSuccessful) {
                        val errorMsg = try {
                            val parsedError = gson.fromJson(responseBody, FeedbackResponse::class.java)
                            parsedError?.error ?: parsedError?.message ?: "HTTP ${response.code}: Gagal mengirim masukan"
                        } catch (_: Exception) {
                            "HTTP ${response.code}: Gagal mengirim masukan"
                        }
                        return@withContext Result.failure(IOException(errorMsg))
                    }

                    val parsed = try {
                        gson.fromJson(responseBody, FeedbackResponse::class.java)
                    } catch (e: JsonSyntaxException) {
                        return@withContext Result.failure(IOException("Format respons server tidak valid", e))
                    } ?: return@withContext Result.failure(IOException("Respons server kosong"))

                    Result.success(parsed)
                }
            } catch (e: Exception) {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "Failed to send feedback: ${e.message}")
                }
                Result.failure(e)
            }
        }
    }
}
