package in.testpress.models.greendao;

import org.greenrobot.greendao.annotation.*;

import java.util.List;
import in.testpress.models.greendao.DaoSession;
import org.greenrobot.greendao.DaoException;

// THIS CODE IS GENERATED BY greenDAO, EDIT ONLY INSIDE THE "KEEP"-SECTIONS

// KEEP INCLUDES - put your custom includes here
import android.content.Context;
import org.greenrobot.greendao.query.QueryBuilder;
import org.greenrobot.greendao.query.WhereCondition;

import in.testpress.core.TestpressSDKDatabase;

import java.util.List;

import in.testpress.core.TestpressSDKDatabase;
// KEEP INCLUDES END

/**
 * Entity mapped to table "CHAPTER".
 */
@Entity(active = true)
public class Chapter {

    @Id
    private Long id;
    private String name;
    private String description;
    private String slug;
    private String image;
    private String modified;
    private Long modifiedDate;
    private String courseUrl;
    private String contentUrl;
    private String childrenUrl;
    private String parentSlug;
    private String parentUrl;
    private Boolean leaf;
    private String url;
    private Integer requiredTrophyCount;
    private Boolean isLocked;
    private Integer contentsCount;
    private Integer childrenCount;
    private Boolean active;
    private Integer order;
    private Long courseId;
    private Long parentId;

    /** Used to resolve relations */
    @Generated
    private transient DaoSession daoSession;

    /** Used for active entity operations. */
    @Generated
    private transient ChapterDao myDao;

    @ToMany(joinProperties = {
        @JoinProperty(name = "id", referencedName = "parentId")
    })
    @OrderBy("order ASC")
    private List<Chapter> children;

    @ToMany(joinProperties = {
        @JoinProperty(name = "id", referencedName = "chapterId")
    })
    private List<Content> contents;

    // KEEP FIELDS - put your custom fields here
    // KEEP FIELDS END

    @Generated
    public Chapter() {
    }

    public Chapter(Long id) {
        this.id = id;
    }

    @Generated
    public Chapter(Long id, String name, String description, String slug, String image, String modified, Long modifiedDate, String courseUrl, String contentUrl, String childrenUrl, String parentSlug, String parentUrl, Boolean leaf, String url, Integer requiredTrophyCount, Boolean isLocked, Integer contentsCount, Integer childrenCount, Boolean active, Integer order, Long courseId, Long parentId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.slug = slug;
        this.image = image;
        this.modified = modified;
        this.modifiedDate = modifiedDate;
        this.courseUrl = courseUrl;
        this.contentUrl = contentUrl;
        this.childrenUrl = childrenUrl;
        this.parentSlug = parentSlug;
        this.parentUrl = parentUrl;
        this.leaf = leaf;
        this.url = url;
        this.requiredTrophyCount = requiredTrophyCount;
        this.isLocked = isLocked;
        this.contentsCount = contentsCount;
        this.childrenCount = childrenCount;
        this.active = active;
        this.order = order;
        this.courseId = courseId;
        this.parentId = parentId;
    }

    /** called by internal mechanisms, do not call yourself. */
    @Generated
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getChapterDao() : null;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
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

    public String getCourseUrl() {
        return courseUrl;
    }

    public void setCourseUrl(String courseUrl) {
        this.courseUrl = courseUrl;
    }

    public String getContentUrl() {
        return contentUrl;
    }

    public void setContentUrl(String contentUrl) {
        this.contentUrl = contentUrl;
    }

    public String getChildrenUrl() {
        return childrenUrl;
    }

    public void setChildrenUrl(String childrenUrl) {
        this.childrenUrl = childrenUrl;
    }

    public String getParentSlug() {
        return parentSlug;
    }

    public void setParentSlug(String parentSlug) {
        this.parentSlug = parentSlug;
    }

    public String getParentUrl() {
        return parentUrl;
    }

    public void setParentUrl(String parentUrl) {
        this.parentUrl = parentUrl;
    }

    public Boolean getLeaf() {
        return leaf;
    }

    public void setLeaf(Boolean leaf) {
        this.leaf = leaf;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Integer getRequiredTrophyCount() {
        return requiredTrophyCount;
    }

    public void setRequiredTrophyCount(Integer requiredTrophyCount) {
        this.requiredTrophyCount = requiredTrophyCount;
    }

    public Boolean getIsLocked() {
        return isLocked;
    }

    public void setIsLocked(Boolean isLocked) {
        this.isLocked = isLocked;
    }

    public Integer getContentsCount() {
        return contentsCount;
    }

    public void setContentsCount(Integer contentsCount) {
        this.contentsCount = contentsCount;
    }

    public Integer getChildrenCount() {
        return childrenCount;
    }

    public void setChildrenCount(Integer childrenCount) {
        this.childrenCount = childrenCount;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    /** To-many relationship, resolved on first access (and after reset). Changes to to-many relations are not persisted, make changes to the target entity. */
    @Generated
    public List<Chapter> getChildren() {
        if (children == null) {
            __throwIfDetached();
            ChapterDao targetDao = daoSession.getChapterDao();
            List<Chapter> childrenNew = targetDao._queryChapter_Children(id);
            synchronized (this) {
                if(children == null) {
                    children = childrenNew;
                }
            }
        }
        return children;
    }

    /** Resets a to-many relationship, making the next get call to query for a fresh result. */
    @Generated
    public synchronized void resetChildren() {
        children = null;
    }

    /** To-many relationship, resolved on first access (and after reset). Changes to to-many relations are not persisted, make changes to the target entity. */
    @Generated
    public List<Content> getContents() {
        if (contents == null) {
            __throwIfDetached();
            ContentDao targetDao = daoSession.getContentDao();
            List<Content> contentsNew = targetDao._queryChapter_Contents(id);
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

    public static QueryBuilder<Chapter> getCourseChaptersQueryBuilder(
            Context context, String courseId) {
        ChapterDao chapterDao = TestpressSDKDatabase.getChapterDao(context);
        return chapterDao.queryBuilder().where(ChapterDao.Properties.CourseId.eq(courseId),
                ChapterDao.Properties.Active.eq(true));
    }

    public static QueryBuilder<Chapter> getParentChaptersQueryBuilder(Context context, String courseId, String parentId) {
        WhereCondition parentCondition;
        if (parentId == null) {
            parentCondition = ChapterDao.Properties.ParentId.isNull();
        } else {
            parentCondition = ChapterDao.Properties.ParentId.eq(parentId);
        }
        return getCourseChaptersQueryBuilder(context, courseId).where(parentCondition);
    }

    public boolean hasContents() {
        if (contentsCount != null) {
            return getContentsCount() > 0;
        }

        return getContents().size() > 0;
    }

    public boolean hasChildren() {
        if (childrenCount != null) {
            return getChildrenCount() > 0;
        }

        return getChildren().size() > 0;
    }

    public static Chapter get(Context context, String chapterId) {
        ChapterDao chapterDao = TestpressSDKDatabase.getChapterDao(context);
        List<Chapter> chapters = chapterDao.queryBuilder().where(ChapterDao.Properties.Id.eq(chapterId)).list();

        if (chapters.isEmpty()) {
            return null;
        }

        return chapters.get(0);
    }
    // KEEP METHODS END

}
