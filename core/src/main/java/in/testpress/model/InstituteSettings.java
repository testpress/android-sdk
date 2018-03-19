package in.testpress.model;

import in.testpress.util.Assert;

public class InstituteSettings {

    private String baseUrl;
    private boolean showGameFrontend;
    private boolean coursesEnableGamification;
    private boolean commentsVotingEnabled;
    private boolean accessCodeEnabled;

    public InstituteSettings(String baseUrl) {
        setBaseUrl(baseUrl);
        showGameFrontend = false;
        coursesEnableGamification = false;
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
}
