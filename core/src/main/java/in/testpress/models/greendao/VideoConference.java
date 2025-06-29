package in.testpress.models.greendao;

import org.greenrobot.greendao.annotation.*;

// THIS CODE IS GENERATED BY greenDAO, EDIT ONLY INSIDE THE "KEEP"-SECTIONS

// KEEP INCLUDES - put your custom includes here
// KEEP INCLUDES END

/**
 * Entity mapped to table "VIDEO_CONFERENCE".
 */
@Entity
public class VideoConference {
    private String title;
    private String joinUrl;

    @Id
    private Long id;
    private String start;
    private Integer duration;
    private String provider;
    private String conferenceId;
    private String accessToken;
    private String password;
    private Boolean showRecordedVideo;
    private String state;

    // KEEP FIELDS - put your custom fields here
    // KEEP FIELDS END

    @Generated
    public VideoConference() {
    }

    public VideoConference(Long id) {
        this.id = id;
    }

    @Generated
    public VideoConference(String title, String joinUrl, Long id, String start, Integer duration, String provider, String conferenceId, String accessToken, String password, Boolean showRecordedVideo, String state) {
        this.title = title;
        this.joinUrl = joinUrl;
        this.id = id;
        this.start = start;
        this.duration = duration;
        this.provider = provider;
        this.conferenceId = conferenceId;
        this.accessToken = accessToken;
        this.password = password;
        this.showRecordedVideo = showRecordedVideo;
        this.state = state;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getJoinUrl() {
        return joinUrl;
    }

    public void setJoinUrl(String joinUrl) {
        this.joinUrl = joinUrl;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getConferenceId() {
        return conferenceId;
    }

    public void setConferenceId(String conferenceId) {
        this.conferenceId = conferenceId;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Boolean getShowRecordedVideo() {
        return showRecordedVideo;
    }

    public void setShowRecordedVideo(Boolean showRecordedVideo) {
        this.showRecordedVideo = showRecordedVideo;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    // KEEP METHODS - put your custom methods here
    // KEEP METHODS END

}
