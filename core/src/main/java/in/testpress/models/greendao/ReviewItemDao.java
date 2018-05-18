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
import org.greenrobot.greendao.query.Query;
import org.greenrobot.greendao.query.QueryBuilder;

import in.testpress.util.IntegerList;
import in.testpress.util.IntegerListConverter;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "REVIEW_ITEM".
*/
public class ReviewItemDao extends AbstractDao<ReviewItem, Long> {

    public static final String TABLENAME = "REVIEW_ITEM";

    /**
     * Properties of entity ReviewItem.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "ID");
        public final static Property Index = new Property(1, Integer.class, "index", false, "INDEX");
        public final static Property Url = new Property(2, String.class, "url", false, "URL");
        public final static Property Order = new Property(3, Integer.class, "order", false, "ORDER");
        public final static Property Duration = new Property(4, String.class, "duration", false, "DURATION");
        public final static Property BestDuration = new Property(5, String.class, "bestDuration", false, "BEST_DURATION");
        public final static Property AverageDuration = new Property(6, String.class, "averageDuration", false, "AVERAGE_DURATION");
        public final static Property EssayText = new Property(7, String.class, "essayText", false, "ESSAY_TEXT");
        public final static Property EssayTopic = new Property(8, String.class, "essayTopic", false, "ESSAY_TOPIC");
        public final static Property SelectedAnswers = new Property(9, String.class, "selectedAnswers", false, "SELECTED_ANSWERS");
        public final static Property Review = new Property(10, Boolean.class, "review", false, "REVIEW");
        public final static Property CommentsCount = new Property(11, Integer.class, "commentsCount", false, "COMMENTS_COUNT");
        public final static Property CorrectPercentage = new Property(12, Integer.class, "correctPercentage", false, "CORRECT_PERCENTAGE");
        public final static Property BookmarkId = new Property(13, Long.class, "bookmarkId", false, "BOOKMARK_ID");
        public final static Property AttemptId = new Property(14, Long.class, "attemptId", false, "ATTEMPT_ID");
        public final static Property QuestionId = new Property(15, Long.class, "questionId", false, "QUESTION_ID");
    }

    private DaoSession daoSession;

    private final IntegerListConverter selectedAnswersConverter = new IntegerListConverter();
    private Query<ReviewItem> reviewAttempt_ReviewItemsQuery;

    public ReviewItemDao(DaoConfig config) {
        super(config);
    }
    
    public ReviewItemDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
        this.daoSession = daoSession;
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"REVIEW_ITEM\" (" + //
                "\"ID\" INTEGER PRIMARY KEY ," + // 0: id
                "\"INDEX\" INTEGER," + // 1: index
                "\"URL\" TEXT," + // 2: url
                "\"ORDER\" INTEGER," + // 3: order
                "\"DURATION\" TEXT," + // 4: duration
                "\"BEST_DURATION\" TEXT," + // 5: bestDuration
                "\"AVERAGE_DURATION\" TEXT," + // 6: averageDuration
                "\"ESSAY_TEXT\" TEXT," + // 7: essayText
                "\"ESSAY_TOPIC\" TEXT," + // 8: essayTopic
                "\"SELECTED_ANSWERS\" TEXT," + // 9: selectedAnswers
                "\"REVIEW\" INTEGER," + // 10: review
                "\"COMMENTS_COUNT\" INTEGER," + // 11: commentsCount
                "\"CORRECT_PERCENTAGE\" INTEGER," + // 12: correctPercentage
                "\"BOOKMARK_ID\" INTEGER," + // 13: bookmarkId
                "\"ATTEMPT_ID\" INTEGER," + // 14: attemptId
                "\"QUESTION_ID\" INTEGER);"); // 15: questionId
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"REVIEW_ITEM\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, ReviewItem entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
 
        Integer index = entity.getIndex();
        if (index != null) {
            stmt.bindLong(2, index);
        }
 
        String url = entity.getUrl();
        if (url != null) {
            stmt.bindString(3, url);
        }
 
        Integer order = entity.getOrder();
        if (order != null) {
            stmt.bindLong(4, order);
        }
 
        String duration = entity.getDuration();
        if (duration != null) {
            stmt.bindString(5, duration);
        }
 
        String bestDuration = entity.getBestDuration();
        if (bestDuration != null) {
            stmt.bindString(6, bestDuration);
        }
 
        String averageDuration = entity.getAverageDuration();
        if (averageDuration != null) {
            stmt.bindString(7, averageDuration);
        }
 
        String essayText = entity.getEssayText();
        if (essayText != null) {
            stmt.bindString(8, essayText);
        }
 
        String essayTopic = entity.getEssayTopic();
        if (essayTopic != null) {
            stmt.bindString(9, essayTopic);
        }
 
        IntegerList selectedAnswers = entity.getSelectedAnswers();
        if (selectedAnswers != null) {
            stmt.bindString(10, selectedAnswersConverter.convertToDatabaseValue(selectedAnswers));
        }
 
        Boolean review = entity.getReview();
        if (review != null) {
            stmt.bindLong(11, review ? 1L: 0L);
        }
 
        Integer commentsCount = entity.getCommentsCount();
        if (commentsCount != null) {
            stmt.bindLong(12, commentsCount);
        }
 
        Integer correctPercentage = entity.getCorrectPercentage();
        if (correctPercentage != null) {
            stmt.bindLong(13, correctPercentage);
        }
 
        Long bookmarkId = entity.getBookmarkId();
        if (bookmarkId != null) {
            stmt.bindLong(14, bookmarkId);
        }
 
        Long attemptId = entity.getAttemptId();
        if (attemptId != null) {
            stmt.bindLong(15, attemptId);
        }
 
        Long questionId = entity.getQuestionId();
        if (questionId != null) {
            stmt.bindLong(16, questionId);
        }
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, ReviewItem entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
 
        Integer index = entity.getIndex();
        if (index != null) {
            stmt.bindLong(2, index);
        }
 
        String url = entity.getUrl();
        if (url != null) {
            stmt.bindString(3, url);
        }
 
        Integer order = entity.getOrder();
        if (order != null) {
            stmt.bindLong(4, order);
        }
 
        String duration = entity.getDuration();
        if (duration != null) {
            stmt.bindString(5, duration);
        }
 
        String bestDuration = entity.getBestDuration();
        if (bestDuration != null) {
            stmt.bindString(6, bestDuration);
        }
 
        String averageDuration = entity.getAverageDuration();
        if (averageDuration != null) {
            stmt.bindString(7, averageDuration);
        }
 
        String essayText = entity.getEssayText();
        if (essayText != null) {
            stmt.bindString(8, essayText);
        }
 
        String essayTopic = entity.getEssayTopic();
        if (essayTopic != null) {
            stmt.bindString(9, essayTopic);
        }
 
        IntegerList selectedAnswers = entity.getSelectedAnswers();
        if (selectedAnswers != null) {
            stmt.bindString(10, selectedAnswersConverter.convertToDatabaseValue(selectedAnswers));
        }
 
        Boolean review = entity.getReview();
        if (review != null) {
            stmt.bindLong(11, review ? 1L: 0L);
        }
 
        Integer commentsCount = entity.getCommentsCount();
        if (commentsCount != null) {
            stmt.bindLong(12, commentsCount);
        }
 
        Integer correctPercentage = entity.getCorrectPercentage();
        if (correctPercentage != null) {
            stmt.bindLong(13, correctPercentage);
        }
 
        Long bookmarkId = entity.getBookmarkId();
        if (bookmarkId != null) {
            stmt.bindLong(14, bookmarkId);
        }
 
        Long attemptId = entity.getAttemptId();
        if (attemptId != null) {
            stmt.bindLong(15, attemptId);
        }
 
        Long questionId = entity.getQuestionId();
        if (questionId != null) {
            stmt.bindLong(16, questionId);
        }
    }

    @Override
    protected final void attachEntity(ReviewItem entity) {
        super.attachEntity(entity);
        entity.__setDaoSession(daoSession);
    }

    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    @Override
    public ReviewItem readEntity(Cursor cursor, int offset) {
        ReviewItem entity = new ReviewItem( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
            cursor.isNull(offset + 1) ? null : cursor.getInt(offset + 1), // index
            cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2), // url
            cursor.isNull(offset + 3) ? null : cursor.getInt(offset + 3), // order
            cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4), // duration
            cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5), // bestDuration
            cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6), // averageDuration
            cursor.isNull(offset + 7) ? null : cursor.getString(offset + 7), // essayText
            cursor.isNull(offset + 8) ? null : cursor.getString(offset + 8), // essayTopic
            cursor.isNull(offset + 9) ? null : selectedAnswersConverter.convertToEntityProperty(cursor.getString(offset + 9)), // selectedAnswers
            cursor.isNull(offset + 10) ? null : cursor.getShort(offset + 10) != 0, // review
            cursor.isNull(offset + 11) ? null : cursor.getInt(offset + 11), // commentsCount
            cursor.isNull(offset + 12) ? null : cursor.getInt(offset + 12), // correctPercentage
            cursor.isNull(offset + 13) ? null : cursor.getLong(offset + 13), // bookmarkId
            cursor.isNull(offset + 14) ? null : cursor.getLong(offset + 14), // attemptId
            cursor.isNull(offset + 15) ? null : cursor.getLong(offset + 15) // questionId
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, ReviewItem entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setIndex(cursor.isNull(offset + 1) ? null : cursor.getInt(offset + 1));
        entity.setUrl(cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2));
        entity.setOrder(cursor.isNull(offset + 3) ? null : cursor.getInt(offset + 3));
        entity.setDuration(cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4));
        entity.setBestDuration(cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5));
        entity.setAverageDuration(cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6));
        entity.setEssayText(cursor.isNull(offset + 7) ? null : cursor.getString(offset + 7));
        entity.setEssayTopic(cursor.isNull(offset + 8) ? null : cursor.getString(offset + 8));
        entity.setSelectedAnswers(cursor.isNull(offset + 9) ? null : selectedAnswersConverter.convertToEntityProperty(cursor.getString(offset + 9)));
        entity.setReview(cursor.isNull(offset + 10) ? null : cursor.getShort(offset + 10) != 0);
        entity.setCommentsCount(cursor.isNull(offset + 11) ? null : cursor.getInt(offset + 11));
        entity.setCorrectPercentage(cursor.isNull(offset + 12) ? null : cursor.getInt(offset + 12));
        entity.setBookmarkId(cursor.isNull(offset + 13) ? null : cursor.getLong(offset + 13));
        entity.setAttemptId(cursor.isNull(offset + 14) ? null : cursor.getLong(offset + 14));
        entity.setQuestionId(cursor.isNull(offset + 15) ? null : cursor.getLong(offset + 15));
     }
    
    @Override
    protected final Long updateKeyAfterInsert(ReviewItem entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    @Override
    public Long getKey(ReviewItem entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    @Override
    public boolean hasKey(ReviewItem entity) {
        return entity.getId() != null;
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
    /** Internal query to resolve the "reviewItems" to-many relationship of ReviewAttempt. */
    public List<ReviewItem> _queryReviewAttempt_ReviewItems(Long attemptId) {
        synchronized (this) {
            if (reviewAttempt_ReviewItemsQuery == null) {
                QueryBuilder<ReviewItem> queryBuilder = queryBuilder();
                queryBuilder.where(Properties.AttemptId.eq(null));
                reviewAttempt_ReviewItemsQuery = queryBuilder.build();
            }
        }
        Query<ReviewItem> query = reviewAttempt_ReviewItemsQuery.forCurrentThread();
        query.setParameter(0, attemptId);
        return query.list();
    }

