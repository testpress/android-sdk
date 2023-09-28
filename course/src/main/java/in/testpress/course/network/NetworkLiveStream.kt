package `in`.testpress.course.network

import `in`.testpress.models.greendao.LiveStream

data class NetworkLiveStream(
    val id: Long,
    val title: String = "",
    val streamUrl: String = "",
    val duration: Int? = null,
    val status: String = "",
    val showRecordedVideo: Boolean? = false,
)

fun NetworkLiveStream.asGreenDaoModel(): LiveStream {
    return LiveStream(
        this.id,
        this.title, 
        this.streamUrl,  
        this.duration,
        this.status,
        this.showRecordedVideo
    )
}
