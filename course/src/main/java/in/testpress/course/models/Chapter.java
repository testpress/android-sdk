package in.testpress.course.models;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

public class Chapter implements Parcelable {

    private Integer id;
    private String name;
    private String description;
    private String slug;
    private String image;
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
    private Integer examContentCount;
    private Integer practiceContentCount;
    private Integer htmlContentCount;
    private Integer videoContentCount;

    protected Chapter(Parcel in) {
        id = in.readInt();
        name = in.readString();
        description = in.readString();
        slug = in.readString();
        image = in.readString();
        courseUrl = in.readString();
        contentUrl = in.readString();
        childrenUrl = in.readString();
        url = in.readString();
        requiredTrophyCount = in.readInt();
        examContentCount = in.readInt();
        practiceContentCount = in.readInt();
        htmlContentCount = in.readInt();
        videoContentCount = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeString(description);
        dest.writeString(slug);
        dest.writeString(image);
        dest.writeString(courseUrl);
        dest.writeString(contentUrl);
        dest.writeString(childrenUrl);
        dest.writeString(url);
        dest.writeInt(requiredTrophyCount);
        dest.writeInt(examContentCount);
        dest.writeInt(practiceContentCount);
        dest.writeInt(htmlContentCount);
        dest.writeInt(videoContentCount);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Chapter> CREATOR = new Creator<Chapter>() {
        @Override
        public Chapter createFromParcel(Parcel in) {
            return new Chapter(in);
        }

        @Override
        public Chapter[] newArray(int size) {
            return new Chapter[size];
        }
    };

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
     * The name
     */
    public String getName() {
        return name;
    }

    /**
     *
     * @param name
     * The name
     */
    public void setName(String name) {
        this.name = name;
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
     * The slug
     */
    public String getSlug() {
        return slug;
    }

    /**
     *
     * @param slug
     * The slug
     */
    public void setSlug(String slug) {
        this.slug = slug;
    }

    /**
     *
     * @return
     * The image
     */
    public String getImage() {
        return image;
    }

    /**
     *
     * @param image
     * The image
     */
    public void setImage(String image) {
        this.image = image;
    }

    /**
     *
     * @return
     * The courseId
     */
    public Integer getCourseId() {
        return courseId;
    }

    /**
     *
     * @param courseId
     * The course_id
     */
    public void setCourseId(Integer courseId) {
        this.courseId = courseId;
    }

    /**
     *
     * @return
     * The courseUrl
     */
    public String getCourseUrl() {
        return courseUrl;
    }

    /**
     *
     * @param courseUrl
     * The course_url
     */
    public void setCourseUrl(String courseUrl) {
        this.courseUrl = courseUrl;
    }

    /**
     *
     * @return
     * The contentUrl
     */
    public String getContentUrl() {
        return contentUrl;
    }

    public String getContentUrlFrag() {
        if (contentUrl != null) {
            Uri uri = Uri.parse(contentUrl);
            return uri.getPath();
        }
        return null;
    }

    /**
     *
     * @param contentUrl
     * The content_url
     */
    public void setContentUrl(String contentUrl) {
        this.contentUrl = contentUrl;
    }

    /**
     *
     * @return
     * The childrenUrl
     */
    public String getChildrenUrl() {
        return childrenUrl;
    }

    public String getChildrenUrlFrag() {
        if (childrenUrl != null) {
            Uri uri = Uri.parse(childrenUrl);
            return uri.getPath();
        }
        return null;
    }

    /**
     *
     * @param childrenUrl
     * The children_url
     */
    public void setChildrenUrl(String childrenUrl) {
        this.childrenUrl = childrenUrl;
    }

    /**
     *
     * @return
     * The parentId
     */
    public Integer getParentId() {
        return parentId;
    }

    /**
     *
     * @param parentId
     * The parent_id
     */
    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }

    /**
     *
     * @return
     * The parentSlug
     */
    public String getParentSlug() {
        return parentSlug;
    }

    /**
     *
     * @param parentSlug
     * The parent_slug
     */
    public void setParentSlug(String parentSlug) {
        this.parentSlug = parentSlug;
    }

    /**
     *
     * @return
     * The parentUrl
     */
    public String getParentUrl() {
        return parentUrl;
    }

    /**
     *
     * @param parentUrl
     * The parent_url
     */
    public void setParentUrl(String parentUrl) {
        this.parentUrl = parentUrl;
    }

    /**
     *
     * @return
     * The leaf
     */
    public Boolean getLeaf() {
        return leaf;
    }

    /**
     *
     * @param leaf
     * The leaf
     */
    public void setLeaf(Boolean leaf) {
        this.leaf = leaf;
    }

    /**
     *
     * @return
     * The url
     */
    public String getUrl() {
        return url;
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
     * The requiredTrophyCount
     */
    public Integer getRequiredTrophyCount() {
        return requiredTrophyCount;
    }

    /**
     *
     * @param requiredTrophyCount
     * The required_trophy_count
     */
    public void setRequiredTrophyCount(Integer requiredTrophyCount) {
        this.requiredTrophyCount = requiredTrophyCount;
    }

    /**
     *
     * @return
     * The isLocked
     */
    public Boolean getIsLocked() {
        return isLocked;
    }

    /**
     *
     * @param isLocked
     * The is_locked
     */
    public void setIsLocked(Boolean isLocked) {
        this.isLocked = isLocked;
    }

    /**
     *
     * @return
     * The order
     */
    public Integer getOrder() {
        return order;
    }

    /**
     *
     * @param order
     * The order
     */
    public void setOrder(Integer order) {
        this.order = order;
    }

    /**
     *
     * @return
     * The contentsCount
     */
    public Integer getContentsCount() {
        return contentsCount;
    }

    /**
     *
     * @param contentsCount
     * The contents_count
     */
    public void setContentsCount(Integer contentsCount) {
        this.contentsCount = contentsCount;
    }

    /**
     *
     * @return
     * The childrenCount
     */
    public Integer getChildrenCount() {
        return childrenCount;
    }

    /**
     *
     * @param childrenCount
     * The children_count
     */
    public void setChildrenCount(Integer childrenCount) {
        this.childrenCount = childrenCount;
    }

    public Integer getExamContentCount() {
        return examContentCount;
    }

    public void setExamContentCount(Integer examContentCount) {
        this.examContentCount = examContentCount;
    }

    public Integer getPracticeContentCount() {
        return practiceContentCount;
    }

    public void setPracticeContentCount(Integer practiceContentCount) {
        this.practiceContentCount = practiceContentCount;
    }

    public Integer getHtmlContentCount() {
        return htmlContentCount;
    }

    public void setHtmlContentCount(Integer htmlContentCount) {
        this.htmlContentCount = htmlContentCount;
    }

    public Integer getVideoContentCount() {
        return videoContentCount;
    }

    public void setVideoContentCount(Integer videoContentCount) {
        this.videoContentCount = videoContentCount;
    }

}
