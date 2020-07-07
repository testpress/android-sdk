package `in`.testpress.course.network

import `in`.testpress.models.greendao.VideoConference

data class NetworkVideoConferenceContent(
    val id: Long? = null,
    val conferenceId: String? = null,
    val duration: Int? = null,
    val joinUrl: String? = null,
    val provider: String? = null,
    val start: String? = null,
    val title: String? = null
)

fun NetworkVideoConferenceContent.asGreenDaoModel(): VideoConference {
    return VideoConference(
        this.title, this.joinUrl, this.id, this.start, this.duration, this.provider, this.conferenceId
    )
}
