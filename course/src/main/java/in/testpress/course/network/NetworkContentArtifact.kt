package `in`.testpress.course.network

import com.google.gson.annotations.SerializedName

/**
 * Network model representing a single downloadable artifact (resource) attached to a content item.
 * Maps to the response from /api/v3/contents/{contentId}/artifacts/
 */
data class NetworkContentArtifact(
    val id: Long,
    val name: String,
    val url: String? = null,
    @SerializedName("accessible_without_attempt")
    val accessibleWithoutAttempt: Boolean = false
)

data class NetworkContentArtifactsResponse(
    val count: Int = 0,
    val results: List<NetworkContentArtifact> = emptyList()
)
