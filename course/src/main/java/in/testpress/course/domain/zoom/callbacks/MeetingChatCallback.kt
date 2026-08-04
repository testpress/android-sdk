package `in`.testpress.course.domain.zoom.callbacks

import `in`.testpress.course.util.SimpleInMeetingListener
import us.zoom.sdk.ChatMessageDeleteType
import us.zoom.sdk.InMeetingChatMessage
import us.zoom.sdk.ZoomSDK

object MeetingChatCallback : BaseCallback<MeetingChatCallback.ChatEvent?>() {
    interface ChatEvent : BaseEvent {
        fun onChatMessageReceived(inMeetingChatMessage: InMeetingChatMessage)
        fun onChatMsgDeleteNotification(msgID: String, deleteBy: ChatMessageDeleteType)
    }

    private var chatListener = object : SimpleInMeetingListener() {
        override fun onChatMessageReceived(inMeetingChatMessage: InMeetingChatMessage) {
            for (event in callbacks) {
                event?.onChatMessageReceived(inMeetingChatMessage)
            }
        }

        override fun onChatMsgDeleteNotification(msgID: String, deleteBy: ChatMessageDeleteType) {
            for (event in callbacks) {
                event?.onChatMsgDeleteNotification(msgID, deleteBy)
            }
        }
    }

    @Volatile private var isRegistered = false

    fun register() {
        if (isRegistered) return
        ZoomSDK.getInstance().inMeetingService?.addListener(chatListener)
        isRegistered = true
    }

    fun unregister() {
        if (!isRegistered) return
        ZoomSDK.getInstance().inMeetingService?.removeListener(chatListener)
        isRegistered = false
    }
}

