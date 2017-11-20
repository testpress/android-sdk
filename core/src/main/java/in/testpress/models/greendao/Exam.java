package in.testpress.models.greendao;

import android.annotation.SuppressLint;

import org.greenrobot.greendao.annotation.*;

import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT. Enable "keep" sections if you want to edit.

/**
 * Entity mapped to table "EXAM".
 */
@Entity
public class Exam {
    private String totalMarks;
    private String url;

    @Id
    private Long id;
    private Integer attemptsCount;
    private Integer pausedAttemptsCount;
    private String title;
    private String description;
    private String course_category;
    private java.util.Date startDate;
    private java.util.Date endDate;
    private String duration;
    private Integer numberOfQuestions;
    private String negativeMarks;
    private String markPerQuestion;
    private Integer templateType;
    private Boolean allowRetake;
    private Boolean allowPdf;
    private Boolean showAnswers;
    private Integer maxRetakes;
    private String attemptsUrl;
    private String deviceAccessControl;
    private Integer commentsCount;
    private String slug;
    private String selectedLanguage;

    @Generated
    public Exam() {
    }

    public Exam(Long id) {
        this.id = id;
    }

    @Generated
    public Exam(String totalMarks, String url, Long id, Integer attemptsCount, Integer pausedAttemptsCount, String title, String description, String course_category, java.util.Date startDate, java.util.Date endDate, String duration, Integer numberOfQuestions, String negativeMarks, String markPerQuestion, Integer templateType, Boolean allowRetake, Boolean allowPdf, Boolean showAnswers, Integer maxRetakes, String attemptsUrl, String deviceAccessControl, Integer commentsCount, String slug, String selectedLanguage) {
        this.totalMarks = totalMarks;
        this.url = url;
        this.id = id;
        this.attemptsCount = attemptsCount;
        this.pausedAttemptsCount = pausedAttemptsCount;
        this.title = title;
        this.description = description;
        this.course_category = course_category;
        this.startDate = startDate;
        this.endDate = endDate;
        this.duration = duration;
        this.numberOfQuestions = numberOfQuestions;
        this.negativeMarks = negativeMarks;
        this.markPerQuestion = markPerQuestion;
        this.templateType = templateType;
        this.allowRetake = allowRetake;
        this.allowPdf = allowPdf;
        this.showAnswers = showAnswers;
        this.maxRetakes = maxRetakes;
        this.attemptsUrl = attemptsUrl;
        this.deviceAccessControl = deviceAccessControl;
        this.commentsCount = commentsCount;
        this.slug = slug;
        this.selectedLanguage = selectedLanguage;
    }

    public String getTotalMarks() {
        return totalMarks;
    }

    public void setTotalMarks(String totalMarks) {
        this.totalMarks = totalMarks;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getAttemptsCount() {
        return attemptsCount;
    }

    public void setAttemptsCount(Integer attemptsCount) {
        this.attemptsCount = attemptsCount;
    }

    public Integer getPausedAttemptsCount() {
        return pausedAttemptsCount;
    }

    public void setPausedAttemptsCount(Integer pausedAttemptsCount) {
        this.pausedAttemptsCount = pausedAttemptsCount;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCourse_category() {
        return course_category;
    }

    public void setCourse_category(String course_category) {
        this.course_category = course_category;
    }

    public java.util.Date getStartDate() {
        return startDate;
    }

    public void setStartDate(java.util.Date startDate) {
        this.startDate = startDate;
    }

    public java.util.Date getEndDate() {
        return endDate;
    }

    public void setEndDate(java.util.Date endDate) {
        this.endDate = endDate;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public Integer getNumberOfQuestions() {
        return numberOfQuestions;
    }

    public void setNumberOfQuestions(Integer numberOfQuestions) {
        this.numberOfQuestions = numberOfQuestions;
    }

    public String getNegativeMarks() {
        return negativeMarks;
    }

    public void setNegativeMarks(String negativeMarks) {
        this.negativeMarks = negativeMarks;
    }

    public String getMarkPerQuestion() {
        return markPerQuestion;
    }

    public void setMarkPerQuestion(String markPerQuestion) {
        this.markPerQuestion = markPerQuestion;
    }

    public Integer getTemplateType() {
        return templateType;
    }

    public void setTemplateType(Integer templateType) {
        this.templateType = templateType;
    }

    public Boolean getAllowRetake() {
        return allowRetake;
    }

    public void setAllowRetake(Boolean allowRetake) {
        this.allowRetake = allowRetake;
    }

    public Boolean getAllowPdf() {
        return allowPdf;
    }

    public void setAllowPdf(Boolean allowPdf) {
        this.allowPdf = allowPdf;
    }

    public Boolean getShowAnswers() {
        return showAnswers;
    }

    public void setShowAnswers(Boolean showAnswers) {
        this.showAnswers = showAnswers;
    }

    public Integer getMaxRetakes() {
        return maxRetakes;
    }

    public void setMaxRetakes(Integer maxRetakes) {
        this.maxRetakes = maxRetakes;
    }

    public String getAttemptsUrl() {
        return attemptsUrl;
    }

    public void setAttemptsUrl(String attemptsUrl) {
        this.attemptsUrl = attemptsUrl;
    }

    public String getDeviceAccessControl() {
        return deviceAccessControl;
    }

    public void setDeviceAccessControl(String deviceAccessControl) {
        this.deviceAccessControl = deviceAccessControl;
    }

    public Integer getCommentsCount() {
        return commentsCount;
    }

    public void setCommentsCount(Integer commentsCount) {
        this.commentsCount = commentsCount;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getSelectedLanguage() {
        return selectedLanguage;
    }

    public void setSelectedLanguage(String selectedLanguage) {
        this.selectedLanguage = selectedLanguage;
    }

    public String getAttemptsFrag() {
        try {
            URL url = new URL(attemptsUrl);
            return url.getFile().substring(1);
        }
        catch (Exception e) {
            return null;
        }
    }
    public String getFormattedStartDate() {
        return formatDate(startDate.toString());
    }
    public String getFormattedEndDate() {
        return formatDate(endDate.toString());
    }
    @SuppressLint("SimpleDateFormat")
    public boolean isEnded() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            if(getEndDate() != null && !getEndDate().toString().isEmpty()) {
                return simpleDateFormat.parse(getEndDate().toString()).before(new Date());
            }
        } catch (ParseException e) {
        }
        return false;
    }
    @SuppressLint("SimpleDateFormat")
    public String formatDate(String inputString) {
        Date date = null;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            if(inputString != null && !inputString.isEmpty()) {
                date = simpleDateFormat.parse(inputString);
                DateFormat dateformat = DateFormat.getDateInstance();
                return dateformat.format(date);
            }
        } catch (ParseException e) {
        }
        return "forever";
    }
    @SuppressLint("SimpleDateFormat")
    public boolean hasStarted() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            if(getStartDate() != null && !getStartDate().toString().isEmpty()) {
                return simpleDateFormat.parse(getStartDate().toString()).before(new Date());
            }
        } catch (ParseException e) {
        }
        return true;
    }
    public String getUrlFrag() {
        try {
            URL fragUrl = new URL(url);
            return fragUrl.getFile().substring(1);
        }
        catch (Exception e) {
            return null;
        }
    }
    public boolean canRetake() {
        return getAllowRetake() &&
                (getAttemptsCount() <= getMaxRetakes() || getMaxRetakes() < 0);
    }

}
