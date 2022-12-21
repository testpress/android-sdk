package `in`.testpress.course.ui.callbacks

import `in`.testpress.course.util.SimpleInMeetingListener
import us.zoom.sdk.ZoomSDK

object MeetingUserCallback: BaseCallback<MeetingUserCallback.UserEvent?>() {
    interface UserEvent : BaseEvent {
        fun onMeetingUserJoin(list: List<Long?>?)
        fun onMeetingUserLeave(list: List<Long?>?)
        fun onSilentModeChanged(inSilentMode: Boolean)
        fun onLowOrRaiseHandStatusChanged(userId: Long, isRaisedHand: Boolean)
    }

    private var userListener = object: SimpleInMeetingListener() {
        override fun onMeetingUserJoin(list: List<Long>) {
            for (event in callbacks) {
                event?.onMeetingUserJoin(list)
            }
        }

        override fun onMeetingUserLeave(list: List<Long>) {
            for (event in callbacks) {
                event?.onMeetingUserLeave(list)
            }
        }

        override fun onSilentModeChanged(inSilentMode: Boolean) {
            for (event in callbacks) {
                event?.onSilentModeChanged(inSilentMode)
            }
        }

        override fun onLowOrRaiseHandStatusChanged(userId: Long, isRaisedHand: Boolean) {
            for (event in callbacks) {
                event?.onLowOrRaiseHandStatusChanged(userId, isRaisedHand)
            }
        }

    }

    init{
        ZoomSDK.getInstance().inMeetingService.addListener(userListener)
    }
}