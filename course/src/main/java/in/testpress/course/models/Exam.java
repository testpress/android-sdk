package in.testpress.course.models;

public class Exam {

    private String url;
    private Integer id;
    private String title;
    private String description;
    private String startDate;
    private Object endDate;
    private String duration;
    private Integer numberOfQuestions;
    private String negativeMarks;
    private String markPerQuestion;
    private Integer templateType;
    private Boolean allowRetake;
    private Integer maxRetakes;
    private Boolean enableRanks;
    private String rankPublishingDate;
    private String attemptsUrl;
    private String attemptsCount;
    private String pausedAttemptsCount;
    private Boolean allowPdf;
    private Boolean allowQuestionPdf;
    private String created;
    private String slug;
    private Boolean variableMarkPerQuestion;
    private Boolean showAnswers;
    private Integer commentsCount;
    private Boolean allowPreemptiveSectionEnding;
    private Boolean immediateFeedback;

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
     * The startDate
     */
    public String getStartDate() {
        return startDate;
    }

    /**
     *
     * @param startDate
     * The start_date
     */
    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    /**
     *
     * @return
     * The endDate
     */
    public Object getEndDate() {
        return endDate;
    }

    /**
     *
     * @param endDate
     * The end_date
     */
    public void setEndDate(Object endDate) {
        this.endDate = endDate;
    }

    /**
     *
     * @return
     * The duration
     */
    public String getDuration() {
        return duration;
    }

    /**
     *
     * @param duration
     * The duration
     */
    public void setDuration(String duration) {
        this.duration = duration;
    }

    /**
     *
     * @return
     * The numberOfQuestions
     */
    public Integer getNumberOfQuestions() {
        return numberOfQuestions;
    }

    /**
     *
     * @param numberOfQuestions
     * The number_of_questions
     */
    public void setNumberOfQuestions(Integer numberOfQuestions) {
        this.numberOfQuestions = numberOfQuestions;
    }

    /**
     *
     * @return
     * The negativeMarks
     */
    public String getNegativeMarks() {
        return negativeMarks;
    }

    /**
     *
     * @param negativeMarks
     * The negative_marks
     */
    public void setNegativeMarks(String negativeMarks) {
        this.negativeMarks = negativeMarks;
    }

    /**
     *
     * @return
     * The markPerQuestion
     */
    public String getMarkPerQuestion() {
        return markPerQuestion;
    }

    /**
     *
     * @param markPerQuestion
     * The mark_per_question
     */
    public void setMarkPerQuestion(String markPerQuestion) {
        this.markPerQuestion = markPerQuestion;
    }

    /**
     *
     * @return
     * The templateType
     */
    public Integer getTemplateType() {
        return templateType;
    }

    /**
     *
     * @param templateType
     * The template_type
     */
    public void setTemplateType(Integer templateType) {
        this.templateType = templateType;
    }

    /**
     *
     * @return
     * The allowRetake
     */
    public Boolean getAllowRetake() {
        return allowRetake;
    }

    /**
     *
     * @param allowRetake
     * The allow_retake
     */
    public void setAllowRetake(Boolean allowRetake) {
        this.allowRetake = allowRetake;
    }

    /**
     *
     * @return
     * The maxRetakes
     */
    public Integer getMaxRetakes() {
        return maxRetakes;
    }

    /**
     *
     * @param maxRetakes
     * The max_retakes
     */
    public void setMaxRetakes(Integer maxRetakes) {
        this.maxRetakes = maxRetakes;
    }

    /**
     *
     * @return
     * The enableRanks
     */
    public Boolean getEnableRanks() {
        return enableRanks;
    }

    /**
     *
     * @param enableRanks
     * The enable_ranks
     */
    public void setEnableRanks(Boolean enableRanks) {
        this.enableRanks = enableRanks;
    }

    /**
     *
     * @return
     * The rankPublishingDate
     */
    public String getRankPublishingDate() {
        return rankPublishingDate;
    }

