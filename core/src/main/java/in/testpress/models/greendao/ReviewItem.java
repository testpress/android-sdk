package in.testpress.models.greendao;

import org.greenrobot.greendao.annotation.*;

import in.testpress.models.greendao.DaoSession;
import org.greenrobot.greendao.DaoException;

import in.testpress.util.IntegerList;

// THIS CODE IS GENERATED BY greenDAO, EDIT ONLY INSIDE THE "KEEP"-SECTIONS

// KEEP INCLUDES - put your custom includes here
import java.util.List;

import android.content.Context;
import android.os.Parcel;

import com.google.gson.annotations.SerializedName;

import in.testpress.core.TestpressSDKDatabase;
// KEEP INCLUDES END

/**
 * Entity mapped to table "REVIEW_ITEM".
 */
@Entity(active = true)
public class ReviewItem {

    @Id
    private Long id;
    private Integer index;
    private String url;
    private Integer order;
    private String duration;
    private String bestDuration;
    private String averageDuration;
    private String essayText;
    private String essayTopic;

    @SerializedName(value="selected_answer_ids", alternate={"selected_answers"})
    @Convert(converter = in.testpress.util.IntegerListConverter.class, columnType = String.class)
    private IntegerList selectedAnswers;
    private Boolean review;
    private Integer commentsCount;
    private Integer correctPercentage;
    private Long bookmarkId;
    private Long attemptId;
    private Long questionId;

    /** Used to resolve relations */
    @Generated
    private transient DaoSession daoSession;

    /** Used for active entity operations. */
    @Generated
    private transient ReviewItemDao myDao;

    @ToOne(joinProperty = "questionId")
    private ReviewQuestion question;

    @Generated
    private transient Long question__resolvedKey;

    // KEEP FIELDS - put your custom fields here
    // KEEP FIELDS END

    @Generated
    public ReviewItem() {
    }

    public ReviewItem(Long id) {
        this.id = id;
    }

    @Generated
    public ReviewItem(Long id, Integer index, String url, Integer order, String duration, String bestDuration, String averageDuration, String essayText, String essayTopic, IntegerList selectedAnswers, Boolean review, Integer commentsCount, Integer correctPercentage, Long bookmarkId, Long attemptId, Long questionId) {
        this.id = id;
        this.index = index;
        this.url = url;
        this.order = order;
        this.duration = duration;
        this.bestDuration = bestDuration;
        this.averageDuration = averageDuration;
        this.essayText = essayText;
        this.essayTopic = essayTopic;
        this.selectedAnswers = selectedAnswers;
        this.review = review;
        this.commentsCount = commentsCount;
        this.correctPercentage = correctPercentage;
        this.bookmarkId = bookmarkId;
        this.attemptId = attemptId;
        this.questionId = questionId;
    }

    /** called by internal mechanisms, do not call yourself. */
    @Generated
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getReviewItemDao() : null;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getBestDuration() {
        return bestDuration;
    }

    public void setBestDuration(String bestDuration) {
        this.bestDuration = bestDuration;
    }

    public String getAverageDuration() {
        return averageDuration;
    }

    public void setAverageDuration(String averageDuration) {
        this.averageDuration = averageDuration;
    }

    public String getEssayText() {
        return essayText;
    }

    public void setEssayText(String essayText) {
        this.essayText = essayText;
    }

    public String getEssayTopic() {
        return essayTopic;
    }

    public void setEssayTopic(String essayTopic) {
        this.essayTopic = essayTopic;
    }

    public IntegerList getSelectedAnswers() {
        return selectedAnswers;
    }

    public void setSelectedAnswers(IntegerList selectedAnswers) {
        this.selectedAnswers = selectedAnswers;
    }

    public Boolean getReview() {
        return review;
    }

    public void setReview(Boolean review) {
        this.review = review;
    }

    public Integer getCommentsCount() {
        return commentsCount;
    }

    public void setCommentsCount(Integer commentsCount) {
        this.commentsCount = commentsCount;
    }

    public Integer getCorrectPercentage() {
        return correctPercentage;
    }

    public void setCorrectPercentage(Integer correctPercentage) {
        this.correctPercentage = correctPercentage;
    }

    public Long getBookmarkId() {
        return bookmarkId;
    }

    public void setBookmarkId(Long bookmarkId) {
        this.bookmarkId = bookmarkId;
    }

    public Long getAttemptId() {
        return attemptId;
    }

    public void setAttemptId(Long attemptId) {
        this.attemptId = attemptId;
    }

    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    /** To-one relationship, resolved on first access. */
    @Generated
    public ReviewQuestion getQuestion() {
        Long __key = this.questionId;
        if (question__resolvedKey == null || !question__resolvedKey.equals(__key)) {
            __throwIfDetached();
            ReviewQuestionDao targetDao = daoSession.getReviewQuestionDao();
            ReviewQuestion questionNew = targetDao.load(__key);
            synchronized (this) {
                question = questionNew;
            	question__resolvedKey = __key;
            }
        }
        return question;
    }

    @Generated
    public void setQuestion(ReviewQuestion question) {
        synchronized (this) {
            this.question = question;
            questionId = question == null ? null : question.getId();
            question__resolvedKey = questionId;
        }
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
    public ReviewQuestion getRawQuestion() {
        if (myDao == null || question != null) {
            return question;
        }
        return getQuestion();
    }

    public static void save(Context context, List<ReviewItem> reviewItems) {
        ReviewItemDao reviewItemDao = TestpressSDKDatabase.getReviewItemDao(context);
        for (int i = 0; i < reviewItems.size(); i++) {
            ReviewItem reviewItem = reviewItems.get(i);
            List<ReviewItem> reviewItemsFromDB = reviewItemDao.queryBuilder()
                    .where(ReviewItemDao.Properties.Id.eq(reviewItem.getId())).list();

            if (!reviewItemsFromDB.isEmpty()) {
                reviewItem = reviewItemsFromDB.get(0);
            }
            reviewItemDao.insertOrReplace(reviewItem);
        }
    }
    // KEEP METHODS END

}
