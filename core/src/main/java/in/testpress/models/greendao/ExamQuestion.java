package in.testpress.models.greendao;

import org.greenrobot.greendao.annotation.*;

import in.testpress.models.greendao.DaoSession;
import org.greenrobot.greendao.DaoException;

// THIS CODE IS GENERATED BY greenDAO, EDIT ONLY INSIDE THE "KEEP"-SECTIONS

// KEEP INCLUDES - put your custom includes here
// KEEP INCLUDES END

/**
 * Entity mapped to table "EXAM_QUESTION".
 */
@Entity(active = true)
public class ExamQuestion {

    @Id
    private Long id;
    private Integer order;
    private Long examId;
    private Long attemptId;
    private Long questionId;

    /** Used to resolve relations */
    @Generated
    private transient DaoSession daoSession;

    /** Used for active entity operations. */
    @Generated
    private transient ExamQuestionDao myDao;

    @ToOne(joinProperty = "questionId")
    private Question question;

    @Generated
    private transient Long question__resolvedKey;

    // KEEP FIELDS - put your custom fields here
    // KEEP FIELDS END

    @Generated
    public ExamQuestion() {
    }

    public ExamQuestion(Long id) {
        this.id = id;
    }

    @Generated
    public ExamQuestion(Long id, Integer order, Long examId, Long attemptId, Long questionId) {
        this.id = id;
        this.order = order;
        this.examId = examId;
        this.attemptId = attemptId;
        this.questionId = questionId;
    }

    /** called by internal mechanisms, do not call yourself. */
    @Generated
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getExamQuestionDao() : null;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    public Long getExamId() {
        return examId;
    }

    public void setExamId(Long examId) {
        this.examId = examId;
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
    public Question getQuestion() {
        Long __key = this.questionId;
        if (question__resolvedKey == null || !question__resolvedKey.equals(__key)) {
            __throwIfDetached();
            QuestionDao targetDao = daoSession.getQuestionDao();
            Question questionNew = targetDao.load(__key);
            synchronized (this) {
                question = questionNew;
            	question__resolvedKey = __key;
            }
        }
        return question;
    }

    @Generated
    public void setQuestion(Question question) {
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
    // KEEP METHODS END

}