    private String selectDeep;

    protected String getSelectDeep() {
        if (selectDeep == null) {
            StringBuilder builder = new StringBuilder("SELECT ");
            SqlUtils.appendColumns(builder, "T", getAllColumns());
            builder.append(',');
            SqlUtils.appendColumns(builder, "T0", daoSession.getReviewQuestionDao().getAllColumns());
            builder.append(" FROM REVIEW_ITEM T");
            builder.append(" LEFT JOIN REVIEW_QUESTION T0 ON T.\"QUESTION_ID\"=T0.\"ID\"");
            builder.append(' ');
            selectDeep = builder.toString();
        }
        return selectDeep;
    }
    
    protected ReviewItem loadCurrentDeep(Cursor cursor, boolean lock) {
        ReviewItem entity = loadCurrent(cursor, 0, lock);
        int offset = getAllColumns().length;

        ReviewQuestion question = loadCurrentOther(daoSession.getReviewQuestionDao(), cursor, offset);
        entity.setQuestion(question);

        return entity;    
    }

    public ReviewItem loadDeep(Long key) {
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
    public List<ReviewItem> loadAllDeepFromCursor(Cursor cursor) {
        int count = cursor.getCount();
        List<ReviewItem> list = new ArrayList<ReviewItem>(count);
        
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
    
    protected List<ReviewItem> loadDeepAllAndCloseCursor(Cursor cursor) {
        try {
            return loadAllDeepFromCursor(cursor);
        } finally {
            cursor.close();
        }
    }
    

    /** A raw-style query where you can pass any WHERE clause and arguments. */
    public List<ReviewItem> queryDeep(String where, String... selectionArg) {
        Cursor cursor = db.rawQuery(getSelectDeep() + where, selectionArg);
        return loadDeepAllAndCloseCursor(cursor);
    }
 
}
