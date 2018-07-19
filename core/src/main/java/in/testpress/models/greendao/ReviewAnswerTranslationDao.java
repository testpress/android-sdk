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
 * DAO for table "REVIEW_ANSWER_TRANSLATION".
*/
public class ReviewAnswerTranslationDao extends AbstractDao<ReviewAnswerTranslation, Void> {

    public static final String TABLENAME = "REVIEW_ANSWER_TRANSLATION";

    /**
     * Properties of entity ReviewAnswerTranslation.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", false, "ID");
        public final static Property TextHtml = new Property(1, String.class, "textHtml", false, "TEXT_HTML");
        public final static Property IsCorrect = new Property(2, Boolean.class, "isCorrect", false, "IS_CORRECT");
        public final static Property Marks = new Property(3, String.class, "marks", false, "MARKS");
        public final static Property QuestionTranslationId = new Property(4, Long.class, "questionTranslationId", false, "QUESTION_TRANSLATION_ID");
    }

    private Query<ReviewAnswerTranslation> reviewQuestionTranslation_AnswersQuery;

    public ReviewAnswerTranslationDao(DaoConfig config) {
        super(config);
    }
    
    public ReviewAnswerTranslationDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"REVIEW_ANSWER_TRANSLATION\" (" + //
                "\"ID\" INTEGER," + // 0: id
                "\"TEXT_HTML\" TEXT," + // 1: textHtml
                "\"IS_CORRECT\" INTEGER," + // 2: isCorrect
                "\"MARKS\" TEXT," + // 3: marks
                "\"QUESTION_TRANSLATION_ID\" INTEGER);"); // 4: questionTranslationId
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"REVIEW_ANSWER_TRANSLATION\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, ReviewAnswerTranslation entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
 
        String textHtml = entity.getTextHtml();
        if (textHtml != null) {
            stmt.bindString(2, textHtml);
        }
 
        Boolean isCorrect = entity.getIsCorrect();
        if (isCorrect != null) {
            stmt.bindLong(3, isCorrect ? 1L: 0L);
        }
 
        String marks = entity.getMarks();
        if (marks != null) {
            stmt.bindString(4, marks);
        }
 
        Long questionTranslationId = entity.getQuestionTranslationId();
        if (questionTranslationId != null) {
            stmt.bindLong(5, questionTranslationId);
        }
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, ReviewAnswerTranslation entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
 
        String textHtml = entity.getTextHtml();
        if (textHtml != null) {
            stmt.bindString(2, textHtml);
        }
 
        Boolean isCorrect = entity.getIsCorrect();
        if (isCorrect != null) {
            stmt.bindLong(3, isCorrect ? 1L: 0L);
        }
 
        String marks = entity.getMarks();
        if (marks != null) {
            stmt.bindString(4, marks);
        }
 
        Long questionTranslationId = entity.getQuestionTranslationId();
        if (questionTranslationId != null) {
            stmt.bindLong(5, questionTranslationId);
        }
    }

    @Override
    public Void readKey(Cursor cursor, int offset) {
        return null;
    }    

    @Override
    public ReviewAnswerTranslation readEntity(Cursor cursor, int offset) {
        ReviewAnswerTranslation entity = new ReviewAnswerTranslation( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
            cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1), // textHtml
            cursor.isNull(offset + 2) ? null : cursor.getShort(offset + 2) != 0, // isCorrect
            cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3), // marks
            cursor.isNull(offset + 4) ? null : cursor.getLong(offset + 4) // questionTranslationId
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, ReviewAnswerTranslation entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setTextHtml(cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1));
        entity.setIsCorrect(cursor.isNull(offset + 2) ? null : cursor.getShort(offset + 2) != 0);
        entity.setMarks(cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3));
        entity.setQuestionTranslationId(cursor.isNull(offset + 4) ? null : cursor.getLong(offset + 4));
     }
    
    @Override
    protected final Void updateKeyAfterInsert(ReviewAnswerTranslation entity, long rowId) {
        // Unsupported or missing PK type
        return null;
    }
    
    @Override
    public Void getKey(ReviewAnswerTranslation entity) {
        return null;
    }

    @Override
    public boolean hasKey(ReviewAnswerTranslation entity) {
        // TODO
        return false;
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
    /** Internal query to resolve the "answers" to-many relationship of ReviewQuestionTranslation. */
    public List<ReviewAnswerTranslation> _queryReviewQuestionTranslation_Answers(Long questionTranslationId) {
        synchronized (this) {
            if (reviewQuestionTranslation_AnswersQuery == null) {
                QueryBuilder<ReviewAnswerTranslation> queryBuilder = queryBuilder();
                queryBuilder.where(Properties.QuestionTranslationId.eq(null));
                reviewQuestionTranslation_AnswersQuery = queryBuilder.build();
            }
        }
        Query<ReviewAnswerTranslation> query = reviewQuestionTranslation_AnswersQuery.forCurrentThread();
        query.setParameter(0, questionTranslationId);
        return query.list();
    }

}
