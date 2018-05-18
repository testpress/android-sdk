package in.testpress.models.greendao;

import java.util.List;
import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.internal.DaoConfig;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;
import org.greenrobot.greendao.query.Query;
import org.greenrobot.greendao.query.QueryBuilder;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "ANSWER_TRANSLATION".
*/
public class AnswerTranslationDao extends AbstractDao<AnswerTranslation, Long> {

    public static final String TABLENAME = "ANSWER_TRANSLATION";

    /**
     * Properties of entity AnswerTranslation.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "ID");
        public final static Property TextHtml = new Property(1, String.class, "textHtml", false, "TEXT_HTML");
        public final static Property AnswerId = new Property(2, Long.class, "answerId", false, "ANSWER_ID");
        public final static Property QuestionId = new Property(3, Long.class, "questionId", false, "QUESTION_ID");
    }

    private Query<AnswerTranslation> reviewQuestionTranslation_AnswerTranslationsQuery;

    public AnswerTranslationDao(DaoConfig config) {
        super(config);
    }
    
    public AnswerTranslationDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"ANSWER_TRANSLATION\" (" + //
                "\"ID\" INTEGER PRIMARY KEY ," + // 0: id
                "\"TEXT_HTML\" TEXT," + // 1: textHtml
                "\"ANSWER_ID\" INTEGER," + // 2: answerId
                "\"QUESTION_ID\" INTEGER);"); // 3: questionId
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"ANSWER_TRANSLATION\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, AnswerTranslation entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
 
        String textHtml = entity.getTextHtml();
        if (textHtml != null) {
            stmt.bindString(2, textHtml);
        }
 
        Long answerId = entity.getAnswerId();
        if (answerId != null) {
            stmt.bindLong(3, answerId);
        }
 
        Long questionId = entity.getQuestionId();
        if (questionId != null) {
            stmt.bindLong(4, questionId);
        }
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, AnswerTranslation entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
 
        String textHtml = entity.getTextHtml();
        if (textHtml != null) {
            stmt.bindString(2, textHtml);
        }
 
        Long answerId = entity.getAnswerId();
        if (answerId != null) {
            stmt.bindLong(3, answerId);
        }
 
        Long questionId = entity.getQuestionId();
        if (questionId != null) {
            stmt.bindLong(4, questionId);
        }
    }

    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    @Override
    public AnswerTranslation readEntity(Cursor cursor, int offset) {
        AnswerTranslation entity = new AnswerTranslation( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
            cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1), // textHtml
            cursor.isNull(offset + 2) ? null : cursor.getLong(offset + 2), // answerId
            cursor.isNull(offset + 3) ? null : cursor.getLong(offset + 3) // questionId
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, AnswerTranslation entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setTextHtml(cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1));
        entity.setAnswerId(cursor.isNull(offset + 2) ? null : cursor.getLong(offset + 2));
        entity.setQuestionId(cursor.isNull(offset + 3) ? null : cursor.getLong(offset + 3));
     }
    
    @Override
    protected final Long updateKeyAfterInsert(AnswerTranslation entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    @Override
    public Long getKey(AnswerTranslation entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    @Override
    public boolean hasKey(AnswerTranslation entity) {
        return entity.getId() != null;
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
    /** Internal query to resolve the "answerTranslations" to-many relationship of ReviewQuestionTranslation. */
    public List<AnswerTranslation> _queryReviewQuestionTranslation_AnswerTranslations(Long questionId) {
        synchronized (this) {
            if (reviewQuestionTranslation_AnswerTranslationsQuery == null) {
                QueryBuilder<AnswerTranslation> queryBuilder = queryBuilder();
                queryBuilder.where(Properties.QuestionId.eq(null));
                reviewQuestionTranslation_AnswerTranslationsQuery = queryBuilder.build();
            }
        }
        Query<AnswerTranslation> query = reviewQuestionTranslation_AnswerTranslationsQuery.forCurrentThread();
        query.setParameter(0, questionId);
        return query.list();
    }

}
