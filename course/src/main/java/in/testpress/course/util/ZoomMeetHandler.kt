package `in`.testpress.course.util

import `in`.testpress.course.domain.DomainVideoConferenceContent
import `in`.testpress.models.ProfileDetails
import `in`.testpress.util.isEmailValid
import android.content.Context
import android.widget.Toast
import us.zoom.sdk.*
import us.zoom.sdk.MeetingViewsOptions.NO_TEXT_MEETING_ID
import us.zoom.sdk.MeetingViewsOptions.NO_TEXT_PASSWORD

class ZoomMeetHandler(
    val context: Context,
    val videoConference: DomainVideoConferenceContent,
    val profileDetails: ProfileDetails?
) : MeetingServiceListener,
    ZoomSDKInitializeListener, InMeetingServiceListener {

    private lateinit var zoomSDK: ZoomSDK

    fun init() {
        zoomSDK = ZoomSDK.getInstance()
        zoomSDK.initialize(context, this, getInitializationParams())

        if (zoomSDK.isInitialized) {
            registerMeetingServiceListener()
        }
    }

    fun getInitializationParams(): ZoomSDKInitParams {
        val initParams = ZoomSDKInitParams()
        initParams.jwtToken = videoConference.accessToken
        initParams.enableLog = true
        initParams.logSize = 50
        initParams.domain = "zoom.us"
        return initParams
    }

    fun goToMeet() {
        if (!zoomSDK.isInitialized) {
            val errorMessage =
                "Zoom Meet has not been initialized yet. Please click start class button after a minute"
            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
            return
        }

        zoomSDK.meetingService?.let { meetingService ->
            if (meetingService.meetingStatus == MeetingStatus.MEETING_STATUS_IDLE) {
                startMeeting()
            } else {
                meetingService.returnToMeeting(context)
            }
        }
    }

    private fun registerMeetingServiceListener() {
        val meetingService = zoomSDK.meetingService
        meetingService?.addListener(this)
        zoomSDK.inMeetingService.addListener(this)
    }

    override fun onMeetingStatusChanged(
        meetingStatus: MeetingStatus?,
        errorCode: Int,
        internalErrorCode: Int
    ) {
        if (meetingStatus == MeetingStatus.MEETING_STATUS_FAILED && errorCode == MeetingError.MEETING_ERROR_CLIENT_INCOMPATIBLE) {
            Toast.makeText(context, "Version of ZoomSDK is too low!", Toast.LENGTH_LONG).show()
        }

        if (meetingStatus == MeetingStatus.MEETING_STATUS_FAILED) {
            Toast.makeText(
                context,
                "Could not join the meeting. Possibly meeting expired or ended",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onZoomSDKInitializeResult(errorCode: Int, internalErrorCode: Int) {
        if (errorCode != ZoomError.ZOOM_ERROR_SUCCESS) {
            Toast.makeText(
                context,
                "Failed to initialize Zoom Meeting. Error: $errorCode, internalErrorCode=$internalErrorCode",
                Toast.LENGTH_LONG
            ).show()
        } else {
            registerMeetingServiceListener()
        }
    }

    override fun onZoomAuthIdentityExpired() {
        Toast.makeText(
            context,
            "Please refresh the page(By touching and pulling down in the screen) and again click start class",
            Toast.LENGTH_LONG
        ).show()
        return
    }

    fun removeListeners() {
        if (zoomSDK.isInitialized) {
            val meetingService = zoomSDK.meetingService
            meetingService.removeListener(this)
        }
    }

    fun startMeeting() {
        val meetingService = zoomSDK.meetingService
        val ret = meetingService.joinMeetingWithParams(
            context,
            getMeetingParameters(),
            getMeetingOptions()
        )
    }

    private fun getMeetingOptions(): JoinMeetingOptions {
        val options = JoinMeetingOptions()
        options.no_driving_mode = true
        options.no_titlebar = false
        options.no_meeting_error_message = true
        options.meeting_views_options = NO_TEXT_PASSWORD + NO_TEXT_MEETING_ID
        options.no_bottom_toolbar = false
        options.no_webinar_register_dialog = profileDetails != null && profileDetails.email.isEmailValid()
        options.no_invite = true
        return options
    }

    private fun getMeetingParameters(): JoinMeetingParams {
        val params = JoinMeetingParams()
        params.meetingNo = videoConference.conferenceId
        params.password = videoConference.password
        return params
    }

    override fun onMeetingActiveVideo(p0: Long) {
    }

    override fun onFreeMeetingReminder(p0: Boolean, p1: Boolean, p2: Boolean) {
    }

    override fun onJoinWebinarNeedUserNameAndEmail(inMeetingEventHandler: InMeetingEventHandler) {
        if (profileDetails != null && profileDetails.email.isEmailValid()) {
            val name = profileDetails.displayName ?: profileDetails.username
            inMeetingEventHandler.setRegisterWebinarInfo(name, profileDetails.email, false)
        }
    }

    override fun onActiveVideoUserChanged(p0: Long) {
    }

    override fun onActiveSpeakerVideoUserChanged(p0: Long) {
    }

    override fun onChatMessageReceived(p0: InMeetingChatMessage?) {
    }

    override fun onUserNetworkQualityChanged(p0: Long) {
    }

    override fun onMeetingUserJoin(p0: MutableList<Long>?) {
    }

    override fun onRecordingStatus(p0: InMeetingServiceListener.RecordingStatus?) {
         
    }

    override fun onMeetingUserLeave(p0: MutableList<Long>?) {
    }

    override fun onMeetingFail(p0: Int, p1: Int) {
    }

    override fun onFreeMeetingUpgradeToProMeeting() {
         
    }

    override fun onClosedCaptionReceived(p0: String?) {
         
    }

    override fun onFreeMeetingNeedToUpgrade(p0: FreeMeetingNeedUpgradeType?, p1: String?) {
         
    }

    override fun onUserAudioTypeChanged(p0: Long) {
    }

    override fun onFreeMeetingUpgradeToGiftFreeTrialStop() {
         
    }

    override fun onMyAudioSourceTypeChanged(p0: Int) {
    }

    override fun onSilentModeChanged(p0: Boolean) {
    }

    override fun onFreeMeetingUpgradeToGiftFreeTrialStart() {
         
    }

    override fun onMeetingCoHostChanged(p0: Long) {
    }

    override fun onLowOrRaiseHandStatusChanged(p0: Long, p1: Boolean) {
    }

    override fun onSinkAttendeeChatPriviledgeChanged(p0: Int) {
    }

    override fun onMeetingUserUpdated(p0: Long) {
    }

    override fun onMeetingSecureKeyNotification(p0: ByteArray?) {
    }

    override fun onMeetingNeedColseOtherMeeting(p0: InMeetingEventHandler?) {
    }

    override fun onMicrophoneStatusError(p0: InMeetingAudioController.MobileRTCMicrophoneError?) {
    }

    override fun onHostAskStartVideo(p0: Long) {
    }

    override fun onSinkAllowAttendeeChatNotification(p0: Int) {
    }

    override fun onWebinarNeedRegister() {

    }

    override fun onSpotlightVideoChanged(p0: Boolean) {
    }

    override fun onMeetingHostChanged(p0: Long) {
    }

    override fun onMeetingLeaveComplete(p0: Long) {
    }

    override fun onHostAskUnMute(p0: Long) {
    }

    override fun onUserAudioStatusChanged(p0: Long) {
    }

    override fun onUserAudioStatusChanged(p0: Long, p1: InMeetingServiceListener.AudioStatus?) {
         
    }

    override fun onUserNameChanged(p0: Long, p1: String?) {
    }

    override fun onMeetingNeedPasswordOrDisplayName(
        p0: Boolean,
        p1: Boolean,
        p2: InMeetingEventHandler?
    ) {
    }

    override fun onUserVideoStatusChanged(p0: Long) {
    }

    override fun onUserVideoStatusChanged(p0: Long, p1: InMeetingServiceListener.VideoStatus?) {
         
    }
}