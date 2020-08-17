package in.testpress.models.greendao;

import java.util.List;
import java.util.ArrayList;
import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.internal.SqlUtils;
import org.greenrobot.greendao.internal.DaoConfig;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;
import org.greenrobot.greendao.query.Query;
import org.greenrobot.greendao.query.QueryBuilder;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "CONTENT".
*/
public class ContentDao extends AbstractDao<Content, Long> {

    public static final String TABLENAME = "CONTENT";

    /**
     * Properties of entity Content.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property Order = new Property(0, Integer.class, "order", false, "ORDER");
        public final static Property HtmlContentTitle = new Property(1, String.class, "htmlContentTitle", false, "HTML_CONTENT_TITLE");
        public final static Property HtmlContentUrl = new Property(2, String.class, "htmlContentUrl", false, "HTML_CONTENT_URL");
        public final static Property Url = new Property(3, String.class, "url", false, "URL");
        public final static Property AttemptsUrl = new Property(4, String.class, "attemptsUrl", false, "ATTEMPTS_URL");
        public final static Property ChapterSlug = new Property(5, String.class, "chapterSlug", false, "CHAPTER_SLUG");
        public final static Property ChapterUrl = new Property(6, String.class, "chapterUrl", false, "CHAPTER_URL");
        public final static Property Id = new Property(7, Long.class, "id", true, "ID");
        public final static Property Title = new Property(8, String.class, "title", false, "TITLE");
        public final static Property ContentType = new Property(9, String.class, "contentType", false, "CONTENT_TYPE");
        public final static Property Image = new Property(10, String.class, "image", false, "IMAGE");
        public final static Property Description = new Property(11, String.class, "description", false, "DESCRIPTION");
        public final static Property IsLocked = new Property(12, Boolean.class, "isLocked", false, "IS_LOCKED");
        public final static Property AttemptsCount = new Property(13, int.class, "attemptsCount", false, "ATTEMPTS_COUNT");
        public final static Property Start = new Property(14, String.class, "start", false, "START");
        public final static Property End = new Property(15, String.class, "end", false, "END");
        public final static Property HasStarted = new Property(16, Boolean.class, "hasStarted", false, "HAS_STARTED");
        public final static Property Active = new Property(17, Boolean.class, "active", false, "ACTIVE");
        public final static Property BookmarkId = new Property(18, Long.class, "bookmarkId", false, "BOOKMARK_ID");
        public final static Property VideoWatchedPercentage = new Property(19, int.class, "videoWatchedPercentage", false, "VIDEO_WATCHED_PERCENTAGE");
        public final static Property Modified = new Property(20, String.class, "modified", false, "MODIFIED");
        public final static Property ModifiedDate = new Property(21, Long.class, "modifiedDate", false, "MODIFIED_DATE");
        public final static Property FreePreview = new Property(22, Boolean.class, "freePreview", false, "FREE_PREVIEW");
        public final static Property IsScheduled = new Property(23, Boolean.class, "isScheduled", false, "IS_SCHEDULED");
        public final static Property CoverImage = new Property(24, String.class, "coverImage", false, "COVER_IMAGE");
        public final static Property IsCourseAvailable = new Property(25, Boolean.class, "isCourseAvailable", false, "IS_COURSE_AVAILABLE");
        public final static Property CourseId = new Property(26, Long.class, "courseId", false, "COURSE_ID");
        public final static Property ChapterId = new Property(27, Long.class, "chapterId", false, "CHAPTER_ID");
        public final static Property VideoConferenceId = new Property(28, Long.class, "videoConferenceId", false, "VIDEO_CONFERENCE_ID");
        public final static Property HtmlId = new Property(29, Long.class, "htmlId", false, "HTML_ID");
        public final static Property VideoId = new Property(30, Long.class, "videoId", false, "VIDEO_ID");
        public final static Property AttachmentId = new Property(31, Long.class, "attachmentId", false, "ATTACHMENT_ID");
        public final static Property ExamId = new Property(32, Long.class, "examId", false, "EXAM_ID");
    }

    private DaoSession daoSession;

    private Query<Content> course_ContentsQuery;
    private Query<Content> chapter_ContentsQuery;

    public ContentDao(DaoConfig config) {
        super(config);
    }
    
    public ContentDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
        this.daoSession = daoSession;
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"CONTENT\" (" + //
                "\"ORDER\" INTEGER," + // 0: order
                "\"HTML_CONTENT_TITLE\" TEXT," + // 1: htmlContentTitle
                "\"HTML_CONTENT_URL\" TEXT," + // 2: htmlContentUrl
                "\"URL\" TEXT," + // 3: url
                "\"ATTEMPTS_URL\" TEXT," + // 4: attemptsUrl
                "\"CHAPTER_SLUG\" TEXT," + // 5: chapterSlug
                "\"CHAPTER_URL\" TEXT," + // 6: chapterUrl
                "\"ID\" INTEGER PRIMARY KEY ," + // 7: id
                "\"TITLE\" TEXT," + // 8: title
                "\"CONTENT_TYPE\" TEXT," + // 9: contentType
                "\"IMAGE\" TEXT," + // 10: image
                "\"DESCRIPTION\" TEXT," + // 11: description
                "\"IS_LOCKED\" INTEGER," + // 12: isLocked
                "\"ATTEMPTS_COUNT\" INTEGER NOT NULL ," + // 13: attemptsCount
                "\"START\" TEXT," + // 14: start
                "\"END\" TEXT," + // 15: end
                "\"HAS_STARTED\" INTEGER," + // 16: hasStarted
                "\"ACTIVE\" INTEGER," + // 17: active
                "\"BOOKMARK_ID\" INTEGER," + // 18: bookmarkId
                "\"VIDEO_WATCHED_PERCENTAGE\" INTEGER NOT NULL ," + // 19: videoWatchedPercentage
                "\"MODIFIED\" TEXT," + // 20: modified
                "\"MODIFIED_DATE\" INTEGER," + // 21: modifiedDate
                "\"FREE_PREVIEW\" INTEGER," + // 22: freePreview
                "\"IS_SCHEDULED\" INTEGER," + // 23: isScheduled
                "\"COVER_IMAGE\" TEXT," + // 24: coverImage
                "\"IS_COURSE_AVAILABLE\" INTEGER," + // 25: isCourseAvailable
                "\"COURSE_ID\" INTEGER," + // 26: courseId
                "\"CHAPTER_ID\" INTEGER," + // 27: chapterId
                "\"VIDEO_CONFERENCE_ID\" INTEGER," + // 28: videoConferenceId
                "\"HTML_ID\" INTEGER," + // 29: htmlId
                "\"VIDEO_ID\" INTEGER," + // 30: videoId
                "\"ATTACHMENT_ID\" INTEGER," + // 31: attachmentId
                "\"EXAM_ID\" INTEGER);"); // 32: examId
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"CONTENT\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, Content entity) {
        stmt.clearBindings();
 
        Integer order = entity.getOrder();
        if (order != null) {
            stmt.bindLong(1, order);
        }
 
        String htmlContentTitle = entity.getHtmlContentTitle();
        if (htmlContentTitle != null) {
            stmt.bindString(2, htmlContentTitle);
        }
 
        String htmlContentUrl = entity.getHtmlContentUrl();
        if (htmlContentUrl != null) {
            stmt.bindString(3, htmlContentUrl);
        }
 
        String url = entity.getUrl();
        if (url != null) {
            stmt.bindString(4, url);
        }
 
        String attemptsUrl = entity.getAttemptsUrl();
        if (attemptsUrl != null) {
            stmt.bindString(5, attemptsUrl);
        }
 
        String chapterSlug = entity.getChapterSlug();
        if (chapterSlug != null) {
            stmt.bindString(6, chapterSlug);
        }
 
        String chapterUrl = entity.getChapterUrl();
        if (chapterUrl != null) {
            stmt.bindString(7, chapterUrl);
        }
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(8, id);
        }
 
        String title = entity.getTitle();
        if (title != null) {
            stmt.bindString(9, title);
        }
 
        String contentType = entity.getContentType();
        if (contentType != null) {
            stmt.bindString(10, contentType);
        }
 
        String image = entity.getImage();
        if (image != null) {
            stmt.bindString(11, image);
        }
 
        String description = entity.getDescription();
        if (description != null) {
            stmt.bindString(12, description);
        }
 
        Boolean isLocked = entity.getIsLocked();
        if (isLocked != null) {
            stmt.bindLong(13, isLocked ? 1L: 0L);
        }
        stmt.bindLong(14, entity.getAttemptsCount());
 
        String start = entity.getStart();
        if (start != null) {
            stmt.bindString(15, start);
        }
 
        String end = entity.getEnd();
        if (end != null) {
            stmt.bindString(16, end);
        }
 
        Boolean hasStarted = entity.getHasStarted();
        if (hasStarted != null) {
            stmt.bindLong(17, hasStarted ? 1L: 0L);
        }
 
        Boolean active = entity.getActive();
        if (active != null) {
            stmt.bindLong(18, active ? 1L: 0L);
        }
 
        Long bookmarkId = entity.getBookmarkId();
        if (bookmarkId != null) {
            stmt.bindLong(19, bookmarkId);
        }
        stmt.bindLong(20, entity.getVideoWatchedPercentage());
 
        String modified = entity.getModified();
        if (modified != null) {
            stmt.bindString(21, modified);
        }
 
        Long modifiedDate = entity.getModifiedDate();
        if (modifiedDate != null) {
            stmt.bindLong(22, modifiedDate);
        }
 
        Boolean freePreview = entity.getFreePreview();
        if (freePreview != null) {
            stmt.bindLong(23, freePreview ? 1L: 0L);
        }
 
        Boolean isScheduled = entity.getIsScheduled();
        if (isScheduled != null) {
            stmt.bindLong(24, isScheduled ? 1L: 0L);
        }
 
        String coverImage = entity.getCoverImage();
        if (coverImage != null) {
            stmt.bindString(25, coverImage);
        }
 
        Boolean isCourseAvailable = entity.getIsCourseAvailable();
        if (isCourseAvailable != null) {
            stmt.bindLong(26, isCourseAvailable ? 1L: 0L);
        }
 
        Long courseId = entity.getCourseId();
        if (courseId != null) {
            stmt.bindLong(27, courseId);
        }
 
        Long chapterId = entity.getChapterId();
        if (chapterId != null) {
            stmt.bindLong(28, chapterId);
        }
 
        Long videoConferenceId = entity.getVideoConferenceId();
        if (videoConferenceId != null) {
            stmt.bindLong(29, videoConferenceId);
        }
 
        Long htmlId = entity.getHtmlId();
        if (htmlId != null) {
            stmt.bindLong(30, htmlId);
        }
 
        Long videoId = entity.getVideoId();
        if (videoId != null) {
            stmt.bindLong(31, videoId);
        }
 
        Long attachmentId = entity.getAttachmentId();
        if (attachmentId != null) {
            stmt.bindLong(32, attachmentId);
        }
 
        Long examId = entity.getExamId();
        if (examId != null) {
            stmt.bindLong(33, examId);
        }
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, Content entity) {
        stmt.clearBindings();
 
        Integer order = entity.getOrder();
        if (order != null) {
            stmt.bindLong(1, order);
        }
 
        String htmlContentTitle = entity.getHtmlContentTitle();
        if (htmlContentTitle != null) {
            stmt.bindString(2, htmlContentTitle);
        }
 
        String htmlContentUrl = entity.getHtmlContentUrl();
        if (htmlContentUrl != null) {
            stmt.bindString(3, htmlContentUrl);
        }
 
        String url = entity.getUrl();
        if (url != null) {
            stmt.bindString(4, url);
        }
 
        String attemptsUrl = entity.getAttemptsUrl();
        if (attemptsUrl != null) {
            stmt.bindString(5, attemptsUrl);
        }
 
        String chapterSlug = entity.getChapterSlug();
        if (chapterSlug != null) {
            stmt.bindString(6, chapterSlug);
        }
 
        String chapterUrl = entity.getChapterUrl();
        if (chapterUrl != null) {
            stmt.bindString(7, chapterUrl);
        }
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(8, id);
        }
 
        String title = entity.getTitle();
        if (title != null) {
            stmt.bindString(9, title);
        }
 
        String contentType = entity.getContentType();
        if (contentType != null) {
            stmt.bindString(10, contentType);
        }
 
        String image = entity.getImage();
        if (image != null) {
            stmt.bindString(11, image);
        }
 
        String description = entity.getDescription();
        if (description != null) {
            stmt.bindString(12, description);
        }
 
        Boolean isLocked = entity.getIsLocked();
        if (isLocked != null) {
            stmt.bindLong(13, isLocked ? 1L: 0L);
        }
        stmt.bindLong(14, entity.getAttemptsCount());
 
        String start = entity.getStart();
        if (start != null) {
            stmt.bindString(15, start);
        }
 
        String end = entity.getEnd();
        if (end != null) {
            stmt.bindString(16, end);
        }
 
        Boolean hasStarted = entity.getHasStarted();
        if (hasStarted != null) {
            stmt.bindLong(17, hasStarted ? 1L: 0L);
        }
 
        Boolean active = entity.getActive();
        if (active != null) {
            stmt.bindLong(18, active ? 1L: 0L);
        }
 
        Long bookmarkId = entity.getBookmarkId();
        if (bookmarkId != null) {
            stmt.bindLong(19, bookmarkId);
        }
        stmt.bindLong(20, entity.getVideoWatchedPercentage());
 
        String modified = entity.getModified();
        if (modified != null) {
            stmt.bindString(21, modified);
        }
 
        Long modifiedDate = entity.getModifiedDate();
        if (modifiedDate != null) {
            stmt.bindLong(22, modifiedDate);
        }
 
        Boolean freePreview = entity.getFreePreview();
        if (freePreview != null) {
            stmt.bindLong(23, freePreview ? 1L: 0L);
        }
 
        Boolean isScheduled = entity.getIsScheduled();
        if (isScheduled != null) {
            stmt.bindLong(24, isScheduled ? 1L: 0L);
        }
 
        String coverImage = entity.getCoverImage();
        if (coverImage != null) {
            stmt.bindString(25, coverImage);
        }
 
        Boolean isCourseAvailable = entity.getIsCourseAvailable();
        if (isCourseAvailable != null) {
            stmt.bindLong(26, isCourseAvailable ? 1L: 0L);
        }
 
        Long courseId = entity.getCourseId();
        if (courseId != null) {
            stmt.bindLong(27, courseId);
        }
 
        Long chapterId = entity.getChapterId();
        if (chapterId != null) {
            stmt.bindLong(28, chapterId);
        }
 
        Long videoConferenceId = entity.getVideoConferenceId();
        if (videoConferenceId != null) {
            stmt.bindLong(29, videoConferenceId);
        }
 
        Long htmlId = entity.getHtmlId();
        if (htmlId != null) {
            stmt.bindLong(30, htmlId);
        }
 
        Long videoId = entity.getVideoId();
        if (videoId != null) {
            stmt.bindLong(31, videoId);
        }
 
        Long attachmentId = entity.getAttachmentId();
        if (attachmentId != null) {
            stmt.bindLong(32, attachmentId);
        }
 
        Long examId = entity.getExamId();
        if (examId != null) {
            stmt.bindLong(33, examId);
        }
    }

    @Override
    protected final void attachEntity(Content entity) {
        super.attachEntity(entity);
        entity.__setDaoSession(daoSession);
    }

    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 7) ? null : cursor.getLong(offset + 7);
    }    

    @Override
    public Content readEntity(Cursor cursor, int offset) {
        Content entity = new Content( //
            cursor.isNull(offset + 0) ? null : cursor.getInt(offset + 0), // order
            cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1), // htmlContentTitle
            cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2), // htmlContentUrl
            cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3), // url
            cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4), // attemptsUrl
            cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5), // chapterSlug
            cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6), // chapterUrl
            cursor.isNull(offset + 7) ? null : cursor.getLong(offset + 7), // id
            cursor.isNull(offset + 8) ? null : cursor.getString(offset + 8), // title
            cursor.isNull(offset + 9) ? null : cursor.getString(offset + 9), // contentType
            cursor.isNull(offset + 10) ? null : cursor.getString(offset + 10), // image
            cursor.isNull(offset + 11) ? null : cursor.getString(offset + 11), // description
            cursor.isNull(offset + 12) ? null : cursor.getShort(offset + 12) != 0, // isLocked
            cursor.getInt(offset + 13), // attemptsCount
            cursor.isNull(offset + 14) ? null : cursor.getString(offset + 14), // start
            cursor.isNull(offset + 15) ? null : cursor.getString(offset + 15), // end
            cursor.isNull(offset + 16) ? null : cursor.getShort(offset + 16) != 0, // hasStarted
            cursor.isNull(offset + 17) ? null : cursor.getShort(offset + 17) != 0, // active
            cursor.isNull(offset + 18) ? null : cursor.getLong(offset + 18), // bookmarkId
            cursor.getInt(offset + 19), // videoWatchedPercentage
            cursor.isNull(offset + 20) ? null : cursor.getString(offset + 20), // modified
            cursor.isNull(offset + 21) ? null : cursor.getLong(offset + 21), // modifiedDate
            cursor.isNull(offset + 22) ? null : cursor.getShort(offset + 22) != 0, // freePreview
            cursor.isNull(offset + 23) ? null : cursor.getShort(offset + 23) != 0, // isScheduled
            cursor.isNull(offset + 24) ? null : cursor.getString(offset + 24), // coverImage
            cursor.isNull(offset + 25) ? null : cursor.getShort(offset + 25) != 0, // isCourseAvailable
            cursor.isNull(offset + 26) ? null : cursor.getLong(offset + 26), // courseId
            cursor.isNull(offset + 27) ? null : cursor.getLong(offset + 27), // chapterId
            cursor.isNull(offset + 28) ? null : cursor.getLong(offset + 28), // videoConferenceId
            cursor.isNull(offset + 29) ? null : cursor.getLong(offset + 29), // htmlId
            cursor.isNull(offset + 30) ? null : cursor.getLong(offset + 30), // videoId
            cursor.isNull(offset + 31) ? null : cursor.getLong(offset + 31), // attachmentId
            cursor.isNull(offset + 32) ? null : cursor.getLong(offset + 32) // examId
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, Content entity, int offset) {
        entity.setOrder(cursor.isNull(offset + 0) ? null : cursor.getInt(offset + 0));
        entity.setHtmlContentTitle(cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1));
        entity.setHtmlContentUrl(cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2));
        entity.setUrl(cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3));
        entity.setAttemptsUrl(cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4));
        entity.setChapterSlug(cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5));
        entity.setChapterUrl(cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6));
        entity.setId(cursor.isNull(offset + 7) ? null : cursor.getLong(offset + 7));
        entity.setTitle(cursor.isNull(offset + 8) ? null : cursor.getString(offset + 8));
        entity.setContentType(cursor.isNull(offset + 9) ? null : cursor.getString(offset + 9));
        entity.setImage(cursor.isNull(offset + 10) ? null : cursor.getString(offset + 10));
        entity.setDescription(cursor.isNull(offset + 11) ? null : cursor.getString(offset + 11));
        entity.setIsLocked(cursor.isNull(offset + 12) ? null : cursor.getShort(offset + 12) != 0);
        entity.setAttemptsCount(cursor.getInt(offset + 13));
        entity.setStart(cursor.isNull(offset + 14) ? null : cursor.getString(offset + 14));
        entity.setEnd(cursor.isNull(offset + 15) ? null : cursor.getString(offset + 15));
        entity.setHasStarted(cursor.isNull(offset + 16) ? null : cursor.getShort(offset + 16) != 0);
        entity.setActive(cursor.isNull(offset + 17) ? null : cursor.getShort(offset + 17) != 0);
        entity.setBookmarkId(cursor.isNull(offset + 18) ? null : cursor.getLong(offset + 18));
        entity.setVideoWatchedPercentage(cursor.getInt(offset + 19));
        entity.setModified(cursor.isNull(offset + 20) ? null : cursor.getString(offset + 20));
        entity.setModifiedDate(cursor.isNull(offset + 21) ? null : cursor.getLong(offset + 21));
        entity.setFreePreview(cursor.isNull(offset + 22) ? null : cursor.getShort(offset + 22) != 0);
        entity.setIsScheduled(cursor.isNull(offset + 23) ? null : cursor.getShort(offset + 23) != 0);
        entity.setCoverImage(cursor.isNull(offset + 24) ? null : cursor.getString(offset + 24));
        entity.setIsCourseAvailable(cursor.isNull(offset + 25) ? null : cursor.getShort(offset + 25) != 0);
        entity.setCourseId(cursor.isNull(offset + 26) ? null : cursor.getLong(offset + 26));
        entity.setChapterId(cursor.isNull(offset + 27) ? null : cursor.getLong(offset + 27));
        entity.setVideoConferenceId(cursor.isNull(offset + 28) ? null : cursor.getLong(offset + 28));
        entity.setHtmlId(cursor.isNull(offset + 29) ? null : cursor.getLong(offset + 29));
        entity.setVideoId(cursor.isNull(offset + 30) ? null : cursor.getLong(offset + 30));
        entity.setAttachmentId(cursor.isNull(offset + 31) ? null : cursor.getLong(offset + 31));
        entity.setExamId(cursor.isNull(offset + 32) ? null : cursor.getLong(offset + 32));
     }
    
    @Override
    protected final Long updateKeyAfterInsert(Content entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    @Override
    public Long getKey(Content entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    @Override
    public boolean hasKey(Content entity) {
        return entity.getId() != null;
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
    /** Internal query to resolve the "contents" to-many relationship of Course. */
    public List<Content> _queryCourse_Contents(Long courseId) {
        synchronized (this) {
            if (course_ContentsQuery == null) {
                QueryBuilder<Content> queryBuilder = queryBuilder();
                queryBuilder.where(Properties.CourseId.eq(null));
                course_ContentsQuery = queryBuilder.build();
            }
        }
        Query<Content> query = course_ContentsQuery.forCurrentThread();
        query.setParameter(0, courseId);
        return query.list();
    }

