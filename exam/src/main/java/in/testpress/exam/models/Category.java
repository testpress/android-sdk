package in.testpress.exam.models;

public class Category {

    private Integer id;
    private String url;
    private String name;
    private String description;
    private String slug;
    private String parentUrl;
    private Boolean leaf;
    private String parentSlug;
    private Integer examsCount;
    private Integer completedCount;
    private Integer availableCount;

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
     * The examsCount
     */
    public Integer getExamsCount() {
        return examsCount;
    }

    /**
     *
     * @param examsCount
     * The exams_count
     */
    public void setExamsCount(Integer examsCount) {
        this.examsCount = examsCount;
    }

    /**
     *
     * @return
     * The completedCount
     */
    public Integer getCompletedCount() {
        return completedCount;
    }

    /**
     *
     * @param completedCount
     * The completed_count
     */
    public void setCompletedCount(Integer completedCount) {
        this.completedCount = completedCount;
    }

    /**
     *
     * @return
     * The availableCount
     */
    public Integer getAvailableCount() {
        return availableCount;
    }

    /**
     *
     * @param availableCount
     * The available_count
     */
    public void setAvailableCount(Integer availableCount) {
        this.availableCount = availableCount;
    }

}
