package `in`.testpress.course.domain

import `in`.testpress.models.greendao.LiveStream

data class DomainLiveStream(
    val id: Long,
    val title: String = "",
    val streamUrl: String? = "",
    val duration: Int? = null,
    val status: String = "",
    val showRecordedVideo: Boolean? = false,
    val chatEmbedUrl: String? = null
)

fun createDomainLiveStream(liveStream: LiveStream): DomainLiveStream =
    DomainLiveStream(
        id = liveStream.id,
        title = liveStream.title,
        status = liveStream.status,
        streamUrl = liveStream.streamUrl,
        duration = liveStream.duration,
        showRecordedVideo = liveStream.showRecordedVideo,
        chatEmbedUrl = liveStream.chatEmbedUrl
    )

fun LiveStream.asDomainContent(): DomainLiveStream = createDomainLiveStream(this)