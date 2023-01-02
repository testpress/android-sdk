
package in.testpress.models;

import android.content.Context;
import android.content.SharedPreferences;

import in.testpress.util.Assert;

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

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public Boolean getIsCustomMeetingUIEnabled() {
        return this.isCustomMeetingUIEnabled;
    }

    public void setIsCustomMeetingUIEnabled(Boolean customMeetingUIEnabled) {
        this.isCustomMeetingUIEnabled = customMeetingUIEnabled;
    }
}
