package `in`.testpress.course.util

import `in`.testpress.course.domain.DomainVideoConferenceContent
import `in`.testpress.course.domain.zoom.callbacks.MeetingCommonCallback
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
) : ZoomSDKInitializeListener, MeetingCommonCallback.CommonEvent {

    private lateinit var zoomSDK: ZoomSDK
    private var onInitializeCallback: VideoConferenceInitializeListener? = null

    fun init(callback: VideoConferenceInitializeListener) {
        zoomSDK = ZoomSDK.getInstance()
        this.onInitializeCallback = callback
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

    fun goToMeet(onInitializeCallback: VideoConferenceInitializeListener) {
        if (zoomSDK.isInitialized) {
            onInitializeCallback.onSuccess()
            joinMeeting()
        } else {
            init(object: VideoConferenceInitializeListener {
                override fun onSuccess() {
                    onInitializeCallback.onSuccess()
                    joinMeeting()
                }

                override fun onFailure() {
                    onInitializeCallback.onFailure()
                }
            })
        }

    }

    private fun joinMeeting() {
        zoomSDK.meetingService?.let { meetingService ->
            if (meetingService.meetingStatus == MeetingStatus.MEETING_STATUS_IDLE) {
                startMeeting()
            } else {
                meetingService.returnToMeeting(context)
            }
        }
    }

    private fun registerMeetingServiceListener() {
        MeetingCommonCallback.addListener(this)
    }

    override fun onMeetingFail(errorCode: Int, internalErrorCode: Int) {

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

    override fun onMeetingNeedCloseOtherMeeting(inMeetingEventHandler: InMeetingEventHandler) {
    }

    override fun onMeetingNeedPasswordOrDisplayName(
        needPassword: Boolean,
        needUsername: Boolean,
        inMeetingEventHandler: InMeetingEventHandler
    ) {
    }

    override fun onZoomSDKInitializeResult(errorCode: Int, internalErrorCode: Int) {
        if (errorCode != ZoomError.ZOOM_ERROR_SUCCESS) {
            Toast.makeText(
                context,
                "Failed to initialize Zoom Meeting. Error: $errorCode, internalErrorCode=$internalErrorCode",
                Toast.LENGTH_LONG
            ).show()
            onInitializeCallback?.onFailure()
        } else {
            registerMeetingServiceListener()
            onInitializeCallback?.onSuccess()
        }
    }

    override fun onZoomAuthIdentityExpired() {
        Toast.makeText(
            context,
            "Please refresh the page(By touching and pulling down in the screen) and again click start class",
            Toast.LENGTH_LONG
        ).show()
        onInitializeCallback?.onFailure()
        return
    }

    fun removeListeners() {
        if (zoomSDK.isInitialized) {
            MeetingCommonCallback.removeListener(this)
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
        if (profileDetails != null) {
            options.customer_key = profileDetails.username
        }
        return options
    }

    private fun getMeetingParameters(): JoinMeetingParams {
        val params = JoinMeetingParams()
        params.meetingNo = videoConference.conferenceId
        params.password = videoConference.password
        return params
    }

    override fun onJoinWebinarNeedUserNameAndEmail(inMeetingEventHandler: InMeetingEventHandler) {
        if (profileDetails != null && profileDetails.email.isEmailValid()) {
            val name = profileDetails.displayName ?: profileDetails.username
            inMeetingEventHandler.setRegisterWebinarInfo(name, profileDetails.email, false)
        }
    }
}
