package in.testpress.course.models.greendao;

import org.greenrobot.greendao.annotation.*;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT. Enable "keep" sections if you want to edit.

/**
 * Entity mapped to table "CHAPTER".
 */
@Entity
public class Chapter {

    @Id
    private Long id;
    private String name;
    private String description;
    private String slug;
    private String image;
    private String modified;
    private Long modifiedDate;
    private Integer courseId;
    private String courseUrl;
    private String contentUrl;
    private String childrenUrl;
    private Integer parentId;
    private String parentSlug;
    private String parentUrl;
    private Boolean leaf;
    private String url;
    private Integer requiredTrophyCount;
    private Boolean isLocked;
    private Integer order;
    private Integer contentsCount;
    private Integer childrenCount;

    @Generated
    public Chapter() {
    }

    public Chapter(Long id) {
        this.id = id;
    }

    @Generated
    public Chapter(Long id, String name, String description, String slug, String image, String modified, Long modifiedDate, Integer courseId, String courseUrl, String contentUrl, String childrenUrl, Integer parentId, String parentSlug, String parentUrl, Boolean leaf, String url, Integer requiredTrophyCount, Boolean isLocked, Integer order, Integer contentsCount, Integer childrenCount) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.slug = slug;
        this.image = image;
        this.modified = modified;
        this.modifiedDate = modifiedDate;
        this.courseId = courseId;
        this.courseUrl = courseUrl;
        this.contentUrl = contentUrl;
        this.childrenUrl = childrenUrl;
        this.parentId = parentId;
        this.parentSlug = parentSlug;
        this.parentUrl = parentUrl;
        this.leaf = leaf;
        this.url = url;
        this.requiredTrophyCount = requiredTrophyCount;
        this.isLocked = isLocked;
        this.order = order;
        this.contentsCount = contentsCount;
        this.childrenCount = childrenCount;
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

    public Integer getCourseId() {
        return courseId;
    }

    public void setCourseId(Integer courseId) {
        this.courseId = courseId;
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

    public Integer getParentId() {
        return parentId;
    }

    public void setParentId(Integer parentId) {
        this.parentId = parentId;
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

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
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

}
