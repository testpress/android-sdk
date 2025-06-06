
package in.testpress.models;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import in.testpress.util.Assert;
import in.testpress.util.EventsTrackerFacade;

public class InstituteSettings {

    private String baseUrl;
    private boolean showGameFrontend;
    private boolean coursesEnableGamification;
    private boolean commentsVotingEnabled;
    private boolean accessCodeEnabled;
    private boolean screenshotDisabled;
    private boolean bookmarksEnabled;
    private boolean displayUserEmailOnVideo;
    private Boolean questionShareDisabled;
    private boolean disableStudentAnalytics;
    private boolean enableParallelLoginRestriction;
    private Integer maxParallelLogins;
    private Integer lockoutLimit;
    private String cooloffTime;
    private String storeLabel;
    private String appToolbarLogo;
    private String appShareLink;
    private boolean isGrowthHackEnabled;
    private String appShareText;
    private boolean isFacebookEventTrackingEnabled;
    private String facebookAppId;
    private boolean isFirebaseEventTrackingEnabled;
    private boolean isBranchEventTrackingEnabled;
    private long serverTime;
    private String leaderboardLabel;
    private String threatsAndTargetsLabel;
    private boolean isVideoDownloadEnabled;
    private boolean showPDFVertically;
    private String appName;
    private boolean isCustomMeetingUIEnabled;
    private Integer maxAllowedDownloadedVideos;
    private String whiteLabeledHostUrl;
    private String currentPaymentApp;
    private Boolean enableCustomTest;
    private Boolean storeEnabled;
    private Boolean disableStoreInApp;
    private String androidSentryDns;
    private Boolean disableImageFullscreenZoomInExam;
    private Boolean enableOfflineExam;
    private Boolean showOfflineExamEndingAlert;
    private String videoWatermarkType;
    private String videoWatermarkPosition;
    private Boolean useNewDiscountFeat;

