package in.testpress.models.greendao;

import org.greenrobot.greendao.annotation.*;

import in.testpress.models.greendao.DaoSession;
import org.greenrobot.greendao.DaoException;

// THIS CODE IS GENERATED BY greenDAO, EDIT ONLY INSIDE THE "KEEP"-SECTIONS

// KEEP INCLUDES - put your custom includes here
import java.util.List;

import android.content.Context;
import android.os.Parcel;

import in.testpress.core.TestpressSDKDatabase;
// KEEP INCLUDES END

/**
 * Entity mapped to table "CONTENT".
 */
@Entity(active = true)
public class Content implements android.os.Parcelable {
    private Integer order;
    private String htmlContentTitle;
    private String htmlContentUrl;
    private String url;
    private String attemptsUrl;
    private Integer chapterId;
    private String chapterSlug;
    private String chapterUrl;

    @Id
    private Long id;
    private String name;
    private String image;
    private String description;
    private Boolean isLocked;
    private Integer attemptsCount;
    private String start;
    private String end;
    private Boolean hasStarted;
    private Boolean active;
    private Long bookmarkId;
    private Long htmlId;
    private Long videoId;
    private Long attachmentId;
    private Long examId;

    /** Used to resolve relations */
    @Generated
    private transient DaoSession daoSession;

    /** Used for active entity operations. */
    @Generated
    private transient ContentDao myDao;

    @ToOne(joinProperty = "htmlId")
    private HtmlContent html;

    @Generated
    private transient Long html__resolvedKey;

    @ToOne(joinProperty = "videoId")
    private Video video;

    @Generated
    private transient Long video__resolvedKey;

    @ToOne(joinProperty = "attachmentId")
    private Attachment attachment;

    @Generated
    private transient Long attachment__resolvedKey;

    @ToOne(joinProperty = "examId")
    private Exam exam;

    @Generated
    private transient Long exam__resolvedKey;

    // KEEP FIELDS - put your custom fields here
    // KEEP FIELDS END

    @Generated
    public Content() {
    }

    public Content(Long id) {
        this.id = id;
    }

    @Generated
    public Content(Integer order, String htmlContentTitle, String htmlContentUrl, String url, String attemptsUrl, Integer chapterId, String chapterSlug, String chapterUrl, Long id, String name, String image, String description, Boolean isLocked, Integer attemptsCount, String start, String end, Boolean hasStarted, Boolean active, Long bookmarkId, Long htmlId, Long videoId, Long attachmentId, Long examId) {
        this.order = order;
        this.htmlContentTitle = htmlContentTitle;
        this.htmlContentUrl = htmlContentUrl;
        this.url = url;
        this.attemptsUrl = attemptsUrl;
        this.chapterId = chapterId;
        this.chapterSlug = chapterSlug;
        this.chapterUrl = chapterUrl;
        this.id = id;
        this.name = name;
        this.image = image;
        this.description = description;
        this.isLocked = isLocked;
        this.attemptsCount = attemptsCount;
        this.start = start;
        this.end = end;
        this.hasStarted = hasStarted;
        this.active = active;
        this.bookmarkId = bookmarkId;
        this.htmlId = htmlId;
        this.videoId = videoId;
        this.attachmentId = attachmentId;
        this.examId = examId;
    }

    /** called by internal mechanisms, do not call yourself. */
    @Generated
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getContentDao() : null;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    public String getHtmlContentTitle() {
        return htmlContentTitle;
    }

    public void setHtmlContentTitle(String htmlContentTitle) {
        this.htmlContentTitle = htmlContentTitle;
    }

    public String getHtmlContentUrl() {
        return htmlContentUrl;
    }

