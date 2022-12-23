package `in`.testpress.course.ui

import `in`.testpress.core.TestpressSdk.COURSE_CONTENT_DETAIL_REQUEST_CODE
import `in`.testpress.core.TestpressUserDetails
import `in`.testpress.course.R
import `in`.testpress.course.databinding.ActivityCustomMeetingBinding
import `in`.testpress.course.databinding.MeetingScreenBinding
import `in`.testpress.course.domain.zoom.callbacks.MeetingCommonCallback
import `in`.testpress.course.domain.zoom.callbacks.MeetingShareCallback
import `in`.testpress.course.domain.zoom.callbacks.MeetingUserCallback
import `in`.testpress.models.ProfileDetails
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import androidx.fragment.app.FragmentActivity
import us.zoom.sdk.*


class CustomMeetingActivity : FragmentActivity(), MeetingUserCallback.UserEvent,
    MeetingCommonCallback.CommonEvent, MeetingShareCallback.ShareEvent {

    private var currentLayoutType = -1
    private val LAYOUT_TYPE_CONNECTING = 0
    private val LAYOUT_TYPE_WAITHOST = 1
    private val LAYOUT_TYPE_IN_WAIT_ROOM = 2
    private val LAYOUT_TYPE_ATTENDEE = 3

    private lateinit var zoomSDK: ZoomSDK
    private lateinit var meetingService: MeetingService
    private lateinit var inMeetingService: InMeetingService
    private lateinit var meetingScreenContainer: FrameLayout
    private lateinit var primaryVideoViewManager: MobileRTCVideoViewManager
    private lateinit var audioController: InMeetingAudioController
    private var profileDetails: ProfileDetails? = null
    private var isHostSharingScreen = false
    private var isMeetingFailed = false
    private lateinit var customMeetingBinding: ActivityCustomMeetingBinding
    private lateinit var meetingScreenBinding: MeetingScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setWindowFullScreen()

        customMeetingBinding = ActivityCustomMeetingBinding.inflate(layoutInflater)
        setContentView(customMeetingBinding.rootView)

        initializeZoomSDK()
        initializeProfileDetails()
        initializeMeetingScreen()
        setPrimaryVideoViewManager()
        registerCallbackListener()
    }

    private fun setWindowFullScreen() {
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun initializeZoomSDK(){
        zoomSDK = ZoomSDK.getInstance()
        if (zoomSDK.meetingService == null || zoomSDK.inMeetingService == null) {
            goBack()
            return
        }
        inMeetingService = zoomSDK.inMeetingService
        meetingService = zoomSDK.meetingService
        audioController = inMeetingService.inMeetingAudioController
    }

    private fun initializeProfileDetails(){
        profileDetails = TestpressUserDetails.getInstance().profileDetails
    }

    private fun initializeMeetingScreen(){
        meetingScreenContainer = customMeetingBinding.meetingViewContainer
        meetingScreenBinding = MeetingScreenBinding.inflate(layoutInflater)
        meetingScreenContainer.addView(
            meetingScreenBinding.root,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
    }

    private fun setPrimaryVideoViewManager(){
        primaryVideoViewManager = meetingScreenBinding.primaryMeetingView.videoViewManager
    }

    private fun registerCallbackListener() {
        MeetingCommonCallback.addListener(this)
        MeetingUserCallback.addListener(this)
        MeetingShareCallback.addListener(this)
    }

    override fun onShareActiveUser(userId: Long) {
        if (inMeetingService.isHostUser(userId) || userId == 0L && isHostSharingScreen) {
            checkShowMeetingLayout(true)
        }
    }

    override fun onMeetingStatusChanged(
        meetingStatus: MeetingStatus?,
        errorCode: Int,
        internalErrorCode: Int
    ) {
        val forceRefresh = intent.getBooleanExtra("forceRefresh", false)
        checkShowMeetingLayout(forceRefresh)
    }

    private fun getMeetingLayoutType(): Int {
        return when (meetingService.meetingStatus){
            MeetingStatus.MEETING_STATUS_WAITINGFORHOST -> LAYOUT_TYPE_WAITHOST
            MeetingStatus.MEETING_STATUS_IN_WAITING_ROOM -> LAYOUT_TYPE_IN_WAIT_ROOM
            MeetingStatus.MEETING_STATUS_CONNECTING -> LAYOUT_TYPE_CONNECTING
            MeetingStatus.MEETING_STATUS_INMEETING -> LAYOUT_TYPE_ATTENDEE
            else -> -1
        }
    }

    private fun checkShowMeetingLayout(forceRefresh: Boolean) {
        val newLayoutType: Int = getMeetingLayoutType()
        if (currentLayoutType != newLayoutType || forceRefresh) {
            removeOldLayout(currentLayoutType)
            currentLayoutType = newLayoutType
            addNewLayout(currentLayoutType)
        }
    }

    private fun removeOldLayout(type: Int){
        if (type == LAYOUT_TYPE_WAITHOST) {
            customMeetingBinding.waitingForHost.visibility = View.GONE
            meetingScreenContainer.visibility = View.VISIBLE
        } else if (type == LAYOUT_TYPE_IN_WAIT_ROOM) {
            customMeetingBinding.waitingRoom.visibility = View.GONE
            meetingScreenContainer.visibility = View.VISIBLE
        } else if (type == LAYOUT_TYPE_CONNECTING){
            customMeetingBinding.connecting.visibility = View.GONE
            meetingScreenContainer.visibility = View.VISIBLE
        } else if (type == LAYOUT_TYPE_ATTENDEE){
            primaryVideoViewManager.removeAllVideoUnits()
        }
    }

    private fun addNewLayout(type: Int){
        if (type == LAYOUT_TYPE_WAITHOST) {
            customMeetingBinding.waitingForHost.visibility = View.VISIBLE
            meetingScreenContainer.visibility = View.GONE
        } else if (type == LAYOUT_TYPE_IN_WAIT_ROOM) {
            customMeetingBinding.waitingRoom.visibility = View.VISIBLE
            meetingScreenContainer.visibility = View.GONE
        } else if (type == LAYOUT_TYPE_CONNECTING) {
            customMeetingBinding.connecting.visibility = View.VISIBLE
            meetingScreenContainer.visibility = View.GONE
        } else if (type == LAYOUT_TYPE_ATTENDEE) {
            meetingScreenContainer.visibility = View.VISIBLE
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
            isHostSharingScreen = true
        } else {
            isHostSharingScreen = false
            primaryVideoViewManager.addActiveVideoUnit(defaultVideoViewRenderInfo)
        }
    }

    override fun onChatMessageReceived(inMeetingChatMessage: InMeetingChatMessage) {}
    override fun onShareUserReceivingStatus(userId: Long) {}
    override fun onMeetingNeedCloseOtherMeeting(inMeetingEventHandler: InMeetingEventHandler) {}
    override fun onMeetingUserLeave(list: List<Long?>?) {}
    override fun onSilentModeChanged(inSilentMode: Boolean) {}
    override fun onLowOrRaiseHandStatusChanged(userId: Long, isRaisedHand: Boolean) {}
    override fun onMeetingNeedPasswordOrDisplayName(
        needPassword: Boolean,
        needUsername: Boolean,
        inMeetingEventHandler: InMeetingEventHandler
    ) {
        showUsernameDialog(needPassword, needUsername, inMeetingEventHandler)
    }

    override fun onJoinWebinarNeedUserNameAndEmail(inMeetingEventHandler: InMeetingEventHandler) {

    }

    var dialog: Dialog? = null
    private fun showUsernameDialog(
        needPassword: Boolean,
        needDisplayName: Boolean,
        handler: InMeetingEventHandler
    ) {
        dialog?.dismiss()
        dialog = Dialog(this, R.style.TestpressAppCompatAlertDialogStyle)
        dialog!!.setTitle("Please enter your name")
        dialog!!.setContentView(R.layout.layout_input_username)
        val username: EditText = dialog!!.findViewById(R.id.edit_name)
        username.setText(profileDetails?.displayName ?: "")
        dialog!!.findViewById<Button>(R.id.btn_cancel).setOnClickListener(View.OnClickListener {
            dialog!!.dismiss()

            inMeetingService.leaveCurrentMeeting(true)
        })
        dialog!!.findViewById<Button>(R.id.btn_join).setOnClickListener {
            val userName = username.text.toString()
            if (TextUtils.isEmpty(userName)) {
                dialog!!.dismiss()
                onMeetingNeedPasswordOrDisplayName(needPassword, needDisplayName, handler)
            }
            dialog!!.dismiss()
            handler.setMeetingNamePassword(inMeetingService.meetingPassword, userName, false)
        }

        dialog!!.setCancelable(false)
        dialog!!.setCanceledOnTouchOutside(false)
        dialog!!.show()
        username.requestFocus()
    }

    override fun onMeetingUserJoin(list: List<Long?>?) {
        checkShowMeetingLayout(false)
        audioController.connectAudioWithVoIP()
        audioController.muteMyAudio(true)
    }

    override fun onMeetingFail(errorCode: Int, internalErrorCode: Int) {
        isMeetingFailed = true
        meetingScreenBinding.primaryMeetingView.visibility = View.GONE
        customMeetingBinding.connecting.visibility = View.GONE
        showJoinFailDialog(errorCode)
    }

    private fun showJoinFailDialog(error: Int) {
        val dialog = AlertDialog.Builder(this, R.style.TestpressAppCompatAlertDialogStyle)
            .setCancelable(false)
            .setTitle("Meeting Fail")
            .setMessage("Error: $error")
            .setPositiveButton(
                "Ok"
            ) { _: DialogInterface?, _: Int ->
                goBack()
            }.create()
        dialog.show()
    }

    override fun onMeetingLeaveComplete(ret: Long) {
        if (!isMeetingFailed)
            goBack()
    }

    override fun onResume() {
        super.onResume()
        checkShowMeetingLayout(false)
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

    private fun clearSubscribe() {
        primaryVideoViewManager.removeAllVideoUnits()
    }

    override fun onDestroy() {
        super.onDestroy()
        unRegisterListener()
    }

    private fun unRegisterListener() {
        MeetingUserCallback.removeListener(this)
        MeetingCommonCallback.removeListener(this)
        MeetingShareCallback.removeListener(this)
    }

    private fun goBack(){
        setResult(COURSE_CONTENT_DETAIL_REQUEST_CODE)
        finish()
    }

    companion object {
        private const val TAG = "CustomMeetingActivity"
    }
}
