package id.churma.maganghubcompanion.data.model

import com.google.gson.annotations.SerializedName

/**
 * User profile details returned by GET /api/v1/users/me on MagangHub Monev API.
 */
data class UserProfileData(
    @SerializedName("id", alternate = ["participant_id", "participantId", "user_id", "userId"])
    val id: String = "",

    @SerializedName("name", alternate = ["full_name", "fullName"])
    val name: String = "",

    @SerializedName("photo_url", alternate = ["photoUrl", "avatar", "avatar_url"])
    val photoUrl: String? = null,

    @SerializedName("internship_company", alternate = ["internshipCompany"])
    val internshipCompany: String? = null,

    @SerializedName("internship_start_date", alternate = ["internshipStartDate", "start_date", "startDate"])
    val internshipStartDate: String? = null,

    @SerializedName("internship_end_date", alternate = ["internshipEndDate", "end_date", "endDate"])
    val internshipEndDate: String? = null
) {
    val internship_start_date: String?
        get() = internshipStartDate

    val internship_end_date: String?
        get() = internshipEndDate
}

/**
 * Envelope response wrapper for /users/me endpoint.
 */
data class UserProfileResponse(
    @SerializedName("data")
    val data: UserProfileData = UserProfileData()
)
