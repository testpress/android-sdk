package `in`.testpress.course.ui

import `in`.testpress.course.databinding.MeetingScreenBinding
import `in`.testpress.course.domain.zoom.callbacks.MeetingShareCallback
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import us.zoom.sdk.*


class MeetingScreenFragment : Fragment(), MeetingShareCallback.ShareEvent {
    private lateinit var meetingScreenBinding: MeetingScreenBinding
    private lateinit var inMeetingService: InMeetingService
    private lateinit var primaryVideoViewManager: MobileRTCVideoViewManager
    private lateinit var webCamVideoViewManager: MobileRTCVideoViewManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        inMeetingService = ZoomSDK.getInstance().inMeetingService
        MeetingShareCallback.addListener(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        meetingScreenBinding = MeetingScreenBinding.inflate(inflater, container, false)
        return meetingScreenBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        primaryVideoViewManager = meetingScreenBinding.primaryMeetingView.videoViewManager
        webCamVideoViewManager = meetingScreenBinding.webCamView.videoViewManager
        renderVideo()
    }

    override fun onSharingStatus(status: SharingStatus, userId: Long) {
        if (inMeetingService.isHostUser(userId) &&
            status == SharingStatus.Sharing_Other_Share_Begin ||
            status == SharingStatus.Sharing_Other_Share_End
        ) {
            renderVideo()
        }
    }

    private fun renderVideo() {
        primaryVideoViewManager.removeAllVideoUnits()
        webCamVideoViewManager.removeAllVideoUnits()

        val defaultVideoViewRenderInfo = MobileRTCVideoUnitRenderInfo(0, 0, 100, 100)
        val screenShareUserId = inMeetingService.activeShareUserID()
        if (inMeetingService.inMeetingShareController.isOtherSharing && inMeetingService.isHostUser(screenShareUserId)) {
            primaryVideoViewManager.addShareVideoUnit(screenShareUserId, defaultVideoViewRenderInfo)
            renderWebCamVideo()
        } else {
            meetingScreenBinding.webCamView.visibility = View.GONE
            primaryVideoViewManager.addActiveVideoUnit(defaultVideoViewRenderInfo)
        }
    }

    private fun renderWebCamVideo(){
        meetingScreenBinding.webCamView.visibility = View.VISIBLE
        val defaultVideoViewRenderInfo = MobileRTCVideoUnitRenderInfo(0, 0, 100, 100).apply {
            is_border_visible = false
            aspect_mode = MobileRTCVideoUnitAspectMode.VIDEO_ASPECT_PAN_AND_SCAN
        }
        webCamVideoViewManager.addAttendeeVideoUnit(
            inMeetingService.activeShareUserID(),
            defaultVideoViewRenderInfo
        )
    }

    override fun onResume() {
        super.onResume()
        meetingScreenBinding.primaryMeetingView.onResume()
    }

    override fun onPause() {
        super.onPause()
        meetingScreenBinding.primaryMeetingView.onPause()
    }

    override fun onStop() {
        super.onStop()
        primaryVideoViewManager.removeAllVideoUnits()
    }
}
