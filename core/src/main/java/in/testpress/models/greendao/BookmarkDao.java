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
 * DAO for table "BOOKMARK".
*/
public class BookmarkDao extends AbstractDao<Bookmark, Long> {

    public static final String TABLENAME = "BOOKMARK";

    /**
     * Properties of entity Bookmark.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "ID");
        public final static Property Folder = new Property(1, String.class, "folder", false, "FOLDER");
        public final static Property FolderId = new Property(2, Long.class, "folderId", false, "FOLDER_ID");
        public final static Property ObjectId = new Property(3, Long.class, "objectId", false, "OBJECT_ID");
        public final static Property Modified = new Property(4, String.class, "modified", false, "MODIFIED");
        public final static Property ModifiedDate = new Property(5, Long.class, "modifiedDate", false, "MODIFIED_DATE");
        public final static Property Created = new Property(6, String.class, "created", false, "CREATED");
        public final static Property CreatedDate = new Property(7, Long.class, "createdDate", false, "CREATED_DATE");
        public final static Property LoadedInAllFolder = new Property(8, Boolean.class, "loadedInAllFolder", false, "LOADED_IN_ALL_FOLDER");
        public final static Property LoadedInRespectiveFolder = new Property(9, Boolean.class, "loadedInRespectiveFolder", false, "LOADED_IN_RESPECTIVE_FOLDER");
        public final static Property Active = new Property(10, Boolean.class, "active", false, "ACTIVE");
        public final static Property ContentTypeId = new Property(11, Long.class, "contentTypeId", false, "CONTENT_TYPE_ID");
    }

    private DaoSession daoSession;


    public BookmarkDao(DaoConfig config) {
        super(config);
    }
    
    public BookmarkDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
        this.daoSession = daoSession;
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"BOOKMARK\" (" + //
                "\"ID\" INTEGER PRIMARY KEY ," + // 0: id
                "\"FOLDER\" TEXT," + // 1: folder
                "\"FOLDER_ID\" INTEGER," + // 2: folderId
                "\"OBJECT_ID\" INTEGER," + // 3: objectId
                "\"MODIFIED\" TEXT," + // 4: modified
                "\"MODIFIED_DATE\" INTEGER," + // 5: modifiedDate
                "\"CREATED\" TEXT," + // 6: created
                "\"CREATED_DATE\" INTEGER," + // 7: createdDate
                "\"LOADED_IN_ALL_FOLDER\" INTEGER," + // 8: loadedInAllFolder
                "\"LOADED_IN_RESPECTIVE_FOLDER\" INTEGER," + // 9: loadedInRespectiveFolder
                "\"ACTIVE\" INTEGER," + // 10: active
                "\"CONTENT_TYPE_ID\" INTEGER);"); // 11: contentTypeId
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"BOOKMARK\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, Bookmark entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
 
        String folder = entity.getFolder();
        if (folder != null) {
            stmt.bindString(2, folder);
        }
 
        Long folderId = entity.getFolderId();
        if (folderId != null) {
            stmt.bindLong(3, folderId);
        }
 
        Long objectId = entity.getObjectId();
        if (objectId != null) {
            stmt.bindLong(4, objectId);
        }
 
        String modified = entity.getModified();
        if (modified != null) {
            stmt.bindString(5, modified);
        }
 
        Long modifiedDate = entity.getModifiedDate();
        if (modifiedDate != null) {
            stmt.bindLong(6, modifiedDate);
        }
 
        String created = entity.getCreated();
        if (created != null) {
            stmt.bindString(7, created);
        }
 
        Long createdDate = entity.getCreatedDate();
        if (createdDate != null) {
            stmt.bindLong(8, createdDate);
        }
 
        Boolean loadedInAllFolder = entity.getLoadedInAllFolder();
        if (loadedInAllFolder != null) {
            stmt.bindLong(9, loadedInAllFolder ? 1L: 0L);
        }
 
        Boolean loadedInRespectiveFolder = entity.getLoadedInRespectiveFolder();
        if (loadedInRespectiveFolder != null) {
            stmt.bindLong(10, loadedInRespectiveFolder ? 1L: 0L);
        }
 
        Boolean active = entity.getActive();
        if (active != null) {
            stmt.bindLong(11, active ? 1L: 0L);
        }
 
        Long contentTypeId = entity.getContentTypeId();
        if (contentTypeId != null) {
            stmt.bindLong(12, contentTypeId);
        }
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, Bookmark entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
 
        String folder = entity.getFolder();
        if (folder != null) {
            stmt.bindString(2, folder);
        }
 
        Long folderId = entity.getFolderId();
        if (folderId != null) {
            stmt.bindLong(3, folderId);
        }
 
        Long objectId = entity.getObjectId();
        if (objectId != null) {
            stmt.bindLong(4, objectId);
        }
 
        String modified = entity.getModified();
        if (modified != null) {
            stmt.bindString(5, modified);
        }
 
        Long modifiedDate = entity.getModifiedDate();
        if (modifiedDate != null) {
            stmt.bindLong(6, modifiedDate);
        }
 
        String created = entity.getCreated();
        if (created != null) {
            stmt.bindString(7, created);
        }
 
        Long createdDate = entity.getCreatedDate();
        if (createdDate != null) {
            stmt.bindLong(8, createdDate);
        }
 
        Boolean loadedInAllFolder = entity.getLoadedInAllFolder();
        if (loadedInAllFolder != null) {
            stmt.bindLong(9, loadedInAllFolder ? 1L: 0L);
        }
 
        Boolean loadedInRespectiveFolder = entity.getLoadedInRespectiveFolder();
        if (loadedInRespectiveFolder != null) {
            stmt.bindLong(10, loadedInRespectiveFolder ? 1L: 0L);
        }
 
        Boolean active = entity.getActive();
        if (active != null) {
            stmt.bindLong(11, active ? 1L: 0L);
        }
 
        Long contentTypeId = entity.getContentTypeId();
        if (contentTypeId != null) {
            stmt.bindLong(12, contentTypeId);
        }
    }

    @Override
    protected final void attachEntity(Bookmark entity) {
        super.attachEntity(entity);
        entity.__setDaoSession(daoSession);
    }

    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    @Override
    public Bookmark readEntity(Cursor cursor, int offset) {
        Bookmark entity = new Bookmark( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
            cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1), // folder
            cursor.isNull(offset + 2) ? null : cursor.getLong(offset + 2), // folderId
            cursor.isNull(offset + 3) ? null : cursor.getLong(offset + 3), // objectId
            cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4), // modified
            cursor.isNull(offset + 5) ? null : cursor.getLong(offset + 5), // modifiedDate
            cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6), // created
            cursor.isNull(offset + 7) ? null : cursor.getLong(offset + 7), // createdDate
            cursor.isNull(offset + 8) ? null : cursor.getShort(offset + 8) != 0, // loadedInAllFolder
            cursor.isNull(offset + 9) ? null : cursor.getShort(offset + 9) != 0, // loadedInRespectiveFolder
            cursor.isNull(offset + 10) ? null : cursor.getShort(offset + 10) != 0, // active
            cursor.isNull(offset + 11) ? null : cursor.getLong(offset + 11) // contentTypeId
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, Bookmark entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setFolder(cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1));
        entity.setFolderId(cursor.isNull(offset + 2) ? null : cursor.getLong(offset + 2));
        entity.setObjectId(cursor.isNull(offset + 3) ? null : cursor.getLong(offset + 3));
        entity.setModified(cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4));
        entity.setModifiedDate(cursor.isNull(offset + 5) ? null : cursor.getLong(offset + 5));
        entity.setCreated(cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6));
        entity.setCreatedDate(cursor.isNull(offset + 7) ? null : cursor.getLong(offset + 7));
        entity.setLoadedInAllFolder(cursor.isNull(offset + 8) ? null : cursor.getShort(offset + 8) != 0);
        entity.setLoadedInRespectiveFolder(cursor.isNull(offset + 9) ? null : cursor.getShort(offset + 9) != 0);
        entity.setActive(cursor.isNull(offset + 10) ? null : cursor.getShort(offset + 10) != 0);
        entity.setContentTypeId(cursor.isNull(offset + 11) ? null : cursor.getLong(offset + 11));
     }
    
    @Override
    protected final Long updateKeyAfterInsert(Bookmark entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    @Override
    public Long getKey(Bookmark entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    @Override
    public boolean hasKey(Bookmark entity) {
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
            SqlUtils.appendColumns(builder, "T0", daoSession.getContentTypeDao().getAllColumns());
            builder.append(" FROM BOOKMARK T");
            builder.append(" LEFT JOIN CONTENT_TYPE T0 ON T.\"CONTENT_TYPE_ID\"=T0.\"ID\"");
            builder.append(' ');
            selectDeep = builder.toString();
        }
        return selectDeep;
    }
    
    protected Bookmark loadCurrentDeep(Cursor cursor, boolean lock) {
        Bookmark entity = loadCurrent(cursor, 0, lock);
        int offset = getAllColumns().length;

        ContentType contentType = loadCurrentOther(daoSession.getContentTypeDao(), cursor, offset);
        entity.setContentType(contentType);

        return entity;    
    }

    public Bookmark loadDeep(Long key) {
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
    public List<Bookmark> loadAllDeepFromCursor(Cursor cursor) {
        int count = cursor.getCount();
        List<Bookmark> list = new ArrayList<Bookmark>(count);
        
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
    
    protected List<Bookmark> loadDeepAllAndCloseCursor(Cursor cursor) {
        try {
            return loadAllDeepFromCursor(cursor);
        } finally {
            cursor.close();
        }
    }
    

    /** A raw-style query where you can pass any WHERE clause and arguments. */
    public List<Bookmark> queryDeep(String where, String... selectionArg) {
        Cursor cursor = db.rawQuery(getSelectDeep() + where, selectionArg);
        return loadDeepAllAndCloseCursor(cursor);
    }
 
}
