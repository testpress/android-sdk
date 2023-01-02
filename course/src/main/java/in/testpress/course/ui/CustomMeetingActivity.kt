package `in`.testpress.course.ui

import `in`.testpress.core.TestpressSdk.COURSE_CONTENT_DETAIL_REQUEST_CODE
import `in`.testpress.core.TestpressUserDetails
import `in`.testpress.course.R
import `in`.testpress.course.databinding.ActivityCustomMeetingBinding
import `in`.testpress.course.domain.zoom.callbacks.MeetingCommonCallback
import `in`.testpress.course.domain.zoom.callbacks.MeetingUserCallback
import `in`.testpress.models.ProfileDetails
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.TextUtils
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import us.zoom.sdk.*


class CustomMeetingActivity : FragmentActivity(), MeetingUserCallback.UserEvent,
    MeetingCommonCallback.CommonEvent {

    private lateinit var zoomSDK: ZoomSDK
    private lateinit var meetingService: MeetingService
    private lateinit var inMeetingService: InMeetingService
    private lateinit var audioController: InMeetingAudioController
    private var profileDetails: ProfileDetails? = null
    private var isMeetingFailed = false
    private lateinit var customMeetingBinding: ActivityCustomMeetingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setUpWindow()

        customMeetingBinding = ActivityCustomMeetingBinding.inflate(layoutInflater)
        setContentView(customMeetingBinding.rootView)

        initializeZoomSDK()
        initializeProfileDetails()
        registerCallbackListener()
    }

    private fun setUpWindow() {
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun initializeZoomSDK() {
        zoomSDK = ZoomSDK.getInstance()
        if (zoomSDK.meetingService == null || zoomSDK.inMeetingService == null) {
            goBack()
            return
        }
        inMeetingService = zoomSDK.inMeetingService
        meetingService = zoomSDK.meetingService
        audioController = inMeetingService.inMeetingAudioController
    }

    private fun initializeProfileDetails() {
        profileDetails = TestpressUserDetails.getInstance().profileDetails
    }

    private fun registerCallbackListener() {
        MeetingCommonCallback.addListener(this)
        MeetingUserCallback.addListener(this)
    }

    override fun onMeetingStatusChanged(
        meetingStatus: MeetingStatus?,
        errorCode: Int,
        internalErrorCode: Int
    ) {
        showMeetingFragment()
    }

    private fun showMeetingFragment() {
        if (showNoticeScreen()) {
            changeFragment(
                NoticeScreenFragment(
                    meetingService.meetingStatus,
                    intent.getStringExtra("conferenceTitle") ?: "Video conference"
                )
            )
        } else if (showMeetingScreen()) {
            changeFragment(MeetingScreenFragment())
        }
    }

    private fun changeFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(customMeetingBinding.meetingViewContainer.id, fragment).commit()
    }

    private var showNoticeScreen = {
        meetingService.meetingStatus in listOf(
            MeetingStatus.MEETING_STATUS_WAITINGFORHOST,
            MeetingStatus.MEETING_STATUS_IN_WAITING_ROOM,
            MeetingStatus.MEETING_STATUS_CONNECTING,
        )
    }

    private var showMeetingScreen = {
        meetingService.meetingStatus == MeetingStatus.MEETING_STATUS_INMEETING
    }

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
        username.setText(profileDetails?.displayName ?: "")
        dialog!!.findViewById<Button>(R.id.btn_cancel).setOnClickListener {
            dialog!!.dismiss()
            inMeetingService.leaveCurrentMeeting(true)
        }
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
        audioController.connectAudioWithVoIP()
        audioController.muteMyAudio(true)
    }

    override fun onMeetingFail(errorCode: Int, internalErrorCode: Int) {
        isMeetingFailed = true
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

    override fun onBackPressed() {
        if (inMeetingService.isMeetingConnected) {
            showLeaveMeetingDialog()
        } else {
            goBack()
        }
    }

    private fun showLeaveMeetingDialog() {
        val builder = AlertDialog.Builder(this, R.style.TestpressAppCompatAlertDialogStyle)
        if (inMeetingService.isMeetingConnected) {
            builder.setMessage("Do you want to leave this meeting?")
            builder.setCancelable(true)
            builder.setPositiveButton("Yes") { _: DialogInterface, _: Int -> leave() }
            builder.setNegativeButton("No") { dialog: DialogInterface, i: Int -> dialog.cancel() }
        }
        builder.create().show()
    }

    private fun leave() {
        inMeetingService.leaveCurrentMeeting(false)
    }

    override fun onMeetingLeaveComplete(ret: Long) {
        if (!isMeetingFailed) goBack()
    }

    private fun goBack() {
        setResult(COURSE_CONTENT_DETAIL_REQUEST_CODE)
        finish()
    }

    override fun onJoinWebinarNeedUserNameAndEmail(inMeetingEventHandler: InMeetingEventHandler) {}
    override fun onMeetingNeedCloseOtherMeeting(inMeetingEventHandler: InMeetingEventHandler) {}
    override fun onMeetingUserLeave(list: List<Long?>?) {}
    override fun onSilentModeChanged(inSilentMode: Boolean) {}
    override fun onLowOrRaiseHandStatusChanged(userId: Long, isRaisedHand: Boolean) {}

    override fun onResume() {
        super.onResume()
        showMeetingFragment()
    }

    override fun onDestroy() {
        super.onDestroy()
        unRegisterListener()
    }

    private fun unRegisterListener() {
        MeetingUserCallback.removeListener(this)
        MeetingCommonCallback.removeListener(this)
    }
}
