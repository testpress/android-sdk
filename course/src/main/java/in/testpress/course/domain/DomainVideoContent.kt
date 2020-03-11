package `in`.testpress.course.domain

import `in`.testpress.database.StreamEntity
import `in`.testpress.database.VideoContentEntity
import `in`.testpress.models.greendao.Stream
import `in`.testpress.models.greendao.Video

data class DomainVideoContent(
    val id: Long,
    val title: String? = null,
    val url: String? = null,
    val embedCode: String? = null,
    val duration: String? = null,
    val requiredWatchDuration: String? = null,
    val isDomainRestricted: Boolean? = null,
    val description: String? = null,
    val streams: List<DomainVideoStream>? = arrayListOf<DomainVideoStream>()
) {
    fun hlsUrl(): String? {
        if (streams != null) {
            for (stream in streams) {
                if (stream.format == "HLS") {
                    return stream.url
                }
            }
        }
        return url
    }
}

data class DomainVideoStream(
    val id: Long,
    val url: String? = null,
    val format: String? = null,
    val videoId: Long? = null
)

fun createDomainVideoContent(video: Video): DomainVideoContent {
    return DomainVideoContent(
        id = video.id,
        title = video.title,
        url = video.url,
        embedCode = video.embedCode,
        duration = video.duration,
        isDomainRestricted = video.isDomainRestricted,
        streams = video.rawStreams?.asDomainStreams()
    )
}

fun createDomainVideoContent(video: VideoContentEntity): DomainVideoContent {
    return DomainVideoContent(
        id = video.id,
        title = video.title,
        url = video.url,
        embedCode = video.embedCode,
        duration = video.duration,
        isDomainRestricted = video.isDomainRestricted,
        streams = video.streams?.toDomainStreams()
    )
}


fun createDomainVideoStream(stream: Stream): DomainVideoStream {
    return DomainVideoStream(
        id = stream.id,
        url = stream.url,
        format = stream.format,
        videoId = stream.videoId
    )
}

fun createDomainVideoStream(stream: StreamEntity): DomainVideoStream {
    return DomainVideoStream(
        id = stream.id,
        url = stream.url,
        format = stream.format,
        videoId = stream.videoId
    )
}

fun Stream.asDomainStream(): DomainVideoStream {
    return createDomainVideoStream(this)
}

fun StreamEntity.asDomainStream(): DomainVideoStream {
    return createDomainVideoStream(this)
}

fun List<Stream>.asDomainStreams(): List<DomainVideoStream> {
    return this.map {
        it.asDomainStream()
    }
}

fun List<StreamEntity>.toDomainStreams(): List<DomainVideoStream> {
    return this.map {
        it.asDomainStream()
    }
}

fun Video.asDomainVideo(): DomainVideoContent {
    return createDomainVideoContent(this)
}

fun VideoContentEntity.asDomainVideo(): DomainVideoContent {
    return createDomainVideoContent(this)
}
