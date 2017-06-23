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
 * DAO for table "REVIEW_QUESTION".
*/
public class ReviewQuestionDao extends AbstractDao<ReviewQuestion, Long> {

    public static final String TABLENAME = "REVIEW_QUESTION";

    /**
     * Properties of entity ReviewQuestion.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "ID");
        public final static Property QuestionHtml = new Property(1, String.class, "questionHtml", false, "QUESTION_HTML");
        public final static Property Direction = new Property(2, String.class, "direction", false, "DIRECTION");
        public final static Property Subject = new Property(3, String.class, "subject", false, "SUBJECT");
        public final static Property ExplanationHtml = new Property(4, String.class, "explanationHtml", false, "EXPLANATION_HTML");
        public final static Property CommentsUrl = new Property(5, String.class, "commentsUrl", false, "COMMENTS_URL");
    }

    private DaoSession daoSession;


    public ReviewQuestionDao(DaoConfig config) {
        super(config);
    }
    
    public ReviewQuestionDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
        this.daoSession = daoSession;
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"REVIEW_QUESTION\" (" + //
                "\"ID\" INTEGER PRIMARY KEY ," + // 0: id
                "\"QUESTION_HTML\" TEXT," + // 1: questionHtml
                "\"DIRECTION\" TEXT," + // 2: direction
                "\"SUBJECT\" TEXT," + // 3: subject
                "\"EXPLANATION_HTML\" TEXT," + // 4: explanationHtml
                "\"COMMENTS_URL\" TEXT);"); // 5: commentsUrl
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"REVIEW_QUESTION\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, ReviewQuestion entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
 
        String questionHtml = entity.getQuestionHtml();
        if (questionHtml != null) {
            stmt.bindString(2, questionHtml);
        }
 
        String direction = entity.getDirection();
        if (direction != null) {
            stmt.bindString(3, direction);
        }
 
        String subject = entity.getSubject();
        if (subject != null) {
            stmt.bindString(4, subject);
        }
 
        String explanationHtml = entity.getExplanationHtml();
        if (explanationHtml != null) {
            stmt.bindString(5, explanationHtml);
        }
 
        String commentsUrl = entity.getCommentsUrl();
        if (commentsUrl != null) {
            stmt.bindString(6, commentsUrl);
        }
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, ReviewQuestion entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
 
        String questionHtml = entity.getQuestionHtml();
        if (questionHtml != null) {
            stmt.bindString(2, questionHtml);
        }
 
        String direction = entity.getDirection();
        if (direction != null) {
            stmt.bindString(3, direction);
        }
 
        String subject = entity.getSubject();
        if (subject != null) {
            stmt.bindString(4, subject);
        }
 
        String explanationHtml = entity.getExplanationHtml();
        if (explanationHtml != null) {
            stmt.bindString(5, explanationHtml);
        }
 
        String commentsUrl = entity.getCommentsUrl();
        if (commentsUrl != null) {
            stmt.bindString(6, commentsUrl);
        }
    }

    @Override
    protected final void attachEntity(ReviewQuestion entity) {
        super.attachEntity(entity);
        entity.__setDaoSession(daoSession);
    }

    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    @Override
    public ReviewQuestion readEntity(Cursor cursor, int offset) {
        ReviewQuestion entity = new ReviewQuestion( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
            cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1), // questionHtml
            cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2), // direction
            cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3), // subject
            cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4), // explanationHtml
            cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5) // commentsUrl
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, ReviewQuestion entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setQuestionHtml(cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1));
        entity.setDirection(cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2));
        entity.setSubject(cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3));
        entity.setExplanationHtml(cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4));
        entity.setCommentsUrl(cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5));
     }
    
    @Override
    protected final Long updateKeyAfterInsert(ReviewQuestion entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    @Override
    public Long getKey(ReviewQuestion entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    @Override
    public boolean hasKey(ReviewQuestion entity) {
        return entity.getId() != null;
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
}
