package `in`.testpress.course.ui

import `in`.testpress.course.databinding.MeetingScreenBinding
import `in`.testpress.course.domain.zoom.callbacks.MeetingShareCallback
import `in`.testpress.course.domain.zoom.callbacks.MeetingUserCallback
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.core.view.isVisible
import us.zoom.sdk.*


class MeetingScreenFragment : Fragment(), MeetingShareCallback.ShareEvent {
    private lateinit var meetingScreenBinding: MeetingScreenBinding
    private lateinit var inMeetingService: InMeetingService
    private lateinit var primaryVideoViewManager: MobileRTCVideoViewManager
    private lateinit var webCamVideoViewManager: MobileRTCVideoViewManager
    private lateinit var audioController: InMeetingAudioController
    private var optionBarFragment: MeetingOptionBarFragment? = null

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
        audioController = inMeetingService.inMeetingAudioController
        optionBarFragment = meetingScreenBinding.optionBar.getFragment<MeetingOptionBarFragment>()
        setCallbackOnOptionBar()
        renderVideo()
    }

    private fun setCallbackOnOptionBar(){
        optionBarFragment?.setCallback(object: MeetingOptionBarFragment.Companion.MeetingOptionBarCallback {
            override fun onClickChats() {
                val sidebar = meetingScreenBinding.sidebar
                if (sidebar.isVisible){
                    meetingScreenBinding.sidebar.visibility = View.GONE
                }else{
                    meetingScreenBinding.sidebar.visibility = View.VISIBLE
                }
                renderVideo()
                optionBarFragment!!.refreshChatIcon(sidebar.isVisible)
            }

            override fun onClickSpeaker() {
                audioController.loudSpeakerStatus = !audioController.loudSpeakerStatus
                optionBarFragment!!.refreshspeakerIcon(audioController.loudSpeakerStatus)
            }

            override fun onClickHand() {
                inMeetingService.raiseMyHand()
            }
        }
        )
    }

    override fun onLowOrRaiseHandStatusChanged(userId: Long, isRaisedHand: Boolean) {
        optionBarFragment?.refreshHandIcon(!inMeetingService.isMyself(userId) && isRaisedHand)
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
        renderPrimaryVideo()
        renderWebCamVideo()
    }

    private fun renderPrimaryVideo() {
        primaryVideoViewManager.removeAllVideoUnits()
        val screenShareUserId = inMeetingService.activeShareUserID()
        val defaultVideoViewRenderInfo = MobileRTCVideoUnitRenderInfo(0, 0, 100, 100)
        if (isHostSharingScreen(screenShareUserId)) {
            primaryVideoViewManager.addShareVideoUnit(screenShareUserId, defaultVideoViewRenderInfo)
        } else {
            primaryVideoViewManager.addActiveVideoUnit(defaultVideoViewRenderInfo)
        }
    }

    private fun renderWebCamVideo() {
        webCamVideoViewManager.removeAllVideoUnits()

        val screenShareUserId = inMeetingService.activeShareUserID()
        if (isHostSharingScreen(screenShareUserId)) {
            meetingScreenBinding.webCamView.visibility = View.VISIBLE
            val defaultVideoViewRenderInfo = MobileRTCVideoUnitRenderInfo(0, 0, 100, 100).apply {
                is_border_visible = false
                aspect_mode = MobileRTCVideoUnitAspectMode.VIDEO_ASPECT_PAN_AND_SCAN
            }
            webCamVideoViewManager.addAttendeeVideoUnit(
                inMeetingService.activeShareUserID(),
                defaultVideoViewRenderInfo
            )
        } else {
            meetingScreenBinding.webCamView.visibility = View.GONE
        }
    }

    private fun isHostSharingScreen(screenShareUserId: Long): Boolean {
        return inMeetingService.inMeetingShareController.isOtherSharing && inMeetingService.isHostUser(
            screenShareUserId
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

    override fun onMeetingUserJoin(list: List<Long?>?) {}
    override fun onMeetingUserLeave(list: List<Long?>?) {}
    override fun onMeetingLeaveComplete(ret: Long) {}
    override fun onSilentModeChanged(inSilentMode: Boolean) {}
}
