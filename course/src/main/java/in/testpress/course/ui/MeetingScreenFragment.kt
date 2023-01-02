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

        val defaultVideoViewRenderInfo = MobileRTCVideoUnitRenderInfo(0, 0, 100, 100)
        val screenShareUserId = inMeetingService.activeShareUserID()
        if (inMeetingService.inMeetingShareController.isOtherSharing && inMeetingService.isHostUser(screenShareUserId)) {
            primaryVideoViewManager.addShareVideoUnit(screenShareUserId, defaultVideoViewRenderInfo)
        } else {
            primaryVideoViewManager.addActiveVideoUnit(defaultVideoViewRenderInfo)
        }
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
