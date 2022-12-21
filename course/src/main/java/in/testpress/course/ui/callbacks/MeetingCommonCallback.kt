package `in`.testpress.course.ui.callbacks

import `in`.testpress.course.util.SimpleInMeetingListener
import us.zoom.sdk.*

object MeetingCommonCallback: BaseCallback<MeetingCommonCallback.CommonEvent?>() {
    interface CommonEvent : BaseEvent {
        fun onMeetingFail(errorCode: Int, internalErrorCode: Int)
        fun onMeetingLeaveComplete(ret: Long)
        fun onMeetingStatusChanged(
            meetingStatus: MeetingStatus?,
            errorCode: Int,
            internalErrorCode: Int
        )
        fun onChatMessageReceived(inMeetingChatMessage: InMeetingChatMessage)
        fun onMeetingNeedCloseOtherMeeting(inMeetingEventHandler: InMeetingEventHandler)
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

        override fun onMeetingLeaveComplete(ret: Long) {
            for (event in callbacks) {
                event?.onMeetingLeaveComplete(ret)
            }
        }

        override fun onChatMessageReceived(inMeetingChatMessage: InMeetingChatMessage) {
            for (event in callbacks) {
                event?.onChatMessageReceived(inMeetingChatMessage)
            }
        }

        override fun onMeetingNeedCloseOtherMeeting(inMeetingEventHandler: InMeetingEventHandler){
            for (event in callbacks) {
                event?.onMeetingNeedCloseOtherMeeting(inMeetingEventHandler)
            }
        }
    }

    init {
        ZoomSDK.getInstance().inMeetingService.addListener(commonListener)
        ZoomSDK.getInstance().meetingService.addListener(serviceListener)
    }
}