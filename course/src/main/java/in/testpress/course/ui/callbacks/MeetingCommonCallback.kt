package `in`.testpress.course.ui.callbacks

import `in`.testpress.course.util.SimpleInMeetingListener
import us.zoom.sdk.*

object MeetingCommonCallback: BaseCallback<MeetingCommonCallback.CommonEvent?>() {
    interface CommonEvent : BaseEvent {
        fun onMeetingFail(errorCode: Int, internalErrorCode: Int)
        fun onMeetingStatusChanged(
            meetingStatus: MeetingStatus?,
            errorCode: Int,
            internalErrorCode: Int
        )
        fun onMeetingNeedCloseOtherMeeting(inMeetingEventHandler: InMeetingEventHandler)
        fun onMeetingNeedPasswordOrDisplayName(
            needPassword: Boolean,
            needUsername: Boolean,
            inMeetingEventHandler: InMeetingEventHandler
        )
        fun onJoinWebinarNeedUserNameAndEmail(inMeetingEventHandler: InMeetingEventHandler)
    }

    private var serviceListener: MeetingServiceListener = object : MeetingServiceListener {
        override fun onMeetingStatusChanged(
            meetingStatus: MeetingStatus,
            errorCode: Int,
            internalErrorCode: Int
        ) {
            for (event in callbacks) {
                event?.onMeetingStatusChanged(meetingStatus, errorCode, internalErrorCode)
            }
        }

        override fun onMeetingParameterNotification(meetingParameter: MeetingParameter) {}
    }

    private var commonListener = object : SimpleInMeetingListener() {
        override fun onMeetingFail(errorCode: Int, internalErrorCode: Int) {
            for (event in callbacks) {
                event?.onMeetingFail(errorCode, internalErrorCode)
            }
        }

        override fun onMeetingNeedCloseOtherMeeting(inMeetingEventHandler: InMeetingEventHandler){
            for (event in callbacks) {
                event?.onMeetingNeedCloseOtherMeeting(inMeetingEventHandler)
            }
        }

        override fun onMeetingNeedPasswordOrDisplayName(
            needPassword: Boolean,
            needUsername: Boolean,
            inMeetingEventHandler: InMeetingEventHandler
        ) {
            for (event in callbacks) {
                event?.onMeetingNeedPasswordOrDisplayName(needPassword, needUsername, inMeetingEventHandler)
            }
        }

        override fun onJoinWebinarNeedUserNameAndEmail(inMeetingEventHandler: InMeetingEventHandler) {
            for (event in callbacks) {
                event?.onJoinWebinarNeedUserNameAndEmail(inMeetingEventHandler)
            }
        }


    }

    init {
        ZoomSDK.getInstance().inMeetingService.addListener(commonListener)
        ZoomSDK.getInstance().meetingService.addListener(serviceListener)
    }
}