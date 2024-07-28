package in.testpress.models.greendao;

import org.greenrobot.greendao.annotation.*;

import java.util.List;
import in.testpress.models.greendao.DaoSession;
import org.greenrobot.greendao.DaoException;

import in.testpress.util.StringList;

// THIS CODE IS GENERATED BY greenDAO, EDIT ONLY INSIDE THE "KEEP"-SECTIONS

// KEEP INCLUDES - put your custom includes here
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Parcel;
import android.text.TextUtils;

import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.TimeZone;

import in.testpress.core.TestpressSDKDatabase;
// KEEP INCLUDES END

/**
 * Entity mapped to table "EXAM".
 */
@Entity(active = true)
public class Exam implements android.os.Parcelable {
    private String totalMarks;
    private String url;

    @Id
    private Long id;
    private Integer attemptsCount;
    private Integer pausedAttemptsCount;
    private String title;
    private String description;
    private String startDate;
    private String endDate;
    private String duration;
    private Integer numberOfQuestions;
    private String negativeMarks;
    private String markPerQuestion;
    private Integer templateType;
    private Boolean allowRetake;
    private Boolean allowPdf;
    private Boolean showAnswers;
    private Integer maxRetakes;
    private String attemptsUrl;
    private String deviceAccessControl;
    private Integer commentsCount;
    private String slug;
    private String selectedLanguage;
    private Boolean variableMarkPerQuestion;
    private Integer passPercentage;
    private Boolean enableRanks;
    private Boolean showScore;
    private Boolean showPercentile;

    @Convert(converter = in.testpress.util.StringListConverter.class, columnType = String.class)
    private StringList categories;
    private Boolean isDetailsFetched;
    private Boolean isGrowthHackEnabled;
    private String shareTextForSolutionUnlock;
    private Boolean showAnalytics;
    private String instructions;
    private Boolean hasAudioQuestions;
    private String rankPublishingDate;
    private Boolean enableQuizMode;
    private Boolean disableAttemptResume;
    private Boolean allowPreemptiveSectionEnding;
    private String examDataModifiedOn;
    private Boolean isOfflineExam;
    private Long graceDurationForOfflineSubmission;

    /** Used to resolve relations */
    @Generated
    private transient DaoSession daoSession;

    /** Used for active entity operations. */
    @Generated
    private transient ExamDao myDao;

    @ToMany(joinProperties = {
        @JoinProperty(name = "id", referencedName = "examId")
    })
    private List<Language> languages;

    // KEEP FIELDS - put your custom fields here
    private static final String SHARE_TO_UNLOCK = "_SHARE_TO_UNLOCK";
    // KEEP FIELDS END

    @Generated
    public Exam() {
    }

    public Exam(Long id) {
        this.id = id;
    }

