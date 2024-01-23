package `in`.testpress.course.util


import us.zoom.sdk.*
import us.zoom.sdk.InMeetingAudioController.MobileRTCMicrophoneError
import us.zoom.sdk.InMeetingChatController.MobileRTCWebinarPanelistChatPrivilege
import us.zoom.sdk.InMeetingServiceListener.RecordingStatus

open class SimpleInMeetingListener : InMeetingServiceListener {
    override fun onMeetingNeedPasswordOrDisplayName(
        b: Boolean,
        b1: Boolean,
        inMeetingEventHandler: InMeetingEventHandler
    ) {
    }

    override fun onWebinarNeedRegister(registerUrl: String) {}
    override fun onJoinWebinarNeedUserNameAndEmail(inMeetingEventHandler: InMeetingEventHandler) {}
    override fun onMeetingNeedCloseOtherMeeting(inMeetingEventHandler: InMeetingEventHandler) {}
    override fun onMeetingFail(i: Int, i1: Int) {}
    override fun onMeetingLeaveComplete(l: Long) {}
    override fun onMeetingUserJoin(list: List<Long>) {}
    override fun onMeetingUserLeave(list: List<Long>) {}
    override fun onMeetingUserUpdated(l: Long) {}
    override fun onInMeetingUserAvatarPathUpdated(p0: Long) {}
    override fun onMeetingHostChanged(l: Long) {}
    override fun onMeetingCoHostChanged(l: Long) {}
    override fun onMeetingCoHostChange(userId: Long, isCoHost: Boolean) {}
    override fun onActiveVideoUserChanged(var1: Long) {}
    override fun onActiveSpeakerVideoUserChanged(var1: Long) {}
    override fun onSpotlightVideoChanged(b: Boolean) {}
    override fun onSpotlightVideoChanged(userList: List<Long>) {}
    override fun onUserVideoStatusChanged(
        userId: Long,
        status: InMeetingServiceListener.VideoStatus
    ) {
    }

    override fun onMicrophoneStatusError(mobileRTCMicrophoneError: MobileRTCMicrophoneError) {}
    override fun onUserAudioStatusChanged(
        userId: Long,
        audioStatus: InMeetingServiceListener.AudioStatus
    ) {
    }

    override fun onUserAudioTypeChanged(l: Long) {}
    override fun onMyAudioSourceTypeChanged(i: Int) {}
    override fun onLowOrRaiseHandStatusChanged(l: Long, b: Boolean) {}
    override fun onChatMessageReceived(inMeetingChatMessage: InMeetingChatMessage) {}
    override fun onChatMsgDeleteNotification(msgID: String, deleteBy: ChatMessageDeleteType) {}
    override fun onShareMeetingChatStatusChanged(p0: Boolean) {}
    override fun onUserNetworkQualityChanged(userId: Long) {}
    override fun onSinkMeetingVideoQualityChanged(videoQuality: VideoQuality, userId: Long) {}
    override fun onHostAskUnMute(userId: Long) {}
    override fun onHostAskStartVideo(userId: Long) {}
    override fun onSilentModeChanged(inSilentMode: Boolean) {}
    override fun onFreeMeetingReminder(
        isOrignalHost: Boolean,
        canUpgrade: Boolean,
        isFirstGift: Boolean
    ) {
    }

    override fun onMeetingActiveVideo(userId: Long) {}
    override fun onSinkAttendeeChatPriviledgeChanged(privilege: Int) {}
    override fun onSinkAllowAttendeeChatNotification(privilege: Int) {}
    override fun onSinkPanelistChatPrivilegeChanged(privilege: MobileRTCWebinarPanelistChatPrivilege) {}
    override fun onUserNameChanged(userId: Long, name: String) {}
    override fun onUserNamesChanged(userList: List<Long>) {}
    override fun onFreeMeetingNeedToUpgrade(type: FreeMeetingNeedUpgradeType, gifUrl: String) {}
    override fun onFreeMeetingUpgradeToGiftFreeTrialStart() {}
    override fun onFreeMeetingUpgradeToGiftFreeTrialStop() {}
    override fun onFreeMeetingUpgradeToProMeeting() {}
    override fun onClosedCaptionReceived(message: String, senderId: Long) {}
    override fun onRecordingStatus(status: RecordingStatus) {}
    override fun onLocalRecordingStatus(userId: Long, status: RecordingStatus) {}
    override fun onInvalidReclaimHostkey() {}
    override fun onHostVideoOrderUpdated(orderList: List<Long>) {}
    override fun onFollowHostVideoOrderChanged(bFollow: Boolean) {}
    override fun onPermissionRequested(permissions: Array<String>) {}
    override fun onAllHandsLowered() {}
    override fun onLocalVideoOrderUpdated(localOrderList: List<Long>) {}
    override fun onLocalRecordingPrivilegeRequested(p0: IRequestLocalRecordingPrivilegeHandler?) {}
    override fun onSuspendParticipantsActivities() {}
    override fun onAllowParticipantsStartVideoNotification(p0: Boolean) {}
    override fun onAllowParticipantsRenameNotification(p0: Boolean) {}
    override fun onAllowParticipantsUnmuteSelfNotification(p0: Boolean) {}
    override fun onAllowParticipantsShareWhiteBoardNotification(p0: Boolean) {}
    override fun onMeetingLockStatus(p0: Boolean) {}
    override fun onRequestLocalRecordingPriviligeChanged(p0: LocalRecordingRequestPrivilegeStatus?) {}
}
