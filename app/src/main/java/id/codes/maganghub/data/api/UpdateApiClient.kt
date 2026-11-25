package id.codes.maganghub.data.api

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.annotations.SerializedName
import id.codes.maganghub.data.model.UpdateInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.concurrent.TimeUnit

// ---------------------------------------------------------------------------
// GitHub API response models (internal to this file)
// ---------------------------------------------------------------------------

private data class GithubRelease(
    @SerializedName("tag_name") val tagName: String = "",
    @SerializedName("name") val name: String = "",
    @SerializedName("body") val body: String? = null,
    @SerializedName("draft") val draft: Boolean = false,
    @SerializedName("prerelease") val prerelease: Boolean = false,
    @SerializedName("html_url") val htmlUrl: String = "",
    @SerializedName("published_at") val publishedAt: String = "",
    @SerializedName("assets") val assets: List<GithubAsset> = emptyList()
)

private data class GithubAsset(
    @SerializedName("name") val name: String = "",
    @SerializedName("browser_download_url") val browserDownloadUrl: String = "",
    @SerializedName("size") val size: Long = 0L
)

// ---------------------------------------------------------------------------
// Public interface and implementation
// ---------------------------------------------------------------------------

interface UpdateApiClient {
    suspend fun getLatestRelease(): Result<UpdateInfo>
}

class UpdateApiClientImpl(
    private val releasesUrl: String = DEFAULT_RELEASES_URL,
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build(),
    private val gson: Gson = Gson()
) : UpdateApiClient {

    companion object {
        const val DEFAULT_RELEASES_URL =
            "https://api.github.com/repos/Churma16/maganghub-logbook-companion/releases?per_page=5"
    }

    override suspend fun getLatestRelease(): Result<UpdateInfo> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(releasesUrl)
                .header("Accept", "application/vnd.github+json")
                .header("X-GitHub-Api-Version", "2022-11-28")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext Result.failure(
                        IOException("GitHub API error ${response.code}")
                    )
                }

                val body = response.body?.string().orEmpty()
                val releases: List<GithubRelease> = try {
                    val type = object : com.google.gson.reflect.TypeToken<List<GithubRelease>>() {}.type
                    gson.fromJson(body, type) ?: emptyList()
                } catch (e: JsonSyntaxException) {
                    return@withContext Result.failure(IOException("Failed to parse releases JSON", e))
                }

                // Pick the first non-draft, non-PR-preview release
                val release = releases.firstOrNull { !it.draft && !it.tagName.contains("-pr") }
                    ?: return@withContext Result.failure(IOException("No published releases found"))

                val apkAsset = release.assets.firstOrNull { it.name.endsWith(".apk") }
                val downloadUrl = apkAsset?.browserDownloadUrl?.takeIf { it.isNotBlank() }
                    ?: release.htmlUrl

                Result.success(
                    UpdateInfo(
                        versionName = release.tagName,
                        changelog = extractChangelog(release.body, release.name),
                        downloadUrl = downloadUrl
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Extracts a human-friendly changelog from the release body.
     *
     * Priority:
     *  1. First line matching "Yang baru:" or "What's new:" (case-insensitive)
     *  2. First bullet point (line starting with '-', '*', or '•')
     *  3. First non-blank line of the body
     *  4. Release name
     *  5. Generic fallback
     */
    private fun extractChangelog(body: String?, releaseName: String): String {
        if (!body.isNullOrBlank()) {
            val lines = body.lines().map { it.trim() }.filter { it.isNotBlank() }

            // 1. "Yang baru:" / "What's new:" line (strip prefix to prevent duplication in UI)
            val yangBaru = lines.firstOrNull { line ->
                line.startsWith("Yang baru:", ignoreCase = true) ||
                    line.startsWith("What's new:", ignoreCase = true) ||
                    line.startsWith("Whats new:", ignoreCase = true)
            }
            if (yangBaru != null) return yangBaru.substringAfter(':').trim().take(200)

            // 2. First bullet point
            val bullet = lines.firstOrNull { it.startsWith("-") || it.startsWith("*") || it.startsWith("•") }
            if (bullet != null) return bullet.removePrefix("-").removePrefix("*").removePrefix("•").trim().take(200)

            // 3. First non-blank line (body snippet)
            return lines.first().take(200)
        }

        // 4. Release name as fallback
        if (releaseName.isNotBlank()) return releaseName

        return "Pembaruan tersedia"
    }
}
