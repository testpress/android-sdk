package in.testpress.models.greendao;

import org.greenrobot.greendao.annotation.*;

import in.testpress.models.greendao.DaoSession;
import org.greenrobot.greendao.DaoException;

// THIS CODE IS GENERATED BY greenDAO, EDIT ONLY INSIDE THE "KEEP"-SECTIONS

// KEEP INCLUDES - put your custom includes here
import in.testpress.core.TestpressSDKDatabase;

import android.content.Context;
import android.os.Parcel;
// KEEP INCLUDES END

/**
 * Entity mapped to table "COURSE_ATTEMPT".
 */
@Entity(active = true)
public class CourseAttempt implements android.os.Parcelable {

    @Id
    private Long id;
    private String type;
    private Integer objectId;
    private String objectUrl;
    private String trophies;
    private Long courseContentId;
    private Long attemptId;

    /** Used to resolve relations */
    @Generated
    private transient DaoSession daoSession;

    /** Used for active entity operations. */
    @Generated
    private transient CourseAttemptDao myDao;

    @ToOne(joinProperty = "courseContentId")
    private Content chapterContent;

    @Generated
    private transient Long chapterContent__resolvedKey;

    @ToOne(joinProperty = "attemptId")
    private Attempt assessment;

    @Generated
    private transient Long assessment__resolvedKey;

    // KEEP FIELDS - put your custom fields here
    public static final String CONTENT_ATTEMPTS_PATH =  "/api/v2.2/content_attempts/";
    public static final String END_EXAM_PATH =  "/end/";
    // KEEP FIELDS END

    @Generated
    public CourseAttempt() {
    }

    public CourseAttempt(Long id) {
        this.id = id;
    }

    @Generated
    public CourseAttempt(Long id, String type, Integer objectId, String objectUrl, String trophies, Long courseContentId, Long attemptId) {
        this.id = id;
        this.type = type;
        this.objectId = objectId;
        this.objectUrl = objectUrl;
        this.trophies = trophies;
        this.courseContentId = courseContentId;
        this.attemptId = attemptId;
    }

    /** called by internal mechanisms, do not call yourself. */
    @Generated
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getCourseAttemptDao() : null;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getObjectId() {
        return objectId;
    }

    public void setObjectId(Integer objectId) {
        this.objectId = objectId;
    }

    public String getObjectUrl() {
        return objectUrl;
    }

    public void setObjectUrl(String objectUrl) {
        this.objectUrl = objectUrl;
    }

    public String getTrophies() {
        return trophies;
    }

    public void setTrophies(String trophies) {
        this.trophies = trophies;
    }

    public Long getCourseContentId() {
        return courseContentId;
    }

    public void setCourseContentId(Long courseContentId) {
        this.courseContentId = courseContentId;
    }

    public Long getAttemptId() {
        return attemptId;
    }

    public void setAttemptId(Long attemptId) {
        this.attemptId = attemptId;
    }

    /** To-one relationship, resolved on first access. */
    @Generated
    public Content getChapterContent() {
        Long __key = this.courseContentId;
        if (chapterContent__resolvedKey == null || !chapterContent__resolvedKey.equals(__key)) {
            __throwIfDetached();
            ContentDao targetDao = daoSession.getContentDao();
            Content chapterContentNew = targetDao.load(__key);
            synchronized (this) {
                chapterContent = chapterContentNew;
            	chapterContent__resolvedKey = __key;
            }
        }
        return chapterContent;
    }

    @Generated
    public void setChapterContent(Content chapterContent) {
        synchronized (this) {
            this.chapterContent = chapterContent;
            courseContentId = chapterContent == null ? null : chapterContent.getId();
            chapterContent__resolvedKey = courseContentId;
        }
    }

    /** To-one relationship, resolved on first access. */
    @Generated
    public Attempt getAssessment() {
        Long __key = this.attemptId;
        if (assessment__resolvedKey == null || !assessment__resolvedKey.equals(__key)) {
            __throwIfDetached();
            AttemptDao targetDao = daoSession.getAttemptDao();
            Attempt assessmentNew = targetDao.load(__key);
            synchronized (this) {
                assessment = assessmentNew;
            	assessment__resolvedKey = __key;
            }
        }
        return assessment;
    }

    @Generated
    public void setAssessment(Attempt assessment) {
        synchronized (this) {
            this.assessment = assessment;
            attemptId = assessment == null ? null : assessment.getId();
            assessment__resolvedKey = attemptId;
        }
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
    protected CourseAttempt(Parcel in) {
        if (in.readByte() == 0) {
            id = null;
        } else {
            id = in.readLong();
        }
        type = in.readString();
        if (in.readByte() == 0) {
            objectId = null;
        } else {
            objectId = in.readInt();
        }
        objectUrl = in.readString();
        trophies = in.readString();
        if (in.readByte() == 0) {
            courseContentId = null;
        } else {
            courseContentId = in.readLong();
        }
        if (in.readByte() == 0) {
            attemptId = null;
        } else {
            attemptId = in.readLong();
        }
        setChapterContent((Content) in.readParcelable(Content.class.getClassLoader()));
        setAssessment((Attempt) in.readParcelable(Attempt.class.getClassLoader()));
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (id == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(id);
        }
        dest.writeString(type);
        if (objectId == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(objectId);
        }
        dest.writeString(objectUrl);
        dest.writeString(trophies);
        if (courseContentId == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(courseContentId);
        }
        if (attemptId == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(attemptId);
        }
        dest.writeParcelable(getRawChapterContent(), flags);
        dest.writeParcelable(getRawAssessment(), flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<CourseAttempt> CREATOR = new Creator<CourseAttempt>() {
        @Override
        public CourseAttempt createFromParcel(Parcel in) {
            return new CourseAttempt(in);
        }

        @Override
        public CourseAttempt[] newArray(int size) {
            return new CourseAttempt[size];
        }
    };

    public void saveInDB(Context context, Content content) {
        CourseAttemptDao courseAttemptDao = TestpressSDKDatabase.getCourseAttemptDao(context);
        AttemptDao attemptDao = TestpressSDKDatabase.getAttemptDao(context);
        Attempt attempt = getRawAssessment();
        attemptDao.insertOrReplace(attempt);
        setAttemptId(attempt.getId());
        setCourseContentId(content.getId());
        courseAttemptDao.insertOrReplace(this);
    }

    public String getEndAttemptUrl() {
        return CONTENT_ATTEMPTS_PATH + id + END_EXAM_PATH;
    }

    public Attempt getRawAssessment() {
        if (myDao == null || assessment != null) {
            return assessment;
        }
        return getAssessment();
    }

    public Content getRawChapterContent() {
        if (myDao == null || chapterContent != null) {
            return chapterContent;
        }
        return getChapterContent();
    }
    // KEEP METHODS END

}
