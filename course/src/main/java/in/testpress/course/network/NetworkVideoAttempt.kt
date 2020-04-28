package `in`.testpress.course.network

data class NetworkVideoAttempt(
    val id: Long,
    var lastPosition: String? = null,
    val state: Int? = null,
    val watchedDuration: String? = null,
    val videoContentId: Long? = null
)