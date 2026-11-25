package id.codes.maganghub.data.model

import com.google.gson.annotations.SerializedName

/**
 * Categories of feedback available to the user.
 */
enum class FeedbackType(val serializedName: String, val displayName: String) {
    @SerializedName("bug")
    BUG("bug", "Bug"),

    @SerializedName("feature")
    FEATURE_REQUEST("feature", "Saran fitur"),

    @SerializedName("other")
    OTHER("other", "Lainnya");
}

/**
 * Payload sent to the telegram bridge microservice.
 */
data class FeedbackRequest(
    @SerializedName("type")
    val type: FeedbackType,

    @SerializedName("message")
    val message: String,

    @SerializedName("app_version")
    val appVersion: String,

    @SerializedName("device_model")
    val deviceModel: String,

    @SerializedName("os_version")
    val osVersion: String,

    @SerializedName("timestamp")
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Response structure from the feedback microservice.
 */
data class FeedbackResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String? = null
)
