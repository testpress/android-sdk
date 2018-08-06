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

    public InstituteSettings setDisplayUserEmailOnVideo(boolean displayUserEmailOnVideo) {
        this.displayUserEmailOnVideo = displayUserEmailOnVideo;
        return this;
    }
}
