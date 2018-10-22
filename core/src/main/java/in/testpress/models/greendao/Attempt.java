package in.testpress.models.greendao;

import org.greenrobot.greendao.annotation.*;

import java.util.List;
import in.testpress.models.greendao.DaoSession;
import org.greenrobot.greendao.DaoException;

// THIS CODE IS GENERATED BY greenDAO, EDIT ONLY INSIDE THE "KEEP"-SECTIONS

// KEEP INCLUDES - put your custom includes here
import android.annotation.SuppressLint;
import android.os.Parcel;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
// KEEP INCLUDES END

/**
 * Entity mapped to table "ATTEMPT".
 */
@Entity(active = true)
public class Attempt implements android.os.Parcelable {
    private String url;

    @Id
    private Long id;
    private String date;
    private Integer totalQuestions;
    private String score;
    private String rank;
    private String maxRank;
    private String reviewUrl;
    private String questionsUrl;
    private Integer correctCount;
    private Integer incorrectCount;
    private String lastStartedTime;
    private String remainingTime;
    private String timeTaken;
    private String state;
    private String percentile;
    private Integer speed;
    private Integer accuracy;
    private String percentage;

    /** Used to resolve relations */
    @Generated
    private transient DaoSession daoSession;

    /** Used for active entity operations. */
    @Generated
    private transient AttemptDao myDao;

    @ToMany(joinProperties = {
        @JoinProperty(name = "id", referencedName = "attemptId")
    })
    private List<AttemptSection> sections;

    // KEEP FIELDS - put your custom fields here
    public static final String NOT_STARTED = "Not Started";
    public static final String RUNNING = "Running";
    public static final String COMPLETED = "Completed";
    // KEEP FIELDS END

    @Generated
    public Attempt() {
    }

    public Attempt(Long id) {
        this.id = id;
    }

    @Generated
    public Attempt(String url, Long id, String date, Integer totalQuestions, String score, String rank, String maxRank, String reviewUrl, String questionsUrl, Integer correctCount, Integer incorrectCount, String lastStartedTime, String remainingTime, String timeTaken, String state, String percentile, Integer speed, Integer accuracy, String percentage) {
        this.url = url;
        this.id = id;
        this.date = date;
        this.totalQuestions = totalQuestions;
        this.score = score;
        this.rank = rank;
        this.maxRank = maxRank;
        this.reviewUrl = reviewUrl;
        this.questionsUrl = questionsUrl;
        this.correctCount = correctCount;
        this.incorrectCount = incorrectCount;
        this.lastStartedTime = lastStartedTime;
        this.remainingTime = remainingTime;
        this.timeTaken = timeTaken;
        this.state = state;
        this.percentile = percentile;
        this.speed = speed;
        this.accuracy = accuracy;
        this.percentage = percentage;
    }

    /** called by internal mechanisms, do not call yourself. */
    @Generated
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getAttemptDao() : null;
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

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Integer getTotalQuestions() {
        return totalQuestions;
    }

