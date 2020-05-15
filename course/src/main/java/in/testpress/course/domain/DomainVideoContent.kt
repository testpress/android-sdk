package `in`.testpress.course.domain

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
    val isNativeVideo: Boolean
        get() = !hasEmbedCode()

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

    fun hasEmbedCode(): Boolean {
        return embedCode != null && embedCode.isNotBlank()
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

fun createDomainVideoStream(stream: Stream): DomainVideoStream {
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

fun List<Stream>.asDomainStreams(): List<DomainVideoStream> {
    return this.map {
        it.asDomainStream()
    }
}

fun Video.asDomainContent(): DomainVideoContent {
    return createDomainVideoContent(this)
}
