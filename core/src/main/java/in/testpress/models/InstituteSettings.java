package in.testpress.models;

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
}
