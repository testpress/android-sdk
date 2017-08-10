package in.testpress.exam.models.greendao;

import org.greenrobot.greendao.annotation.*;

import java.util.List;
import in.testpress.exam.models.greendao.DaoSession;
import org.greenrobot.greendao.DaoException;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT. Enable "keep" sections if you want to edit.

/**
 * Entity mapped to table "REVIEW_QUESTION_TRANSLATION".
 */
@Entity(active = true)
public class ReviewQuestionTranslation {

    @Id(autoincrement = true)
    private Long id;
    private String questionHtml;
    private String direction;
    private String explanation;
    private String language;
    private Long questionId;

    /** Used to resolve relations */
    @Generated
    private transient DaoSession daoSession;

    /** Used for active entity operations. */
    @Generated
    private transient ReviewQuestionTranslationDao myDao;

    @ToMany(joinProperties = {
        @JoinProperty(name = "id", referencedName = "questionTranslationId")
    })
    private List<ReviewAnswerTranslation> answers;

    @Generated
    public ReviewQuestionTranslation() {
    }

    public ReviewQuestionTranslation(Long id) {
        this.id = id;
    }

    @Generated
    public ReviewQuestionTranslation(Long id, String questionHtml, String direction, String explanation, String language, Long questionId) {
        this.id = id;
        this.questionHtml = questionHtml;
        this.direction = direction;
        this.explanation = explanation;
        this.language = language;
        this.questionId = questionId;
    }

    /** called by internal mechanisms, do not call yourself. */
    @Generated
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getReviewQuestionTranslationDao() : null;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getQuestionHtml() {
        return questionHtml;
    }

    public void setQuestionHtml(String questionHtml) {
        this.questionHtml = questionHtml;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    /** To-many relationship, resolved on first access (and after reset). Changes to to-many relations are not persisted, make changes to the target entity. */
    @Generated
    public List<ReviewAnswerTranslation> getAnswers() {
        if (answers == null) {
            __throwIfDetached();
            ReviewAnswerTranslationDao targetDao = daoSession.getReviewAnswerTranslationDao();
            List<ReviewAnswerTranslation> answersNew = targetDao._queryReviewQuestionTranslation_Answers(id);
            synchronized (this) {
                if(answers == null) {
                    answers = answersNew;
                }
            }
        }
        return answers;
    }

    /** Resets a to-many relationship, making the next get call to query for a fresh result. */
    @Generated
    public synchronized void resetAnswers() {
        answers = null;
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
