package `in`.testpress.course.network

import `in`.testpress.models.greendao.VideoConference

data class NetworkVideoConferenceContent(
    val id: Long? = null,
    val conferenceId: String? = null,
    val duration: Int? = null,
    val joinUrl: String? = null,
    val provider: String? = null,
    val start: String? = null,
    val title: String? = null,
    val password: String? = null,
    val accessToken: String? = null,
    val showRecordedVideo: Boolean? = false,
    val state: String? = null
)

fun NetworkVideoConferenceContent.asGreenDaoModel(): VideoConference {
    return VideoConference(
        this.title, this.joinUrl, this.id, this.start, this.duration, this.provider, this.conferenceId,
        this.accessToken, this.password, this.showRecordedVideo, this.state
    )
}