    public void setHtmlContentUrl(String htmlContentUrl) {
        this.htmlContentUrl = htmlContentUrl;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getAttemptsUrl() {
        return attemptsUrl;
    }

    public void setAttemptsUrl(String attemptsUrl) {
        this.attemptsUrl = attemptsUrl;
    }

    public Integer getChapterId() {
        return chapterId;
    }

    public void setChapterId(Integer chapterId) {
        this.chapterId = chapterId;
    }

    public String getChapterSlug() {
        return chapterSlug;
    }

    public void setChapterSlug(String chapterSlug) {
        this.chapterSlug = chapterSlug;
    }

    public String getChapterUrl() {
        return chapterUrl;
    }

    public void setChapterUrl(String chapterUrl) {
        this.chapterUrl = chapterUrl;
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

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getIsLocked() {
        return isLocked;
    }

    public void setIsLocked(Boolean isLocked) {
        this.isLocked = isLocked;
    }

    public Integer getAttemptsCount() {
        return attemptsCount;
    }

    public void setAttemptsCount(Integer attemptsCount) {
        this.attemptsCount = attemptsCount;
    }

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    public Boolean getHasStarted() {
        return hasStarted;
    }

    public void setHasStarted(Boolean hasStarted) {
        this.hasStarted = hasStarted;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Long getBookmarkId() {
        return bookmarkId;
    }

    public void setBookmarkId(Long bookmarkId) {
        this.bookmarkId = bookmarkId;
    }

    public Long getHtmlId() {
        return htmlId;
    }

    public void setHtmlId(Long htmlId) {
        this.htmlId = htmlId;
    }

    public Long getVideoId() {
        return videoId;
    }

    public void setVideoId(Long videoId) {
        this.videoId = videoId;
    }

    public Long getAttachmentId() {
        return attachmentId;
    }

    public void setAttachmentId(Long attachmentId) {
        this.attachmentId = attachmentId;
    }

    public Long getExamId() {
        return examId;
    }

    public void setExamId(Long examId) {
        this.examId = examId;
    }

    /** To-one relationship, resolved on first access. */
    @Generated
    public HtmlContent getHtml() {
        Long __key = this.htmlId;
        if (html__resolvedKey == null || !html__resolvedKey.equals(__key)) {
            __throwIfDetached();
            HtmlContentDao targetDao = daoSession.getHtmlContentDao();
            HtmlContent htmlNew = targetDao.load(__key);
            synchronized (this) {
                html = htmlNew;
            	html__resolvedKey = __key;
            }
        }
        return html;
    }

    @Generated
    public void setHtml(HtmlContent html) {
        synchronized (this) {
            this.html = html;
            htmlId = html == null ? null : html.getId();
            html__resolvedKey = htmlId;
        }
    }

    /** To-one relationship, resolved on first access. */
    @Generated
    public Video getVideo() {
        Long __key = this.videoId;
        if (video__resolvedKey == null || !video__resolvedKey.equals(__key)) {
            __throwIfDetached();
            VideoDao targetDao = daoSession.getVideoDao();
            Video videoNew = targetDao.load(__key);
            synchronized (this) {
                video = videoNew;
            	video__resolvedKey = __key;
            }
        }
        return video;
    }

    @Generated
    public void setVideo(Video video) {
        synchronized (this) {
            this.video = video;
            videoId = video == null ? null : video.getId();
            video__resolvedKey = videoId;
        }
    }

    /** To-one relationship, resolved on first access. */
    @Generated
    public Attachment getAttachment() {
        Long __key = this.attachmentId;
        if (attachment__resolvedKey == null || !attachment__resolvedKey.equals(__key)) {
            __throwIfDetached();
            AttachmentDao targetDao = daoSession.getAttachmentDao();
            Attachment attachmentNew = targetDao.load(__key);
            synchronized (this) {
                attachment = attachmentNew;
            	attachment__resolvedKey = __key;
            }
        }
        return attachment;
    }

    @Generated
    public void setAttachment(Attachment attachment) {
        synchronized (this) {
            this.attachment = attachment;
            attachmentId = attachment == null ? null : attachment.getId();
            attachment__resolvedKey = attachmentId;
        }
    }

    /** To-one relationship, resolved on first access. */
    @Generated
    public Exam getExam() {
        Long __key = this.examId;
        if (exam__resolvedKey == null || !exam__resolvedKey.equals(__key)) {
            __throwIfDetached();
            ExamDao targetDao = daoSession.getExamDao();
            Exam examNew = targetDao.load(__key);
            synchronized (this) {
                exam = examNew;
            	exam__resolvedKey = __key;
            }
        }
        return exam;
    }

    @Generated
    public void setExam(Exam exam) {
        synchronized (this) {
            this.exam = exam;
            examId = exam == null ? null : exam.getId();
            exam__resolvedKey = examId;
        }
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
    protected Content(Parcel in) {
        if (in.readByte() == 0) {
            order = null;
        } else {
            order = in.readInt();
        }
        htmlContentTitle = in.readString();
        htmlContentUrl = in.readString();
        url = in.readString();
        attemptsUrl = in.readString();
        if (in.readByte() == 0) {
            chapterId = null;
        } else {
            chapterId = in.readInt();
        }
        chapterSlug = in.readString();
        chapterUrl = in.readString();
        if (in.readByte() == 0) {
            id = null;
        } else {
            id = in.readLong();
        }
        name = in.readString();
        image = in.readString();
        description = in.readString();
        byte tmpIsLocked = in.readByte();
        isLocked = tmpIsLocked == 0 ? null : tmpIsLocked == 1;
        if (in.readByte() == 0) {
            attemptsCount = null;
        } else {
            attemptsCount = in.readInt();
        }
        start = in.readString();
        end = in.readString();
        byte tmpHasStarted = in.readByte();
        hasStarted = tmpHasStarted == 0 ? null : tmpHasStarted == 1;
        byte tmpActive = in.readByte();
        active = tmpActive == 0 ? null : tmpActive == 1;
        if (in.readByte() == 0) {
            videoId = null;
        } else {
            videoId = in.readLong();
        }
        if (in.readByte() == 0) {
            attachmentId = null;
        } else {
            attachmentId = in.readLong();
        }
        if (in.readByte() == 0) {
            examId = null;
        } else {
            examId = in.readLong();
        }
        video = in.readParcelable(Video.class.getClassLoader());
        attachment = in.readParcelable(Attachment.class.getClassLoader());
        exam = in.readParcelable(Exam.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (order == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(order);
        }
        dest.writeString(htmlContentTitle);
        dest.writeString(htmlContentUrl);
        dest.writeString(url);
        dest.writeString(attemptsUrl);
        if (chapterId == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(chapterId);
        }
        dest.writeString(chapterSlug);
        dest.writeString(chapterUrl);
        if (id == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(id);
        }
        dest.writeString(name);
        dest.writeString(image);
        dest.writeString(description);
        dest.writeByte((byte) (isLocked == null ? 0 : isLocked ? 1 : 2));
        if (attemptsCount == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(attemptsCount);
        }
        dest.writeString(start);
        dest.writeString(end);
        dest.writeByte((byte) (hasStarted == null ? 0 : hasStarted ? 1 : 2));
        dest.writeByte((byte) (active == null ? 0 : active ? 1 : 2));
        if (videoId == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(videoId);
        }
        if (attachmentId == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(attachmentId);
        }
        if (examId == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(examId);
        }
        dest.writeParcelable(getRawVideo(), flags);
        dest.writeParcelable(getRawAttachment(), flags);
        dest.writeParcelable(getRawExam(), flags);
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

    public Video getRawVideo() {
        if (myDao == null || video != null) {
            return video;
        }
        return getVideo();
    }

    public Attachment getRawAttachment() {
        if (myDao == null || attachment != null) {
            return attachment;
        }
        return getAttachment();
    }

    public Exam getRawExam() {
        if (myDao == null || exam != null) {
            return exam;
        }
        return getExam();
    }

    public static void save(Context context, List<Content> contents) {
        ContentDao contentDao = TestpressSDKDatabase.getContentDao(context);
        for (int i = 0; i < contents.size(); i++) {
            Content content = contents.get(i);
            List<Content> contentsFromDB = contentDao.queryBuilder()
                    .where(ContentDao.Properties.Id.eq(content.getId())).list();

            if (!contentsFromDB.isEmpty()) {
                Content contentFromDB = contentsFromDB.get(0);
                if (content.getHtmlId() != null) {
                    contentFromDB.setHtmlId(content.getHtmlId());
                }
                content = contentFromDB;
            }
            contentDao.insertOrReplace(content);
        }
    }
    // KEEP METHODS END

}
