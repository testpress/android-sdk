package in.testpress.models.greendao;

import org.greenrobot.greendao.annotation.*;

import java.util.List;
import in.testpress.models.greendao.DaoSession;
import org.greenrobot.greendao.DaoException;

// THIS CODE IS GENERATED BY greenDAO, EDIT ONLY INSIDE THE "KEEP"-SECTIONS

// KEEP INCLUDES - put your custom includes here
import android.content.Context;
import android.os.Parcel;

import java.util.List;

import in.testpress.core.TestpressSDKDatabase;
// KEEP INCLUDES END

/**
 * Entity mapped to table "COURSE".
 */
@Entity(active = true)
public class Course implements android.os.Parcelable {

    @Id
    private Long id;
    private String url;
    private String title;
    private String description;
    private String image;
    private String modified;
    private Long modifiedDate;
    private String contentsUrl;
    private String chaptersUrl;
    private String slug;
    private Integer trophiesCount;
    private Integer chaptersCount;
    private Integer contentsCount;
    private Integer order;
    private Boolean active;
    private boolean childItemsLoaded;
    private Boolean isTocUi;

    /** Used to resolve relations */
    @Generated
    private transient DaoSession daoSession;

    /** Used for active entity operations. */
    @Generated
    private transient CourseDao myDao;

    @ToMany(joinProperties = {
        @JoinProperty(name = "id", referencedName = "courseId")
    })
    private List<Content> contents;

    // KEEP FIELDS - put your custom fields here
    // KEEP FIELDS END

    @Generated
    public Course() {
    }

    public Course(Long id) {
        this.id = id;
    }

    @Generated
    public Course(Long id, String url, String title, String description, String image, String modified, Long modifiedDate, String contentsUrl, String chaptersUrl, String slug, Integer trophiesCount, Integer chaptersCount, Integer contentsCount, Integer order, Boolean active, boolean childItemsLoaded, Boolean isTocUi) {
        this.id = id;
        this.url = url;
        this.title = title;
        this.description = description;
        this.image = image;
        this.modified = modified;
        this.modifiedDate = modifiedDate;
        this.contentsUrl = contentsUrl;
        this.chaptersUrl = chaptersUrl;
        this.slug = slug;
        this.trophiesCount = trophiesCount;
        this.chaptersCount = chaptersCount;
        this.contentsCount = contentsCount;
        this.order = order;
        this.active = active;
        this.childItemsLoaded = childItemsLoaded;
        this.isTocUi = isTocUi;
    }

    /** called by internal mechanisms, do not call yourself. */
    @Generated
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getCourseDao() : null;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
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

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getModified() {
        return modified;
    }

    public void setModified(String modified) {
        this.modified = modified;
    }

    public Long getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(Long modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public String getContentsUrl() {
        return contentsUrl;
    }

    public void setContentsUrl(String contentsUrl) {
        this.contentsUrl = contentsUrl;
    }

    public String getChaptersUrl() {
        return chaptersUrl;
    }

    public void setChaptersUrl(String chaptersUrl) {
        this.chaptersUrl = chaptersUrl;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public Integer getTrophiesCount() {
        return trophiesCount;
    }

    public void setTrophiesCount(Integer trophiesCount) {
        this.trophiesCount = trophiesCount;
    }

    public Integer getChaptersCount() {
        return chaptersCount;
    }

    public void setChaptersCount(Integer chaptersCount) {
        this.chaptersCount = chaptersCount;
    }

    public Integer getContentsCount() {
        return contentsCount;
    }

    public void setContentsCount(Integer contentsCount) {
        this.contentsCount = contentsCount;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public boolean getChildItemsLoaded() {
        return childItemsLoaded;
    }

    public void setChildItemsLoaded(boolean childItemsLoaded) {
        this.childItemsLoaded = childItemsLoaded;
    }

    public Boolean getIsTocUi() {
        return isTocUi;
    }

    public void setIsTocUi(Boolean isTocUi) {
        this.isTocUi = isTocUi;
    }

    /** To-many relationship, resolved on first access (and after reset). Changes to to-many relations are not persisted, make changes to the target entity. */
    @Generated
    public List<Content> getContents() {
        if (contents == null) {
            __throwIfDetached();
            ContentDao targetDao = daoSession.getContentDao();
            List<Content> contentsNew = targetDao._queryCourse_Contents(id);
            synchronized (this) {
                if(contents == null) {
                    contents = contentsNew;
                }
            }
        }
        return contents;
    }

    /** Resets a to-many relationship, making the next get call to query for a fresh result. */
    @Generated
    public synchronized void resetContents() {
        contents = null;
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

    protected Course(Parcel in) {
        if (in.readByte() == 0) {
            id = null;
        } else {
            id = in.readLong();
        }
        url = in.readString();
        title = in.readString();
        description = in.readString();
        image = in.readString();
        modified = in.readString();
        if (in.readByte() == 0) {
            modifiedDate = null;
        } else {
            modifiedDate = in.readLong();
        }
        contentsUrl = in.readString();
        chaptersUrl = in.readString();
        slug = in.readString();
        if (in.readByte() == 0) {
            trophiesCount = null;
        } else {
            trophiesCount = in.readInt();
        }
        if (in.readByte() == 0) {
            chaptersCount = null;
        } else {
            chaptersCount = in.readInt();
        }
        if (in.readByte() == 0) {
            contentsCount = null;
        } else {
            contentsCount = in.readInt();
        }
        if (in.readByte() == 0) {
            order = null;
        } else {
            order = in.readInt();
        }
        byte tmpActive = in.readByte();
        active = tmpActive == 0 ? null : tmpActive == 1;
        childItemsLoaded = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (id == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(id);
        }
        dest.writeString(url);
        dest.writeString(title);
        dest.writeString(description);
        dest.writeString(image);
        dest.writeString(modified);
        if (modifiedDate == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(modifiedDate);
        }
        dest.writeString(contentsUrl);
        dest.writeString(chaptersUrl);
        dest.writeString(slug);
        if (trophiesCount == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(trophiesCount);
        }
        if (chaptersCount == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(chaptersCount);
        }
        if (contentsCount == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(contentsCount);
        }
        if (order == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(order);
        }
        dest.writeByte((byte) (active == null ? 0 : active ? 1 : 2));
        dest.writeByte((byte) (childItemsLoaded ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Course> CREATOR = new Creator<Course>() {
        @Override
        public Course createFromParcel(Parcel in) {
            return new Course(in);
        }

        @Override
        public Course[] newArray(int size) {
            return new Course[size];
        }
    };

    public static List<Course> updateCoursesWithLocalVariables(Context context,
                                                               List<Course> courses) {

        CourseDao courseDao = TestpressSDKDatabase.getCourseDao(context);
        for (int i = 0; i < courses.size(); i++) {
            Course course = courses.get(i);
            List<Course> coursesFromDB = courseDao.queryBuilder()
                    .where(CourseDao.Properties.Id.eq(course.getId())).list();

            if (!coursesFromDB.isEmpty()) {
                Course courseFromDB = coursesFromDB.get(0);
                course.setChildItemsLoaded(courseFromDB.getChildItemsLoaded());
            }
        }
        return courses;
    }
    // KEEP METHODS END

}
