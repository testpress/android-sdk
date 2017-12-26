package in.testpress.exam.models;

import android.annotation.SuppressLint;
import android.os.Parcel;
import android.os.Parcelable;

import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

public class Exam implements Parcelable {
    private String totalMarks;
    private String url;
    private Integer id;
    private Integer attemptsCount;
    private Integer pausedAttemptsCount;
    private String title;
    private String description;
    private String course_category;
    private String startDate;
    private String endDate;
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

    // Parcelling part
    public Exam(Parcel parcel){
        totalMarks          = parcel.readString();
        url                 = parcel.readString();
        id                  = parcel.readInt();
        attemptsCount       = parcel.readInt();
        pausedAttemptsCount = parcel.readInt();
        title               = parcel.readString();
        description         = parcel.readString();
        course_category = parcel.readString();
        startDate           = parcel.readString();
        endDate             = parcel.readString();
        duration            = parcel.readString();
        numberOfQuestions   = parcel.readInt();
        negativeMarks       = parcel.readString();
        markPerQuestion     = parcel.readString();
        templateType        = parcel.readInt();
        allowRetake         = parcel.readByte() != 0;
        allowPdf            = parcel.readByte() != 0;
        showAnswers         = parcel.readByte() != 0;
        maxRetakes          = parcel.readInt();
        attemptsUrl         = parcel.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(totalMarks);
        parcel.writeString(url);
        parcel.writeInt(id);
        parcel.writeInt(attemptsCount);
        parcel.writeInt(pausedAttemptsCount);
        parcel.writeString(title);
        parcel.writeString(description);
        parcel.writeString(course_category);
        parcel.writeString(startDate);
        parcel.writeString(endDate);
        parcel.writeString(duration);
        parcel.writeInt(numberOfQuestions);
        parcel.writeString(negativeMarks);
        parcel.writeString(markPerQuestion);
        parcel.writeInt(templateType);
        if (allowRetake == null) {
            parcel.writeByte((byte) (0));
        } else {
            parcel.writeByte((byte) (allowRetake ? 1 : 0));
        }
        if (allowPdf == null) {
            parcel.writeByte((byte) (0));
        } else {
            parcel.writeByte((byte) (allowPdf ? 1 : 0));
        }
        if (showAnswers == null) {
            parcel.writeByte((byte) (0));
        } else {
            parcel.writeByte((byte) (showAnswers ? 1 : 0));
        }
        parcel.writeInt(maxRetakes);
        parcel.writeString(attemptsUrl);
    }

    public static final Creator CREATOR = new Creator() {
        public Exam createFromParcel(Parcel in) {
            return new Exam(in);
        }

        public Exam[] newArray(int size) {
            return new Exam[size];
        }
    };

    /**
     *
     * @return
     * The totalMarks
     */
    public String getTotalMarks() {
        return totalMarks;
    }

    /**
     *
     * @param totalMarks
     * The total_marks
     */
    public void setTotalMarks(String totalMarks) {
        this.totalMarks = totalMarks;
    }

    /**
     *
     * @return
     * The url
     */
    public String getUrl() {
        return url;
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

    /**
     *
     * @param url
     * The url
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     *
     * @return
     * The id
     */
    public Integer getId() {
        return id;
    }

    /**
     *
     * @param id
     * The id
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     *
     * @return
     * The attempts count
     */
    public Integer getAttemptsCount() {
        return attemptsCount;
    }

    /**
     *
     * @param attemptsCount
     * The attempts count
     */
    public void setAttemptsCount(Integer attemptsCount) {
        this.attemptsCount = attemptsCount;
    }

    /**
     *
     * @return
     * Paused attempts count
     */
    public Integer getPausedAttemptsCount() {
        return pausedAttemptsCount;
    }

    /**
     *
     * @param pausedAttemptsCount
     * Paused attempts count
     */
    public void setPausedAttemptsCount(Integer pausedAttemptsCount) {
        this.pausedAttemptsCount = pausedAttemptsCount;
    }
    /**
     *
     * @return
     * The title
     */
    public String getTitle() {
        return title;
    }

    /**
     *
     * @param title
     * The title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     *
     * @return
     * The description
     */
    public String getDescription() {
        return description;
    }

    /**
     *
     * @param description
     * The description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     *
     * @return
     * The course_category
     */
    public String getCourse_category() {
        return course_category;
    }

    /**
     *
     * @param course_category
     * The course_category
     */
    public void setCourse_category(String course_category) {
        this.course_category = course_category;
    }
    /**
     *
     * @return
     * The startDate
     */
    public String getStartDate() {
        return startDate;
    }

    /**
     *
     * @param startDate
     * The start_date
     */
    public void setStartDate(String startDate) {
        this.startDate = startDate;
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

    /**
     *
     * @return
     * The endDate
     */
    public String getEndDate() {
        return endDate;
    }

    public String getFormattedStartDate() {
        return formatDate(startDate);
    }

    public String getFormattedEndDate() {
        return formatDate(endDate);
    }

    /**
     *
     * @param endDate
     * The end_date
     */
    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    /**
     *
     * @return
     * The duration
     */
    public String getDuration() {
        return duration;
    }

    /**
     *
     * @param duration
     * The duration
     */
    public void setDuration(String duration) {
        this.duration = duration;
    }

    /**
     *
     * @return
     * The numberOfQuestions
     */
    public Integer getNumberOfQuestions() {
        return numberOfQuestions;
    }

    public String getNumberOfQuestionsString() {
        return numberOfQuestions.toString();
    }

    /**
     *
     * @param numberOfQuestions
     * The number_of_questions
     */
    public void setNumberOfQuestions(Integer numberOfQuestions) {
        this.numberOfQuestions = numberOfQuestions;
    }

    /**
     *
     * @return
     * The negativeMarks
     */
    public String getNegativeMarks() {
        return negativeMarks;
    }

    /**
     *
     * @param negativeMarks
     * The negative_marks
     */
    public void setNegativeMarks(String negativeMarks) {
        this.negativeMarks = negativeMarks;
    }


    /**
     *
     * @return
     * The mark per question
     */
    public String getMarkPerQuestion() {
        return markPerQuestion;
    }

    /**
     *
     * @param markPerQuestion
     * The mark per question
     */
    public void setMarkPerQuestion(String markPerQuestion) {
        this.markPerQuestion = markPerQuestion;
    }

    /**
     *
     * @return
     * The templateType
     */
    public Integer getTemplateType() {
        return templateType;
    }

    /**
     *
     * @param templateType
     * The template_type
     */
    public void setTemplateType(Integer templateType) {
        this.templateType = templateType;
    }


    /**
     *
     * @return
     * Allow retake
     */
    public Boolean getAllowRetake() {
        return allowRetake;
    }

    /**
     *
     * @param allowRetake
     * Allow retake
     */
    public void setAllowRetake(Boolean allowRetake) {
        this.allowRetake = allowRetake;
    }

    /**
     *
     * @return
     * Allow pdf
     */
    public Boolean getAllowPdf() {
        return allowPdf;
    }

    /**
     *
     * @param allowPdf
     * Allow retake
     */
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

    public void setMaxRetakes(Integer maxRetakes) { this.maxRetakes = maxRetakes; }

    /**
     *
     * @return
     * The startUrl
     */
    public String getAttemptsUrl() {
        return attemptsUrl;
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

    /**
     *
     * @param startUrl
     * The start_url
     */
    public void setAttemptsUrl(String startUrl) {
        this.attemptsUrl = startUrl;
    }

}
