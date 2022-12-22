package `in`.testpress.course.ui

import `in`.testpress.course.R
import `in`.testpress.course.ui.callbacks.MeetingCommonCallback
import `in`.testpress.course.ui.callbacks.MeetingShareCallback
import `in`.testpress.course.ui.callbacks.MeetingUserCallback
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
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
    private lateinit var meetingScreen: View
    private lateinit var primaryVideoView: MobileRTCVideoView
    private lateinit var primaryVideoViewManager: MobileRTCVideoViewManager
    private lateinit var waitingForHostView: View
    private lateinit var waitingRoomView: View
    private lateinit var connectingView: View
    private var isHostSharingScreen = false
    private var isMeetingFailed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.setWindowFullScreen()
        zoomSDK = ZoomSDK.getInstance()
        if (zoomSDK.meetingService == null || zoomSDK.inMeetingService == null) {
            finish()
            return
        }
        inMeetingService = zoomSDK.inMeetingService
        meetingService = zoomSDK.meetingService

        setContentView(R.layout.activity_custom_meeting)

        waitingForHostView = findViewById(R.id.waiting_for_host)
        waitingRoomView = findViewById(R.id.waitingRoom)
        connectingView = findViewById(R.id.connecting)
        meetingScreenContainer = findViewById(R.id.meetingViewContainer)
        meetingScreen = layoutInflater.inflate(R.layout.meeting_screen, null) as View
        meetingScreenContainer.addView(
            meetingScreen,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
        primaryVideoView = meetingScreen.findViewById<View>(R.id.primaryMeetingView) as MobileRTCVideoView
        primaryVideoViewManager = primaryVideoView.videoViewManager

        registerCallbackListener()
    }

    private fun setWindowFullScreen() {
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun registerCallbackListener() {
        MeetingCommonCallback.addListener(this)
        MeetingUserCallback.addListener(this)
        MeetingShareCallback.addListener(this)
    }

    override fun onShareActiveUser(userId: Long) {
        if (inMeetingService.isHostUser(userId) || userId == 0L && isHostSharingScreen) {
            checkShowMeetingLayout()
        }
    }

    override fun onMeetingStatusChanged(
        meetingStatus: MeetingStatus?,
        errorCode: Int,
        internalErrorCode: Int
    ) {
        checkShowMeetingLayout()
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

    private fun checkShowMeetingLayout() {
        primaryVideoViewManager = primaryVideoView.videoViewManager
        val newLayoutType: Int = getMeetingLayoutType()
        if (currentLayoutType != newLayoutType) {
            removeOldLayout(currentLayoutType)
            currentLayoutType = newLayoutType
            addNewLayout(newLayoutType)
        }
    }

    private fun removeOldLayout(type: Int){
        if (type == LAYOUT_TYPE_WAITHOST) {
            waitingForHostView.visibility = View.GONE
            meetingScreenContainer.visibility = View.VISIBLE
        } else if (type == LAYOUT_TYPE_IN_WAIT_ROOM) {
            waitingRoomView.visibility = View.GONE
            meetingScreenContainer.visibility = View.VISIBLE
        } else if (type == LAYOUT_TYPE_CONNECTING){
            connectingView.visibility = View.GONE
            meetingScreenContainer.visibility = View.VISIBLE
        }
    }

    private fun addNewLayout(type: Int){
        if (type == LAYOUT_TYPE_WAITHOST) {
            waitingForHostView.visibility = View.VISIBLE
            meetingScreenContainer.visibility = View.GONE
        } else if (type == LAYOUT_TYPE_IN_WAIT_ROOM) {
            waitingRoomView.visibility = View.VISIBLE
            meetingScreenContainer.visibility = View.GONE
        } else if (type == LAYOUT_TYPE_CONNECTING) {
            connectingView.visibility = View.VISIBLE
            meetingScreenContainer.visibility = View.GONE
        } else if (type == LAYOUT_TYPE_ATTENDEE) {
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

    override fun onBackPressed() {
        showLeaveMeetingDialog()
    }

    private fun showLeaveMeetingDialog() {
        val builder = AlertDialog.Builder(this, R.style.TestpressAppCompatAlertDialogStyle)
        if (inMeetingService.isMeetingConnected) {
            builder.setMessage("Do you want to leave this meeting?")
            builder.setCancelable(true)
            builder.setPositiveButton("Yes"){ _: DialogInterface, _: Int -> leave()}
            builder.setNegativeButton("No"){ dialog: DialogInterface, i: Int -> dialog.cancel()}
        }
        builder.create().show()
    }

    private fun leave(){
        finish()
        inMeetingService.leaveCurrentMeeting(false)
    }

    override fun onMeetingUserJoin(list: List<Long?>?) {
        checkShowMeetingLayout()
    }

    override fun onMeetingFail(errorCode: Int, internalErrorCode: Int) {
        isMeetingFailed = true
        primaryVideoView.visibility = View.GONE
        connectingView.visibility = View.GONE
        showJoinFailDialog(errorCode)
    }

    private fun showJoinFailDialog(error: Int) {
        val dialog = AlertDialog.Builder(this, R.style.TestpressAppCompatAlertDialogStyle)
            .setCancelable(false)
            .setTitle("Meeting Fail")
            .setMessage("Error: $error")
            .setPositiveButton(
                "Ok"
            ) { _: DialogInterface?, _: Int -> finish() }.create()
        dialog.show()
    }

    override fun onMeetingLeaveComplete(ret: Long) {
        if (!isMeetingFailed) finish()
    }

    override fun onPause() {
        super.onPause()
        primaryVideoView.onPause()
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

    companion object {
        private const val TAG = "CustomMeetingActivity"
    }
}
