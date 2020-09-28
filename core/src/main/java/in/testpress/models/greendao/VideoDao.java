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
 * DAO for table "VIDEO".
*/
public class VideoDao extends AbstractDao<Video, Long> {

    public static final String TABLENAME = "VIDEO";

    /**
     * Properties of entity Video.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property Title = new Property(0, String.class, "title", false, "TITLE");
        public final static Property Url = new Property(1, String.class, "url", false, "URL");
        public final static Property Id = new Property(2, Long.class, "id", true, "ID");
        public final static Property EmbedCode = new Property(3, String.class, "embedCode", false, "EMBED_CODE");
        public final static Property Duration = new Property(4, String.class, "duration", false, "DURATION");
        public final static Property IsDomainRestricted = new Property(5, Boolean.class, "isDomainRestricted", false, "IS_DOMAIN_RESTRICTED");
        public final static Property Thumbnail = new Property(6, String.class, "thumbnail", false, "THUMBNAIL");
        public final static Property ThumbnailMedium = new Property(7, String.class, "thumbnailMedium", false, "THUMBNAIL_MEDIUM");
        public final static Property ThumbnailSmall = new Property(8, String.class, "thumbnailSmall", false, "THUMBNAIL_SMALL");
        public final static Property StreamId = new Property(9, Long.class, "streamId", false, "STREAM_ID");
    }

    private DaoSession daoSession;


    public VideoDao(DaoConfig config) {
        super(config);
    }
    
    public VideoDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
        this.daoSession = daoSession;
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"VIDEO\" (" + //
                "\"TITLE\" TEXT," + // 0: title
                "\"URL\" TEXT," + // 1: url
                "\"ID\" INTEGER PRIMARY KEY ," + // 2: id
                "\"EMBED_CODE\" TEXT," + // 3: embedCode
                "\"DURATION\" TEXT," + // 4: duration
                "\"IS_DOMAIN_RESTRICTED\" INTEGER," + // 5: isDomainRestricted
                "\"THUMBNAIL\" TEXT," + // 6: thumbnail
                "\"THUMBNAIL_MEDIUM\" TEXT," + // 7: thumbnailMedium
                "\"THUMBNAIL_SMALL\" TEXT," + // 8: thumbnailSmall
                "\"STREAM_ID\" INTEGER);"); // 9: streamId
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"VIDEO\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, Video entity) {
        stmt.clearBindings();
 
        String title = entity.getTitle();
        if (title != null) {
            stmt.bindString(1, title);
        }
 
        String url = entity.getUrl();
        if (url != null) {
            stmt.bindString(2, url);
        }
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(3, id);
        }
 
        String embedCode = entity.getEmbedCode();
        if (embedCode != null) {
            stmt.bindString(4, embedCode);
        }
 
        String duration = entity.getDuration();
        if (duration != null) {
            stmt.bindString(5, duration);
        }
 
        Boolean isDomainRestricted = entity.getIsDomainRestricted();
        if (isDomainRestricted != null) {
            stmt.bindLong(6, isDomainRestricted ? 1L: 0L);
        }
 
        String thumbnail = entity.getThumbnail();
        if (thumbnail != null) {
            stmt.bindString(7, thumbnail);
        }
 
        String thumbnailMedium = entity.getThumbnailMedium();
        if (thumbnailMedium != null) {
            stmt.bindString(8, thumbnailMedium);
        }
 
        String thumbnailSmall = entity.getThumbnailSmall();
        if (thumbnailSmall != null) {
            stmt.bindString(9, thumbnailSmall);
        }
 
        Long streamId = entity.getStreamId();
        if (streamId != null) {
            stmt.bindLong(10, streamId);
        }
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, Video entity) {
        stmt.clearBindings();
 
        String title = entity.getTitle();
        if (title != null) {
            stmt.bindString(1, title);
        }
 
        String url = entity.getUrl();
        if (url != null) {
            stmt.bindString(2, url);
        }
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(3, id);
        }
 
        String embedCode = entity.getEmbedCode();
        if (embedCode != null) {
            stmt.bindString(4, embedCode);
        }
 
        String duration = entity.getDuration();
        if (duration != null) {
            stmt.bindString(5, duration);
        }
 
        Boolean isDomainRestricted = entity.getIsDomainRestricted();
        if (isDomainRestricted != null) {
            stmt.bindLong(6, isDomainRestricted ? 1L: 0L);
        }
 
        String thumbnail = entity.getThumbnail();
        if (thumbnail != null) {
            stmt.bindString(7, thumbnail);
        }
 
        String thumbnailMedium = entity.getThumbnailMedium();
        if (thumbnailMedium != null) {
            stmt.bindString(8, thumbnailMedium);
        }
 
        String thumbnailSmall = entity.getThumbnailSmall();
        if (thumbnailSmall != null) {
            stmt.bindString(9, thumbnailSmall);
        }
 
        Long streamId = entity.getStreamId();
        if (streamId != null) {
            stmt.bindLong(10, streamId);
        }
    }

    @Override
    protected final void attachEntity(Video entity) {
        super.attachEntity(entity);
        entity.__setDaoSession(daoSession);
    }

    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 2) ? null : cursor.getLong(offset + 2);
    }    

    @Override
    public Video readEntity(Cursor cursor, int offset) {
        Video entity = new Video( //
            cursor.isNull(offset + 0) ? null : cursor.getString(offset + 0), // title
            cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1), // url
            cursor.isNull(offset + 2) ? null : cursor.getLong(offset + 2), // id
            cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3), // embedCode
            cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4), // duration
            cursor.isNull(offset + 5) ? null : cursor.getShort(offset + 5) != 0, // isDomainRestricted
            cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6), // thumbnail
            cursor.isNull(offset + 7) ? null : cursor.getString(offset + 7), // thumbnailMedium
            cursor.isNull(offset + 8) ? null : cursor.getString(offset + 8), // thumbnailSmall
            cursor.isNull(offset + 9) ? null : cursor.getLong(offset + 9) // streamId
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, Video entity, int offset) {
        entity.setTitle(cursor.isNull(offset + 0) ? null : cursor.getString(offset + 0));
        entity.setUrl(cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1));
        entity.setId(cursor.isNull(offset + 2) ? null : cursor.getLong(offset + 2));
        entity.setEmbedCode(cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3));
        entity.setDuration(cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4));
        entity.setIsDomainRestricted(cursor.isNull(offset + 5) ? null : cursor.getShort(offset + 5) != 0);
        entity.setThumbnail(cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6));
        entity.setThumbnailMedium(cursor.isNull(offset + 7) ? null : cursor.getString(offset + 7));
        entity.setThumbnailSmall(cursor.isNull(offset + 8) ? null : cursor.getString(offset + 8));
        entity.setStreamId(cursor.isNull(offset + 9) ? null : cursor.getLong(offset + 9));
     }
    
    @Override
    protected final Long updateKeyAfterInsert(Video entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    @Override
    public Long getKey(Video entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    @Override
    public boolean hasKey(Video entity) {
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
            SqlUtils.appendColumns(builder, "T0", daoSession.getStreamDao().getAllColumns());
            builder.append(" FROM VIDEO T");
            builder.append(" LEFT JOIN STREAM T0 ON T.\"STREAM_ID\"=T0.\"_id\"");
            builder.append(' ');
            selectDeep = builder.toString();
        }
        return selectDeep;
    }
    
    protected Video loadCurrentDeep(Cursor cursor, boolean lock) {
        Video entity = loadCurrent(cursor, 0, lock);
        int offset = getAllColumns().length;

        Stream stream = loadCurrentOther(daoSession.getStreamDao(), cursor, offset);
        entity.setStream(stream);

        return entity;    
    }

    public Video loadDeep(Long key) {
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
    public List<Video> loadAllDeepFromCursor(Cursor cursor) {
        int count = cursor.getCount();
        List<Video> list = new ArrayList<Video>(count);
        
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
    
    protected List<Video> loadDeepAllAndCloseCursor(Cursor cursor) {
        try {
            return loadAllDeepFromCursor(cursor);
        } finally {
            cursor.close();
        }
    }
    

    /** A raw-style query where you can pass any WHERE clause and arguments. */
    public List<Video> queryDeep(String where, String... selectionArg) {
        Cursor cursor = db.rawQuery(getSelectDeep() + where, selectionArg);
        return loadDeepAllAndCloseCursor(cursor);
    }
 
}