    @Generated
    public Exam(String totalMarks, String url, Long id, Integer attemptsCount, Integer pausedAttemptsCount, String title, String description, String startDate, String endDate, String duration, Integer numberOfQuestions, String negativeMarks, String markPerQuestion, Integer templateType, Boolean allowRetake, Boolean allowPdf, Boolean showAnswers, Integer maxRetakes, String attemptsUrl, String deviceAccessControl, Integer commentsCount, String slug, String selectedLanguage, Boolean variableMarkPerQuestion, Integer passPercentage, Boolean enableRanks, Boolean showScore, Boolean showPercentile, StringList categories, Boolean isDetailsFetched, Boolean isGrowthHackEnabled, String shareTextForSolutionUnlock, Boolean showAnalytics, String instructions, Boolean hasAudioQuestions, String rankPublishingDate, Boolean enableQuizMode, Boolean disableAttemptResume, Boolean allowPreemptiveSectionEnding, String examDataModifiedOn, Boolean isOfflineExam, Long graceDurationForOfflineSubmission) {
        this.totalMarks = totalMarks;
        this.url = url;
        this.id = id;
        this.attemptsCount = attemptsCount;
        this.pausedAttemptsCount = pausedAttemptsCount;
        this.title = title;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.duration = duration;
        this.numberOfQuestions = numberOfQuestions;
        this.negativeMarks = negativeMarks;
        this.markPerQuestion = markPerQuestion;
        this.templateType = templateType;
        this.allowRetake = allowRetake;
        this.allowPdf = allowPdf;
        this.showAnswers = showAnswers;
        this.maxRetakes = maxRetakes;
        this.attemptsUrl = attemptsUrl;
        this.deviceAccessControl = deviceAccessControl;
        this.commentsCount = commentsCount;
        this.slug = slug;
        this.selectedLanguage = selectedLanguage;
        this.variableMarkPerQuestion = variableMarkPerQuestion;
        this.passPercentage = passPercentage;
        this.enableRanks = enableRanks;
        this.showScore = showScore;
        this.showPercentile = showPercentile;
        this.categories = categories;
        this.isDetailsFetched = isDetailsFetched;
        this.isGrowthHackEnabled = isGrowthHackEnabled;
        this.shareTextForSolutionUnlock = shareTextForSolutionUnlock;
        this.showAnalytics = showAnalytics;
        this.instructions = instructions;
        this.hasAudioQuestions = hasAudioQuestions;
        this.rankPublishingDate = rankPublishingDate;
        this.enableQuizMode = enableQuizMode;
        this.disableAttemptResume = disableAttemptResume;
        this.allowPreemptiveSectionEnding = allowPreemptiveSectionEnding;
        this.examDataModifiedOn = examDataModifiedOn;
        this.isOfflineExam = isOfflineExam;
        this.graceDurationForOfflineSubmission = graceDurationForOfflineSubmission;
    }

