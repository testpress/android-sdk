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
        public final static Property ChapterId = new Property(5, Integer.class, "chapterId", false, "CHAPTER_ID");
        public final static Property ChapterSlug = new Property(6, String.class, "chapterSlug", false, "CHAPTER_SLUG");
        public final static Property ChapterUrl = new Property(7, String.class, "chapterUrl", false, "CHAPTER_URL");
        public final static Property Id = new Property(8, Long.class, "id", true, "ID");
        public final static Property Name = new Property(9, String.class, "name", false, "NAME");
        public final static Property Image = new Property(10, String.class, "image", false, "IMAGE");
        public final static Property Description = new Property(11, String.class, "description", false, "DESCRIPTION");
        public final static Property IsLocked = new Property(12, Boolean.class, "isLocked", false, "IS_LOCKED");
        public final static Property AttemptsCount = new Property(13, Integer.class, "attemptsCount", false, "ATTEMPTS_COUNT");
        public final static Property Start = new Property(14, String.class, "start", false, "START");
        public final static Property End = new Property(15, String.class, "end", false, "END");
        public final static Property HasStarted = new Property(16, Boolean.class, "hasStarted", false, "HAS_STARTED");
        public final static Property Active = new Property(17, Boolean.class, "active", false, "ACTIVE");
        public final static Property BookmarkId = new Property(18, Long.class, "bookmarkId", false, "BOOKMARK_ID");
        public final static Property HtmlId = new Property(19, Long.class, "htmlId", false, "HTML_ID");
        public final static Property VideoId = new Property(20, Long.class, "videoId", false, "VIDEO_ID");
        public final static Property AttachmentId = new Property(21, Long.class, "attachmentId", false, "ATTACHMENT_ID");
        public final static Property ExamId = new Property(22, Long.class, "examId", false, "EXAM_ID");
    }

    private DaoSession daoSession;


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
                "\"CHAPTER_ID\" INTEGER," + // 5: chapterId
                "\"CHAPTER_SLUG\" TEXT," + // 6: chapterSlug
                "\"CHAPTER_URL\" TEXT," + // 7: chapterUrl
                "\"ID\" INTEGER PRIMARY KEY ," + // 8: id
                "\"NAME\" TEXT," + // 9: name
                "\"IMAGE\" TEXT," + // 10: image
                "\"DESCRIPTION\" TEXT," + // 11: description
                "\"IS_LOCKED\" INTEGER," + // 12: isLocked
                "\"ATTEMPTS_COUNT\" INTEGER," + // 13: attemptsCount
                "\"START\" TEXT," + // 14: start
                "\"END\" TEXT," + // 15: end
                "\"HAS_STARTED\" INTEGER," + // 16: hasStarted
                "\"ACTIVE\" INTEGER," + // 17: active
                "\"BOOKMARK_ID\" INTEGER," + // 18: bookmarkId
                "\"HTML_ID\" INTEGER," + // 19: htmlId
                "\"VIDEO_ID\" INTEGER," + // 20: videoId
                "\"ATTACHMENT_ID\" INTEGER," + // 21: attachmentId
                "\"EXAM_ID\" INTEGER);"); // 22: examId
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
 
        Integer chapterId = entity.getChapterId();
        if (chapterId != null) {
            stmt.bindLong(6, chapterId);
        }
 
        String chapterSlug = entity.getChapterSlug();
        if (chapterSlug != null) {
            stmt.bindString(7, chapterSlug);
        }
 
        String chapterUrl = entity.getChapterUrl();
        if (chapterUrl != null) {
            stmt.bindString(8, chapterUrl);
        }
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(9, id);
        }
 
        String name = entity.getName();
        if (name != null) {
            stmt.bindString(10, name);
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
 
        Integer attemptsCount = entity.getAttemptsCount();
        if (attemptsCount != null) {
            stmt.bindLong(14, attemptsCount);
        }
 
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
 
        Long htmlId = entity.getHtmlId();
        if (htmlId != null) {
            stmt.bindLong(20, htmlId);
        }
 
        Long videoId = entity.getVideoId();
        if (videoId != null) {
            stmt.bindLong(21, videoId);
        }
 
        Long attachmentId = entity.getAttachmentId();
        if (attachmentId != null) {
            stmt.bindLong(22, attachmentId);
        }
 
        Long examId = entity.getExamId();
        if (examId != null) {
            stmt.bindLong(23, examId);
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
 
        Integer chapterId = entity.getChapterId();
        if (chapterId != null) {
            stmt.bindLong(6, chapterId);
        }
 
        String chapterSlug = entity.getChapterSlug();
        if (chapterSlug != null) {
            stmt.bindString(7, chapterSlug);
        }
 
        String chapterUrl = entity.getChapterUrl();
        if (chapterUrl != null) {
            stmt.bindString(8, chapterUrl);
        }
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(9, id);
        }
 
        String name = entity.getName();
        if (name != null) {
            stmt.bindString(10, name);
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
 
        Integer attemptsCount = entity.getAttemptsCount();
        if (attemptsCount != null) {
            stmt.bindLong(14, attemptsCount);
        }
 
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
 
        Long htmlId = entity.getHtmlId();
        if (htmlId != null) {
            stmt.bindLong(20, htmlId);
        }
 
        Long videoId = entity.getVideoId();
        if (videoId != null) {
            stmt.bindLong(21, videoId);
        }
 
        Long attachmentId = entity.getAttachmentId();
        if (attachmentId != null) {
            stmt.bindLong(22, attachmentId);
        }
 
        Long examId = entity.getExamId();
        if (examId != null) {
            stmt.bindLong(23, examId);
        }
    }

    @Override
    protected final void attachEntity(Content entity) {
        super.attachEntity(entity);
        entity.__setDaoSession(daoSession);
    }

    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 8) ? null : cursor.getLong(offset + 8);
    }    

    @Override
    public Content readEntity(Cursor cursor, int offset) {
        Content entity = new Content( //
            cursor.isNull(offset + 0) ? null : cursor.getInt(offset + 0), // order
            cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1), // htmlContentTitle
            cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2), // htmlContentUrl
            cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3), // url
            cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4), // attemptsUrl
            cursor.isNull(offset + 5) ? null : cursor.getInt(offset + 5), // chapterId
            cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6), // chapterSlug
            cursor.isNull(offset + 7) ? null : cursor.getString(offset + 7), // chapterUrl
            cursor.isNull(offset + 8) ? null : cursor.getLong(offset + 8), // id
            cursor.isNull(offset + 9) ? null : cursor.getString(offset + 9), // name
            cursor.isNull(offset + 10) ? null : cursor.getString(offset + 10), // image
            cursor.isNull(offset + 11) ? null : cursor.getString(offset + 11), // description
            cursor.isNull(offset + 12) ? null : cursor.getShort(offset + 12) != 0, // isLocked
            cursor.isNull(offset + 13) ? null : cursor.getInt(offset + 13), // attemptsCount
            cursor.isNull(offset + 14) ? null : cursor.getString(offset + 14), // start
            cursor.isNull(offset + 15) ? null : cursor.getString(offset + 15), // end
            cursor.isNull(offset + 16) ? null : cursor.getShort(offset + 16) != 0, // hasStarted
            cursor.isNull(offset + 17) ? null : cursor.getShort(offset + 17) != 0, // active
            cursor.isNull(offset + 18) ? null : cursor.getLong(offset + 18), // bookmarkId
            cursor.isNull(offset + 19) ? null : cursor.getLong(offset + 19), // htmlId
            cursor.isNull(offset + 20) ? null : cursor.getLong(offset + 20), // videoId
            cursor.isNull(offset + 21) ? null : cursor.getLong(offset + 21), // attachmentId
            cursor.isNull(offset + 22) ? null : cursor.getLong(offset + 22) // examId
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
        entity.setChapterId(cursor.isNull(offset + 5) ? null : cursor.getInt(offset + 5));
        entity.setChapterSlug(cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6));
        entity.setChapterUrl(cursor.isNull(offset + 7) ? null : cursor.getString(offset + 7));
        entity.setId(cursor.isNull(offset + 8) ? null : cursor.getLong(offset + 8));
        entity.setName(cursor.isNull(offset + 9) ? null : cursor.getString(offset + 9));
        entity.setImage(cursor.isNull(offset + 10) ? null : cursor.getString(offset + 10));
        entity.setDescription(cursor.isNull(offset + 11) ? null : cursor.getString(offset + 11));
        entity.setIsLocked(cursor.isNull(offset + 12) ? null : cursor.getShort(offset + 12) != 0);
        entity.setAttemptsCount(cursor.isNull(offset + 13) ? null : cursor.getInt(offset + 13));
        entity.setStart(cursor.isNull(offset + 14) ? null : cursor.getString(offset + 14));
        entity.setEnd(cursor.isNull(offset + 15) ? null : cursor.getString(offset + 15));
        entity.setHasStarted(cursor.isNull(offset + 16) ? null : cursor.getShort(offset + 16) != 0);
        entity.setActive(cursor.isNull(offset + 17) ? null : cursor.getShort(offset + 17) != 0);
        entity.setBookmarkId(cursor.isNull(offset + 18) ? null : cursor.getLong(offset + 18));
        entity.setHtmlId(cursor.isNull(offset + 19) ? null : cursor.getLong(offset + 19));
        entity.setVideoId(cursor.isNull(offset + 20) ? null : cursor.getLong(offset + 20));
        entity.setAttachmentId(cursor.isNull(offset + 21) ? null : cursor.getLong(offset + 21));
        entity.setExamId(cursor.isNull(offset + 22) ? null : cursor.getLong(offset + 22));
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
    
    private String selectDeep;

    protected String getSelectDeep() {
        if (selectDeep == null) {
            StringBuilder builder = new StringBuilder("SELECT ");
            SqlUtils.appendColumns(builder, "T", getAllColumns());
            builder.append(',');
            SqlUtils.appendColumns(builder, "T0", daoSession.getHtmlContentDao().getAllColumns());
            builder.append(',');
            SqlUtils.appendColumns(builder, "T1", daoSession.getVideoDao().getAllColumns());
            builder.append(',');
            SqlUtils.appendColumns(builder, "T2", daoSession.getAttachmentDao().getAllColumns());
            builder.append(',');
            SqlUtils.appendColumns(builder, "T3", daoSession.getExamDao().getAllColumns());
            builder.append(" FROM CONTENT T");
            builder.append(" LEFT JOIN HTML_CONTENT T0 ON T.\"HTML_ID\"=T0.\"ID\"");
            builder.append(" LEFT JOIN VIDEO T1 ON T.\"VIDEO_ID\"=T1.\"ID\"");
            builder.append(" LEFT JOIN ATTACHMENT T2 ON T.\"ATTACHMENT_ID\"=T2.\"ID\"");
            builder.append(" LEFT JOIN EXAM T3 ON T.\"EXAM_ID\"=T3.\"ID\"");
            builder.append(' ');
            selectDeep = builder.toString();
        }
        return selectDeep;
    }
    
    protected Content loadCurrentDeep(Cursor cursor, boolean lock) {
        Content entity = loadCurrent(cursor, 0, lock);
        int offset = getAllColumns().length;

        HtmlContent html = loadCurrentOther(daoSession.getHtmlContentDao(), cursor, offset);
        entity.setHtml(html);
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
