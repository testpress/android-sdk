package in.testpress.models.greendao;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.internal.DaoConfig;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;

import in.testpress.util.IntegerList;
import in.testpress.util.IntegerListConverter;

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
        public final static Property Type = new Property(5, String.class, "type", false, "TYPE");
        public final static Property CommentsUrl = new Property(6, String.class, "commentsUrl", false, "COMMENTS_URL");
        public final static Property Language = new Property(7, String.class, "language", false, "LANGUAGE");
        public final static Property PercentageGotCorrect = new Property(8, Float.class, "percentageGotCorrect", false, "PERCENTAGE_GOT_CORRECT");
        public final static Property DirectionId = new Property(9, Long.class, "directionId", false, "DIRECTION_ID");
        public final static Property SubjectId = new Property(10, Long.class, "subjectId", false, "SUBJECT_ID");
        public final static Property AnswerIds = new Property(11, String.class, "answerIds", false, "ANSWER_IDS");
        public final static Property TranslationIds = new Property(12, String.class, "translationIds", false, "TRANSLATION_IDS");
    }

    private DaoSession daoSession;

    private final IntegerListConverter answerIdsConverter = new IntegerListConverter();
    private final IntegerListConverter translationIdsConverter = new IntegerListConverter();

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
                "\"TYPE\" TEXT," + // 5: type
                "\"COMMENTS_URL\" TEXT," + // 6: commentsUrl
                "\"LANGUAGE\" TEXT," + // 7: language
                "\"PERCENTAGE_GOT_CORRECT\" REAL," + // 8: percentageGotCorrect
                "\"DIRECTION_ID\" INTEGER," + // 9: directionId
                "\"SUBJECT_ID\" INTEGER," + // 10: subjectId
                "\"ANSWER_IDS\" TEXT," + // 11: answerIds
                "\"TRANSLATION_IDS\" TEXT);"); // 12: translationIds
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
 
        String type = entity.getType();
        if (type != null) {
            stmt.bindString(6, type);
        }
 
        String commentsUrl = entity.getCommentsUrl();
        if (commentsUrl != null) {
            stmt.bindString(7, commentsUrl);
        }
 
        String language = entity.getLanguage();
        if (language != null) {
            stmt.bindString(8, language);
        }
 
        Float percentageGotCorrect = entity.getPercentageGotCorrect();
        if (percentageGotCorrect != null) {
            stmt.bindDouble(9, percentageGotCorrect);
        }
 
        Long directionId = entity.getDirectionId();
        if (directionId != null) {
            stmt.bindLong(10, directionId);
        }
 
        Long subjectId = entity.getSubjectId();
        if (subjectId != null) {
            stmt.bindLong(11, subjectId);
        }
 
        IntegerList answerIds = entity.getAnswerIds();
        if (answerIds != null) {
            stmt.bindString(12, answerIdsConverter.convertToDatabaseValue(answerIds));
        }
 
        IntegerList translationIds = entity.getTranslationIds();
        if (translationIds != null) {
            stmt.bindString(13, translationIdsConverter.convertToDatabaseValue(translationIds));
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
 
        String type = entity.getType();
        if (type != null) {
            stmt.bindString(6, type);
        }
 
        String commentsUrl = entity.getCommentsUrl();
        if (commentsUrl != null) {
            stmt.bindString(7, commentsUrl);
        }
 
        String language = entity.getLanguage();
        if (language != null) {
            stmt.bindString(8, language);
        }
 
        Float percentageGotCorrect = entity.getPercentageGotCorrect();
        if (percentageGotCorrect != null) {
            stmt.bindDouble(9, percentageGotCorrect);
        }
 
        Long directionId = entity.getDirectionId();
        if (directionId != null) {
            stmt.bindLong(10, directionId);
        }
 
        Long subjectId = entity.getSubjectId();
        if (subjectId != null) {
            stmt.bindLong(11, subjectId);
        }
 
        IntegerList answerIds = entity.getAnswerIds();
        if (answerIds != null) {
            stmt.bindString(12, answerIdsConverter.convertToDatabaseValue(answerIds));
        }
 
        IntegerList translationIds = entity.getTranslationIds();
        if (translationIds != null) {
            stmt.bindString(13, translationIdsConverter.convertToDatabaseValue(translationIds));
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
            cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5), // type
            cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6), // commentsUrl
            cursor.isNull(offset + 7) ? null : cursor.getString(offset + 7), // language
            cursor.isNull(offset + 8) ? null : cursor.getFloat(offset + 8), // percentageGotCorrect
            cursor.isNull(offset + 9) ? null : cursor.getLong(offset + 9), // directionId
            cursor.isNull(offset + 10) ? null : cursor.getLong(offset + 10), // subjectId
            cursor.isNull(offset + 11) ? null : answerIdsConverter.convertToEntityProperty(cursor.getString(offset + 11)), // answerIds
            cursor.isNull(offset + 12) ? null : translationIdsConverter.convertToEntityProperty(cursor.getString(offset + 12)) // translationIds
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
        entity.setType(cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5));
        entity.setCommentsUrl(cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6));
        entity.setLanguage(cursor.isNull(offset + 7) ? null : cursor.getString(offset + 7));
        entity.setPercentageGotCorrect(cursor.isNull(offset + 8) ? null : cursor.getFloat(offset + 8));
        entity.setDirectionId(cursor.isNull(offset + 9) ? null : cursor.getLong(offset + 9));
        entity.setSubjectId(cursor.isNull(offset + 10) ? null : cursor.getLong(offset + 10));
        entity.setAnswerIds(cursor.isNull(offset + 11) ? null : answerIdsConverter.convertToEntityProperty(cursor.getString(offset + 11)));
        entity.setTranslationIds(cursor.isNull(offset + 12) ? null : translationIdsConverter.convertToEntityProperty(cursor.getString(offset + 12)));
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
