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
 * DAO for table "ATTEMPT".
*/
public class AttemptDao extends AbstractDao<Attempt, Long> {

    public static final String TABLENAME = "ATTEMPT";

    /**
     * Properties of entity Attempt.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property Url = new Property(0, String.class, "url", false, "URL");
        public final static Property Id = new Property(1, Long.class, "id", true, "ID");
        public final static Property Date = new Property(2, String.class, "date", false, "DATE");
        public final static Property TotalQuestions = new Property(3, Integer.class, "totalQuestions", false, "TOTAL_QUESTIONS");
        public final static Property Score = new Property(4, String.class, "score", false, "SCORE");
        public final static Property Rank = new Property(5, String.class, "rank", false, "RANK");
        public final static Property MaxRank = new Property(6, String.class, "maxRank", false, "MAX_RANK");
        public final static Property ReviewUrl = new Property(7, String.class, "reviewUrl", false, "REVIEW_URL");
        public final static Property QuestionsUrl = new Property(8, String.class, "questionsUrl", false, "QUESTIONS_URL");
        public final static Property CorrectCount = new Property(9, Integer.class, "correctCount", false, "CORRECT_COUNT");
        public final static Property IncorrectCount = new Property(10, Integer.class, "incorrectCount", false, "INCORRECT_COUNT");
        public final static Property LastStartedTime = new Property(11, String.class, "lastStartedTime", false, "LAST_STARTED_TIME");
        public final static Property RemainingTime = new Property(12, String.class, "remainingTime", false, "REMAINING_TIME");
        public final static Property TimeTaken = new Property(13, String.class, "timeTaken", false, "TIME_TAKEN");
        public final static Property State = new Property(14, String.class, "state", false, "STATE");
        public final static Property Percentile = new Property(15, String.class, "percentile", false, "PERCENTILE");
        public final static Property Speed = new Property(16, Integer.class, "speed", false, "SPEED");
        public final static Property Accuracy = new Property(17, Integer.class, "accuracy", false, "ACCURACY");
        public final static Property Percentage = new Property(18, String.class, "percentage", false, "PERCENTAGE");
    }

    private DaoSession daoSession;


    public AttemptDao(DaoConfig config) {
        super(config);
    }
    
    public AttemptDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
        this.daoSession = daoSession;
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"ATTEMPT\" (" + //
                "\"URL\" TEXT," + // 0: url
                "\"ID\" INTEGER PRIMARY KEY ," + // 1: id
                "\"DATE\" TEXT," + // 2: date
                "\"TOTAL_QUESTIONS\" INTEGER," + // 3: totalQuestions
                "\"SCORE\" TEXT," + // 4: score
                "\"RANK\" TEXT," + // 5: rank
                "\"MAX_RANK\" TEXT," + // 6: maxRank
                "\"REVIEW_URL\" TEXT," + // 7: reviewUrl
                "\"QUESTIONS_URL\" TEXT," + // 8: questionsUrl
                "\"CORRECT_COUNT\" INTEGER," + // 9: correctCount
                "\"INCORRECT_COUNT\" INTEGER," + // 10: incorrectCount
                "\"LAST_STARTED_TIME\" TEXT," + // 11: lastStartedTime
                "\"REMAINING_TIME\" TEXT," + // 12: remainingTime
                "\"TIME_TAKEN\" TEXT," + // 13: timeTaken
                "\"STATE\" TEXT," + // 14: state
                "\"PERCENTILE\" TEXT," + // 15: percentile
                "\"SPEED\" INTEGER," + // 16: speed
                "\"ACCURACY\" INTEGER," + // 17: accuracy
                "\"PERCENTAGE\" TEXT);"); // 18: percentage
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"ATTEMPT\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, Attempt entity) {
        stmt.clearBindings();
 
        String url = entity.getUrl();
        if (url != null) {
            stmt.bindString(1, url);
        }
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(2, id);
        }
 
        String date = entity.getDate();
        if (date != null) {
            stmt.bindString(3, date);
        }
 
        Integer totalQuestions = entity.getTotalQuestions();
        if (totalQuestions != null) {
            stmt.bindLong(4, totalQuestions);
        }
 
        String score = entity.getScore();
        if (score != null) {
            stmt.bindString(5, score);
        }
 
        String rank = entity.getRank();
        if (rank != null) {
            stmt.bindString(6, rank);
        }
 
        String maxRank = entity.getMaxRank();
        if (maxRank != null) {
            stmt.bindString(7, maxRank);
        }
 
        String reviewUrl = entity.getReviewUrl();
        if (reviewUrl != null) {
            stmt.bindString(8, reviewUrl);
        }
 
        String questionsUrl = entity.getQuestionsUrl();
        if (questionsUrl != null) {
            stmt.bindString(9, questionsUrl);
        }
 
        Integer correctCount = entity.getCorrectCount();
        if (correctCount != null) {
            stmt.bindLong(10, correctCount);
        }
 
        Integer incorrectCount = entity.getIncorrectCount();
        if (incorrectCount != null) {
            stmt.bindLong(11, incorrectCount);
        }
 
        String lastStartedTime = entity.getLastStartedTime();
        if (lastStartedTime != null) {
            stmt.bindString(12, lastStartedTime);
        }
 
        String remainingTime = entity.getRemainingTime();
        if (remainingTime != null) {
            stmt.bindString(13, remainingTime);
        }
 
        String timeTaken = entity.getTimeTaken();
        if (timeTaken != null) {
            stmt.bindString(14, timeTaken);
        }
 
        String state = entity.getState();
        if (state != null) {
            stmt.bindString(15, state);
        }
 
        String percentile = entity.getPercentile();
        if (percentile != null) {
            stmt.bindString(16, percentile);
        }
 
        Integer speed = entity.getSpeed();
        if (speed != null) {
            stmt.bindLong(17, speed);
        }
 
        Integer accuracy = entity.getAccuracy();
        if (accuracy != null) {
            stmt.bindLong(18, accuracy);
        }
 
        String percentage = entity.getPercentage();
        if (percentage != null) {
            stmt.bindString(19, percentage);
        }
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, Attempt entity) {
        stmt.clearBindings();
 
        String url = entity.getUrl();
        if (url != null) {
            stmt.bindString(1, url);
        }
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(2, id);
        }
 
        String date = entity.getDate();
        if (date != null) {
            stmt.bindString(3, date);
        }
 
        Integer totalQuestions = entity.getTotalQuestions();
        if (totalQuestions != null) {
            stmt.bindLong(4, totalQuestions);
        }
 
        String score = entity.getScore();
        if (score != null) {
            stmt.bindString(5, score);
        }
 
        String rank = entity.getRank();
        if (rank != null) {
            stmt.bindString(6, rank);
        }
 
        String maxRank = entity.getMaxRank();
        if (maxRank != null) {
            stmt.bindString(7, maxRank);
        }
 
        String reviewUrl = entity.getReviewUrl();
        if (reviewUrl != null) {
            stmt.bindString(8, reviewUrl);
        }
 
        String questionsUrl = entity.getQuestionsUrl();
        if (questionsUrl != null) {
            stmt.bindString(9, questionsUrl);
        }
 
        Integer correctCount = entity.getCorrectCount();
        if (correctCount != null) {
            stmt.bindLong(10, correctCount);
        }
 
        Integer incorrectCount = entity.getIncorrectCount();
        if (incorrectCount != null) {
            stmt.bindLong(11, incorrectCount);
        }
 
        String lastStartedTime = entity.getLastStartedTime();
        if (lastStartedTime != null) {
            stmt.bindString(12, lastStartedTime);
        }
 
        String remainingTime = entity.getRemainingTime();
        if (remainingTime != null) {
            stmt.bindString(13, remainingTime);
        }
 
        String timeTaken = entity.getTimeTaken();
        if (timeTaken != null) {
            stmt.bindString(14, timeTaken);
        }
 
        String state = entity.getState();
        if (state != null) {
            stmt.bindString(15, state);
        }
 
        String percentile = entity.getPercentile();
        if (percentile != null) {
            stmt.bindString(16, percentile);
        }
 
        Integer speed = entity.getSpeed();
        if (speed != null) {
            stmt.bindLong(17, speed);
        }
 
        Integer accuracy = entity.getAccuracy();
        if (accuracy != null) {
            stmt.bindLong(18, accuracy);
        }
 
        String percentage = entity.getPercentage();
        if (percentage != null) {
            stmt.bindString(19, percentage);
        }
    }

    @Override
    protected final void attachEntity(Attempt entity) {
        super.attachEntity(entity);
        entity.__setDaoSession(daoSession);
    }

    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 1) ? null : cursor.getLong(offset + 1);
    }    

    @Override
    public Attempt readEntity(Cursor cursor, int offset) {
        Attempt entity = new Attempt( //
            cursor.isNull(offset + 0) ? null : cursor.getString(offset + 0), // url
            cursor.isNull(offset + 1) ? null : cursor.getLong(offset + 1), // id
            cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2), // date
            cursor.isNull(offset + 3) ? null : cursor.getInt(offset + 3), // totalQuestions
            cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4), // score
            cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5), // rank
            cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6), // maxRank
            cursor.isNull(offset + 7) ? null : cursor.getString(offset + 7), // reviewUrl
            cursor.isNull(offset + 8) ? null : cursor.getString(offset + 8), // questionsUrl
            cursor.isNull(offset + 9) ? null : cursor.getInt(offset + 9), // correctCount
            cursor.isNull(offset + 10) ? null : cursor.getInt(offset + 10), // incorrectCount
            cursor.isNull(offset + 11) ? null : cursor.getString(offset + 11), // lastStartedTime
            cursor.isNull(offset + 12) ? null : cursor.getString(offset + 12), // remainingTime
            cursor.isNull(offset + 13) ? null : cursor.getString(offset + 13), // timeTaken
            cursor.isNull(offset + 14) ? null : cursor.getString(offset + 14), // state
            cursor.isNull(offset + 15) ? null : cursor.getString(offset + 15), // percentile
            cursor.isNull(offset + 16) ? null : cursor.getInt(offset + 16), // speed
            cursor.isNull(offset + 17) ? null : cursor.getInt(offset + 17), // accuracy
            cursor.isNull(offset + 18) ? null : cursor.getString(offset + 18) // percentage
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, Attempt entity, int offset) {
        entity.setUrl(cursor.isNull(offset + 0) ? null : cursor.getString(offset + 0));
        entity.setId(cursor.isNull(offset + 1) ? null : cursor.getLong(offset + 1));
        entity.setDate(cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2));
        entity.setTotalQuestions(cursor.isNull(offset + 3) ? null : cursor.getInt(offset + 3));
        entity.setScore(cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4));
        entity.setRank(cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5));
        entity.setMaxRank(cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6));
        entity.setReviewUrl(cursor.isNull(offset + 7) ? null : cursor.getString(offset + 7));
        entity.setQuestionsUrl(cursor.isNull(offset + 8) ? null : cursor.getString(offset + 8));
        entity.setCorrectCount(cursor.isNull(offset + 9) ? null : cursor.getInt(offset + 9));
        entity.setIncorrectCount(cursor.isNull(offset + 10) ? null : cursor.getInt(offset + 10));
        entity.setLastStartedTime(cursor.isNull(offset + 11) ? null : cursor.getString(offset + 11));
        entity.setRemainingTime(cursor.isNull(offset + 12) ? null : cursor.getString(offset + 12));
        entity.setTimeTaken(cursor.isNull(offset + 13) ? null : cursor.getString(offset + 13));
        entity.setState(cursor.isNull(offset + 14) ? null : cursor.getString(offset + 14));
        entity.setPercentile(cursor.isNull(offset + 15) ? null : cursor.getString(offset + 15));
        entity.setSpeed(cursor.isNull(offset + 16) ? null : cursor.getInt(offset + 16));
        entity.setAccuracy(cursor.isNull(offset + 17) ? null : cursor.getInt(offset + 17));
        entity.setPercentage(cursor.isNull(offset + 18) ? null : cursor.getString(offset + 18));
     }
    
    @Override
    protected final Long updateKeyAfterInsert(Attempt entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    @Override
    public Long getKey(Attempt entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    @Override
    public boolean hasKey(Attempt entity) {
        return entity.getId() != null;
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
}
