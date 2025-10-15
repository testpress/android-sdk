package `in`.testpress.course.domain.zoom.callbacks

import us.zoom.sdk.InMeetingShareController.InMeetingShareListener
import us.zoom.sdk.ShareSettingType
import us.zoom.sdk.SharingStatus
import us.zoom.sdk.ZoomSDK
import us.zoom.sdk.ZoomSDKSharingSourceInfo


object MeetingShareCallback : BaseCallback<MeetingShareCallback.ShareEvent?>() {
    interface ShareEvent : BaseEvent {
        fun onSharingStatus(sharingSourceInfo: ZoomSDKSharingSourceInfo)
    }

    private var shareListener: InMeetingShareListener = object : InMeetingShareListener {
        override fun onSharingStatus(sharingSourceInfo: ZoomSDKSharingSourceInfo) {
            for (event in callbacks) {
                event?.onSharingStatus(sharingSourceInfo)
            }
        }

        override fun onShareContentChanged(sharingSourceInfo: ZoomSDKSharingSourceInfo) {}
        override fun onShareUserReceivingStatus(shareSourceId: Long) {}
        override fun onShareSettingTypeChanged(p0: ShareSettingType?) {}
    }

    init {
        ZoomSDK.getInstance().inMeetingService.inMeetingShareController.addListener(shareListener)
    }
}
