package in.testpress.course.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Content implements Parcelable {

    private Integer order;
    private Exam exam;
    private Object practice;
    private String htmlContentTitle;
    private String htmlContentUrl;
    private String url;
    private String attemptsUrl;
    private Integer chapterId;
    private String chapterSlug;
    private String chapterUrl;
    private Integer id;
    private Object video;
    private String name;
    private String image;
    private Object attachment;
    private String description;
    private Boolean isLocked;
    private Integer attemptsCount;
    private String start;
    private String end;
    private Boolean hasStarted;

    protected Content(Parcel in) {
        order = in.readInt();
        htmlContentTitle = in.readString();
        htmlContentUrl = in.readString();
        url = in.readString();
        attemptsUrl = in.readString();
        chapterSlug = in.readString();
        chapterUrl = in.readString();
        id = in.readInt();
        name = in.readString();
        image = in.readString();
        description = in.readString();
        isLocked = in.readByte() != 0;
        attemptsCount = in.readInt();
        start = in.readString();
        end = in.readString();
        hasStarted = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(order);
        dest.writeString(htmlContentTitle);
        dest.writeString(htmlContentUrl);
        dest.writeString(url);
        dest.writeString(attemptsUrl);
        dest.writeString(chapterSlug);
        dest.writeString(chapterUrl);
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeString(image);
        dest.writeString(description);
        if (isLocked == null) {
            dest.writeByte((byte) (0));
        } else {
            dest.writeByte((byte) (isLocked ? 1 : 0)); //if isLocked == true, byte == 1
        }
        dest.writeInt(attemptsCount);
        dest.writeString(start);
        dest.writeString(end);
        if (hasStarted == null) {
            dest.writeByte((byte) (0));
        } else {
            dest.writeByte((byte) (hasStarted ? 1 : 0)); //if hasStarted == true, byte == 1
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Content> CREATOR = new Creator<Content>() {
        @Override
        public Content createFromParcel(Parcel in) {
            return new Content(in);
        }

        @Override
        public Content[] newArray(int size) {
            return new Content[size];
        }
    };

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
     * The exam
     */
    public Exam getExam() {
        return exam;
    }

    /**
     *
     * @param exam
     * The exam
     */
    public void setExam(Exam exam) {
        this.exam = exam;
    }

    /**
     *
     * @return
     * The practice
     */
    public Object getPractice() {
        return practice;
    }

    /**
     *
     * @param practice
     * The practice
     */
    public void setPractice(Object practice) {
        this.practice = practice;
    }

    /**
     *
     * @return
     * The htmlContentTitle
     */
    public String getHtmlContentTitle() {
        return htmlContentTitle;
    }

    /**
     *
     * @param htmlContentTitle
     * The html_content_title
     */
    public void setHtmlContentTitle(String htmlContentTitle) {
        this.htmlContentTitle = htmlContentTitle;
    }

    /**
     *
     * @return
     * The htmlContentUrl
     */
    public String getHtmlContentUrl() {
        return htmlContentUrl;
    }

    /**
     *
     * @param htmlContentUrl
     * The html_content_url
     */
    public void setHtmlContentUrl(String htmlContentUrl) {
        this.htmlContentUrl = htmlContentUrl;
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
     * The chapterId
     */
    public Integer getChapterId() {
        return chapterId;
    }

    /**
     *
     * @param chapterId
     * The chapter_id
     */
    public void setChapterId(Integer chapterId) {
        this.chapterId = chapterId;
    }

    /**
     *
     * @return
     * The chapterSlug
     */
    public String getChapterSlug() {
        return chapterSlug;
    }

    /**
     *
     * @param chapterSlug
     * The chapter_slug
     */
    public void setChapterSlug(String chapterSlug) {
        this.chapterSlug = chapterSlug;
    }

    /**
     *
     * @return
     * The chapterUrl
     */
    public String getChapterUrl() {
        return chapterUrl;
    }

    /**
     *
     * @param chapterUrl
     * The chapter_url
     */
    public void setChapterUrl(String chapterUrl) {
        this.chapterUrl = chapterUrl;
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
     * The video
     */
    public Object getVideo() {
        return video;
    }

    /**
     *
     * @param video
     * The video
     */
    public void setVideo(Object video) {
        this.video = video;
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
     * The attachment
     */
    public Object getAttachment() {
        return attachment;
    }

    /**
     *
     * @param attachment
     * The attachment
     */
    public void setAttachment(Object attachment) {
        this.attachment = attachment;
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
     * The attemptsCount
     */
    public Integer getAttemptsCount() {
        return attemptsCount;
    }

    /**
     *
     * @param attemptsCount
     * The attempts_count
     */
    public void setAttemptsCount(Integer attemptsCount) {
        this.attemptsCount = attemptsCount;
    }

    /**
     *
     * @return
     * The start
     */
    public String getStart() {
        return start;
    }

    /**
     *
     * @param start
     * The start
     */
    public void setStart(String start) {
        this.start = start;
    }

    /**
     *
     * @return
     * The end
     */
    public String getEnd() {
        return end;
    }

    /**
     *
     * @param end
     * The end
     */
    public void setEnd(String end) {
        this.end = end;
    }

    /**
     *
     * @return
     * The hasStarted
     */
    public Boolean getHasStarted() {
        return hasStarted;
    }

    /**
     *
     * @param hasStarted
     * The has_started
     */
    public void setHasStarted(Boolean hasStarted) {
        this.hasStarted = hasStarted;
    }

}