    /** Internal query to resolve the "contents" to-many relationship of Chapter. */
    public List<Content> _queryChapter_Contents(Long chapterId) {
        synchronized (this) {
            if (chapter_ContentsQuery == null) {
                QueryBuilder<Content> queryBuilder = queryBuilder();
                queryBuilder.where(Properties.ChapterId.eq(null));
                chapter_ContentsQuery = queryBuilder.build();
            }
        }
        Query<Content> query = chapter_ContentsQuery.forCurrentThread();
        query.setParameter(0, chapterId);
        return query.list();
    }

    private String selectDeep;

    protected String getSelectDeep() {
        if (selectDeep == null) {
            StringBuilder builder = new StringBuilder("SELECT ");
            SqlUtils.appendColumns(builder, "T", getAllColumns());
            builder.append(',');
            SqlUtils.appendColumns(builder, "T0", daoSession.getChapterDao().getAllColumns());
            builder.append(',');
            SqlUtils.appendColumns(builder, "T1", daoSession.getVideoConferenceDao().getAllColumns());
            builder.append(',');
            SqlUtils.appendColumns(builder, "T2", daoSession.getHtmlContentDao().getAllColumns());
            builder.append(',');
            SqlUtils.appendColumns(builder, "T3", daoSession.getVideoDao().getAllColumns());
            builder.append(',');
            SqlUtils.appendColumns(builder, "T4", daoSession.getAttachmentDao().getAllColumns());
            builder.append(',');
            SqlUtils.appendColumns(builder, "T5", daoSession.getExamDao().getAllColumns());
            builder.append(" FROM CONTENT T");
            builder.append(" LEFT JOIN CHAPTER T0 ON T.\"CHAPTER_ID\"=T0.\"ID\"");
            builder.append(" LEFT JOIN VIDEO_CONFERENCE T1 ON T.\"VIDEO_CONFERENCE_ID\"=T1.\"ID\"");
            builder.append(" LEFT JOIN HTML_CONTENT T2 ON T.\"HTML_ID\"=T2.\"ID\"");
            builder.append(" LEFT JOIN VIDEO T3 ON T.\"VIDEO_ID\"=T3.\"ID\"");
            builder.append(" LEFT JOIN ATTACHMENT T4 ON T.\"ATTACHMENT_ID\"=T4.\"ID\"");
            builder.append(" LEFT JOIN EXAM T5 ON T.\"EXAM_ID\"=T5.\"ID\"");
            builder.append(' ');
            selectDeep = builder.toString();
        }
        return selectDeep;
    }
    
