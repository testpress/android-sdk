package in.testpress.models.greendao;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.internal.DaoConfig;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;

import in.testpress.util.StringList;
import in.testpress.util.StringListConverter;

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
        public final static Property StartDate = new Property(7, String.class, "startDate", false, "START_DATE");
        public final static Property EndDate = new Property(8, String.class, "endDate", false, "END_DATE");
        public final static Property Duration = new Property(9, String.class, "duration", false, "DURATION");
        public final static Property NumberOfQuestions = new Property(10, Integer.class, "numberOfQuestions", false, "NUMBER_OF_QUESTIONS");
        public final static Property NegativeMarks = new Property(11, String.class, "negativeMarks", false, "NEGATIVE_MARKS");
        public final static Property MarkPerQuestion = new Property(12, String.class, "markPerQuestion", false, "MARK_PER_QUESTION");
        public final static Property TemplateType = new Property(13, Integer.class, "templateType", false, "TEMPLATE_TYPE");
        public final static Property AllowRetake = new Property(14, Boolean.class, "allowRetake", false, "ALLOW_RETAKE");
        public final static Property AllowPdf = new Property(15, Boolean.class, "allowPdf", false, "ALLOW_PDF");
        public final static Property ShowAnswers = new Property(16, Boolean.class, "showAnswers", false, "SHOW_ANSWERS");
        public final static Property MaxRetakes = new Property(17, Integer.class, "maxRetakes", false, "MAX_RETAKES");
        public final static Property AttemptsUrl = new Property(18, String.class, "attemptsUrl", false, "ATTEMPTS_URL");
        public final static Property DeviceAccessControl = new Property(19, String.class, "deviceAccessControl", false, "DEVICE_ACCESS_CONTROL");
        public final static Property CommentsCount = new Property(20, Integer.class, "commentsCount", false, "COMMENTS_COUNT");
        public final static Property Slug = new Property(21, String.class, "slug", false, "SLUG");
        public final static Property SelectedLanguage = new Property(22, String.class, "selectedLanguage", false, "SELECTED_LANGUAGE");
        public final static Property VariableMarkPerQuestion = new Property(23, Boolean.class, "variableMarkPerQuestion", false, "VARIABLE_MARK_PER_QUESTION");
        public final static Property PassPercentage = new Property(24, Integer.class, "passPercentage", false, "PASS_PERCENTAGE");
        public final static Property EnableRanks = new Property(25, Boolean.class, "enableRanks", false, "ENABLE_RANKS");
        public final static Property ShowScore = new Property(26, Boolean.class, "showScore", false, "SHOW_SCORE");
        public final static Property ShowPercentile = new Property(27, Boolean.class, "showPercentile", false, "SHOW_PERCENTILE");
        public final static Property Categories = new Property(28, String.class, "categories", false, "CATEGORIES");
        public final static Property IsDetailsFetched = new Property(29, Boolean.class, "isDetailsFetched", false, "IS_DETAILS_FETCHED");
        public final static Property IsGrowthHackEnabled = new Property(30, Boolean.class, "isGrowthHackEnabled", false, "IS_GROWTH_HACK_ENABLED");
        public final static Property ShareTextForSolutionUnlock = new Property(31, String.class, "shareTextForSolutionUnlock", false, "SHARE_TEXT_FOR_SOLUTION_UNLOCK");
        public final static Property ShowAnalytics = new Property(32, Boolean.class, "showAnalytics", false, "SHOW_ANALYTICS");
        public final static Property Instructions = new Property(33, String.class, "instructions", false, "INSTRUCTIONS");
        public final static Property HasAudioQuestions = new Property(34, Boolean.class, "hasAudioQuestions", false, "HAS_AUDIO_QUESTIONS");
        public final static Property RankPublishingDate = new Property(35, String.class, "rankPublishingDate", false, "RANK_PUBLISHING_DATE");
        public final static Property EnableQuizMode = new Property(36, Boolean.class, "enableQuizMode", false, "ENABLE_QUIZ_MODE");
        public final static Property DisableAttemptResume = new Property(37, Boolean.class, "disableAttemptResume", false, "DISABLE_ATTEMPT_RESUME");
        public final static Property AllowPreemptiveSectionEnding = new Property(38, Boolean.class, "allowPreemptiveSectionEnding", false, "ALLOW_PREEMPTIVE_SECTION_ENDING");
        public final static Property ExamDataModifiedOn = new Property(39, String.class, "examDataModifiedOn", false, "EXAM_DATA_MODIFIED_ON");
        public final static Property IsOfflineExam = new Property(40, Boolean.class, "isOfflineExam", false, "IS_OFFLINE_EXAM");
        public final static Property GraceDurationForOfflineSubmission = new Property(41, Long.class, "graceDurationForOfflineSubmission", false, "GRACE_DURATION_FOR_OFFLINE_SUBMISSION");
        public final static Property EnableExamWindowMonitoring = new Property(42, Boolean.class, "enableExamWindowMonitoring", false, "ENABLE_EXAM_WINDOW_MONITORING");
    }

    private DaoSession daoSession;

    private final StringListConverter categoriesConverter = new StringListConverter();

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
                "\"START_DATE\" TEXT," + // 7: startDate
                "\"END_DATE\" TEXT," + // 8: endDate
                "\"DURATION\" TEXT," + // 9: duration
                "\"NUMBER_OF_QUESTIONS\" INTEGER," + // 10: numberOfQuestions
                "\"NEGATIVE_MARKS\" TEXT," + // 11: negativeMarks
                "\"MARK_PER_QUESTION\" TEXT," + // 12: markPerQuestion
                "\"TEMPLATE_TYPE\" INTEGER," + // 13: templateType
                "\"ALLOW_RETAKE\" INTEGER," + // 14: allowRetake
                "\"ALLOW_PDF\" INTEGER," + // 15: allowPdf
                "\"SHOW_ANSWERS\" INTEGER," + // 16: showAnswers
                "\"MAX_RETAKES\" INTEGER," + // 17: maxRetakes
                "\"ATTEMPTS_URL\" TEXT," + // 18: attemptsUrl
                "\"DEVICE_ACCESS_CONTROL\" TEXT," + // 19: deviceAccessControl
                "\"COMMENTS_COUNT\" INTEGER," + // 20: commentsCount
                "\"SLUG\" TEXT," + // 21: slug
                "\"SELECTED_LANGUAGE\" TEXT," + // 22: selectedLanguage
                "\"VARIABLE_MARK_PER_QUESTION\" INTEGER," + // 23: variableMarkPerQuestion
                "\"PASS_PERCENTAGE\" INTEGER," + // 24: passPercentage
                "\"ENABLE_RANKS\" INTEGER," + // 25: enableRanks
                "\"SHOW_SCORE\" INTEGER," + // 26: showScore
                "\"SHOW_PERCENTILE\" INTEGER," + // 27: showPercentile
                "\"CATEGORIES\" TEXT," + // 28: categories
                "\"IS_DETAILS_FETCHED\" INTEGER," + // 29: isDetailsFetched
                "\"IS_GROWTH_HACK_ENABLED\" INTEGER," + // 30: isGrowthHackEnabled
                "\"SHARE_TEXT_FOR_SOLUTION_UNLOCK\" TEXT," + // 31: shareTextForSolutionUnlock
                "\"SHOW_ANALYTICS\" INTEGER," + // 32: showAnalytics
                "\"INSTRUCTIONS\" TEXT," + // 33: instructions
                "\"HAS_AUDIO_QUESTIONS\" INTEGER," + // 34: hasAudioQuestions
                "\"RANK_PUBLISHING_DATE\" TEXT," + // 35: rankPublishingDate
                "\"ENABLE_QUIZ_MODE\" INTEGER," + // 36: enableQuizMode
                "\"DISABLE_ATTEMPT_RESUME\" INTEGER," + // 37: disableAttemptResume
                "\"ALLOW_PREEMPTIVE_SECTION_ENDING\" INTEGER," + // 38: allowPreemptiveSectionEnding
                "\"EXAM_DATA_MODIFIED_ON\" TEXT," + // 39: examDataModifiedOn
                "\"IS_OFFLINE_EXAM\" INTEGER," + // 40: isOfflineExam
                "\"GRACE_DURATION_FOR_OFFLINE_SUBMISSION\" INTEGER," + // 41: graceDurationForOfflineSubmission
                "\"ENABLE_EXAM_WINDOW_MONITORING\" INTEGER);"); // 42: enableExamWindowMonitoring
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
 
        String startDate = entity.getStartDate();
        if (startDate != null) {
            stmt.bindString(8, startDate);
        }
 
        String endDate = entity.getEndDate();
        if (endDate != null) {
            stmt.bindString(9, endDate);
        }
 
        String duration = entity.getDuration();
        if (duration != null) {
            stmt.bindString(10, duration);
        }
 
        Integer numberOfQuestions = entity.getNumberOfQuestions();
        if (numberOfQuestions != null) {
            stmt.bindLong(11, numberOfQuestions);
        }
 
        String negativeMarks = entity.getNegativeMarks();
        if (negativeMarks != null) {
            stmt.bindString(12, negativeMarks);
        }
 
        String markPerQuestion = entity.getMarkPerQuestion();
        if (markPerQuestion != null) {
            stmt.bindString(13, markPerQuestion);
        }
 
        Integer templateType = entity.getTemplateType();
        if (templateType != null) {
            stmt.bindLong(14, templateType);
        }
 
        Boolean allowRetake = entity.getAllowRetake();
        if (allowRetake != null) {
            stmt.bindLong(15, allowRetake ? 1L: 0L);
        }
 
        Boolean allowPdf = entity.getAllowPdf();
        if (allowPdf != null) {
            stmt.bindLong(16, allowPdf ? 1L: 0L);
        }
 
        Boolean showAnswers = entity.getShowAnswers();
        if (showAnswers != null) {
            stmt.bindLong(17, showAnswers ? 1L: 0L);
        }
 
        Integer maxRetakes = entity.getMaxRetakes();
        if (maxRetakes != null) {
            stmt.bindLong(18, maxRetakes);
        }
 
        String attemptsUrl = entity.getAttemptsUrl();
        if (attemptsUrl != null) {
            stmt.bindString(19, attemptsUrl);
        }
 
        String deviceAccessControl = entity.getDeviceAccessControl();
        if (deviceAccessControl != null) {
            stmt.bindString(20, deviceAccessControl);
        }
 
        Integer commentsCount = entity.getCommentsCount();
        if (commentsCount != null) {
            stmt.bindLong(21, commentsCount);
        }
 
        String slug = entity.getSlug();
        if (slug != null) {
            stmt.bindString(22, slug);
        }
 
        String selectedLanguage = entity.getSelectedLanguage();
        if (selectedLanguage != null) {
            stmt.bindString(23, selectedLanguage);
        }
 
        Boolean variableMarkPerQuestion = entity.getVariableMarkPerQuestion();
        if (variableMarkPerQuestion != null) {
            stmt.bindLong(24, variableMarkPerQuestion ? 1L: 0L);
        }
 
        Integer passPercentage = entity.getPassPercentage();
        if (passPercentage != null) {
            stmt.bindLong(25, passPercentage);
        }
 
        Boolean enableRanks = entity.getEnableRanks();
        if (enableRanks != null) {
            stmt.bindLong(26, enableRanks ? 1L: 0L);
        }
 
        Boolean showScore = entity.getShowScore();
        if (showScore != null) {
            stmt.bindLong(27, showScore ? 1L: 0L);
        }
 
        Boolean showPercentile = entity.getShowPercentile();
        if (showPercentile != null) {
            stmt.bindLong(28, showPercentile ? 1L: 0L);
        }
 
        StringList categories = entity.getCategories();
        if (categories != null) {
            stmt.bindString(29, categoriesConverter.convertToDatabaseValue(categories));
        }
 
        Boolean isDetailsFetched = entity.getIsDetailsFetched();
        if (isDetailsFetched != null) {
            stmt.bindLong(30, isDetailsFetched ? 1L: 0L);
        }
 
        Boolean isGrowthHackEnabled = entity.getIsGrowthHackEnabled();
        if (isGrowthHackEnabled != null) {
            stmt.bindLong(31, isGrowthHackEnabled ? 1L: 0L);
        }
 
        String shareTextForSolutionUnlock = entity.getShareTextForSolutionUnlock();
        if (shareTextForSolutionUnlock != null) {
            stmt.bindString(32, shareTextForSolutionUnlock);
        }
 
        Boolean showAnalytics = entity.getShowAnalytics();
        if (showAnalytics != null) {
            stmt.bindLong(33, showAnalytics ? 1L: 0L);
        }
 
        String instructions = entity.getInstructions();
        if (instructions != null) {
            stmt.bindString(34, instructions);
        }
 
        Boolean hasAudioQuestions = entity.getHasAudioQuestions();
        if (hasAudioQuestions != null) {
            stmt.bindLong(35, hasAudioQuestions ? 1L: 0L);
        }
 
        String rankPublishingDate = entity.getRankPublishingDate();
        if (rankPublishingDate != null) {
            stmt.bindString(36, rankPublishingDate);
        }
 
        Boolean enableQuizMode = entity.getEnableQuizMode();
        if (enableQuizMode != null) {
            stmt.bindLong(37, enableQuizMode ? 1L: 0L);
        }
 
        Boolean disableAttemptResume = entity.getDisableAttemptResume();
        if (disableAttemptResume != null) {
            stmt.bindLong(38, disableAttemptResume ? 1L: 0L);
        }
 
        Boolean allowPreemptiveSectionEnding = entity.getAllowPreemptiveSectionEnding();
        if (allowPreemptiveSectionEnding != null) {
            stmt.bindLong(39, allowPreemptiveSectionEnding ? 1L: 0L);
        }
 
        String examDataModifiedOn = entity.getExamDataModifiedOn();
        if (examDataModifiedOn != null) {
            stmt.bindString(40, examDataModifiedOn);
        }
 
        Boolean isOfflineExam = entity.getIsOfflineExam();
        if (isOfflineExam != null) {
            stmt.bindLong(41, isOfflineExam ? 1L: 0L);
        }
 
        Long graceDurationForOfflineSubmission = entity.getGraceDurationForOfflineSubmission();
        if (graceDurationForOfflineSubmission != null) {
            stmt.bindLong(42, graceDurationForOfflineSubmission);
        }
 
        Boolean enableExamWindowMonitoring = entity.getEnableExamWindowMonitoring();
        if (enableExamWindowMonitoring != null) {
            stmt.bindLong(43, enableExamWindowMonitoring ? 1L: 0L);
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
 
        String startDate = entity.getStartDate();
        if (startDate != null) {
            stmt.bindString(8, startDate);
        }
 
        String endDate = entity.getEndDate();
        if (endDate != null) {
            stmt.bindString(9, endDate);
        }
 
        String duration = entity.getDuration();
        if (duration != null) {
            stmt.bindString(10, duration);
        }
 
        Integer numberOfQuestions = entity.getNumberOfQuestions();
        if (numberOfQuestions != null) {
            stmt.bindLong(11, numberOfQuestions);
        }
 
        String negativeMarks = entity.getNegativeMarks();
        if (negativeMarks != null) {
            stmt.bindString(12, negativeMarks);
        }
 
        String markPerQuestion = entity.getMarkPerQuestion();
        if (markPerQuestion != null) {
            stmt.bindString(13, markPerQuestion);
        }
 
        Integer templateType = entity.getTemplateType();
        if (templateType != null) {
            stmt.bindLong(14, templateType);
        }
 
        Boolean allowRetake = entity.getAllowRetake();
        if (allowRetake != null) {
            stmt.bindLong(15, allowRetake ? 1L: 0L);
        }
 
        Boolean allowPdf = entity.getAllowPdf();
        if (allowPdf != null) {
            stmt.bindLong(16, allowPdf ? 1L: 0L);
        }
 
        Boolean showAnswers = entity.getShowAnswers();
        if (showAnswers != null) {
            stmt.bindLong(17, showAnswers ? 1L: 0L);
        }
 
        Integer maxRetakes = entity.getMaxRetakes();
        if (maxRetakes != null) {
            stmt.bindLong(18, maxRetakes);
        }
 
        String attemptsUrl = entity.getAttemptsUrl();
        if (attemptsUrl != null) {
            stmt.bindString(19, attemptsUrl);
        }
 
        String deviceAccessControl = entity.getDeviceAccessControl();
        if (deviceAccessControl != null) {
            stmt.bindString(20, deviceAccessControl);
        }
 
        Integer commentsCount = entity.getCommentsCount();
        if (commentsCount != null) {
            stmt.bindLong(21, commentsCount);
        }
 
        String slug = entity.getSlug();
        if (slug != null) {
            stmt.bindString(22, slug);
        }
 
        String selectedLanguage = entity.getSelectedLanguage();
        if (selectedLanguage != null) {
            stmt.bindString(23, selectedLanguage);
        }
 
        Boolean variableMarkPerQuestion = entity.getVariableMarkPerQuestion();
        if (variableMarkPerQuestion != null) {
            stmt.bindLong(24, variableMarkPerQuestion ? 1L: 0L);
        }
 
        Integer passPercentage = entity.getPassPercentage();
        if (passPercentage != null) {
            stmt.bindLong(25, passPercentage);
        }
 
        Boolean enableRanks = entity.getEnableRanks();
        if (enableRanks != null) {
            stmt.bindLong(26, enableRanks ? 1L: 0L);
        }
 
        Boolean showScore = entity.getShowScore();
        if (showScore != null) {
            stmt.bindLong(27, showScore ? 1L: 0L);
        }
 
        Boolean showPercentile = entity.getShowPercentile();
        if (showPercentile != null) {
            stmt.bindLong(28, showPercentile ? 1L: 0L);
        }
 
        StringList categories = entity.getCategories();
        if (categories != null) {
            stmt.bindString(29, categoriesConverter.convertToDatabaseValue(categories));
        }
 
        Boolean isDetailsFetched = entity.getIsDetailsFetched();
        if (isDetailsFetched != null) {
            stmt.bindLong(30, isDetailsFetched ? 1L: 0L);
        }
 
        Boolean isGrowthHackEnabled = entity.getIsGrowthHackEnabled();
        if (isGrowthHackEnabled != null) {
            stmt.bindLong(31, isGrowthHackEnabled ? 1L: 0L);
        }
 
        String shareTextForSolutionUnlock = entity.getShareTextForSolutionUnlock();
        if (shareTextForSolutionUnlock != null) {
            stmt.bindString(32, shareTextForSolutionUnlock);
        }
 
        Boolean showAnalytics = entity.getShowAnalytics();
        if (showAnalytics != null) {
            stmt.bindLong(33, showAnalytics ? 1L: 0L);
        }
 
        String instructions = entity.getInstructions();
        if (instructions != null) {
            stmt.bindString(34, instructions);
        }
 
        Boolean hasAudioQuestions = entity.getHasAudioQuestions();
        if (hasAudioQuestions != null) {
            stmt.bindLong(35, hasAudioQuestions ? 1L: 0L);
        }
 
        String rankPublishingDate = entity.getRankPublishingDate();
        if (rankPublishingDate != null) {
            stmt.bindString(36, rankPublishingDate);
        }
 
        Boolean enableQuizMode = entity.getEnableQuizMode();
        if (enableQuizMode != null) {
            stmt.bindLong(37, enableQuizMode ? 1L: 0L);
        }
 
        Boolean disableAttemptResume = entity.getDisableAttemptResume();
        if (disableAttemptResume != null) {
            stmt.bindLong(38, disableAttemptResume ? 1L: 0L);
        }
 
        Boolean allowPreemptiveSectionEnding = entity.getAllowPreemptiveSectionEnding();
        if (allowPreemptiveSectionEnding != null) {
            stmt.bindLong(39, allowPreemptiveSectionEnding ? 1L: 0L);
        }
 
        String examDataModifiedOn = entity.getExamDataModifiedOn();
        if (examDataModifiedOn != null) {
            stmt.bindString(40, examDataModifiedOn);
        }
 
        Boolean isOfflineExam = entity.getIsOfflineExam();
        if (isOfflineExam != null) {
            stmt.bindLong(41, isOfflineExam ? 1L: 0L);
        }
 
        Long graceDurationForOfflineSubmission = entity.getGraceDurationForOfflineSubmission();
        if (graceDurationForOfflineSubmission != null) {
            stmt.bindLong(42, graceDurationForOfflineSubmission);
        }
 
        Boolean enableExamWindowMonitoring = entity.getEnableExamWindowMonitoring();
        if (enableExamWindowMonitoring != null) {
            stmt.bindLong(43, enableExamWindowMonitoring ? 1L: 0L);
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
            cursor.isNull(offset + 7) ? null : cursor.getString(offset + 7), // startDate
            cursor.isNull(offset + 8) ? null : cursor.getString(offset + 8), // endDate
            cursor.isNull(offset + 9) ? null : cursor.getString(offset + 9), // duration
            cursor.isNull(offset + 10) ? null : cursor.getInt(offset + 10), // numberOfQuestions
            cursor.isNull(offset + 11) ? null : cursor.getString(offset + 11), // negativeMarks
            cursor.isNull(offset + 12) ? null : cursor.getString(offset + 12), // markPerQuestion
            cursor.isNull(offset + 13) ? null : cursor.getInt(offset + 13), // templateType
            cursor.isNull(offset + 14) ? null : cursor.getShort(offset + 14) != 0, // allowRetake
            cursor.isNull(offset + 15) ? null : cursor.getShort(offset + 15) != 0, // allowPdf
            cursor.isNull(offset + 16) ? null : cursor.getShort(offset + 16) != 0, // showAnswers
            cursor.isNull(offset + 17) ? null : cursor.getInt(offset + 17), // maxRetakes
            cursor.isNull(offset + 18) ? null : cursor.getString(offset + 18), // attemptsUrl
            cursor.isNull(offset + 19) ? null : cursor.getString(offset + 19), // deviceAccessControl
            cursor.isNull(offset + 20) ? null : cursor.getInt(offset + 20), // commentsCount
            cursor.isNull(offset + 21) ? null : cursor.getString(offset + 21), // slug
            cursor.isNull(offset + 22) ? null : cursor.getString(offset + 22), // selectedLanguage
            cursor.isNull(offset + 23) ? null : cursor.getShort(offset + 23) != 0, // variableMarkPerQuestion
            cursor.isNull(offset + 24) ? null : cursor.getInt(offset + 24), // passPercentage
            cursor.isNull(offset + 25) ? null : cursor.getShort(offset + 25) != 0, // enableRanks
            cursor.isNull(offset + 26) ? null : cursor.getShort(offset + 26) != 0, // showScore
            cursor.isNull(offset + 27) ? null : cursor.getShort(offset + 27) != 0, // showPercentile
            cursor.isNull(offset + 28) ? null : categoriesConverter.convertToEntityProperty(cursor.getString(offset + 28)), // categories
            cursor.isNull(offset + 29) ? null : cursor.getShort(offset + 29) != 0, // isDetailsFetched
            cursor.isNull(offset + 30) ? null : cursor.getShort(offset + 30) != 0, // isGrowthHackEnabled
            cursor.isNull(offset + 31) ? null : cursor.getString(offset + 31), // shareTextForSolutionUnlock
            cursor.isNull(offset + 32) ? null : cursor.getShort(offset + 32) != 0, // showAnalytics
            cursor.isNull(offset + 33) ? null : cursor.getString(offset + 33), // instructions
            cursor.isNull(offset + 34) ? null : cursor.getShort(offset + 34) != 0, // hasAudioQuestions
            cursor.isNull(offset + 35) ? null : cursor.getString(offset + 35), // rankPublishingDate
            cursor.isNull(offset + 36) ? null : cursor.getShort(offset + 36) != 0, // enableQuizMode
            cursor.isNull(offset + 37) ? null : cursor.getShort(offset + 37) != 0, // disableAttemptResume
            cursor.isNull(offset + 38) ? null : cursor.getShort(offset + 38) != 0, // allowPreemptiveSectionEnding
            cursor.isNull(offset + 39) ? null : cursor.getString(offset + 39), // examDataModifiedOn
            cursor.isNull(offset + 40) ? null : cursor.getShort(offset + 40) != 0, // isOfflineExam
            cursor.isNull(offset + 41) ? null : cursor.getLong(offset + 41), // graceDurationForOfflineSubmission
            cursor.isNull(offset + 42) ? null : cursor.getShort(offset + 42) != 0 // enableExamWindowMonitoring
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
        entity.setStartDate(cursor.isNull(offset + 7) ? null : cursor.getString(offset + 7));
        entity.setEndDate(cursor.isNull(offset + 8) ? null : cursor.getString(offset + 8));
        entity.setDuration(cursor.isNull(offset + 9) ? null : cursor.getString(offset + 9));
        entity.setNumberOfQuestions(cursor.isNull(offset + 10) ? null : cursor.getInt(offset + 10));
        entity.setNegativeMarks(cursor.isNull(offset + 11) ? null : cursor.getString(offset + 11));
        entity.setMarkPerQuestion(cursor.isNull(offset + 12) ? null : cursor.getString(offset + 12));
        entity.setTemplateType(cursor.isNull(offset + 13) ? null : cursor.getInt(offset + 13));
        entity.setAllowRetake(cursor.isNull(offset + 14) ? null : cursor.getShort(offset + 14) != 0);
        entity.setAllowPdf(cursor.isNull(offset + 15) ? null : cursor.getShort(offset + 15) != 0);
        entity.setShowAnswers(cursor.isNull(offset + 16) ? null : cursor.getShort(offset + 16) != 0);
        entity.setMaxRetakes(cursor.isNull(offset + 17) ? null : cursor.getInt(offset + 17));
        entity.setAttemptsUrl(cursor.isNull(offset + 18) ? null : cursor.getString(offset + 18));
        entity.setDeviceAccessControl(cursor.isNull(offset + 19) ? null : cursor.getString(offset + 19));
        entity.setCommentsCount(cursor.isNull(offset + 20) ? null : cursor.getInt(offset + 20));
        entity.setSlug(cursor.isNull(offset + 21) ? null : cursor.getString(offset + 21));
        entity.setSelectedLanguage(cursor.isNull(offset + 22) ? null : cursor.getString(offset + 22));
        entity.setVariableMarkPerQuestion(cursor.isNull(offset + 23) ? null : cursor.getShort(offset + 23) != 0);
        entity.setPassPercentage(cursor.isNull(offset + 24) ? null : cursor.getInt(offset + 24));
        entity.setEnableRanks(cursor.isNull(offset + 25) ? null : cursor.getShort(offset + 25) != 0);
        entity.setShowScore(cursor.isNull(offset + 26) ? null : cursor.getShort(offset + 26) != 0);
        entity.setShowPercentile(cursor.isNull(offset + 27) ? null : cursor.getShort(offset + 27) != 0);
        entity.setCategories(cursor.isNull(offset + 28) ? null : categoriesConverter.convertToEntityProperty(cursor.getString(offset + 28)));
        entity.setIsDetailsFetched(cursor.isNull(offset + 29) ? null : cursor.getShort(offset + 29) != 0);
        entity.setIsGrowthHackEnabled(cursor.isNull(offset + 30) ? null : cursor.getShort(offset + 30) != 0);
        entity.setShareTextForSolutionUnlock(cursor.isNull(offset + 31) ? null : cursor.getString(offset + 31));
        entity.setShowAnalytics(cursor.isNull(offset + 32) ? null : cursor.getShort(offset + 32) != 0);
        entity.setInstructions(cursor.isNull(offset + 33) ? null : cursor.getString(offset + 33));
        entity.setHasAudioQuestions(cursor.isNull(offset + 34) ? null : cursor.getShort(offset + 34) != 0);
        entity.setRankPublishingDate(cursor.isNull(offset + 35) ? null : cursor.getString(offset + 35));
        entity.setEnableQuizMode(cursor.isNull(offset + 36) ? null : cursor.getShort(offset + 36) != 0);
        entity.setDisableAttemptResume(cursor.isNull(offset + 37) ? null : cursor.getShort(offset + 37) != 0);
        entity.setAllowPreemptiveSectionEnding(cursor.isNull(offset + 38) ? null : cursor.getShort(offset + 38) != 0);
        entity.setExamDataModifiedOn(cursor.isNull(offset + 39) ? null : cursor.getString(offset + 39));
        entity.setIsOfflineExam(cursor.isNull(offset + 40) ? null : cursor.getShort(offset + 40) != 0);
        entity.setGraceDurationForOfflineSubmission(cursor.isNull(offset + 41) ? null : cursor.getLong(offset + 41));
        entity.setEnableExamWindowMonitoring(cursor.isNull(offset + 42) ? null : cursor.getShort(offset + 42) != 0);
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