    /** called by internal mechanisms, do not call yourself. */
    @Generated
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getExamDao() : null;
    }

    public String getTotalMarks() {
        return totalMarks;
    }

    public void setTotalMarks(String totalMarks) {
        this.totalMarks = totalMarks;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getAttemptsCount() {
        return attemptsCount;
    }

    public void setAttemptsCount(Integer attemptsCount) {
        this.attemptsCount = attemptsCount;
    }

    public Integer getPausedAttemptsCount() {
        return pausedAttemptsCount;
    }

    public void setPausedAttemptsCount(Integer pausedAttemptsCount) {
        this.pausedAttemptsCount = pausedAttemptsCount;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public Integer getNumberOfQuestions() {
        return numberOfQuestions;
    }

    public void setNumberOfQuestions(Integer numberOfQuestions) {
        this.numberOfQuestions = numberOfQuestions;
    }

    public String getNegativeMarks() {
        return negativeMarks;
    }

    public void setNegativeMarks(String negativeMarks) {
        this.negativeMarks = negativeMarks;
    }

    public String getMarkPerQuestion() {
        return markPerQuestion;
    }

    public void setMarkPerQuestion(String markPerQuestion) {
        this.markPerQuestion = markPerQuestion;
    }

    public Integer getTemplateType() {
        return templateType;
    }

    public void setTemplateType(Integer templateType) {
        this.templateType = templateType;
    }

    public Boolean getAllowRetake() {
        return allowRetake;
    }

    public void setAllowRetake(Boolean allowRetake) {
        this.allowRetake = allowRetake;
    }

    public Boolean getAllowPdf() {
        return allowPdf;
    }

    public void setAllowPdf(Boolean allowPdf) {
        this.allowPdf = allowPdf;
    }

    public Boolean getShowAnswers() {
        return showAnswers;
    }

    public void setShowAnswers(Boolean showAnswers) {
        this.showAnswers = showAnswers;
    }

    public Integer getMaxRetakes() {
        return maxRetakes;
    }

    public void setMaxRetakes(Integer maxRetakes) {
        this.maxRetakes = maxRetakes;
    }

    public String getAttemptsUrl() {
        return attemptsUrl;
    }

    public void setAttemptsUrl(String attemptsUrl) {
        this.attemptsUrl = attemptsUrl;
    }

    public String getDeviceAccessControl() {
        return deviceAccessControl;
    }

    public void setDeviceAccessControl(String deviceAccessControl) {
        this.deviceAccessControl = deviceAccessControl;
    }

    public Integer getCommentsCount() {
        return commentsCount;
    }

    public void setCommentsCount(Integer commentsCount) {
        this.commentsCount = commentsCount;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getSelectedLanguage() {
        return selectedLanguage;
    }

    public void setSelectedLanguage(String selectedLanguage) {
        this.selectedLanguage = selectedLanguage;
    }

    public Boolean getVariableMarkPerQuestion() {
        return variableMarkPerQuestion;
    }

    public void setVariableMarkPerQuestion(Boolean variableMarkPerQuestion) {
        this.variableMarkPerQuestion = variableMarkPerQuestion;
    }

    public Integer getPassPercentage() {
        return passPercentage;
    }

    public void setPassPercentage(Integer passPercentage) {
        this.passPercentage = passPercentage;
    }

    public Boolean getEnableRanks() {
        return enableRanks;
    }

    public void setEnableRanks(Boolean enableRanks) {
        this.enableRanks = enableRanks;
    }

    public Boolean getShowScore() {
        return showScore;
    }

    public void setShowScore(Boolean showScore) {
        this.showScore = showScore;
    }

    public Boolean getShowPercentile() {
        return showPercentile;
    }

    public void setShowPercentile(Boolean showPercentile) {
        this.showPercentile = showPercentile;
    }

    public StringList getCategories() {
        return categories;
    }

    public void setCategories(StringList categories) {
        this.categories = categories;
    }

    public Boolean getIsDetailsFetched() {
        return isDetailsFetched;
    }

    public void setIsDetailsFetched(Boolean isDetailsFetched) {
        this.isDetailsFetched = isDetailsFetched;
    }

    public Boolean getIsGrowthHackEnabled() {
        return isGrowthHackEnabled;
    }

    public void setIsGrowthHackEnabled(Boolean isGrowthHackEnabled) {
        this.isGrowthHackEnabled = isGrowthHackEnabled;
    }

    public String getShareTextForSolutionUnlock() {
        return shareTextForSolutionUnlock;
    }

    public void setShareTextForSolutionUnlock(String shareTextForSolutionUnlock) {
        this.shareTextForSolutionUnlock = shareTextForSolutionUnlock;
    }

    public Boolean getShowAnalytics() {
        return showAnalytics;
    }

    public void setShowAnalytics(Boolean showAnalytics) {
        this.showAnalytics = showAnalytics;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public Boolean getHasAudioQuestions() {
        return hasAudioQuestions;
    }

    public void setHasAudioQuestions(Boolean hasAudioQuestions) {
        this.hasAudioQuestions = hasAudioQuestions;
    }

    public String getRankPublishingDate() {
        return rankPublishingDate;
    }

    public void setRankPublishingDate(String rankPublishingDate) {
        this.rankPublishingDate = rankPublishingDate;
    }

    public Boolean getEnableQuizMode() {
        return enableQuizMode;
    }

    public void setEnableQuizMode(Boolean enableQuizMode) {
        this.enableQuizMode = enableQuizMode;
    }

    public Boolean getDisableAttemptResume() {
        return disableAttemptResume;
    }

    public void setDisableAttemptResume(Boolean disableAttemptResume) {
        this.disableAttemptResume = disableAttemptResume;
    }

    public Boolean getAllowPreemptiveSectionEnding() {
        return allowPreemptiveSectionEnding;
    }

    public void setAllowPreemptiveSectionEnding(Boolean allowPreemptiveSectionEnding) {
        this.allowPreemptiveSectionEnding = allowPreemptiveSectionEnding;
    }

    public String getExamDataModifiedOn() {
        return examDataModifiedOn;
    }

    public void setExamDataModifiedOn(String examDataModifiedOn) {
        this.examDataModifiedOn = examDataModifiedOn;
    }

    public Boolean getIsOfflineExam() {
        return isOfflineExam;
    }

    public void setIsOfflineExam(Boolean isOfflineExam) {
        this.isOfflineExam = isOfflineExam;
    }

    public Long getGraceDurationForOfflineSubmission() {
        return graceDurationForOfflineSubmission;
    }

    public void setGraceDurationForOfflineSubmission(Long graceDurationForOfflineSubmission) {
        this.graceDurationForOfflineSubmission = graceDurationForOfflineSubmission;
    }

    /** To-many relationship, resolved on first access (and after reset). Changes to to-many relations are not persisted, make changes to the target entity. */
    @Generated
    public List<Language> getLanguages() {
        if (languages == null) {
            __throwIfDetached();
            LanguageDao targetDao = daoSession.getLanguageDao();
            List<Language> languagesNew = targetDao._queryExam_Languages(id);
            synchronized (this) {
                if(languages == null) {
                    languages = languagesNew;
                }
            }
        }
        return languages;
    }

    /** Resets a to-many relationship, making the next get call to query for a fresh result. */
    @Generated
    public synchronized void resetLanguages() {
        languages = null;
    }

    /**
    * Convenient call for {@link org.greenrobot.greendao.AbstractDao#delete(Object)}.
    * Entity must attached to an entity context.
    */
    @Generated
    public void delete() {
        __throwIfDetached();
        myDao.delete(this);
    }

    /**
    * Convenient call for {@link org.greenrobot.greendao.AbstractDao#update(Object)}.
    * Entity must attached to an entity context.
    */
    @Generated
    public void update() {
        __throwIfDetached();
        myDao.update(this);
    }

    /**
    * Convenient call for {@link org.greenrobot.greendao.AbstractDao#refresh(Object)}.
    * Entity must attached to an entity context.
    */
    @Generated
    public void refresh() {
        __throwIfDetached();
        myDao.refresh(this);
    }

    @Generated
    private void __throwIfDetached() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
    }

    // KEEP METHODS - put your custom methods here
    protected Exam(Parcel in) {
        totalMarks = in.readString();
        url = in.readString();
        if (in.readByte() == 0) {
            id = null;
        } else {
            id = in.readLong();
        }
        if (in.readByte() == 0) {
            attemptsCount = null;
        } else {
            attemptsCount = in.readInt();
        }
        if (in.readByte() == 0) {
            pausedAttemptsCount = null;
        } else {
            pausedAttemptsCount = in.readInt();
        }
        title = in.readString();
        description = in.readString();
        startDate = in.readString();
        endDate = in.readString();
        duration = in.readString();
        if (in.readByte() == 0) {
            numberOfQuestions = null;
        } else {
            numberOfQuestions = in.readInt();
        }
        negativeMarks = in.readString();
        markPerQuestion = in.readString();
        if (in.readByte() == 0) {
            templateType = null;
        } else {
            templateType = in.readInt();
        }
        byte tmpAllowRetake = in.readByte();
        allowRetake = tmpAllowRetake == 0 ? null : tmpAllowRetake == 1;
        byte tmpAllowPdf = in.readByte();
        allowPdf = tmpAllowPdf == 0 ? null : tmpAllowPdf == 1;
        byte tmpShowAnswers = in.readByte();
        showAnswers = tmpShowAnswers == 0 ? null : tmpShowAnswers == 1;
        if (in.readByte() == 0) {
            maxRetakes = null;
        } else {
            maxRetakes = in.readInt();
        }
        attemptsUrl = in.readString();
        deviceAccessControl = in.readString();
        if (in.readByte() == 0) {
            commentsCount = null;
        } else {
            commentsCount = in.readInt();
        }
        slug = in.readString();
        selectedLanguage = in.readString();
        byte tmpVariableMarkPerQuestion = in.readByte();
        variableMarkPerQuestion = tmpVariableMarkPerQuestion == 0 ?
                null : tmpVariableMarkPerQuestion == 1;

        if (in.readByte() == 0) {
            passPercentage = null;
        } else {
            passPercentage = in.readInt();
        }
        byte tmpEnableRanks = in.readByte();
        enableRanks = tmpEnableRanks == 0 ? null : tmpEnableRanks == 1;
        byte tmpShowScore = in.readByte();
        showScore = tmpShowScore == 0 ? null : tmpShowScore == 1;
        byte tmpShowPercentile = in.readByte();
        showPercentile = tmpShowPercentile == 0 ? null : tmpShowPercentile == 1;
        languages = in.createTypedArrayList(Language.CREATOR);
        byte tmpIsGrowthHackEnabled = in.readByte();
        isGrowthHackEnabled = tmpIsGrowthHackEnabled == 0 ? null : tmpIsGrowthHackEnabled == 1;
        shareTextForSolutionUnlock = in.readString();
        byte tmpshowAnalytics = in.readByte();
        showAnalytics = tmpshowAnalytics == 0 ? null : tmpshowAnalytics == 1;
        instructions = in.readString();
        rankPublishingDate = in.readString();
        byte tmpEnableQuizMode = in.readByte();
        enableQuizMode = tmpEnableQuizMode == 0 ? null : tmpEnableQuizMode == 1;
        byte tmpDisableAttemptResume = in.readByte();
        disableAttemptResume = tmpDisableAttemptResume == 0 ? null : tmpDisableAttemptResume == 1;
        byte tmpallowPreemptiveSectionEnding = in.readByte();
        allowPreemptiveSectionEnding = tmpallowPreemptiveSectionEnding == 0 ? null : tmpallowPreemptiveSectionEnding == 1;
        examDataModifiedOn = in.readString();
        byte tmpIsOfflineExam = in.readByte();
        isOfflineExam = tmpIsOfflineExam == 0 ? null : tmpIsOfflineExam == 1;
        if (in.readByte() == 0) {
            graceDurationForOfflineSubmission = null;
        } else {
            graceDurationForOfflineSubmission = in.readLong();
        }
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(totalMarks);
        dest.writeString(url);
        if (id == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(id);
        }
        if (attemptsCount == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(attemptsCount);
        }
        if (pausedAttemptsCount == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(pausedAttemptsCount);
        }
        dest.writeString(title);
        dest.writeString(description);
        dest.writeString(startDate);
        dest.writeString(endDate);
        dest.writeString(duration);
        if (numberOfQuestions == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(numberOfQuestions);
        }
        dest.writeString(negativeMarks);
        dest.writeString(markPerQuestion);
        if (templateType == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(templateType);
        }
        dest.writeByte((byte) (allowRetake == null ? 0 : allowRetake ? 1 : 2));
        dest.writeByte((byte) (allowPdf == null ? 0 : allowPdf ? 1 : 2));
        dest.writeByte((byte) (showAnswers == null ? 0 : showAnswers ? 1 : 2));
        if (maxRetakes == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(maxRetakes);
        }
        dest.writeString(attemptsUrl);
        dest.writeString(deviceAccessControl);
        if (commentsCount == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(commentsCount);
        }
        dest.writeString(slug);
        dest.writeString(selectedLanguage);
        dest.writeByte((byte) (variableMarkPerQuestion == null ?
                0 : variableMarkPerQuestion ? 1 : 2));

        if (passPercentage == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(passPercentage);
        }
        dest.writeByte((byte) (enableRanks == null ? 0 : enableRanks ? 1 : 2));
        dest.writeByte((byte) (showScore == null ? 0 : showScore ? 1 : 2));
        dest.writeByte((byte) (showPercentile == null ? 0 : showPercentile ? 1 : 2));
        dest.writeTypedList(getRawLanguages());
        dest.writeByte((byte) (isGrowthHackEnabled == null ? 0 : isGrowthHackEnabled ? 1 : 2));
        dest.writeString(shareTextForSolutionUnlock);
        dest.writeByte((byte) (showAnalytics == null ? 0 : showAnalytics ? 1 : 2));
        dest.writeString(instructions);
        dest.writeString(rankPublishingDate);
        dest.writeByte((byte) (enableQuizMode == null ? 0 : enableQuizMode ? 1 : 2));
        dest.writeByte((byte) (disableAttemptResume == null ? 0 : disableAttemptResume ? 1 : 2));
        dest.writeByte((byte) (allowPreemptiveSectionEnding == null ? 0 : allowPreemptiveSectionEnding ? 1 : 2));
        dest.writeString(examDataModifiedOn);
        dest.writeByte((byte) (isOfflineExam == null ? 0 : isOfflineExam ? 1 : 2));
        if (graceDurationForOfflineSubmission == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(graceDurationForOfflineSubmission);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Exam> CREATOR = new Creator<Exam>() {
        @Override
        public Exam createFromParcel(Parcel in) {
            return new Exam(in);
        }

        @Override
        public Exam[] newArray(int size) {
            return new Exam[size];
        }
    };

    public String getAttemptsFrag() {
        try {
            URL url = new URL(attemptsUrl);
            return url.getFile().substring(1);
        }
        catch (Exception e) {
            return null;
        }
    }

    public String getFormattedStartDate() {
        return formatDate(startDate);
    }

    public String getFormattedEndDate() {
        return formatDate(endDate);
    }

    @SuppressLint("SimpleDateFormat")
    public boolean isEnded() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            if(getEndDate() != null && !getEndDate().isEmpty()) {
                return simpleDateFormat.parse(getEndDate()).before(new Date());
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return false;
    }

    @SuppressLint("SimpleDateFormat")
    public String formatDate(String inputString) {
        Date date = null;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            if(inputString != null && !inputString.isEmpty()) {
                date = simpleDateFormat.parse(inputString);
                DateFormat dateformat = DateFormat.getDateInstance();
                return dateformat.format(date);
            }
        } catch (ParseException e) {
        }
        return "forever";
    }

    @SuppressLint("SimpleDateFormat")
    public boolean hasStarted() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            if(getStartDate() != null && !getStartDate().isEmpty()) {
                return simpleDateFormat.parse(getStartDate()).before(new Date());
            }
        } catch (ParseException e) {
        }
        return true;
    }

    public String getUrlFrag() {
        try {
            URL fragUrl = new URL(url);
            return fragUrl.getFile().substring(1);
        }
        catch (Exception e) {
            return null;
        }
    }

    public boolean canRetake() {
        return getAllowRetake() &&
                (getAttemptsCount() <= getMaxRetakes() || getMaxRetakes() < 0);
    }

    public void setLanguages(List<Language> languages) {
        this.languages = languages;
    }

    public void saveLanguages(Context context) {
        LanguageDao languageDao = TestpressSDKDatabase.getLanguageDao(context);
        languageDao.queryBuilder()
                .where(LanguageDao.Properties.ExamId.eq(getId()))
                .buildDelete()
                .executeDeleteWithoutDetachingEntities();

        List<Language> languages = getRawLanguages();
        for (Language language : languages) {
            language.setExamId(getId());
        }
        languageDao.insertOrReplaceInTx(languages);
    }

    public List<Language> getRawLanguages() {
        if (myDao == null) {
            if (languages == null) {
                return Collections.emptyList();
            }
            return languages;
        }
        return getLanguages();
    }

    public boolean hasMultipleLanguages() {
        return getRawLanguages().size() > 1;
    }

    public String getShareToUnlockSharedPreferenceKey() {
        return this.id.toString() + SHARE_TO_UNLOCK;
    }

    public boolean isGrowthHackEnabled() {
        if (isGrowthHackEnabled != null) {
            return isGrowthHackEnabled;
        }
        return false;
    }

    public boolean hasInstructions(){
        return !TextUtils.isEmpty(this.getInstructions());
    }

    public boolean showAnalytics() {
        return showAnalytics != null && showAnalytics == true;
    }

    public boolean isQuizModeEnabled() {
        return enableQuizMode != null && enableQuizMode;
    }

    public boolean isAttemptResumeDisabled() {
        return disableAttemptResume != null && disableAttemptResume;
    }

    public boolean isPreemptiveSectionEndingEnabled() {
        return allowPreemptiveSectionEnding != null && allowPreemptiveSectionEnding;
    }
    // KEEP METHODS END

}
