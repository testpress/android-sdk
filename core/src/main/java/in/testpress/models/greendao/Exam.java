package in.testpress.models.greendao;

import org.greenrobot.greendao.annotation.*;

import in.testpress.models.greendao.DaoSession;
import org.greenrobot.greendao.DaoException;

// THIS CODE IS GENERATED BY greenDAO, EDIT ONLY INSIDE THE "KEEP"-SECTIONS

// KEEP INCLUDES - put your custom includes here
import android.annotation.SuppressLint;
import android.os.Parcel;
import android.os.Parcelable;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.ArrayList;
import java.util.List;
// KEEP INCLUDES END

/**
 * Entity mapped to table "EXAM".
 */
@Entity(active = true)
public class Exam implements android.os.Parcelable {
    private String totalMarks;
    private String url;

    @Id
    public Long id;
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
    private Boolean variableMarkPerQuestion;
    private Integer passPercentage;

    /** Used to resolve relations */
    @Generated
    private transient DaoSession daoSession;

    /** Used for active entity operations. */
    @Generated
    private transient ExamDao myDao;

    @ToMany(joinProperties = {
        @JoinProperty(name = "id", referencedName = "examId")
    })
    public List<Language> languages;

    // KEEP FIELDS - put your custom fields here
    // KEEP FIELDS END

    @Generated
    public Exam() {
    }

    public Exam(Long id) {
        this.id = id;
    }

    @Generated
    public Exam(String totalMarks, String url, Long id, Integer attemptsCount, Integer pausedAttemptsCount, String title, String description, String course_category, java.util.Date startDate, java.util.Date endDate, String duration, Integer numberOfQuestions, String negativeMarks, String markPerQuestion, Integer templateType, Boolean allowRetake, Boolean allowPdf, Boolean showAnswers, Integer maxRetakes, String attemptsUrl, String deviceAccessControl, Integer commentsCount, String slug, String selectedLanguage, Boolean variableMarkPerQuestion, Integer passPercentage) {
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
        this.variableMarkPerQuestion = variableMarkPerQuestion;
        this.passPercentage = passPercentage;
    }

    /** called by internal mechanisms, do not call yourself. */
    @Generated
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getExamDao() : null;
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

    public Boolean getVariableMarkPerQuestion() {
        return variableMarkPerQuestion;
    }

    public void setVariableMarkPerQuestion(Boolean variableMarkPerQuestion) {
        this.variableMarkPerQuestion = variableMarkPerQuestion;
    }

    public Integer getPassPercentage() {
        return passPercentage;
    }

    public void setPassPercentage(Integer passPercentage) {
        this.passPercentage = passPercentage;
    }

    /** To-many relationship, resolved on first access (and after reset). Changes to to-many relations are not persisted, make changes to the target entity. */
    @Generated
    public List<Language> getLanguages() {
        if (languages == null) {
            __throwIfDetached();
            LanguageDao targetDao = daoSession.getLanguageDao();
            List<Language> languagesNew = targetDao._queryExam_Languages(id);
            synchronized (this) {
                if(languages == null) {
                    languages = languagesNew;
                }
            }
        }
        return languages;
    }

    /** Resets a to-many relationship, making the next get call to query for a fresh result. */
    @Generated
    public synchronized void resetLanguages() {
        languages = null;
    }

    /**
    * Convenient call for {@link org.greenrobot.greendao.AbstractDao#delete(Object)}.
    * Entity must attached to an entity context.
    */
    @Generated
    public void delete() {
        __throwIfDetached();
        myDao.delete(this);
    }

    /**
    * Convenient call for {@link org.greenrobot.greendao.AbstractDao#update(Object)}.
    * Entity must attached to an entity context.
    */
    @Generated
    public void update() {
        __throwIfDetached();
        myDao.update(this);
    }

    /**
    * Convenient call for {@link org.greenrobot.greendao.AbstractDao#refresh(Object)}.
    * Entity must attached to an entity context.
    */
    @Generated
    public void refresh() {
        __throwIfDetached();
        myDao.refresh(this);
    }

