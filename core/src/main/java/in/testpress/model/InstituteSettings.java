package in.testpress.model;

import in.testpress.util.Assert;

public class InstituteSettings {

    private String baseUrl;
    private Boolean showGameFrontend;
    private Boolean coursesEnableGamification;

    public InstituteSettings(String baseUrl) {
        setBaseUrl(baseUrl);
    }

    public InstituteSettings(String baseUrl, boolean showCoursesFrontend, boolean enableGamification) {
        setBaseUrl(baseUrl);
        this.showGameFrontend = showCoursesFrontend;
        this.coursesEnableGamification = enableGamification;
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

}
