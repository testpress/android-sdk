package in.testpress.models.greendao;

import org.greenrobot.greendao.annotation.*;

// THIS CODE IS GENERATED BY greenDAO, EDIT ONLY INSIDE THE "KEEP"-SECTIONS

// KEEP INCLUDES - put your custom includes here
import android.content.Context;

import java.util.List;

import in.testpress.core.TestpressSDKDatabase;
// KEEP INCLUDES END

/**
 * Entity mapped to table "COURSE".
 */
@Entity
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

    // KEEP FIELDS - put your custom fields here
    // KEEP FIELDS END

    @Generated
    public Course() {
    }

    public Course(Long id) {
        this.id = id;
    }

    @Generated
    public Course(Long id, String url, String title, String description, String image, String modified, Long modifiedDate, String contentsUrl, String chaptersUrl, String slug, Integer trophiesCount, Integer chaptersCount, Integer contentsCount, Integer order, Boolean active, String external_content_link, String external_link_label, boolean childItemsLoaded) {
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

    // KEEP METHODS - put your custom methods here

    public boolean isCourseForRegistration() {

        if (this.external_content_link != null && !this.external_content_link.equals("")) {
            return true;
        }

        return false;
    }

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
