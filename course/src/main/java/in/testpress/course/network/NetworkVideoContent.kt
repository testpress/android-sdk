package `in`.testpress.course.network

import `in`.testpress.database.VideoContentEntity
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
    val streams: List<NetworkStream> = arrayListOf<NetworkStream>()
)

fun NetworkVideoContent.asGreenDaoModel(): Video {
    return Video(
        this.title,
        this.url,
        this.id,
        this.embedCode,
        this.duration,
        this.isDomainRestricted,
        null,
        null,
        null
    )
}

fun NetworkVideoContent.asDatabaseModel(): VideoContentEntity {
    return VideoContentEntity(
        id = id,
        url = url,
        title = title,
        embedCode = embedCode,
        duration = duration,
        description = description,
        isDomainRestricted = isDomainRestricted,
        streams = streams.asDatabaseModels()
    )
}
