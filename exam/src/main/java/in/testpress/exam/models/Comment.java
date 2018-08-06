package in.testpress.exam.models;

import in.testpress.models.ProfileDetails;

public class Comment {

    private Integer id;
    private String url;
    private ProfileDetails user;
    private String userName;
    private String userEmail;
    private String userUrl;
    private String comment;
    private String submitDate;
    private Integer upvotes;
    private Integer downvotes;
    private ContentObject contentObject;
    private Integer typeOfVote;
    private Integer voteId;

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
     * The user
     */
    public ProfileDetails getUser() {
        return user;
    }

    /**
     *
     * @param user
     * The user
     */
    public void setUser(ProfileDetails user) {
        this.user = user;
    }

    /**
     *
     * @return
     * The userName
     */
    public String getUserName() {
        return userName;
    }

    /**
     *
     * @param userName
     * The user_name
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     *
     * @return
     * The userEmail
     */
    public String getUserEmail() {
        return userEmail;
    }

    /**
     *
     * @param userEmail
     * The user_email
     */
    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    /**
     *
     * @return
     * The userUrl
     */
    public String getUserUrl() {
        return userUrl;
    }

    /**
     *
     * @param userUrl
     * The user_url
     */
    public void setUserUrl(String userUrl) {
        this.userUrl = userUrl;
    }

    /**
     *
     * @return
     * The comment
     */
    public String getComment() {
        return comment;
    }

    /**
     *
     * @param comment
     * The comment
     */
    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     *
     * @return
     * The submitDate
     */
    public String getSubmitDate() {
        return submitDate;
    }

    /**
     *
     * @param submitDate
     * The submit_date
     */
    public void setSubmitDate(String submitDate) {
        this.submitDate = submitDate;
    }

    /**
     *
     * @return
     * The contentObject
     */
    public ContentObject getContentObject() {
        return contentObject;
    }

    /**
     *
     * @param contentObject
     * The content_object
     */
    public void setContentObject(ContentObject contentObject) {
        this.contentObject = contentObject;
    }

    public Integer getUpvotes() {
        return upvotes;
    }

    public void setUpvotes(Integer upvotes) {
        this.upvotes = upvotes;
    }

    public Integer getDownvotes() {
        return downvotes;
    }

    public void setDownvotes(Integer downvotes) {
        this.downvotes = downvotes;
    }

    public Integer getTypeOfVote() {
        return typeOfVote;
    }

    public void setTypeOfVote(Integer typeOfVote) {
        this.typeOfVote = typeOfVote;
    }

    public Integer getVoteId() {
        return voteId;
    }

    public void setVoteId(Integer voteId) {
        this.voteId = voteId;
    }
}
