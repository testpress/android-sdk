package in.testpress.exam.models.greendao;

import org.greenrobot.greendao.annotation.*;

import in.testpress.exam.models.greendao.DaoSession;
import org.greenrobot.greendao.DaoException;

import java.util.List;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT. Enable "keep" sections if you want to edit.

/**
 * Entity mapped to table "REVIEW_ITEM".
 */
@Entity(active = true)
public class ReviewItem {

    @Id
    private Long id;
    private Integer index;
    private String url;

    @Convert(converter = IntegerListConverter.class, columnType = String.class)
    private List<Integer> selectedAnswers;
    private Boolean review;
    private Long attemptId;
    private Long questionId;

    /** Used to resolve relations */
    @Generated
    private transient DaoSession daoSession;

    /** Used for active entity operations. */
    @Generated
    private transient ReviewItemDao myDao;

    @ToOne(joinProperty = "questionId")
    public ReviewQuestion question;

    @Generated
    private transient Long question__resolvedKey;

    @Generated
    public ReviewItem() {
    }

    public ReviewItem(Long id) {
        this.id = id;
    }

    @Generated
    public ReviewItem(Long id, Integer index, String url, List<Integer> selectedAnswers, Boolean review, Long attemptId, Long questionId) {
        this.id = id;
        this.index = index;
        this.url = url;
        this.selectedAnswers = selectedAnswers;
        this.review = review;
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

    public List<Integer> getSelectedAnswers() {
        return selectedAnswers;
    }

    public void setSelectedAnswers(List<Integer> selectedAnswers) {
        this.selectedAnswers = selectedAnswers;
    }

    public Boolean getReview() {
        return review;
    }

    public void setReview(Boolean review) {
        this.review = review;
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

}