    /**
     *
     * @param rankPublishingDate
     * The rank_publishing_date
     */
    public void setRankPublishingDate(String rankPublishingDate) {
        this.rankPublishingDate = rankPublishingDate;
    }

    /**
     *
     * @return
     * The attemptsUrl
     */
    public String getAttemptsUrl() {
        return attemptsUrl;
    }

    /**
     *
     * @param attemptsUrl
     * The attempts_url
     */
    public void setAttemptsUrl(String attemptsUrl) {
        this.attemptsUrl = attemptsUrl;
    }

    /**
     *
     * @return
     * The attemptsCount
     */
    public String getAttemptsCount() {
        return attemptsCount;
    }

    /**
     *
     * @param attemptsCount
     * The attempts_count
     */
    public void setAttemptsCount(String attemptsCount) {
        this.attemptsCount = attemptsCount;
    }

    /**
     *
     * @return
     * The pausedAttemptsCount
     */
    public String getPausedAttemptsCount() {
        return pausedAttemptsCount;
    }

    /**
     *
     * @param pausedAttemptsCount
     * The paused_attempts_count
     */
    public void setPausedAttemptsCount(String pausedAttemptsCount) {
        this.pausedAttemptsCount = pausedAttemptsCount;
    }

    /**
     *
     * @return
     * The allowPdf
     */
    public Boolean getAllowPdf() {
        return allowPdf;
    }

    /**
     *
     * @param allowPdf
     * The allow_pdf
     */
    public void setAllowPdf(Boolean allowPdf) {
        this.allowPdf = allowPdf;
    }

    /**
     *
     * @return
     * The allowQuestionPdf
     */
    public Boolean getAllowQuestionPdf() {
        return allowQuestionPdf;
    }

    /**
     *
     * @param allowQuestionPdf
     * The allow_question_pdf
     */
    public void setAllowQuestionPdf(Boolean allowQuestionPdf) {
        this.allowQuestionPdf = allowQuestionPdf;
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
     * The variableMarkPerQuestion
     */
    public Boolean getVariableMarkPerQuestion() {
        return variableMarkPerQuestion;
    }

    /**
     *
     * @param variableMarkPerQuestion
     * The variable_mark_per_question
     */
    public void setVariableMarkPerQuestion(Boolean variableMarkPerQuestion) {
        this.variableMarkPerQuestion = variableMarkPerQuestion;
    }

    /**
     *
     * @return
     * The showAnswers
     */
    public Boolean getShowAnswers() {
        return showAnswers;
    }

    /**
     *
     * @param showAnswers
     * The show_answers
     */
    public void setShowAnswers(Boolean showAnswers) {
        this.showAnswers = showAnswers;
    }

    /**
     *
     * @return
     * The commentsCount
     */
    public Integer getCommentsCount() {
        return commentsCount;
    }

    /**
     *
     * @param commentsCount
     * The comments_count
     */
    public void setCommentsCount(Integer commentsCount) {
        this.commentsCount = commentsCount;
    }

    /**
     *
     * @return
     * The allowPreemptiveSectionEnding
     */
    public Boolean getAllowPreemptiveSectionEnding() {
        return allowPreemptiveSectionEnding;
    }

    /**
     *
     * @param allowPreemptiveSectionEnding
     * The allow_preemptive_section_ending
     */
    public void setAllowPreemptiveSectionEnding(Boolean allowPreemptiveSectionEnding) {
        this.allowPreemptiveSectionEnding = allowPreemptiveSectionEnding;
    }

    /**
     *
     * @return
     * The immediateFeedback
     */
    public Boolean getImmediateFeedback() {
        return immediateFeedback;
    }

    /**
     *
     * @param immediateFeedback
     * The immediate_feedback
     */
    public void setImmediateFeedback(Boolean immediateFeedback) {
        this.immediateFeedback = immediateFeedback;
    }

}