    protected Content loadCurrentDeep(Cursor cursor, boolean lock) {
        Content entity = loadCurrent(cursor, 0, lock);
        int offset = getAllColumns().length;

        Chapter chapter = loadCurrentOther(daoSession.getChapterDao(), cursor, offset);
        entity.setChapter(chapter);
        offset += daoSession.getChapterDao().getAllColumns().length;

        VideoConference videoConference = loadCurrentOther(daoSession.getVideoConferenceDao(), cursor, offset);
        entity.setVideoConference(videoConference);
        offset += daoSession.getVideoConferenceDao().getAllColumns().length;

        HtmlContent htmlContent = loadCurrentOther(daoSession.getHtmlContentDao(), cursor, offset);
        entity.setHtmlContent(htmlContent);
        offset += daoSession.getHtmlContentDao().getAllColumns().length;

        Video video = loadCurrentOther(daoSession.getVideoDao(), cursor, offset);
        entity.setVideo(video);
        offset += daoSession.getVideoDao().getAllColumns().length;

        Attachment attachment = loadCurrentOther(daoSession.getAttachmentDao(), cursor, offset);
        entity.setAttachment(attachment);
        offset += daoSession.getAttachmentDao().getAllColumns().length;

        Exam exam = loadCurrentOther(daoSession.getExamDao(), cursor, offset);
        entity.setExam(exam);

        return entity;    
    }