    public InstituteSettings(String baseUrl) {
        setBaseUrl(baseUrl);
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public InstituteSettings setBaseUrl(String baseUrl) {
        Assert.assertNotNullAndNotEmpty("BaseUrl must not be null or Empty.", baseUrl);
        this.baseUrl = baseUrl;
        return this;
    }

    public boolean isCoursesFrontend() {
        return showGameFrontend;
    }

    public InstituteSettings setCoursesFrontend(boolean showGameFrontend) {
        this.showGameFrontend = showGameFrontend;
        return this;
    }

    public boolean isCoursesGamificationEnabled() {
        return coursesEnableGamification;
    }

    public InstituteSettings setCoursesGamificationEnabled(boolean coursesEnableGamification) {
        this.coursesEnableGamification = coursesEnableGamification;
        return this;
    }

    public Boolean isCommentsVotingEnabled() {
        return commentsVotingEnabled;
    }

    public InstituteSettings setCommentsVotingEnabled(Boolean commentsVotingEnabled) {
        this.commentsVotingEnabled = commentsVotingEnabled;
        return this;
    }

    public boolean isAccessCodeEnabled() {
        return accessCodeEnabled;
    }

    public InstituteSettings setAccessCodeEnabled(boolean accessCodeEnabled) {
        this.accessCodeEnabled = accessCodeEnabled;
        return this;
    }

    public boolean isScreenshotDisabled() {
        return screenshotDisabled;
    }

    public InstituteSettings setScreenshotDisabled(boolean screenshotDisabled) {
        this.screenshotDisabled = screenshotDisabled;
        return this;
    }

    public boolean isBookmarksEnabled() {
        return bookmarksEnabled;
    }

    public InstituteSettings setBookmarksEnabled(boolean bookmarksEnabled) {
        this.bookmarksEnabled = bookmarksEnabled;
        return this;
    }

    public boolean isDisplayUserEmailOnVideo() {
        return displayUserEmailOnVideo;
    }

    public boolean getDisableStudentAnalytics() {
        return disableStudentAnalytics;
    }

    public void setDisableStudentAnalytics(boolean disableStudentAnalytics) {
        this.disableStudentAnalytics = disableStudentAnalytics;
    }

    public boolean isQuestionShareDisabled() {
        return questionShareDisabled != null ? questionShareDisabled : screenshotDisabled;
    }

    public InstituteSettings setQuestionShareDisabled(boolean questionShareDisabled) {
        this.questionShareDisabled = questionShareDisabled;
        return this;
    }

    public InstituteSettings setDisplayUserEmailOnVideo(boolean displayUserEmailOnVideo) {
        this.displayUserEmailOnVideo = displayUserEmailOnVideo;
        return this;
    }

    public boolean isParallelLoginRestrictionEnabled() {
        return enableParallelLoginRestriction;
    }

    public InstituteSettings setEnableParallelLoginRestriction(boolean enableParallelLoginRestriction) {
        this.enableParallelLoginRestriction = enableParallelLoginRestriction;
        return this;
    }

    public Integer getMaxParallelLogins() {
        return maxParallelLogins;
    }

    public InstituteSettings setMaxParallelLogins(Integer maxParallelLogins) {
        this.maxParallelLogins = maxParallelLogins;
        return this;
    }

    public Integer getLockoutLimit() {
        return lockoutLimit;
    }

    public InstituteSettings setLockoutLimit(Integer lockoutLimit) {
        this.lockoutLimit = lockoutLimit;
        return this;
    }

    public String getCooloffTime() {
        return cooloffTime;
    }

    public InstituteSettings setCooloffTime(String cooloffTime) {
        this.cooloffTime = cooloffTime;
        return this;
    }

    public String getStoreLabel() {
        return storeLabel;
    }

    public InstituteSettings setStoreLabel(String storeLabel) {
        this.storeLabel = storeLabel;
        return this;
    }

    public String getAppToolbarLogo() {
        return appToolbarLogo;
    }

    public InstituteSettings setAppToolbarLogo(String appToolbarLogo) {
        this.appToolbarLogo = appToolbarLogo;
        return this;
    }

    public String getAppShareLink(Context context) {
        if (appShareLink == null) {
            return "https://play.google.com/store/apps/details?id=" + context.getPackageName();
        }
        return appShareLink;
    }

    public InstituteSettings setAppShareLink(String appShareLink) {
        this.appShareLink = appShareLink;
        return this;
    }

    public boolean isGrowthHackEnabled() {
        return isGrowthHackEnabled;
    }

    public void setGrowthHackEnabled(boolean growthHackEnabled) {
        isGrowthHackEnabled = growthHackEnabled;
    }

    private boolean isAppSharedAlready(Context context) {
        SharedPreferences preferences = context.getSharedPreferences("APP_SHARING", Context.MODE_PRIVATE);
        return preferences.getInt("NO_OF_TIMES_SHARED", 0) >= 2;
    }

    public boolean isAppNotSharedAlready(Context context) {
        return !isAppSharedAlready(context);
    }

    public void updateAppSharedStatus(Context context) {
        SharedPreferences preferences = context.getSharedPreferences("APP_SHARING", Context.MODE_PRIVATE);
        int noOfTimesShared = preferences.getInt("NO_OF_TIMES_SHARED", 0);
        preferences.edit().putInt("NO_OF_TIMES_SHARED", noOfTimesShared + 1).apply();
    }

    public String getAppShareText() {
        return appShareText;
    }

    public void setAppShareText(String appShareText) {
        this.appShareText = appShareText;
    }

    public boolean isFacebookEventTrackingEnabled() {
        return (isFacebookEventTrackingEnabled && facebookAppId != null && !facebookAppId.isEmpty());
    }

    public InstituteSettings setIsFacebookEventTrackingEnabled(boolean isFacebookEventTrackingEnabled) {
        this.isFacebookEventTrackingEnabled = isFacebookEventTrackingEnabled;
        return this;
    }

    public String getFacebookAppId() {
        return facebookAppId;
    }

    public InstituteSettings setFacebookAppId(String facebookAppId) {
        this.facebookAppId = facebookAppId;
        return this;
    }

    public InstituteSettings enableFacebookEventTracking(String facebookAppId, Application application){
        if (facebookAppId != null && !facebookAppId.isEmpty()) {
            this.setFacebookAppId(facebookAppId);
            this.setIsFacebookEventTrackingEnabled(true);
            EventsTrackerFacade.Companion.init(application);
        }
        return this;
    }

    public boolean isFirebaseEventTrackingEnabled() {
        return isFirebaseEventTrackingEnabled;
    }

    public InstituteSettings setFirebaseEventTrackingEnabled(boolean firebaseEventTrackingEnabled) {
        isFirebaseEventTrackingEnabled = firebaseEventTrackingEnabled;
        return this;
    }

    public boolean isBranchEventTrackingEnabled() {
        return isBranchEventTrackingEnabled;
    }

    public void setBranchEventTrackingEnabled(boolean branchEventTrackingEnabled) {
        isBranchEventTrackingEnabled = branchEventTrackingEnabled;
    }

    public long getServerTime() {
        return serverTime;
    }

    public InstituteSettings setServerTime(long serverTime) {
        this.serverTime = serverTime;
        return this;
    }

    public String getLeaderboardLabel() {
        if (leaderboardLabel == null) {
            return "Leaderboard";
        }
        return leaderboardLabel;
    }

    public InstituteSettings setLeaderboardLabel(String leaderboardLabel) {
        this.leaderboardLabel = leaderboardLabel;
        return this;
    }

    public String getThreatsAndTargetsLabel() {
        if (threatsAndTargetsLabel == null) {
            return "Threats / Targets";
        }
        return threatsAndTargetsLabel;
    }

    public InstituteSettings setThreatsAndTargetsLabel(String threatsAndTargetsLabel) {
        this.threatsAndTargetsLabel = threatsAndTargetsLabel;
        return this;
    }

    public boolean isVideoDownloadEnabled() {
        return isVideoDownloadEnabled;
    }

    public InstituteSettings setVideoDownloadEnabled(boolean isVideoDownloadEnabled) {
        this.isVideoDownloadEnabled = isVideoDownloadEnabled;
        return this;
    }

    public boolean shouldShowPDFVertically() {
        return showPDFVertically;
    }

    public InstituteSettings setShowPDFVertically(boolean showPDFVertically) {
        this.showPDFVertically = showPDFVertically;
        return this;
    }

    public String getAppName() {
        return appName;
    }

    public InstituteSettings setAppName(String appName) {
        this.appName = appName;
        return this;
    }

    public Boolean getIsCustomMeetingUIEnabled() {
        return this.isCustomMeetingUIEnabled;
    }

    public InstituteSettings setIsCustomMeetingUIEnabled(Boolean customMeetingUIEnabled) {
        this.isCustomMeetingUIEnabled = customMeetingUIEnabled;
        return this;
    }

    public Integer getMaxAllowedDownloadedVideos() {
        return maxAllowedDownloadedVideos;
    }

    public InstituteSettings setMaxAllowedDownloadedVideos(Integer maxAllowedDownloadedVideos){
        this.maxAllowedDownloadedVideos = maxAllowedDownloadedVideos;
        return this;
    }

    public String getWhiteLabeledHostUrl() {
        return whiteLabeledHostUrl;
    }

    public InstituteSettings setWhiteLabeledHostUrl(String whiteLabeledHostUrl) {
        this.whiteLabeledHostUrl = whiteLabeledHostUrl;
        return this;
    }

    public boolean isInstituteUrl(String url) {
        return url != null &&
                (baseUrl != null && url.contains(baseUrl) ||
                        whiteLabeledHostUrl != null && url.contains(whiteLabeledHostUrl));
    }

    public String getCurrentPaymentApp() {
        return currentPaymentApp;
    }

    public InstituteSettings setCurrentPaymentApp(String currentPaymentApp) {
        this.currentPaymentApp = currentPaymentApp;
        return this;
    }

    public Boolean getEnableCustomTest() {
        return enableCustomTest != null && enableCustomTest;
    }

    public InstituteSettings setEnableCustomTest(Boolean enableCustomTest) {
        this.enableCustomTest = enableCustomTest;
        return this;
    }

    public Boolean getStoreEnabled() {
        return storeEnabled != null && storeEnabled;
    }

    public InstituteSettings setStoreEnabled(Boolean storeEnabled) {
        this.storeEnabled = storeEnabled;
        return this;
    }

    public Boolean getDisableStoreInApp() {
        return disableStoreInApp != null && disableStoreInApp;
    }

    public InstituteSettings setDisableStoreInApp(Boolean disableStoreInApp) {
        this.disableStoreInApp = disableStoreInApp;
        return this;
    }

    public String getAndroidSentryDns() {
        return androidSentryDns;
    }

    public InstituteSettings setAndroidSentryDns(String androidSentryDns) {
        this.androidSentryDns = androidSentryDns;
        return this;
    }

    public Boolean getDisableImageFullscreenZoomInExam() {
        return disableImageFullscreenZoomInExam;
    }

    public InstituteSettings setDisableImageFullscreenZoomInExam(Boolean disableImageFullscreenZoomInExam) {
        this.disableImageFullscreenZoomInExam = disableImageFullscreenZoomInExam;
        return this;
    }

    public Boolean isOfflineExamEnabled() {
        return enableOfflineExam != null && enableOfflineExam;
    }

    public InstituteSettings setEnableOfflineExam(Boolean enableOfflineExam) {
        this.enableOfflineExam = enableOfflineExam;
        return this;
    }

    public Boolean getShowOfflineExamEndingAlert() {
        return showOfflineExamEndingAlert != null && showOfflineExamEndingAlert;
    }

    public InstituteSettings setShowOfflineExamEndingAlert(Boolean showOfflineExamEndingAlert) {
        this.showOfflineExamEndingAlert = showOfflineExamEndingAlert;
        return this;
    }

    public String getDomainUrl() {
        return (whiteLabeledHostUrl != null && !whiteLabeledHostUrl.isEmpty()) ? whiteLabeledHostUrl : baseUrl;
    }

    public String getVideoWatermarkType() {
        return videoWatermarkType;
    }

    public InstituteSettings setVideoWatermarkType(String videoWatermarkType) {
        this.videoWatermarkType = videoWatermarkType;
        return this;
    }

    public String getVideoWatermarkPosition() {
        return videoWatermarkPosition;
    }

    public InstituteSettings setVideoWatermarkPosition(String videoWatermarkPosition) {
        this.videoWatermarkPosition = videoWatermarkPosition;
        return this;
    }

    public Boolean getUseNewDiscountFeat() {
        return useNewDiscountFeat != null && useNewDiscountFeat;
    }

    public InstituteSettings setUseNewDiscountFeat(Boolean useNewDiscountFeat) {
        this.useNewDiscountFeat = useNewDiscountFeat;
        return this;
    }
}
