package `in`.testpress.course.domain.zoom.callbacks

import `in`.testpress.course.util.SimpleInMeetingListener
import io.sentry.Sentry
import us.zoom.sdk.ZoomSDK

object MeetingUserCallback: BaseCallback<MeetingUserCallback.UserEvent?>() {
    interface UserEvent : BaseEvent {
        fun onMeetingUserJoin(list: List<Long?>?)
        fun onMeetingUserLeave(list: List<Long?>?)
        fun onMeetingLeaveComplete(ret: Long)
        fun onSilentModeChanged(inSilentMode: Boolean)
        fun onLowOrRaiseHandStatusChanged(userId: Long, isRaisedHand: Boolean)
    }

    private var userListener = object: SimpleInMeetingListener() {
        private fun isCurrentUserHost(): Boolean {
            return try {
                val inMeetingService = ZoomSDK.getInstance().inMeetingService ?: return false
                val myUserId = inMeetingService.myUserID
                inMeetingService.isHostUser(myUserId)
            } catch (e: Exception) {
                Sentry.captureException(e)
                false
            }
        }

        override fun onMeetingUserJoin(list: List<Long>) {
            // Only host needs participant join events. Attendees do not have a participant UI.
            if (!isCurrentUserHost()) return

            if (ZoomSDK.getInstance().meetingService?.meetingStatus == us.zoom.sdk.MeetingStatus.MEETING_STATUS_INMEETING) {
                for (event in callbacks) {
                    event?.onMeetingUserJoin(list)
                }
            }
        }

        override fun onMeetingUserLeave(list: List<Long>) {
            if (!isCurrentUserHost()) return

            if (ZoomSDK.getInstance().meetingService?.meetingStatus == us.zoom.sdk.MeetingStatus.MEETING_STATUS_INMEETING) {
                for (event in callbacks) {
                    event?.onMeetingUserLeave(list)
                }
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

        override fun onMeetingLeaveComplete(ret: Long) {
            for (event in callbacks) {
                event?.onMeetingLeaveComplete(ret)
            }
        }
    }

    @Volatile private var isRegistered = false

    fun register() {
        if (isRegistered) return
        ZoomSDK.getInstance().inMeetingService?.addListener(userListener)
        isRegistered = true
    }

    fun unregister() {
        if (!isRegistered) return
        ZoomSDK.getInstance().inMeetingService?.removeListener(userListener)
        isRegistered = false
    }
}
