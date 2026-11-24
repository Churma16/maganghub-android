package id.codes.maganghub.data.api

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import id.codes.maganghub.data.model.ApiErrorResponse
import id.codes.maganghub.data.model.AttendanceHomeData
import id.codes.maganghub.data.model.AttendanceHomeResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import java.io.IOException
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

/**
 * Handles communication with the MagangHub Monev API and the 4-step Kemnaker SSO OAuth2 flow.
 * Direct Kotlin port of internal/maganghub/client.go.
 */
class MagangHubApiClient(
    val apiBaseUrl: String = DEFAULT_BASE_URL,
    private val userAgentString: String = DEFAULT_USER_AGENT
) {
    companion object {
        const val DEFAULT_BASE_URL = "https://monev-api.maganghub.kemnaker.go.id/api/v1"
        const val DEFAULT_USER_AGENT =
            "Mozilla/5.0 (Linux; Android 14; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Mobile Safari/537.36"

        private val CSRF_PATTERN_1 = Pattern.compile(
            "(?i)<meta[^>]+name\\s*=\\s*[\"']csrf-token[\"'][^>]+content\\s*=\\s*[\"']([^\"']+)[\"']"
        )
        private val CSRF_PATTERN_2 = Pattern.compile(
            "(?i)<meta[^>]+content\\s*=\\s*[\"']([^\"']+)[\"'][^>]+name\\s*=\\s*[\"']csrf-token[\"']"
        )

        fun cleanBearerToken(rawToken: String?): String {
            if (rawToken.isNullOrBlank()) return ""
            var token = rawToken.trim().trim('"', '\'')
            if (token.startsWith("bearer ", ignoreCase = true)) {
                token = token.substring(7).trim().trim('"', '\'')
            }
            return token
        }

        fun ensureHttps(url: String): String {
            if (url.startsWith("http://", ignoreCase = true)) {
                return "https://" + url.substring(7)
            }
            return url
        }

        /**
         * Parses the HTTP Retry-After header as integer seconds or RFC 1123 HTTP-date.
         * Falls back to defaultSeconds (default: 900 seconds / 15 minutes) on null or parsing failure.
         */
        fun parseRetryAfter(headerValue: String?, defaultSeconds: Long = 900L): Long {
            if (headerValue.isNullOrBlank()) return defaultSeconds
            val trimmed = headerValue.trim()
            trimmed.toLongOrNull()?.let { seconds ->
                return if (seconds > 0) seconds else defaultSeconds
            }
            return try {
                val retryTime = java.time.ZonedDateTime.parse(
                    trimmed,
                    java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME
                )
                val now = java.time.ZonedDateTime.now(retryTime.zone)
                val diffSeconds = java.time.Duration.between(now, retryTime).seconds
                if (diffSeconds > 0) diffSeconds else defaultSeconds
            } catch (_: Exception) {
                defaultSeconds
            }
        }
    }

    private val gson = Gson()

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }

    private val baseHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            var request = chain.request()
            val url = request.url
            if (!request.isHttps && (url.host.endsWith("kemnaker.go.id") || url.host.contains("maganghub"))) {
                val secureUrl = url.newBuilder().scheme("https").port(443).build()
                request = request.newBuilder().url(secureUrl).build()
            }
            chain.proceed(request)
        }
        .addInterceptor(loggingInterceptor)
        .build()

    /**
     * Calls GET /api/v1/users/me/home to retrieve today's attendance status.
     */
    suspend fun fetchTodayAttendanceStatus(monevAccessToken: String): AttendanceHomeData =
        withContext(Dispatchers.IO) {
            val cleanToken = cleanBearerToken(monevAccessToken)
            if (cleanToken.isEmpty()) {
                throw UnauthorizedSessionException("Access token is empty")
            }

            val targetUrl = "${apiBaseUrl.trimEnd('/')}/users/me/home"
            val request = Request.Builder()
                .url(targetUrl)
                .get()
                .header("Authorization", "Bearer $cleanToken")
                .header("Accept", "application/json")
                .header("User-Agent", userAgentString)
                .build()

            val response = baseHttpClient.newCall(request).execute()
            val responseBody = response.body?.string().orEmpty()

            when (response.code) {
                200 -> {
                    val envelope = gson.fromJson(responseBody, AttendanceHomeResponse::class.java)
                        ?: throw IOException("Empty response received from attendance endpoint")
                    envelope.data
                }
                401 -> {
                    val errorEnvelope = try {
                        gson.fromJson(responseBody, ApiErrorResponse::class.java)
                    } catch (e: Exception) {
                        null
                    }
                    val msg = errorEnvelope?.message?.takeIf { it.isNotEmpty() }
                        ?: "Monev access token is expired or invalid (HTTP 401)"
                    throw UnauthorizedSessionException(msg)
                }
                403 -> throw ForbiddenException()
                404 -> throw NotFoundException()
                429 -> {
                    val retrySec = parseRetryAfter(response.header("Retry-After"))
                    throw RateLimitedException(retrySec)
                }
                else -> throw MagangHubApiException(response.code, responseBody)
            }
        }

    /**
     * Executes the 4-step OAuth2 SSO login flow against Kemnaker and MagangHub:
     *
     * Step 1: GET /auth/login -> retrieves SSO authorization URL.
     * Step 2: GET SSO authorization URL -> follows redirect to /auth/login, sets kemnaker_ri_session, extracts CSRF token.
     * Step 3: POST /auth/login -> submits username & password with CSRF token, receives redirect_uri with auth code.
     * Step 4: GET /auth/login/callback?code=...&state=... -> exchanges code for new monev access token.
     */
    suspend fun loginWithCredentials(username: String, password: String): String =
        withContext(Dispatchers.IO) {
            val trimmedUsername = username.trim()
            val trimmedPassword = password.trim()
            if (trimmedUsername.isEmpty() || trimmedPassword.isEmpty()) {
                throw IllegalArgumentException("Username and password must not be empty")
            }

            val cookieJar = InMemoryCookieJar()
            val sessionClient = baseHttpClient.newBuilder()
                .cookieJar(cookieJar)
                .build()

            // Step 1: GET /auth/login to get SSO authorization URL
            val step1Url = "${apiBaseUrl.trimEnd('/')}/auth/login"
            val step1Request = Request.Builder()
                .url(step1Url)
                .get()
                .header("Accept", "application/json, text/plain, */*")
                .header("User-Agent", userAgentString)
                .build()

            val step1Response = sessionClient.newCall(step1Request).execute()
            if (step1Response.code == 429) {
                val retrySec = parseRetryAfter(step1Response.header("Retry-After"))
                throw RateLimitedException(retrySec, "Step 1 rate limited (HTTP 429)")
            }
            val step1Body = step1Response.body?.string().orEmpty()
            if (!step1Response.isSuccessful) {
                throw IOException("Step 1 failed with status ${step1Response.code}: $step1Body")
            }

            val ssoAuthUrlString = ensureHttps(parseSsoAuthorizationUrl(step1Body))
            if (ssoAuthUrlString.isBlank()) {
                throw IOException("Step 1 failed: empty SSO authorization URL in response: $step1Body")
            }

            val parsedSsoUrl = ssoAuthUrlString.toHttpUrlOrNull()
                ?: throw IOException("Step 1 returned invalid SSO authorization URL: $ssoAuthUrlString")

            // Step 2: GET SSO authorization page (follow redirect, store session cookie, extract CSRF)
            val step2Request = Request.Builder()
                .url(ssoAuthUrlString)
                .get()
                .header("User-Agent", userAgentString)
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .build()

            val step2Response = sessionClient.newCall(step2Request).execute()
            if (step2Response.code == 429) {
                val retrySec = parseRetryAfter(step2Response.header("Retry-After"))
                throw RateLimitedException(retrySec, "Step 2 rate limited (HTTP 429)")
            }
            val step2Body = step2Response.body?.string().orEmpty()
            if (!step2Response.isSuccessful) {
                throw IOException("Step 2 failed with status ${step2Response.code}: $step2Body")
            }

            val csrfToken = extractCsrfTokenFromHtml(step2Body)
                ?: throw KemnakerAuthException("Step 2 failed: unable to extract csrf-token from Kemnaker login page")

            // Step 3: POST /auth/login with credentials and CSRF
            // Use no-redirect client to capture 302/303 Location directly
            val step3Client = sessionClient.newBuilder()
                .followRedirects(false)
                .followSslRedirects(false)
                .build()

            val ssoLoginEndpoint = "https://${parsedSsoUrl.host}/auth/login"
            val payload = JsonObject().apply {
                addProperty("username", trimmedUsername)
                addProperty("password", trimmedPassword)
            }
            val requestBody = payload.toString().toRequestBody("application/json; charset=utf-8".toMediaType())

            val step3Request = Request.Builder()
                .url(ssoLoginEndpoint)
                .post(requestBody)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json, text/plain, */*")
                .header("X-CSRF-TOKEN", csrfToken)
                .header("X-Requested-With", "XMLHttpRequest")
                .header("Referer", ssoLoginEndpoint)
                .header("Origin", "${parsedSsoUrl.scheme}://${parsedSsoUrl.host}")
                .header("User-Agent", userAgentString)
                .build()

            val step3Response = step3Client.newCall(step3Request).execute()
            if (step3Response.code == 429) {
                val retrySec = parseRetryAfter(step3Response.header("Retry-After"))
                throw RateLimitedException(retrySec, "Step 3 rate limited (HTTP 429)")
            }
            val step3Body = step3Response.body?.string().orEmpty()

            val redirectUriString = when (step3Response.code) {
                in listOf(302, 303, 307, 308) -> step3Response.header("Location")?.let { ensureHttps(it) }
                200 -> parseRedirectUriFromJson(step3Body)?.let { ensureHttps(it) }
                else -> {
                    val errorMsg = parseErrorMessageFromJson(step3Body)
                        ?: "Kemnaker authentication failed with status ${step3Response.code}: $step3Body"
                    throw KemnakerAuthException(errorMsg)
                }
            }

            if (redirectUriString.isNullOrBlank()) {
                throw KemnakerAuthException("Step 3 response did not contain redirect_uri or Location header: $step3Body")
            }

            // Parse authorization code and state
            var (authCode, state) = extractCodeAndState(redirectUriString, parsedSsoUrl)

            // If redirected to intermediate OAuth consent/authorize page, follow up to 3 hops
            var currentRedirectUrl: String? = redirectUriString
            var hopCount = 0
            while (authCode.isNullOrEmpty() && hopCount < 3) {
                hopCount++
                val urlToLoad = currentRedirectUrl ?: break
                val fullUrl = if (!urlToLoad.startsWith("http://") && !urlToLoad.startsWith("https://")) {
                    "https://${parsedSsoUrl.host}$urlToLoad"
                } else {
                    ensureHttps(urlToLoad)
                }

                val hopRequest = Request.Builder()
                    .url(fullUrl)
                    .get()
                    .header("User-Agent", userAgentString)
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                    .build()

                val nextLocation = step3Client.newCall(hopRequest).execute().use { hopResponse ->
                    hopResponse.header("Location")?.let { ensureHttps(it) }
                } ?: break
                currentRedirectUrl = nextLocation

                val (hopCode, hopState) = extractCodeAndState(nextLocation, parsedSsoUrl)
                if (!hopCode.isNullOrEmpty()) {
                    authCode = hopCode
                }
                if (!hopState.isNullOrEmpty()) {
                    state = hopState
                }
            }

            if (authCode.isNullOrEmpty()) {
                throw KemnakerAuthException("Step 3 redirect missing authorization code: $currentRedirectUrl")
            }

            // Step 4: GET /auth/login/callback to exchange auth code for monev token
            val step4UrlBuilder = "${apiBaseUrl.trimEnd('/')}/auth/login/callback"
                .toHttpUrlOrNull()
                ?.newBuilder()
                ?: throw IOException("Invalid callback URL")

            step4UrlBuilder.addQueryParameter("code", authCode)
            if (!state.isNullOrEmpty()) {
                step4UrlBuilder.addQueryParameter("state", state)
            }

            val step4CallbackUrl = step4UrlBuilder.build().toString()
            val step4Request = Request.Builder()
                .url(step4CallbackUrl)
                .get()
                .header("Accept", "application/json")
                .header("User-Agent", userAgentString)
                .build()

            val step4Response = sessionClient.newCall(step4Request).execute()
            if (step4Response.code == 429) {
                val retrySec = parseRetryAfter(step4Response.header("Retry-After"))
                throw RateLimitedException(retrySec, "Step 4 rate limited (HTTP 429)")
            }
            val step4Body = step4Response.body?.string().orEmpty()

            if (!step4Response.isSuccessful) {
                throw IOException("Step 4 callback failed with status ${step4Response.code}: $step4Body")
            }

            val newMonevAccessToken = extractTokenFromCallbackResponse(step4Body)
                ?: throw IOException("Step 4 failed to extract access token from: $step4Body")

            newMonevAccessToken
        }

    fun extractCsrfTokenFromHtml(htmlContent: String): String? {
        val matcher1 = CSRF_PATTERN_1.matcher(htmlContent)
        if (matcher1.find()) return matcher1.group(1)

        val matcher2 = CSRF_PATTERN_2.matcher(htmlContent)
        if (matcher2.find()) return matcher2.group(1)

        return null
    }

    fun parseSsoAuthorizationUrl(responseBody: String): String {
        val trimmed = responseBody.trim().trim('"', '\'')
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            return trimmed
        }

        return try {
            val jsonElement = JsonParser.parseString(responseBody)
            when {
                jsonElement.isJsonPrimitive && jsonElement.asJsonPrimitive.isString -> {
                    jsonElement.asString.trim()
                }
                jsonElement.isJsonObject -> {
                    val obj = jsonElement.asJsonObject
                    val urlPrimitive = obj.get("url")?.takeIf { it.isJsonPrimitive }?.asString
                    if (!urlPrimitive.isNullOrBlank()) return urlPrimitive

                    val data = obj.get("data")
                    when {
                        data?.isJsonPrimitive == true && data.asJsonPrimitive.isString -> data.asString
                        data?.isJsonObject == true -> {
                            val dataObj = data.asJsonObject
                            dataObj.get("url")?.asString
                                ?: dataObj.get("redirect_uri")?.asString
                                ?: trimmed
                        }
                        else -> trimmed
                    }
                }
                else -> trimmed
            }
        } catch (e: Exception) {
            trimmed
        }
    }

    fun extractTokenFromCallbackResponse(responseBody: String): String? {
        val candidateKeys = listOf("monev_access_token", "access_token", "token", "monevAccessToken", "accessToken")

        return try {
            val element = JsonParser.parseString(responseBody)
            if (element.isJsonPrimitive && element.asJsonPrimitive.isString) {
                val str = element.asString
                if (str.isNotBlank()) return cleanBearerToken(str)
            }

            if (element.isJsonObject) {
                val root = element.asJsonObject

                // Check nested "data"
                if (root.has("data")) {
                    val data = root.get("data")
                    if (data.isJsonPrimitive && data.asJsonPrimitive.isString) {
                        return cleanBearerToken(data.asString)
                    }
                    if (data.isJsonObject) {
                        val dataObj = data.asJsonObject
                        for (key in candidateKeys) {
                            val value = dataObj.get(key)?.takeIf { it.isJsonPrimitive }?.asString
                            if (!value.isNullOrBlank()) return cleanBearerToken(value)
                        }
                    }
                }

                // Check root level
                for (key in candidateKeys) {
                    val value = root.get(key)?.takeIf { it.isJsonPrimitive }?.asString
                    if (!value.isNullOrBlank()) return cleanBearerToken(value)
                }
            }

            // Fallback plain string
            val trimmed = responseBody.trim()
            if (trimmed.isNotBlank() && !trimmed.startsWith("{") && !trimmed.startsWith("<")) {
                cleanBearerToken(trimmed)
            } else null
        } catch (e: Exception) {
            val trimmed = responseBody.trim()
            if (trimmed.isNotBlank() && !trimmed.startsWith("{") && !trimmed.startsWith("<")) {
                cleanBearerToken(trimmed)
            } else null
        }
    }

    private fun parseRedirectUriFromJson(jsonBody: String): String? {
        return try {
            val root = JsonParser.parseString(jsonBody).asJsonObject
            var uri = root.get("redirect_uri")?.asString
                ?: root.get("redirect_url")?.asString

            if (uri.isNullOrEmpty() && root.has("data") && root.get("data").isJsonObject) {
                val dataObj = root.getAsJsonObject("data")
                uri = dataObj.get("redirect_uri")?.asString
                    ?: dataObj.get("redirect_url")?.asString
            }
            uri
        } catch (e: Exception) {
            null
        }
    }

    private fun parseErrorMessageFromJson(jsonBody: String): String? {
        return try {
            val root = JsonParser.parseString(jsonBody).asJsonObject
            val message = root.get("message")?.asString
            val errorsObj = root.getAsJsonObject("errors")
            if (errorsObj == null || errorsObj.isEmpty) {
                return message
            }

            val details = mutableListOf<String>()
            for ((key, value) in errorsObj.entrySet()) {
                val itemText = if (value.isJsonArray) {
                    value.asJsonArray.map { it.asString }.joinToString(", ")
                } else {
                    value.asString
                }
                details.add("$key: $itemText")
            }
            "$message (${details.joinToString("; ")})"
        } catch (e: Exception) {
            null
        }
    }

    private fun extractCodeAndState(uriString: String, baseSsoUrl: HttpUrl): Pair<String?, String?> {
        val parsed = uriString.toHttpUrlOrNull()
            ?: "${baseSsoUrl.scheme}://${baseSsoUrl.host}$uriString".toHttpUrlOrNull()
            ?: return Pair(null, null)

        val error = parsed.queryParameter("error")
        if (!error.isNullOrEmpty()) {
            val errorDesc = parsed.queryParameter("error_description").orEmpty()
            throw KemnakerAuthException("OAuth error: $error ($errorDesc)")
        }

        val code = parsed.queryParameter("code")
        val state = parsed.queryParameter("state") ?: baseSsoUrl.queryParameter("state")
        return Pair(code, state)
    }
}
