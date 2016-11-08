package in.testpress.course.models;

import android.net.Uri;

public class Course {

    private Integer id;
    private String url;
    private String title;
    private String description;
    private String image;
    private String created;
    private String contentsUrl;
    private String chaptersUrl;
    private String slug;
    private Integer trophiesCount;
    private Integer chaptersCount;
    private Integer contentsCount;

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
     * The created
     */
    public String getCreated() {
        return created;
    }

    /**
     *
     * @param created
     * The created
     */
    public void setCreated(String created) {
        this.created = created;
    }

    /**
     *
     * @return
     * The contentsUrl
     */
    public String getContentsUrl() {
        return contentsUrl;
    }

    /**
     *
     * @param contentsUrl
     * The contents_url
     */
    public void setContentsUrl(String contentsUrl) {
        this.contentsUrl = contentsUrl;
    }

    /**
     *
     * @return
     * The chaptersUrl
     */
    public String getChaptersUrl() {
        return chaptersUrl;
    }

    public String getChaptersUrlFrag() {
        if (chaptersUrl != null) {
            Uri uri = Uri.parse(chaptersUrl);
            return uri.getPath();
        }
        return null;
    }

    /**
     *
     * @param chaptersUrl
     * The chapters_url
     */
    public void setChaptersUrl(String chaptersUrl) {
        this.chaptersUrl = chaptersUrl;
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
     * The trophiesCount
     */
    public Integer getTrophiesCount() {
        return trophiesCount;
    }

    /**
     *
     * @param trophiesCount
     * The trophies_count
     */
    public void setTrophiesCount(Integer trophiesCount) {
        this.trophiesCount = trophiesCount;
    }

    /**
     *
     * @return
     * The chaptersCount
     */
    public Integer getChaptersCount() {
        return chaptersCount;
    }

    /**
     *
     * @param chaptersCount
     * The chapters_count
     */
    public void setChaptersCount(Integer chaptersCount) {
        this.chaptersCount = chaptersCount;
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

}
