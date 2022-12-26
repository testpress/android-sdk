package `in`.testpress.course.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import `in`.testpress.course.databinding.MeetingScreenBinding
import `in`.testpress.course.domain.zoom.callbacks.MeetingShareCallback
import android.util.Log
import us.zoom.sdk.*


class MeetingScreenFragment : Fragment() , MeetingShareCallback.ShareEvent {
    private lateinit var meetingScreenBinding: MeetingScreenBinding
    private lateinit var zoomSDK: ZoomSDK
    private lateinit var inMeetingService: InMeetingService
    private lateinit var primaryVideoViewManager: MobileRTCVideoViewManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        zoomSDK = ZoomSDK.getInstance()
        inMeetingService = zoomSDK.inMeetingService
        registerListener()
    }

    private fun registerListener(){
        MeetingShareCallback.addListener(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        meetingScreenBinding = MeetingScreenBinding.inflate(inflater, container, false)
        primaryVideoViewManager = meetingScreenBinding.primaryMeetingView.videoViewManager
        updateVideoView()
        return meetingScreenBinding.root
    }

    override fun onShareActiveUser(userId: Long) {

    }

    override fun onShareUserReceivingStatus(userId: Long) {

    }

    override fun onSharingStatus(status: SharingStatus, userId: Long) {
        if (inMeetingService.isHostUser(userId) && status == SharingStatus.Sharing_Other_Share_Begin || status == SharingStatus.Sharing_Other_Share_End){
            updateVideoView()
        }
    }

    private fun updateVideoView() {
        primaryVideoViewManager.removeAllVideoUnits()
        val defaultVideoViewRenderInfo = MobileRTCVideoUnitRenderInfo(0, 0, 100, 100)
        val screenShareUserId = inMeetingService.activeShareUserID()
        if (inMeetingService.inMeetingShareController.isOtherSharing && inMeetingService.isHostUser(
                screenShareUserId
            )
        ) {
            primaryVideoViewManager.addShareVideoUnit(
                inMeetingService.activeShareUserID(),
                defaultVideoViewRenderInfo
            )
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
        clearSubscribe()
    }

    private fun clearSubscribe(){
        primaryVideoViewManager.removeAllVideoUnits()
    }
}