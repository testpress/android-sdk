package `in`.testpress.course.util

import `in`.testpress.core.TestpressSdk
import `in`.testpress.core.TestpressSdk.COURSE_CONTENT_DETAIL_REQUEST_CODE
import `in`.testpress.course.domain.DomainVideoConferenceContent
import `in`.testpress.course.domain.zoom.callbacks.MeetingCommonCallback
import `in`.testpress.course.ui.CustomMeetingActivity
import `in`.testpress.course.ui.ZoomMeetActivity
import `in`.testpress.models.InstituteSettings
import `in`.testpress.models.ProfileDetails
import `in`.testpress.util.isEmailValid
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.widget.Toast
import io.sentry.Sentry
import io.sentry.SentryLevel
import us.zoom.sdk.*
import us.zoom.sdk.MeetingViewsOptions.NO_TEXT_MEETING_ID
import us.zoom.sdk.MeetingViewsOptions.NO_TEXT_PASSWORD

class ZoomMeetHandler(
    val context: Context,
    val videoConference: DomainVideoConferenceContent,
    val profileDetails: ProfileDetails
) : ZoomSDKInitializeListener, MeetingCommonCallback.CommonEvent {

    private lateinit var zoomSDK: ZoomSDK
    private lateinit var activity: Activity
    private var onInitializeCallback: VideoConferenceInitializeListener? = null
    private var useCustomMeetingUi: Boolean = false
    private var customUiShown: Boolean = false
    private var activityUsableErrorLogged: Boolean = false

    fun init(callback: VideoConferenceInitializeListener) {
        zoomSDK = ZoomSDK.getInstance()
        activity = context as Activity
        this.onInitializeCallback = callback
        zoomSDK.initialize(context, this, getInitializationParams())

        if (zoomSDK.isInitialized) {
            registerMeetingServiceListener()
            setIsCustomizedMeetingUIEnabled()
            configureMeetingUi()
        }
    }

    private fun setIsCustomizedMeetingUIEnabled(){
        val instituteSettings: InstituteSettings =
            TestpressSdk.getTestpressSession(context)!!.instituteSettings
        useCustomMeetingUi = instituteSettings.isCustomMeetingUIEnabled
        zoomSDK.meetingSettingsHelper.isCustomizedMeetingUIEnabled = useCustomMeetingUi
    }

    private fun configureMeetingUi() {
        if (!useCustomMeetingUi) {
            zoomSDK.zoomUIService?.setNewMeetingUI(ZoomMeetActivity::class.java)
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
                returnToMeeting()
            }
        }
    }

    private fun returnToMeeting(){
        if (useCustomMeetingUi) {
            showCustomMeetingUI(true)
        }else{
            zoomSDK.meetingService.returnToMeeting(context)
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
        when (meetingStatus) {
            MeetingStatus.MEETING_STATUS_FAILED -> {
                val message =
                    if (errorCode == MeetingError.MEETING_ERROR_CLIENT_INCOMPATIBLE)
                        "Version of ZoomSDK is too low! Please update your App"
                    else
                        "Could not join the meeting. Possibly meeting expired or ended"
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            }
            MeetingStatus.MEETING_STATUS_CONNECTING -> {
                if (useCustomMeetingUi && !customUiShown && isActivityUsable()) {
                    customUiShown = true
                    showCustomMeetingUI(false)
                }
            }
            else -> {}
        }
    }

    private fun isActivityUsable(): Boolean {
        return try {
            !activity.isFinishing && !activity.isDestroyed
        } catch (t: Throwable) {
            if (!activityUsableErrorLogged) {
                activityUsableErrorLogged = true
                Sentry.captureException(t) { scope ->
                    scope.level = SentryLevel.WARNING
                    scope.setTag("component", "ZoomMeetHandler")
                    scope.setTag("use_custom_meeting_ui", useCustomMeetingUi.toString())
                    scope.setContexts(
                        "Zoom UI Guard",
                        mapOf(
                            "Package Name" to context.packageName,
                            "Conference Content ID" to videoConference.id,
                            "Conference ID" to videoConference.conferenceId,
                        )
                    )
                }
            }
            false
        }
    }

    private fun showCustomMeetingUI(forceRefresh: Boolean){
        val intent = Intent(context, CustomMeetingActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
        intent.putExtra("forceRefresh", forceRefresh)
        intent.putExtra("conferenceTitle", videoConference.title)
        activity.startActivityForResult(intent, COURSE_CONTENT_DETAIL_REQUEST_CODE)
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
            logZoomInitializationFailure(errorCode, internalErrorCode)
            onInitializeCallback?.onFailure()
        } else {
            registerMeetingServiceListener()
            setIsCustomizedMeetingUIEnabled()
            configureMeetingUi()
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
        configureMeetingUi()

        val meetingService = zoomSDK.meetingService
        val ret = meetingService.joinMeetingWithParams(
            context,
            getMeetingParameters(),
            getMeetingOptions()
        )
        zoomSDK.zoomUIService.hideMeetingInviteUrl(true)
    }

    private fun getMeetingOptions(): JoinMeetingOptions {
        val options = JoinMeetingOptions()
        options.no_driving_mode = true
        options.no_titlebar = false
        options.no_meeting_error_message = true
        options.meeting_views_options = NO_TEXT_PASSWORD + NO_TEXT_MEETING_ID
        options.no_bottom_toolbar = false
        options.no_webinar_register_dialog = profileDetails.email.isEmailValid()
        options.no_invite = true
        options.customer_key = "id:${profileDetails.id}"
        return options
    }

    private fun getMeetingParameters(): JoinMeetingParams {
        val params = JoinMeetingParams()
        params.meetingNo = videoConference.conferenceId
        params.password = videoConference.password
        return params
    }

    override fun onJoinWebinarNeedUserNameAndEmail(inMeetingEventHandler: InMeetingEventHandler) {
        if (profileDetails.email.isEmailValid()) {
            val name = profileDetails.displayName ?: profileDetails.username
            inMeetingEventHandler.setRegisterWebinarInfo(name, profileDetails.email, false)
        }
    }

    private fun logZoomInitializationFailure(errorCode: Int, internalErrorCode: Int) {
        val packageName = context.packageName ?: "Package name not available"

        val errorInfoMap = mutableMapOf<String, Any?>(
            "User ID" to profileDetails.id,
            "User Name" to profileDetails.username,
            "User Email" to profileDetails.email,
            "Package Name" to packageName,
            "Video Conference Content ID" to videoConference.id,
            "Video Conference ID" to videoConference.conferenceId,
            "Zoom Error Code" to errorCode,
            "Zoom Internal Error Code" to internalErrorCode,
            "Video Conference AT" to videoConference.accessToken
        )

        logZoomSdkInitializationError(errorInfoMap)
    }

}