    public Content loadDeep(Long key) {
        assertSinglePk();
        if (key == null) {
            return null;
        }

        StringBuilder builder = new StringBuilder(getSelectDeep());
        builder.append("WHERE ");
        SqlUtils.appendColumnsEqValue(builder, "T", getPkColumns());
        String sql = builder.toString();
        
        String[] keyArray = new String[] { key.toString() };
        Cursor cursor = db.rawQuery(sql, keyArray);
        
        try {
            boolean available = cursor.moveToFirst();
            if (!available) {
                return null;
            } else if (!cursor.isLast()) {
                throw new IllegalStateException("Expected unique result, but count was " + cursor.getCount());
            }
            return loadCurrentDeep(cursor, true);
        } finally {
            cursor.close();
        }
    }
    
    /** Reads all available rows from the given cursor and returns a list of new ImageTO objects. */
    public List<Content> loadAllDeepFromCursor(Cursor cursor) {
        int count = cursor.getCount();
        List<Content> list = new ArrayList<Content>(count);
        
        if (cursor.moveToFirst()) {
            if (identityScope != null) {
                identityScope.lock();
                identityScope.reserveRoom(count);
            }
            try {
                do {
                    list.add(loadCurrentDeep(cursor, false));
                } while (cursor.moveToNext());
            } finally {
                if (identityScope != null) {
                    identityScope.unlock();
                }
            }
        }
        return list;
    }
    
    protected List<Content> loadDeepAllAndCloseCursor(Cursor cursor) {
        try {
            return loadAllDeepFromCursor(cursor);
        } finally {
            cursor.close();
        }
    }
    

    /** A raw-style query where you can pass any WHERE clause and arguments. */
    public List<Content> queryDeep(String where, String... selectionArg) {
        Cursor cursor = db.rawQuery(getSelectDeep() + where, selectionArg);
        return loadDeepAllAndCloseCursor(cursor);
    }
 
}