    public void setTotalQuestions(Integer totalQuestions) {
        this.totalQuestions = totalQuestions;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public String getRank() {
        return rank;
    }

    public void setRank(String rank) {
        this.rank = rank;
    }

    public String getMaxRank() {
        return maxRank;
    }

    public void setMaxRank(String maxRank) {
        this.maxRank = maxRank;
    }

    public String getReviewUrl() {
        return reviewUrl;
    }

    public void setReviewUrl(String reviewUrl) {
        this.reviewUrl = reviewUrl;
    }

    public String getQuestionsUrl() {
        return questionsUrl;
    }

    public void setQuestionsUrl(String questionsUrl) {
        this.questionsUrl = questionsUrl;
    }

    public Integer getCorrectCount() {
        return correctCount;
    }

    public void setCorrectCount(Integer correctCount) {
        this.correctCount = correctCount;
    }

    public Integer getIncorrectCount() {
        return incorrectCount;
    }

    public void setIncorrectCount(Integer incorrectCount) {
        this.incorrectCount = incorrectCount;
    }

    public String getLastStartedTime() {
        return lastStartedTime;
    }

    public void setLastStartedTime(String lastStartedTime) {
        this.lastStartedTime = lastStartedTime;
    }

    public String getRemainingTime() {
        return remainingTime;
    }

    public void setRemainingTime(String remainingTime) {
        this.remainingTime = remainingTime;
    }

    public String getTimeTaken() {
        return timeTaken;
    }

    public void setTimeTaken(String timeTaken) {
        this.timeTaken = timeTaken;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getPercentile() {
        return percentile;
    }

    public void setPercentile(String percentile) {
        this.percentile = percentile;
    }

    public Integer getSpeed() {
        return speed;
    }

    public void setSpeed(Integer speed) {
        this.speed = speed;
    }

    public Integer getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(Integer accuracy) {
        this.accuracy = accuracy;
    }

    public String getPercentage() {
        return percentage;
    }

    public void setPercentage(String percentage) {
        this.percentage = percentage;
    }

    /** To-many relationship, resolved on first access (and after reset). Changes to to-many relations are not persisted, make changes to the target entity. */
    @Generated
    public List<AttemptSection> getSections() {
        if (sections == null) {
            __throwIfDetached();
            AttemptSectionDao targetDao = daoSession.getAttemptSectionDao();
            List<AttemptSection> sectionsNew = targetDao._queryAttempt_Sections(id);
            synchronized (this) {
                if(sections == null) {
                    sections = sectionsNew;
                }
            }
        }
        return sections;
    }

    /** Resets a to-many relationship, making the next get call to query for a fresh result. */
    @Generated
    public synchronized void resetSections() {
        sections = null;
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
    protected Attempt(Parcel in) {
        url = in.readString();
        if (in.readByte() == 0) {
            id = null;
        } else {
            id = in.readLong();
        }
        date = in.readString();
        if (in.readByte() == 0) {
            totalQuestions = null;
        } else {
            totalQuestions = in.readInt();
        }
        score = in.readString();
        rank = in.readString();
        maxRank = in.readString();
        reviewUrl = in.readString();
        questionsUrl = in.readString();
        if (in.readByte() == 0) {
            correctCount = null;
        } else {
            correctCount = in.readInt();
        }
        if (in.readByte() == 0) {
            incorrectCount = null;
        } else {
            incorrectCount = in.readInt();
        }
        lastStartedTime = in.readString();
        remainingTime = in.readString();
        timeTaken = in.readString();
        state = in.readString();
        percentile = in.readString();
        if (in.readByte() == 0) {
            speed = null;
        } else {
            speed = in.readInt();
        }
        if (in.readByte() == 0) {
            accuracy = null;
        } else {
            accuracy = in.readInt();
        }
        percentage = in.readString();
        sections = in.createTypedArrayList(AttemptSection.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(url);
        if (id == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(id);
        }
        dest.writeString(date);
        if (totalQuestions == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(totalQuestions);
        }
        dest.writeString(score);
        dest.writeString(rank);
        dest.writeString(maxRank);
        dest.writeString(reviewUrl);
        dest.writeString(questionsUrl);
        if (correctCount == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(correctCount);
        }
        if (incorrectCount == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(incorrectCount);
        }
        dest.writeString(lastStartedTime);
        dest.writeString(remainingTime);
        dest.writeString(timeTaken);
        dest.writeString(state);
        dest.writeString(percentile);
        if (speed == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(speed);
        }
        if (accuracy == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(accuracy);
        }
        dest.writeString(percentage);
        dest.writeTypedList(getRawSections());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Attempt> CREATOR = new Creator<Attempt>() {
        @Override
        public Attempt createFromParcel(Parcel in) {
            return new Attempt(in);
        }

        @Override
        public Attempt[] newArray(int size) {
            return new Attempt[size];
        }
    };

    public List<AttemptSection> getRawSections() {
        if (myDao == null || sections != null) {
            return sections;
        }
        return getSections();
    }

    public void setSections(List<AttemptSection> sections) {
        this.sections = sections;
    }

    public ReviewAttempt getReviewAttempt() {
        return new ReviewAttempt(getId().longValue(), getTotalQuestions(), getScore(), getRank(),
                getReviewUrl(), getCorrectCount(), getIncorrectCount(), getTimeTaken(),
                getPercentile(), getSpeed(), getAccuracy());
    }

    public String getQuestionsUrlFrag() {
        try {
            URL url = new URL(questionsUrl);
            return url.getFile().substring(1);
        } catch (MalformedURLException e) {
            return null;
        }
    }

    @SuppressLint("SimpleDateFormat")
    public String formatDate(String inputString) {
        Date date = null;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            date = simpleDateFormat.parse(inputString);
            DateFormat dateformat = DateFormat.getDateInstance();
            return dateformat.format(date);
        } catch (ParseException e) {
        }
        return null;
    }

    public String getShortDate() {
        return formatShortDate(date);
    }

    @SuppressLint("SimpleDateFormat")
    public String formatShortDate(String inputString) {
        Date date = null;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            date = simpleDateFormat.parse(inputString);
            simpleDateFormat = new SimpleDateFormat("dd MMM");
            String dateMonth = simpleDateFormat.format(date);
            simpleDateFormat = new SimpleDateFormat("yy");
            String year = simpleDateFormat.format(date);
            return dateMonth + " '" + year ;
        } catch (ParseException e) {
        }
        return null;
    }

    public String getUrlFrag() {
        try {
            URL fragUrl = new URL(url);
            return fragUrl.getFile().substring(1);
        } catch (Exception e) {
            return null;
        }
    }

    public String getStartUrlFrag() {
        return getUrlFrag() + "start/";
    }

    public String getEndUrlFrag() {
        return getUrlFrag() + "end/";
    }

    public String getHeartBeatUrlFrag() {
        return getUrlFrag() + "heartbeat/";
    }
    // KEEP METHODS END

}