    @Generated
    private void __throwIfDetached() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
    }

    // KEEP METHODS - put your custom methods here
    protected Exam(Parcel in) {
        totalMarks = in.readString();
        url = in.readString();
        id = in.readByte() == 0x00 ? null : in.readLong();
        attemptsCount = in.readByte() == 0x00 ? null : in.readInt();
        pausedAttemptsCount = in.readByte() == 0x00 ? null : in.readInt();
        title = in.readString();
        description = in.readString();
        course_category = in.readString();
        startDate = (java.util.Date) in.readValue(java.util.Date.class.getClassLoader());
        endDate = (java.util.Date) in.readValue(java.util.Date.class.getClassLoader());
        duration = in.readString();
        numberOfQuestions = in.readByte() == 0x00 ? null : in.readInt();
        negativeMarks = in.readString();
        markPerQuestion = in.readString();
        templateType = in.readByte() == 0x00 ? null : in.readInt();
        byte allowRetakeVal = in.readByte();
        allowRetake = allowRetakeVal == 0x02 ? null : allowRetakeVal != 0x00;
        byte allowPdfVal = in.readByte();
        allowPdf = allowPdfVal == 0x02 ? null : allowPdfVal != 0x00;
        byte showAnswersVal = in.readByte();
        showAnswers = showAnswersVal == 0x02 ? null : showAnswersVal != 0x00;
        maxRetakes = in.readByte() == 0x00 ? null : in.readInt();
        attemptsUrl = in.readString();
        deviceAccessControl = in.readString();
        commentsCount = in.readByte() == 0x00 ? null : in.readInt();
        slug = in.readString();
        selectedLanguage = in.readString();
        byte variableMarkPerQuestionVal = in.readByte();
        variableMarkPerQuestion = variableMarkPerQuestionVal == 0x02 ? null : variableMarkPerQuestionVal != 0x00;
        passPercentage = in.readByte() == 0x00 ? null : in.readInt();
        if (in.readByte() == 0x01) {
            languages = new ArrayList<Language>();
            in.readList(languages, Language.class.getClassLoader());
        } else {
            languages = null;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(totalMarks);
        dest.writeString(url);
        if (id == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeLong(id);
        }
        if (attemptsCount == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeInt(attemptsCount);
        }
        if (pausedAttemptsCount == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeInt(pausedAttemptsCount);
        }
        dest.writeString(title);
        dest.writeString(description);
        dest.writeString(course_category);
        dest.writeValue(startDate);
        dest.writeValue(endDate);
        dest.writeString(duration);
        if (numberOfQuestions == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeInt(numberOfQuestions);
        }
        dest.writeString(negativeMarks);
        dest.writeString(markPerQuestion);
        if (templateType == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeInt(templateType);
        }
        if (allowRetake == null) {
            dest.writeByte((byte) (0x02));
        } else {
            dest.writeByte((byte) (allowRetake ? 0x01 : 0x00));
        }
        if (allowPdf == null) {
            dest.writeByte((byte) (0x02));
        } else {
            dest.writeByte((byte) (allowPdf ? 0x01 : 0x00));
        }
        if (showAnswers == null) {
            dest.writeByte((byte) (0x02));
        } else {
            dest.writeByte((byte) (showAnswers ? 0x01 : 0x00));
        }
        if (maxRetakes == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeInt(maxRetakes);
        }
        dest.writeString(attemptsUrl);
        dest.writeString(deviceAccessControl);
        if (commentsCount == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeInt(commentsCount);
        }
        dest.writeString(slug);
        dest.writeString(selectedLanguage);
        if (variableMarkPerQuestion == null) {
            dest.writeByte((byte) (0x02));
        } else {
            dest.writeByte((byte) (variableMarkPerQuestion ? 0x01 : 0x00));
        }
        if (passPercentage == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeInt(passPercentage);
        }
        if (languages == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(languages);
        }
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Exam> CREATOR = new Parcelable.Creator<Exam>() {
        @Override
        public Exam createFromParcel(Parcel in) {
            return new Exam(in);
        }

        @Override
        public Exam[] newArray(int size) {
            return new Exam[size];
        }
    };

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
    // KEEP METHODS END

}
