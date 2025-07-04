package in.testpress.models.greendao;

import org.greenrobot.greendao.annotation.*;

import java.util.List;
import in.testpress.models.greendao.DaoSession;
import org.greenrobot.greendao.DaoException;

import in.testpress.util.StringList;

// THIS CODE IS GENERATED BY greenDAO, EDIT ONLY INSIDE THE "KEEP"-SECTIONS

// KEEP INCLUDES - put your custom includes here
import java.util.ArrayList;
import java.util.Collections;
// KEEP INCLUDES END

/**
 * Entity mapped to table "COURSE".
 */
@Entity(active = true)
public class Course {

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
    private String external_content_link;
    private String external_link_label;
    private boolean childItemsLoaded;
    private Boolean isProduct;
    private Boolean isMyCourse;
    private Integer examsCount;
    private Integer videosCount;
    private Integer htmlContentsCount;
    private Integer attachmentsCount;
    private String expiryDate;

    @Convert(converter = in.testpress.util.StringListConverter.class, columnType = String.class)
    private StringList tags;
    private Boolean allowCustomTestGeneration;
    private Integer maxAllowedViewsPerVideo;

    /** Used to resolve relations */
    @Generated
    private transient DaoSession daoSession;

    /** Used for active entity operations. */
    @Generated
    private transient CourseDao myDao;

    @ToMany(joinProperties = {
        @JoinProperty(name = "id", referencedName = "courseId")
    })
    @OrderBy("order ASC")
    private List<Chapter> chapters;

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
    public Course(Long id, String url, String title, String description, String image, String modified, Long modifiedDate, String contentsUrl, String chaptersUrl, String slug, Integer trophiesCount, Integer chaptersCount, Integer contentsCount, Integer order, Boolean active, String external_content_link, String external_link_label, boolean childItemsLoaded, Boolean isProduct, Boolean isMyCourse, Integer examsCount, Integer videosCount, Integer htmlContentsCount, Integer attachmentsCount, String expiryDate, StringList tags, Boolean allowCustomTestGeneration, Integer maxAllowedViewsPerVideo) {
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
        this.external_content_link = external_content_link;
        this.external_link_label = external_link_label;
        this.childItemsLoaded = childItemsLoaded;
        this.isProduct = isProduct;
        this.isMyCourse = isMyCourse;
        this.examsCount = examsCount;
        this.videosCount = videosCount;
        this.htmlContentsCount = htmlContentsCount;
        this.attachmentsCount = attachmentsCount;
        this.expiryDate = expiryDate;
        this.tags = tags;
        this.allowCustomTestGeneration = allowCustomTestGeneration;
        this.maxAllowedViewsPerVideo = maxAllowedViewsPerVideo;
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

    public String getExternal_content_link() {
        return external_content_link;
    }

    public void setExternal_content_link(String external_content_link) {
        this.external_content_link = external_content_link;
    }

    public String getExternal_link_label() {
        return external_link_label;
    }

    public void setExternal_link_label(String external_link_label) {
        this.external_link_label = external_link_label;
    }

    public boolean getChildItemsLoaded() {
        return childItemsLoaded;
    }

    public void setChildItemsLoaded(boolean childItemsLoaded) {
        this.childItemsLoaded = childItemsLoaded;
    }

    public Boolean getIsProduct() {
        return isProduct;
    }

    public void setIsProduct(Boolean isProduct) {
        this.isProduct = isProduct;
    }

    public Boolean getIsMyCourse() {
        return isMyCourse;
    }

    public void setIsMyCourse(Boolean isMyCourse) {
        this.isMyCourse = isMyCourse;
    }

    public Integer getExamsCount() {
        return examsCount;
    }

    public void setExamsCount(Integer examsCount) {
        this.examsCount = examsCount;
    }

    public Integer getVideosCount() {
        return videosCount;
    }

    public void setVideosCount(Integer videosCount) {
        this.videosCount = videosCount;
    }

    public Integer getHtmlContentsCount() {
        return htmlContentsCount;
    }

    public void setHtmlContentsCount(Integer htmlContentsCount) {
        this.htmlContentsCount = htmlContentsCount;
    }

    public Integer getAttachmentsCount() {
        return attachmentsCount;
    }

    public void setAttachmentsCount(Integer attachmentsCount) {
        this.attachmentsCount = attachmentsCount;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public StringList getTags() {
        return tags;
    }

    public void setTags(StringList tags) {
        this.tags = tags;
    }

    public Boolean getAllowCustomTestGeneration() {
        return allowCustomTestGeneration;
    }

    public void setAllowCustomTestGeneration(Boolean allowCustomTestGeneration) {
        this.allowCustomTestGeneration = allowCustomTestGeneration;
    }

    public Integer getMaxAllowedViewsPerVideo() {
        return maxAllowedViewsPerVideo;
    }

    public void setMaxAllowedViewsPerVideo(Integer maxAllowedViewsPerVideo) {
        this.maxAllowedViewsPerVideo = maxAllowedViewsPerVideo;
    }

    /** To-many relationship, resolved on first access (and after reset). Changes to to-many relations are not persisted, make changes to the target entity. */
    @Generated
    public List<Chapter> getChapters() {
        if (chapters == null) {
            __throwIfDetached();
            ChapterDao targetDao = daoSession.getChapterDao();
            List<Chapter> chaptersNew = targetDao._queryCourse_Chapters(id);
            synchronized (this) {
                if(chapters == null) {
                    chapters = chaptersNew;
                }
            }
        }
        return chapters;
    }

    /** Resets a to-many relationship, making the next get call to query for a fresh result. */
    @Generated
    public synchronized void resetChapters() {
        chapters = null;
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

    public boolean isCourseForRegistration() {

        if (this.external_content_link != null && !this.external_content_link.equals("")) {
            return true;
        }

        return false;
    }

    public boolean hasChapters() {
        return getChapters().size() > 0;
    }

    public List<Chapter> getRootChapters() {
        ChapterDao chapterDao = daoSession.getChapterDao();
        return chapterDao.queryBuilder()
                .where(ChapterDao.Properties.CourseId.eq(getId()), ChapterDao.Properties.ParentId.isNull())
                .orderAsc(ChapterDao.Properties.Order)
                .list();
    }

    public boolean containsTags(List<String> expectedTags) {
        return tags != null && !Collections.disjoint(tags, expectedTags);
    }

    public static List<Course> filterByTags(List<Course> courses, List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return courses;
        }

        ArrayList<Course> filteredCourses = new ArrayList<Course>();
        for (Course course : courses) {
            if (course.containsTags(tags)) {
                filteredCourses.add(course);
            }
        }
        return filteredCourses;
    }

    public static List<Course> excludeCoursesByTags(List<Course> courses, List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return courses;
        }

        ArrayList<Course> filteredCourses = new ArrayList<Course>();
        for (Course course : courses) {
            if (!course.containsTags(tags)) {
                filteredCourses.add(course);
            }
        }
        return filteredCourses;
    }

    // KEEP METHODS END

}
