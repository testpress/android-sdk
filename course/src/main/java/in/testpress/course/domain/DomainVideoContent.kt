package `in`.testpress.course.domain

import `in`.testpress.models.greendao.Stream
import `in`.testpress.models.greendao.Video
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.util.Util

data class DomainVideoContent(
    val id: Long,
    val title: String? = null,
    val url: String? = null,
    val embedCode: String? = null,
    val duration: String? = null,
    val requiredWatchDuration: String? = null,
    val isDomainRestricted: Boolean? = null,
    val description: String? = null,
    val thumbnailSmall: String? = "",
    val thumbnail: String? = "",
    val thumbnailMedium: String? = "",
    val stream: DomainVideoStream? = null,
    val isViewsExhausted: Boolean? = null,
    val transcodingStatus: String? = null,
    val streams: List<DomainVideoStream>? = arrayListOf<DomainVideoStream>()
) {
    fun getPlaybackURL(): String? {
        if (stream != null) {
            return stream.dashUrl ?: stream.url
        } else if (streams != null) {
            for (stream in streams) {
                return stream.dashUrl ?: stream.url
            }
        }
        return url
    }

    fun hasEmbedCode(): Boolean {
        return embedCode != null && embedCode.isNotBlank()
    }

    fun isDownloadable(): Boolean {
        val type = getPlaybackURL()?.let { Util.inferContentType(it) }
        return type in arrayOf(C.TYPE_HLS, C.TYPE_DASH)
    }

    fun isTranscodingStatusComplete(): Boolean {
        return transcodingStatus?.lowercase()
            .equals("completed") || transcodingStatus?.lowercase()
            .equals("not transcoded video")
    }

}

data class DomainVideoStream(
    val id: Long,
    val url: String? = null,
    val format: String? = null,
    val videoId: Long? = null,
    val hlsUrl: String? = null,
    val dashUrl: String? = null
)

fun createDomainVideoContent(video: Video): DomainVideoContent {
    return DomainVideoContent(
        id = video.id,
        title = video.title,
        url = video.url,
        embedCode = video.embedCode,
        duration = video.duration,
        isDomainRestricted = video.isDomainRestricted,
        streams = video.rawStreams?.asDomainStreams(),
        thumbnail = video.thumbnail,
        thumbnailMedium = video.thumbnailMedium,
        thumbnailSmall = video.thumbnailSmall,
        isViewsExhausted = video.isViewsExhausted,
        transcodingStatus = video.transcodingStatus,
        stream = video.stream?.asDomainStream()
    )
}

fun createDomainVideoStream(stream: Stream): DomainVideoStream {
    return DomainVideoStream(
        id = stream.id,
        url = stream.url,
        format = stream.format,
        videoId = stream.videoId,
        hlsUrl = stream.hlsUrl,
        dashUrl = stream.dashUrl
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
