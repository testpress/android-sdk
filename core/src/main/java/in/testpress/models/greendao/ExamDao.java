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
 * DAO for table "EXAM".
*/
public class ExamDao extends AbstractDao<Exam, Long> {

    public static final String TABLENAME = "EXAM";

    /**
     * Properties of entity Exam.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property TotalMarks = new Property(0, String.class, "totalMarks", false, "TOTAL_MARKS");
        public final static Property Url = new Property(1, String.class, "url", false, "URL");
        public final static Property Id = new Property(2, Long.class, "id", true, "ID");
        public final static Property AttemptsCount = new Property(3, Integer.class, "attemptsCount", false, "ATTEMPTS_COUNT");
        public final static Property PausedAttemptsCount = new Property(4, Integer.class, "pausedAttemptsCount", false, "PAUSED_ATTEMPTS_COUNT");
        public final static Property Title = new Property(5, String.class, "title", false, "TITLE");
        public final static Property Description = new Property(6, String.class, "description", false, "DESCRIPTION");
        public final static Property Course_category = new Property(7, String.class, "course_category", false, "COURSE_CATEGORY");
        public final static Property StartDate = new Property(8, java.util.Date.class, "startDate", false, "START_DATE");
        public final static Property EndDate = new Property(9, java.util.Date.class, "endDate", false, "END_DATE");
        public final static Property Duration = new Property(10, String.class, "duration", false, "DURATION");
        public final static Property NumberOfQuestions = new Property(11, Integer.class, "numberOfQuestions", false, "NUMBER_OF_QUESTIONS");
        public final static Property NegativeMarks = new Property(12, String.class, "negativeMarks", false, "NEGATIVE_MARKS");
        public final static Property MarkPerQuestion = new Property(13, String.class, "markPerQuestion", false, "MARK_PER_QUESTION");
        public final static Property TemplateType = new Property(14, Integer.class, "templateType", false, "TEMPLATE_TYPE");
        public final static Property AllowRetake = new Property(15, Boolean.class, "allowRetake", false, "ALLOW_RETAKE");
        public final static Property AllowPdf = new Property(16, Boolean.class, "allowPdf", false, "ALLOW_PDF");
        public final static Property ShowAnswers = new Property(17, Boolean.class, "showAnswers", false, "SHOW_ANSWERS");
        public final static Property MaxRetakes = new Property(18, Integer.class, "maxRetakes", false, "MAX_RETAKES");
        public final static Property AttemptsUrl = new Property(19, String.class, "attemptsUrl", false, "ATTEMPTS_URL");
        public final static Property DeviceAccessControl = new Property(20, String.class, "deviceAccessControl", false, "DEVICE_ACCESS_CONTROL");
        public final static Property CommentsCount = new Property(21, Integer.class, "commentsCount", false, "COMMENTS_COUNT");
        public final static Property Slug = new Property(22, String.class, "slug", false, "SLUG");
        public final static Property SelectedLanguage = new Property(23, String.class, "selectedLanguage", false, "SELECTED_LANGUAGE");
        public final static Property VariableMarkPerQuestion = new Property(24, Boolean.class, "variableMarkPerQuestion", false, "VARIABLE_MARK_PER_QUESTION");
        public final static Property PassPercentage = new Property(25, Integer.class, "passPercentage", false, "PASS_PERCENTAGE");
        public final static Property EnableRanks = new Property(26, Boolean.class, "enableRanks", false, "ENABLE_RANKS");
        public final static Property ShowScore = new Property(27, Boolean.class, "showScore", false, "SHOW_SCORE");
        public final static Property ShowPercentile = new Property(28, Boolean.class, "showPercentile", false, "SHOW_PERCENTILE");
    }

    private DaoSession daoSession;


    public ExamDao(DaoConfig config) {
        super(config);
    }
    
    public ExamDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
        this.daoSession = daoSession;
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"EXAM\" (" + //
                "\"TOTAL_MARKS\" TEXT," + // 0: totalMarks
                "\"URL\" TEXT," + // 1: url
                "\"ID\" INTEGER PRIMARY KEY ," + // 2: id
                "\"ATTEMPTS_COUNT\" INTEGER," + // 3: attemptsCount
                "\"PAUSED_ATTEMPTS_COUNT\" INTEGER," + // 4: pausedAttemptsCount
                "\"TITLE\" TEXT," + // 5: title
                "\"DESCRIPTION\" TEXT," + // 6: description
                "\"COURSE_CATEGORY\" TEXT," + // 7: course_category
                "\"START_DATE\" INTEGER," + // 8: startDate
                "\"END_DATE\" INTEGER," + // 9: endDate
                "\"DURATION\" TEXT," + // 10: duration
                "\"NUMBER_OF_QUESTIONS\" INTEGER," + // 11: numberOfQuestions
                "\"NEGATIVE_MARKS\" TEXT," + // 12: negativeMarks
                "\"MARK_PER_QUESTION\" TEXT," + // 13: markPerQuestion
                "\"TEMPLATE_TYPE\" INTEGER," + // 14: templateType
                "\"ALLOW_RETAKE\" INTEGER," + // 15: allowRetake
                "\"ALLOW_PDF\" INTEGER," + // 16: allowPdf
                "\"SHOW_ANSWERS\" INTEGER," + // 17: showAnswers
                "\"MAX_RETAKES\" INTEGER," + // 18: maxRetakes
                "\"ATTEMPTS_URL\" TEXT," + // 19: attemptsUrl
                "\"DEVICE_ACCESS_CONTROL\" TEXT," + // 20: deviceAccessControl
                "\"COMMENTS_COUNT\" INTEGER," + // 21: commentsCount
                "\"SLUG\" TEXT," + // 22: slug
                "\"SELECTED_LANGUAGE\" TEXT," + // 23: selectedLanguage
                "\"VARIABLE_MARK_PER_QUESTION\" INTEGER," + // 24: variableMarkPerQuestion
                "\"PASS_PERCENTAGE\" INTEGER," + // 25: passPercentage
                "\"ENABLE_RANKS\" INTEGER," + // 26: enableRanks
                "\"SHOW_SCORE\" INTEGER," + // 27: showScore
                "\"SHOW_PERCENTILE\" INTEGER);"); // 28: showPercentile
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"EXAM\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, Exam entity) {
        stmt.clearBindings();
 
        String totalMarks = entity.getTotalMarks();
        if (totalMarks != null) {
            stmt.bindString(1, totalMarks);
        }
 
        String url = entity.getUrl();
        if (url != null) {
            stmt.bindString(2, url);
        }
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(3, id);
        }
 
        Integer attemptsCount = entity.getAttemptsCount();
        if (attemptsCount != null) {
            stmt.bindLong(4, attemptsCount);
        }
 
        Integer pausedAttemptsCount = entity.getPausedAttemptsCount();
        if (pausedAttemptsCount != null) {
            stmt.bindLong(5, pausedAttemptsCount);
        }
 
        String title = entity.getTitle();
        if (title != null) {
            stmt.bindString(6, title);
        }
 
        String description = entity.getDescription();
        if (description != null) {
            stmt.bindString(7, description);
        }
 
        String course_category = entity.getCourse_category();
        if (course_category != null) {
            stmt.bindString(8, course_category);
        }
 
        java.util.Date startDate = entity.getStartDate();
        if (startDate != null) {
            stmt.bindLong(9, startDate.getTime());
        }
 
        java.util.Date endDate = entity.getEndDate();
        if (endDate != null) {
            stmt.bindLong(10, endDate.getTime());
        }
 
        String duration = entity.getDuration();
        if (duration != null) {
            stmt.bindString(11, duration);
        }
 
        Integer numberOfQuestions = entity.getNumberOfQuestions();
        if (numberOfQuestions != null) {
            stmt.bindLong(12, numberOfQuestions);
        }
 
        String negativeMarks = entity.getNegativeMarks();
        if (negativeMarks != null) {
            stmt.bindString(13, negativeMarks);
        }
 
        String markPerQuestion = entity.getMarkPerQuestion();
        if (markPerQuestion != null) {
            stmt.bindString(14, markPerQuestion);
        }
 
        Integer templateType = entity.getTemplateType();
        if (templateType != null) {
            stmt.bindLong(15, templateType);
        }
 
        Boolean allowRetake = entity.getAllowRetake();
        if (allowRetake != null) {
            stmt.bindLong(16, allowRetake ? 1L: 0L);
        }
 
        Boolean allowPdf = entity.getAllowPdf();
        if (allowPdf != null) {
            stmt.bindLong(17, allowPdf ? 1L: 0L);
        }
 
        Boolean showAnswers = entity.getShowAnswers();
        if (showAnswers != null) {
            stmt.bindLong(18, showAnswers ? 1L: 0L);
        }
 
        Integer maxRetakes = entity.getMaxRetakes();
        if (maxRetakes != null) {
            stmt.bindLong(19, maxRetakes);
        }
 
        String attemptsUrl = entity.getAttemptsUrl();
        if (attemptsUrl != null) {
            stmt.bindString(20, attemptsUrl);
        }
 
        String deviceAccessControl = entity.getDeviceAccessControl();
        if (deviceAccessControl != null) {
            stmt.bindString(21, deviceAccessControl);
        }
 
        Integer commentsCount = entity.getCommentsCount();
        if (commentsCount != null) {
            stmt.bindLong(22, commentsCount);
        }
 
        String slug = entity.getSlug();
        if (slug != null) {
            stmt.bindString(23, slug);
        }
 
        String selectedLanguage = entity.getSelectedLanguage();
        if (selectedLanguage != null) {
            stmt.bindString(24, selectedLanguage);
        }
 
        Boolean variableMarkPerQuestion = entity.getVariableMarkPerQuestion();
        if (variableMarkPerQuestion != null) {
            stmt.bindLong(25, variableMarkPerQuestion ? 1L: 0L);
        }
 
        Integer passPercentage = entity.getPassPercentage();
        if (passPercentage != null) {
            stmt.bindLong(26, passPercentage);
        }
 
        Boolean enableRanks = entity.getEnableRanks();
        if (enableRanks != null) {
            stmt.bindLong(27, enableRanks ? 1L: 0L);
        }
 
        Boolean showScore = entity.getShowScore();
        if (showScore != null) {
            stmt.bindLong(28, showScore ? 1L: 0L);
        }
 
        Boolean showPercentile = entity.getShowPercentile();
        if (showPercentile != null) {
            stmt.bindLong(29, showPercentile ? 1L: 0L);
        }
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, Exam entity) {
        stmt.clearBindings();
 
        String totalMarks = entity.getTotalMarks();
        if (totalMarks != null) {
            stmt.bindString(1, totalMarks);
        }
 
        String url = entity.getUrl();
        if (url != null) {
            stmt.bindString(2, url);
        }
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(3, id);
        }
 
        Integer attemptsCount = entity.getAttemptsCount();
        if (attemptsCount != null) {
            stmt.bindLong(4, attemptsCount);
        }
 
        Integer pausedAttemptsCount = entity.getPausedAttemptsCount();
        if (pausedAttemptsCount != null) {
            stmt.bindLong(5, pausedAttemptsCount);
        }
 
        String title = entity.getTitle();
        if (title != null) {
            stmt.bindString(6, title);
        }
 
        String description = entity.getDescription();
        if (description != null) {
            stmt.bindString(7, description);
        }
 
        String course_category = entity.getCourse_category();
        if (course_category != null) {
            stmt.bindString(8, course_category);
        }
 
        java.util.Date startDate = entity.getStartDate();
        if (startDate != null) {
            stmt.bindLong(9, startDate.getTime());
        }
 
        java.util.Date endDate = entity.getEndDate();
        if (endDate != null) {
            stmt.bindLong(10, endDate.getTime());
        }
 
        String duration = entity.getDuration();
        if (duration != null) {
            stmt.bindString(11, duration);
        }
 
        Integer numberOfQuestions = entity.getNumberOfQuestions();
        if (numberOfQuestions != null) {
            stmt.bindLong(12, numberOfQuestions);
        }
 
        String negativeMarks = entity.getNegativeMarks();
        if (negativeMarks != null) {
            stmt.bindString(13, negativeMarks);
        }
 
        String markPerQuestion = entity.getMarkPerQuestion();
        if (markPerQuestion != null) {
            stmt.bindString(14, markPerQuestion);
        }
 
        Integer templateType = entity.getTemplateType();
        if (templateType != null) {
            stmt.bindLong(15, templateType);
        }
 
        Boolean allowRetake = entity.getAllowRetake();
        if (allowRetake != null) {
            stmt.bindLong(16, allowRetake ? 1L: 0L);
        }
 
        Boolean allowPdf = entity.getAllowPdf();
        if (allowPdf != null) {
            stmt.bindLong(17, allowPdf ? 1L: 0L);
        }
 
        Boolean showAnswers = entity.getShowAnswers();
        if (showAnswers != null) {
            stmt.bindLong(18, showAnswers ? 1L: 0L);
        }
 
        Integer maxRetakes = entity.getMaxRetakes();
        if (maxRetakes != null) {
            stmt.bindLong(19, maxRetakes);
        }
 
        String attemptsUrl = entity.getAttemptsUrl();
        if (attemptsUrl != null) {
            stmt.bindString(20, attemptsUrl);
        }
 
        String deviceAccessControl = entity.getDeviceAccessControl();
        if (deviceAccessControl != null) {
            stmt.bindString(21, deviceAccessControl);
        }
 
        Integer commentsCount = entity.getCommentsCount();
        if (commentsCount != null) {
            stmt.bindLong(22, commentsCount);
        }
 
        String slug = entity.getSlug();
        if (slug != null) {
            stmt.bindString(23, slug);
        }
 
        String selectedLanguage = entity.getSelectedLanguage();
        if (selectedLanguage != null) {
            stmt.bindString(24, selectedLanguage);
        }
 
        Boolean variableMarkPerQuestion = entity.getVariableMarkPerQuestion();
        if (variableMarkPerQuestion != null) {
            stmt.bindLong(25, variableMarkPerQuestion ? 1L: 0L);
        }
 
        Integer passPercentage = entity.getPassPercentage();
        if (passPercentage != null) {
            stmt.bindLong(26, passPercentage);
        }
 
        Boolean enableRanks = entity.getEnableRanks();
        if (enableRanks != null) {
            stmt.bindLong(27, enableRanks ? 1L: 0L);
        }
 
        Boolean showScore = entity.getShowScore();
        if (showScore != null) {
            stmt.bindLong(28, showScore ? 1L: 0L);
        }
 
        Boolean showPercentile = entity.getShowPercentile();
        if (showPercentile != null) {
            stmt.bindLong(29, showPercentile ? 1L: 0L);
        }
    }

    @Override
    protected final void attachEntity(Exam entity) {
        super.attachEntity(entity);
        entity.__setDaoSession(daoSession);
    }

    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 2) ? null : cursor.getLong(offset + 2);
    }    

    @Override
    public Exam readEntity(Cursor cursor, int offset) {
        Exam entity = new Exam( //
            cursor.isNull(offset + 0) ? null : cursor.getString(offset + 0), // totalMarks
            cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1), // url
            cursor.isNull(offset + 2) ? null : cursor.getLong(offset + 2), // id
            cursor.isNull(offset + 3) ? null : cursor.getInt(offset + 3), // attemptsCount
            cursor.isNull(offset + 4) ? null : cursor.getInt(offset + 4), // pausedAttemptsCount
            cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5), // title
            cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6), // description
            cursor.isNull(offset + 7) ? null : cursor.getString(offset + 7), // course_category
            cursor.isNull(offset + 8) ? null : new java.util.Date(cursor.getLong(offset + 8)), // startDate
            cursor.isNull(offset + 9) ? null : new java.util.Date(cursor.getLong(offset + 9)), // endDate
            cursor.isNull(offset + 10) ? null : cursor.getString(offset + 10), // duration
            cursor.isNull(offset + 11) ? null : cursor.getInt(offset + 11), // numberOfQuestions
            cursor.isNull(offset + 12) ? null : cursor.getString(offset + 12), // negativeMarks
            cursor.isNull(offset + 13) ? null : cursor.getString(offset + 13), // markPerQuestion
            cursor.isNull(offset + 14) ? null : cursor.getInt(offset + 14), // templateType
            cursor.isNull(offset + 15) ? null : cursor.getShort(offset + 15) != 0, // allowRetake
            cursor.isNull(offset + 16) ? null : cursor.getShort(offset + 16) != 0, // allowPdf
            cursor.isNull(offset + 17) ? null : cursor.getShort(offset + 17) != 0, // showAnswers
            cursor.isNull(offset + 18) ? null : cursor.getInt(offset + 18), // maxRetakes
            cursor.isNull(offset + 19) ? null : cursor.getString(offset + 19), // attemptsUrl
            cursor.isNull(offset + 20) ? null : cursor.getString(offset + 20), // deviceAccessControl
            cursor.isNull(offset + 21) ? null : cursor.getInt(offset + 21), // commentsCount
            cursor.isNull(offset + 22) ? null : cursor.getString(offset + 22), // slug
            cursor.isNull(offset + 23) ? null : cursor.getString(offset + 23), // selectedLanguage
            cursor.isNull(offset + 24) ? null : cursor.getShort(offset + 24) != 0, // variableMarkPerQuestion
            cursor.isNull(offset + 25) ? null : cursor.getInt(offset + 25), // passPercentage
            cursor.isNull(offset + 26) ? null : cursor.getShort(offset + 26) != 0, // enableRanks
            cursor.isNull(offset + 27) ? null : cursor.getShort(offset + 27) != 0, // showScore
            cursor.isNull(offset + 28) ? null : cursor.getShort(offset + 28) != 0 // showPercentile
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, Exam entity, int offset) {
        entity.setTotalMarks(cursor.isNull(offset + 0) ? null : cursor.getString(offset + 0));
        entity.setUrl(cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1));
        entity.setId(cursor.isNull(offset + 2) ? null : cursor.getLong(offset + 2));
        entity.setAttemptsCount(cursor.isNull(offset + 3) ? null : cursor.getInt(offset + 3));
        entity.setPausedAttemptsCount(cursor.isNull(offset + 4) ? null : cursor.getInt(offset + 4));
        entity.setTitle(cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5));
        entity.setDescription(cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6));
        entity.setCourse_category(cursor.isNull(offset + 7) ? null : cursor.getString(offset + 7));
        entity.setStartDate(cursor.isNull(offset + 8) ? null : new java.util.Date(cursor.getLong(offset + 8)));
        entity.setEndDate(cursor.isNull(offset + 9) ? null : new java.util.Date(cursor.getLong(offset + 9)));
        entity.setDuration(cursor.isNull(offset + 10) ? null : cursor.getString(offset + 10));
        entity.setNumberOfQuestions(cursor.isNull(offset + 11) ? null : cursor.getInt(offset + 11));
        entity.setNegativeMarks(cursor.isNull(offset + 12) ? null : cursor.getString(offset + 12));
        entity.setMarkPerQuestion(cursor.isNull(offset + 13) ? null : cursor.getString(offset + 13));
        entity.setTemplateType(cursor.isNull(offset + 14) ? null : cursor.getInt(offset + 14));
        entity.setAllowRetake(cursor.isNull(offset + 15) ? null : cursor.getShort(offset + 15) != 0);
        entity.setAllowPdf(cursor.isNull(offset + 16) ? null : cursor.getShort(offset + 16) != 0);
        entity.setShowAnswers(cursor.isNull(offset + 17) ? null : cursor.getShort(offset + 17) != 0);
        entity.setMaxRetakes(cursor.isNull(offset + 18) ? null : cursor.getInt(offset + 18));
        entity.setAttemptsUrl(cursor.isNull(offset + 19) ? null : cursor.getString(offset + 19));
        entity.setDeviceAccessControl(cursor.isNull(offset + 20) ? null : cursor.getString(offset + 20));
        entity.setCommentsCount(cursor.isNull(offset + 21) ? null : cursor.getInt(offset + 21));
        entity.setSlug(cursor.isNull(offset + 22) ? null : cursor.getString(offset + 22));
        entity.setSelectedLanguage(cursor.isNull(offset + 23) ? null : cursor.getString(offset + 23));
        entity.setVariableMarkPerQuestion(cursor.isNull(offset + 24) ? null : cursor.getShort(offset + 24) != 0);
        entity.setPassPercentage(cursor.isNull(offset + 25) ? null : cursor.getInt(offset + 25));
        entity.setEnableRanks(cursor.isNull(offset + 26) ? null : cursor.getShort(offset + 26) != 0);
        entity.setShowScore(cursor.isNull(offset + 27) ? null : cursor.getShort(offset + 27) != 0);
        entity.setShowPercentile(cursor.isNull(offset + 28) ? null : cursor.getShort(offset + 28) != 0);
     }
    
    @Override
    protected final Long updateKeyAfterInsert(Exam entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    @Override
    public Long getKey(Exam entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    @Override
    public boolean hasKey(Exam entity) {
        return entity.getId() != null;
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
}
