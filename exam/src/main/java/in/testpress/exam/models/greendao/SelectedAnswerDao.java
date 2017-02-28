package in.testpress.exam.models.greendao;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.internal.DaoConfig;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "SELECTED_ANSWER".
*/
public class SelectedAnswerDao extends AbstractDao<SelectedAnswer, Long> {

    public static final String TABLENAME = "SELECTED_ANSWER";

    /**
     * Properties of entity SelectedAnswer.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "_id");
        public final static Property AnswerId = new Property(1, Integer.class, "answerId", false, "ANSWER_ID");
        public final static Property ReviewItemId = new Property(2, Long.class, "reviewItemId", false, "REVIEW_ITEM_ID");
    }


    public SelectedAnswerDao(DaoConfig config) {
        super(config);
    }
    
    public SelectedAnswerDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"SELECTED_ANSWER\" (" + //
                "\"_id\" INTEGER PRIMARY KEY AUTOINCREMENT ," + // 0: id
                "\"ANSWER_ID\" INTEGER," + // 1: answerId
                "\"REVIEW_ITEM_ID\" INTEGER);"); // 2: reviewItemId
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"SELECTED_ANSWER\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, SelectedAnswer entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
 
        Integer answerId = entity.getAnswerId();
        if (answerId != null) {
            stmt.bindLong(2, answerId);
        }
 
        Long reviewItemId = entity.getReviewItemId();
        if (reviewItemId != null) {
            stmt.bindLong(3, reviewItemId);
        }
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, SelectedAnswer entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
 
        Integer answerId = entity.getAnswerId();
        if (answerId != null) {
            stmt.bindLong(2, answerId);
        }
 
        Long reviewItemId = entity.getReviewItemId();
        if (reviewItemId != null) {
            stmt.bindLong(3, reviewItemId);
        }
    }

    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    @Override
    public SelectedAnswer readEntity(Cursor cursor, int offset) {
        SelectedAnswer entity = new SelectedAnswer( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
            cursor.isNull(offset + 1) ? null : cursor.getInt(offset + 1), // answerId
            cursor.isNull(offset + 2) ? null : cursor.getLong(offset + 2) // reviewItemId
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, SelectedAnswer entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setAnswerId(cursor.isNull(offset + 1) ? null : cursor.getInt(offset + 1));
        entity.setReviewItemId(cursor.isNull(offset + 2) ? null : cursor.getLong(offset + 2));
     }
    
    @Override
    protected final Long updateKeyAfterInsert(SelectedAnswer entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    @Override
    public Long getKey(SelectedAnswer entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    @Override
    public boolean hasKey(SelectedAnswer entity) {
        return entity.getId() != null;
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
}
