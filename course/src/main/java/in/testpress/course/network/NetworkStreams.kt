package `in`.testpress.course.network

import `in`.testpress.models.greendao.Stream

data class NetworkStream(
    val id: Long,
    val url: String? = null,
    val format: String = "",
    val videoId: Long,
    val dashUrl: String? = null,
    val widevineLicenseUrl: String? = null
)

fun NetworkStream.asGreenDaoModel(): Stream {
    return Stream(this.id, this.format, this.url, this.dashUrl, this.widevineLicenseUrl, this.videoId)
}

fun List<NetworkStream>.asGreenDaoModel(): List<Stream> {
    return this.map {
        it.asGreenDaoModel()
    }
}
