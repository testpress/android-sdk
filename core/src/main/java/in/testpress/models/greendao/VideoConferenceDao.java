package in.testpress.models.greendao;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.internal.DaoConfig;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "VIDEO_CONFERENCE".
*/
public class VideoConferenceDao extends AbstractDao<VideoConference, Long> {

    public static final String TABLENAME = "VIDEO_CONFERENCE";

    /**
     * Properties of entity VideoConference.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property Title = new Property(0, String.class, "title", false, "TITLE");
        public final static Property JoinUrl = new Property(1, String.class, "joinUrl", false, "JOIN_URL");
        public final static Property Id = new Property(2, Long.class, "id", true, "ID");
        public final static Property Start = new Property(3, String.class, "start", false, "START");
        public final static Property Duration = new Property(4, Integer.class, "duration", false, "DURATION");
        public final static Property Provider = new Property(5, String.class, "provider", false, "PROVIDER");
        public final static Property ConferenceId = new Property(6, String.class, "conferenceId", false, "CONFERENCE_ID");
    }


    public VideoConferenceDao(DaoConfig config) {
        super(config);
    }
    
    public VideoConferenceDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"VIDEO_CONFERENCE\" (" + //
                "\"TITLE\" TEXT," + // 0: title
                "\"JOIN_URL\" TEXT," + // 1: joinUrl
                "\"ID\" INTEGER PRIMARY KEY ," + // 2: id
                "\"START\" TEXT," + // 3: start
                "\"DURATION\" INTEGER," + // 4: duration
                "\"PROVIDER\" TEXT," + // 5: provider
                "\"CONFERENCE_ID\" TEXT);"); // 6: conferenceId
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"VIDEO_CONFERENCE\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, VideoConference entity) {
        stmt.clearBindings();
 
        String title = entity.getTitle();
        if (title != null) {
            stmt.bindString(1, title);
        }
 
        String joinUrl = entity.getJoinUrl();
        if (joinUrl != null) {
            stmt.bindString(2, joinUrl);
        }
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(3, id);
        }
 
        String start = entity.getStart();
        if (start != null) {
            stmt.bindString(4, start);
        }
 
        Integer duration = entity.getDuration();
        if (duration != null) {
            stmt.bindLong(5, duration);
        }
 
        String provider = entity.getProvider();
        if (provider != null) {
            stmt.bindString(6, provider);
        }
 
        String conferenceId = entity.getConferenceId();
        if (conferenceId != null) {
            stmt.bindString(7, conferenceId);
        }
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, VideoConference entity) {
        stmt.clearBindings();
 
        String title = entity.getTitle();
        if (title != null) {
            stmt.bindString(1, title);
        }
 
        String joinUrl = entity.getJoinUrl();
        if (joinUrl != null) {
            stmt.bindString(2, joinUrl);
        }
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(3, id);
        }
 
        String start = entity.getStart();
        if (start != null) {
            stmt.bindString(4, start);
        }
 
        Integer duration = entity.getDuration();
        if (duration != null) {
            stmt.bindLong(5, duration);
        }
 
        String provider = entity.getProvider();
        if (provider != null) {
            stmt.bindString(6, provider);
        }
 
        String conferenceId = entity.getConferenceId();
        if (conferenceId != null) {
            stmt.bindString(7, conferenceId);
        }
    }

    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 2) ? null : cursor.getLong(offset + 2);
    }    

    @Override
    public VideoConference readEntity(Cursor cursor, int offset) {
        VideoConference entity = new VideoConference( //
            cursor.isNull(offset + 0) ? null : cursor.getString(offset + 0), // title
            cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1), // joinUrl
            cursor.isNull(offset + 2) ? null : cursor.getLong(offset + 2), // id
            cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3), // start
            cursor.isNull(offset + 4) ? null : cursor.getInt(offset + 4), // duration
            cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5), // provider
            cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6) // conferenceId
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, VideoConference entity, int offset) {
        entity.setTitle(cursor.isNull(offset + 0) ? null : cursor.getString(offset + 0));
        entity.setJoinUrl(cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1));
        entity.setId(cursor.isNull(offset + 2) ? null : cursor.getLong(offset + 2));
        entity.setStart(cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3));
        entity.setDuration(cursor.isNull(offset + 4) ? null : cursor.getInt(offset + 4));
        entity.setProvider(cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5));
        entity.setConferenceId(cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6));
     }
    
    @Override
    protected final Long updateKeyAfterInsert(VideoConference entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    @Override
    public Long getKey(VideoConference entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    @Override
    public boolean hasKey(VideoConference entity) {
        return entity.getId() != null;
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
}
