package `in`.testpress.course.ui.callbacks

import us.zoom.sdk.InMeetingShareController.InMeetingShareListener
import us.zoom.sdk.ShareSettingType
import us.zoom.sdk.SharingStatus
import us.zoom.sdk.ZoomSDK


object MeetingShareCallback: BaseCallback<MeetingShareCallback.ShareEvent?>() {
    interface ShareEvent : BaseEvent {
        fun onShareActiveUser(userId: Long)
        fun onShareUserReceivingStatus(userId: Long)
    }

    private var shareListener: InMeetingShareListener = object : InMeetingShareListener {
        override fun onShareActiveUser(userId: Long) {
            for (event in callbacks) {
                event?.onShareActiveUser(userId)
            }
        }

        override fun onSharingStatus(status: SharingStatus, userId: Long) {}
        override fun onShareUserReceivingStatus(userId: Long) {
            for (event in callbacks) {
                event?.onShareUserReceivingStatus(userId)
            }
        }

        override fun onShareSettingTypeChanged(p0: ShareSettingType?) {}
    }

    init{
        ZoomSDK.getInstance().inMeetingService.inMeetingShareController.addListener(shareListener)
    }
}
