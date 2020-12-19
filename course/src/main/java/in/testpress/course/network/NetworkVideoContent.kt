package `in`.testpress.course.network

import `in`.testpress.models.greendao.Video

data class NetworkVideoContent(
    val id: Long,
    val title: String = "",
    val url: String = "",
    val embedCode: String? = null,
    val duration: String? = null,
    val requiredWatchDuration: String = "",
    val isDomainRestricted: Boolean,
    val description: String = "",
    val thumbnailSmall: String? = "",
    val thumbnail: String? = "",
    val thumbnailMedium: String? = "",
    val streamID: Long? = null,
    val streams: List<NetworkStream> = arrayListOf<NetworkStream>(),
    val dashUrl: String? = null,
    val widevineLicenseUrl: String? = null
)

fun NetworkVideoContent.asGreenDaoModel(): Video {
    return Video(
        this.title,
        this.url,
        this.id,
        this.embedCode,
        this.duration,
        this.isDomainRestricted,
        thumbnail,
        thumbnailMedium,
        thumbnailSmall,
        this.dashUrl,
        this.widevineLicenseUrl,
        this.streamID
    )
}
