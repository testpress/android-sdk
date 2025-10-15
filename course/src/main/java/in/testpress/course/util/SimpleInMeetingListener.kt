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
    override fun onMeetingCoHostChange(userId: Long, isCoHost: Boolean) {}
    override fun onActiveVideoUserChanged(var1: Long) {}
    override fun onActiveSpeakerVideoUserChanged(var1: Long) {}
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
    override fun onRequestLocalRecordingPrivilegeChanged(p0: LocalRecordingRequestPrivilegeStatus?) {}
    override fun onAICompanionActiveChangeNotice(p0: Boolean) {}
    override fun onParticipantProfilePictureStatusChange(p0: Boolean) {}
    override fun onCloudRecordingStorageFull(p0: Long) {}
    override fun onUVCCameraStatusChange(p0: String?, p1: InMeetingServiceListener.UVCCameraStatus?) {}
    override fun onFocusModeStateChanged(p0: Boolean) {}
    override fun onFocusModeShareTypeChanged(p0: MobileRTCFocusModeShareType?) {}
    override fun onVideoAlphaChannelStatusChanged(p0: Boolean) {}
    override fun onAllowParticipantsRequestCloudRecording(p0: Boolean) {}
    override fun onJoinMeetingNeedUserInfo(handler: IMeetingInputUserInfoHandler) {}
    override fun onWebinarNeedInputScreenName(handler: InMeetingEventHandler) {}
    override fun onSinkJoin3rdPartyTelephonyAudio(telephonySessionId: String) {}
    override fun onUserConfirmToStartArchive(handler: IMeetingArchiveConfirmHandler) {}
    override fun onCameraControlRequestReceived(userId: Long, requestType: CameraControlRequestType, handler: ICameraControlRequestHandler) {}
    override fun onCameraControlRequestResult(userId: Long, result: CameraControlRequestResult) {}
    override fun onCameraControlRequestResult(userId: Long, isGranted: Boolean) {}
    override fun onFileSendStart(fileSender: ZoomSDKFileSender) {}
    override fun onFileReceived(fileReceiver: ZoomSDKFileReceiver) {}
    override fun onFileTransferProgress(transferInfo: ZoomSDKFileTransferInfo) {}
    override fun onMuteOnEntryStatusChange(isEnabled: Boolean) {}
    override fun onMeetingTopicChanged(topic: String) {}
    override fun onMeetingFullToWatchLiveStream(liveStreamUrl: String) {}
    override fun onRobotRelationChanged(userId: Long) {}
    override fun onVirtualNameTagStatusChanged(isEnabled: Boolean, userId: Long) {}
    override fun onVirtualNameTagRosterInfoUpdated(userId: Long) {}
}
