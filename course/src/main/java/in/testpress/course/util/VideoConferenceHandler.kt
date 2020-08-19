package `in`.testpress.course.util

import `in`.testpress.course.domain.DomainVideoConferenceContent
import `in`.testpress.models.ProfileDetails
import android.content.Context

class VideoConferenceHandler(
    val context: Context,
    val videoConference: DomainVideoConferenceContent,
    val profileDetails: ProfileDetails?
) {

    private var zoomMeetHandler = ZoomMeetHandler(
        context,
        videoConference,
        profileDetails
    )

    fun init() {
        zoomMeetHandler.init()
    }

    fun joinMeet() {
        zoomMeetHandler.goToMeet()
    }

    fun destroy() {
        zoomMeetHandler.removeListeners()
    }
}