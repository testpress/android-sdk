package `in`.testpress.course.network

import `in`.testpress.models.greendao.Stream

data class NetworkStream(
    val id: Long,
    val url: String? = null,
    val format: String = "",
    val videoId: Long
)

fun NetworkStream.asGreenDaoModel(): Stream {
    return Stream(this.id, this.format, this.url, this.videoId)
}
