package `in`.testpress.course.util

import `in`.testpress.course.domain.DomainVideoConferenceContent
import `in`.testpress.models.ProfileDetails
import android.content.Context

class VideoConferenceHandler(
    val context: Context,
    val videoConference: DomainVideoConferenceContent,
    val profileDetails: ProfileDetails
) {

    private var zoomMeetHandler = ZoomMeetHandler(
        context,
        videoConference,
        profileDetails
    )

    fun init(callback: VideoConferenceInitializeListener) {
        zoomMeetHandler.init(callback)
    }

    fun joinMeet(callback: VideoConferenceInitializeListener) {
        zoomMeetHandler.goToMeet(callback)
    }

    fun destroy() {
        zoomMeetHandler.removeListeners()
    }
}


interface VideoConferenceInitializeListener {
    fun onSuccess()
    fun onFailure()
}