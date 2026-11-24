package id.codes.maganghub.data.model

import com.google.gson.annotations.SerializedName

/**
 * User profile details returned by GET /api/v1/users/me on MagangHub Monev API.
 */
data class UserProfileData(
    @SerializedName("id")
    val id: String = "",

    @SerializedName("name")
    val name: String = "",

    @SerializedName("photo_url")
    val photoUrl: String? = null,

    @SerializedName("company")
    val company: String? = null,

    @SerializedName("internship_company")
    val internshipCompany: String? = null
)

/**
 * Envelope response wrapper for /users/me endpoint.
 */
data class UserProfileResponse(
    @SerializedName("data")
    val data: UserProfileData = UserProfileData()
)
