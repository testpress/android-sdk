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

import in.testpress.util.IntegerList;
import in.testpress.util.IntegerListConverter;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "USER_SELECTED_ANSWER".
*/
public class UserSelectedAnswerDao extends AbstractDao<UserSelectedAnswer, Long> {

    public static final String TABLENAME = "USER_SELECTED_ANSWER";

    /**
     * Properties of entity UserSelectedAnswer.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "ID");
        public final static Property Order = new Property(1, Integer.class, "order", false, "ORDER");
        public final static Property Review = new Property(2, Boolean.class, "review", false, "REVIEW");
        public final static Property ExamId = new Property(3, Long.class, "examId", false, "EXAM_ID");
        public final static Property AttemptId = new Property(4, Long.class, "attemptId", false, "ATTEMPT_ID");
        public final static Property ExplanationHtml = new Property(5, String.class, "explanationHtml", false, "EXPLANATION_HTML");
        public final static Property ShortText = new Property(6, String.class, "shortText", false, "SHORT_TEXT");
        public final static Property SelectedAnswers = new Property(7, String.class, "selectedAnswers", false, "SELECTED_ANSWERS");
        public final static Property CorrectAnswers = new Property(8, String.class, "correctAnswers", false, "CORRECT_ANSWERS");
        public final static Property Url = new Property(9, String.class, "url", false, "URL");
        public final static Property QuestionId = new Property(10, Long.class, "questionId", false, "QUESTION_ID");
    }

    private DaoSession daoSession;

    private final IntegerListConverter selectedAnswersConverter = new IntegerListConverter();
    private final IntegerListConverter correctAnswersConverter = new IntegerListConverter();

    public UserSelectedAnswerDao(DaoConfig config) {
        super(config);
    }
    
    public UserSelectedAnswerDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
        this.daoSession = daoSession;
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"USER_SELECTED_ANSWER\" (" + //
                "\"ID\" INTEGER PRIMARY KEY ," + // 0: id
                "\"ORDER\" INTEGER," + // 1: order
                "\"REVIEW\" INTEGER," + // 2: review
                "\"EXAM_ID\" INTEGER," + // 3: examId
                "\"ATTEMPT_ID\" INTEGER," + // 4: attemptId
                "\"EXPLANATION_HTML\" TEXT," + // 5: explanationHtml
                "\"SHORT_TEXT\" TEXT," + // 6: shortText
                "\"SELECTED_ANSWERS\" TEXT," + // 7: selectedAnswers
                "\"CORRECT_ANSWERS\" TEXT," + // 8: correctAnswers
                "\"URL\" TEXT," + // 9: url
                "\"QUESTION_ID\" INTEGER);"); // 10: questionId
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"USER_SELECTED_ANSWER\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, UserSelectedAnswer entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
 
        Integer order = entity.getOrder();
        if (order != null) {
            stmt.bindLong(2, order);
        }
 
        Boolean review = entity.getReview();
        if (review != null) {
            stmt.bindLong(3, review ? 1L: 0L);
        }
 
        Long examId = entity.getExamId();
        if (examId != null) {
            stmt.bindLong(4, examId);
        }
 
        Long attemptId = entity.getAttemptId();
        if (attemptId != null) {
            stmt.bindLong(5, attemptId);
        }
 
        String explanationHtml = entity.getExplanationHtml();
        if (explanationHtml != null) {
            stmt.bindString(6, explanationHtml);
        }
 
        String shortText = entity.getShortText();
        if (shortText != null) {
            stmt.bindString(7, shortText);
        }
 
        IntegerList selectedAnswers = entity.getSelectedAnswers();
        if (selectedAnswers != null) {
            stmt.bindString(8, selectedAnswersConverter.convertToDatabaseValue(selectedAnswers));
        }
 
        IntegerList correctAnswers = entity.getCorrectAnswers();
        if (correctAnswers != null) {
            stmt.bindString(9, correctAnswersConverter.convertToDatabaseValue(correctAnswers));
        }
 
        String url = entity.getUrl();
        if (url != null) {
            stmt.bindString(10, url);
        }
 
        Long questionId = entity.getQuestionId();
        if (questionId != null) {
            stmt.bindLong(11, questionId);
        }
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, UserSelectedAnswer entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
 
        Integer order = entity.getOrder();
        if (order != null) {
            stmt.bindLong(2, order);
        }
 
        Boolean review = entity.getReview();
        if (review != null) {
            stmt.bindLong(3, review ? 1L: 0L);
        }
 
        Long examId = entity.getExamId();
        if (examId != null) {
            stmt.bindLong(4, examId);
        }
 
        Long attemptId = entity.getAttemptId();
        if (attemptId != null) {
            stmt.bindLong(5, attemptId);
        }
 
        String explanationHtml = entity.getExplanationHtml();
        if (explanationHtml != null) {
            stmt.bindString(6, explanationHtml);
        }
 
        String shortText = entity.getShortText();
        if (shortText != null) {
            stmt.bindString(7, shortText);
        }
 
        IntegerList selectedAnswers = entity.getSelectedAnswers();
        if (selectedAnswers != null) {
            stmt.bindString(8, selectedAnswersConverter.convertToDatabaseValue(selectedAnswers));
        }
 
        IntegerList correctAnswers = entity.getCorrectAnswers();
        if (correctAnswers != null) {
            stmt.bindString(9, correctAnswersConverter.convertToDatabaseValue(correctAnswers));
        }
 
        String url = entity.getUrl();
        if (url != null) {
            stmt.bindString(10, url);
        }
 
        Long questionId = entity.getQuestionId();
        if (questionId != null) {
            stmt.bindLong(11, questionId);
        }
    }

    @Override
    protected final void attachEntity(UserSelectedAnswer entity) {
        super.attachEntity(entity);
        entity.__setDaoSession(daoSession);
    }

    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    @Override
    public UserSelectedAnswer readEntity(Cursor cursor, int offset) {
        UserSelectedAnswer entity = new UserSelectedAnswer( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
            cursor.isNull(offset + 1) ? null : cursor.getInt(offset + 1), // order
            cursor.isNull(offset + 2) ? null : cursor.getShort(offset + 2) != 0, // review
            cursor.isNull(offset + 3) ? null : cursor.getLong(offset + 3), // examId
            cursor.isNull(offset + 4) ? null : cursor.getLong(offset + 4), // attemptId
            cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5), // explanationHtml
            cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6), // shortText
            cursor.isNull(offset + 7) ? null : selectedAnswersConverter.convertToEntityProperty(cursor.getString(offset + 7)), // selectedAnswers
            cursor.isNull(offset + 8) ? null : correctAnswersConverter.convertToEntityProperty(cursor.getString(offset + 8)), // correctAnswers
            cursor.isNull(offset + 9) ? null : cursor.getString(offset + 9), // url
            cursor.isNull(offset + 10) ? null : cursor.getLong(offset + 10) // questionId
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, UserSelectedAnswer entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setOrder(cursor.isNull(offset + 1) ? null : cursor.getInt(offset + 1));
        entity.setReview(cursor.isNull(offset + 2) ? null : cursor.getShort(offset + 2) != 0);
        entity.setExamId(cursor.isNull(offset + 3) ? null : cursor.getLong(offset + 3));
        entity.setAttemptId(cursor.isNull(offset + 4) ? null : cursor.getLong(offset + 4));
        entity.setExplanationHtml(cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5));
        entity.setShortText(cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6));
        entity.setSelectedAnswers(cursor.isNull(offset + 7) ? null : selectedAnswersConverter.convertToEntityProperty(cursor.getString(offset + 7)));
        entity.setCorrectAnswers(cursor.isNull(offset + 8) ? null : correctAnswersConverter.convertToEntityProperty(cursor.getString(offset + 8)));
        entity.setUrl(cursor.isNull(offset + 9) ? null : cursor.getString(offset + 9));
        entity.setQuestionId(cursor.isNull(offset + 10) ? null : cursor.getLong(offset + 10));
     }
    
    @Override
    protected final Long updateKeyAfterInsert(UserSelectedAnswer entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    @Override
    public Long getKey(UserSelectedAnswer entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    @Override
    public boolean hasKey(UserSelectedAnswer entity) {
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
            SqlUtils.appendColumns(builder, "T0", daoSession.getQuestionDao().getAllColumns());
            builder.append(" FROM USER_SELECTED_ANSWER T");
            builder.append(" LEFT JOIN QUESTION T0 ON T.\"QUESTION_ID\"=T0.\"ID\"");
            builder.append(' ');
            selectDeep = builder.toString();
        }
        return selectDeep;
    }
    
    protected UserSelectedAnswer loadCurrentDeep(Cursor cursor, boolean lock) {
        UserSelectedAnswer entity = loadCurrent(cursor, 0, lock);
        int offset = getAllColumns().length;

        Question question = loadCurrentOther(daoSession.getQuestionDao(), cursor, offset);
        entity.setQuestion(question);

        return entity;    
    }

    public UserSelectedAnswer loadDeep(Long key) {
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
    public List<UserSelectedAnswer> loadAllDeepFromCursor(Cursor cursor) {
        int count = cursor.getCount();
        List<UserSelectedAnswer> list = new ArrayList<UserSelectedAnswer>(count);
        
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
    
    protected List<UserSelectedAnswer> loadDeepAllAndCloseCursor(Cursor cursor) {
        try {
            return loadAllDeepFromCursor(cursor);
        } finally {
            cursor.close();
        }
    }
    

    /** A raw-style query where you can pass any WHERE clause and arguments. */
    public List<UserSelectedAnswer> queryDeep(String where, String... selectionArg) {
        Cursor cursor = db.rawQuery(getSelectDeep() + where, selectionArg);
        return loadDeepAllAndCloseCursor(cursor);
    }
 
}
