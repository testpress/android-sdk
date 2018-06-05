package in.testpress.models.greendao;

import java.util.Map;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.AbstractDaoSession;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.identityscope.IdentityScopeType;
import org.greenrobot.greendao.internal.DaoConfig;

import in.testpress.models.greendao.ReviewAttempt;
import in.testpress.models.greendao.ReviewItem;
import in.testpress.models.greendao.ReviewQuestion;
import in.testpress.models.greendao.ReviewAnswer;
import in.testpress.models.greendao.ReviewQuestionTranslation;
import in.testpress.models.greendao.ReviewAnswerTranslation;
import in.testpress.models.greendao.SelectedAnswer;
import in.testpress.models.greendao.Course;
import in.testpress.models.greendao.Chapter;
import in.testpress.models.greendao.HtmlContent;
import in.testpress.models.greendao.Video;
import in.testpress.models.greendao.Attachment;
import in.testpress.models.greendao.Exam;
import in.testpress.models.greendao.Language;
import in.testpress.models.greendao.Content;
import in.testpress.models.greendao.CourseAttempt;
import in.testpress.models.greendao.Attempt;
import in.testpress.models.greendao.AttemptSection;
import in.testpress.models.greendao.BookmarkFolder;
import in.testpress.models.greendao.Bookmark;
import in.testpress.models.greendao.ContentType;
import in.testpress.models.greendao.AnswerTranslation;
import in.testpress.models.greendao.Subject;
import in.testpress.models.greendao.Direction;
import in.testpress.models.greendao.DirectionTranslation;

import in.testpress.models.greendao.ReviewAttemptDao;
import in.testpress.models.greendao.ReviewItemDao;
import in.testpress.models.greendao.ReviewQuestionDao;
import in.testpress.models.greendao.ReviewAnswerDao;
import in.testpress.models.greendao.ReviewQuestionTranslationDao;
import in.testpress.models.greendao.ReviewAnswerTranslationDao;
import in.testpress.models.greendao.SelectedAnswerDao;
import in.testpress.models.greendao.CourseDao;
import in.testpress.models.greendao.ChapterDao;
import in.testpress.models.greendao.HtmlContentDao;
import in.testpress.models.greendao.VideoDao;
import in.testpress.models.greendao.AttachmentDao;
import in.testpress.models.greendao.ExamDao;
import in.testpress.models.greendao.LanguageDao;
import in.testpress.models.greendao.ContentDao;
import in.testpress.models.greendao.CourseAttemptDao;
import in.testpress.models.greendao.AttemptDao;
import in.testpress.models.greendao.AttemptSectionDao;
import in.testpress.models.greendao.BookmarkFolderDao;
import in.testpress.models.greendao.BookmarkDao;
import in.testpress.models.greendao.ContentTypeDao;
import in.testpress.models.greendao.AnswerTranslationDao;
import in.testpress.models.greendao.SubjectDao;
import in.testpress.models.greendao.DirectionDao;
import in.testpress.models.greendao.DirectionTranslationDao;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.

/**
 * {@inheritDoc}
 * 
 * @see org.greenrobot.greendao.AbstractDaoSession
 */
public class DaoSession extends AbstractDaoSession {

    private final DaoConfig reviewAttemptDaoConfig;
    private final DaoConfig reviewItemDaoConfig;
    private final DaoConfig reviewQuestionDaoConfig;
    private final DaoConfig reviewAnswerDaoConfig;
    private final DaoConfig reviewQuestionTranslationDaoConfig;
    private final DaoConfig reviewAnswerTranslationDaoConfig;
    private final DaoConfig selectedAnswerDaoConfig;
    private final DaoConfig courseDaoConfig;
    private final DaoConfig chapterDaoConfig;
    private final DaoConfig htmlContentDaoConfig;
    private final DaoConfig videoDaoConfig;
    private final DaoConfig attachmentDaoConfig;
    private final DaoConfig examDaoConfig;
    private final DaoConfig languageDaoConfig;
    private final DaoConfig contentDaoConfig;
    private final DaoConfig courseAttemptDaoConfig;
    private final DaoConfig attemptDaoConfig;
    private final DaoConfig attemptSectionDaoConfig;
    private final DaoConfig bookmarkFolderDaoConfig;
    private final DaoConfig bookmarkDaoConfig;
    private final DaoConfig contentTypeDaoConfig;
    private final DaoConfig answerTranslationDaoConfig;
    private final DaoConfig subjectDaoConfig;
    private final DaoConfig directionDaoConfig;
    private final DaoConfig directionTranslationDaoConfig;

    private final ReviewAttemptDao reviewAttemptDao;
    private final ReviewItemDao reviewItemDao;
    private final ReviewQuestionDao reviewQuestionDao;
    private final ReviewAnswerDao reviewAnswerDao;
    private final ReviewQuestionTranslationDao reviewQuestionTranslationDao;
    private final ReviewAnswerTranslationDao reviewAnswerTranslationDao;
    private final SelectedAnswerDao selectedAnswerDao;
    private final CourseDao courseDao;
    private final ChapterDao chapterDao;
    private final HtmlContentDao htmlContentDao;
    private final VideoDao videoDao;
    private final AttachmentDao attachmentDao;
    private final ExamDao examDao;
    private final LanguageDao languageDao;
    private final ContentDao contentDao;
    private final CourseAttemptDao courseAttemptDao;
    private final AttemptDao attemptDao;
    private final AttemptSectionDao attemptSectionDao;
    private final BookmarkFolderDao bookmarkFolderDao;
    private final BookmarkDao bookmarkDao;
    private final ContentTypeDao contentTypeDao;
    private final AnswerTranslationDao answerTranslationDao;
    private final SubjectDao subjectDao;
    private final DirectionDao directionDao;
    private final DirectionTranslationDao directionTranslationDao;

    public DaoSession(Database db, IdentityScopeType type, Map<Class<? extends AbstractDao<?, ?>>, DaoConfig>
            daoConfigMap) {
        super(db);

        reviewAttemptDaoConfig = daoConfigMap.get(ReviewAttemptDao.class).clone();
        reviewAttemptDaoConfig.initIdentityScope(type);

        reviewItemDaoConfig = daoConfigMap.get(ReviewItemDao.class).clone();
        reviewItemDaoConfig.initIdentityScope(type);

        reviewQuestionDaoConfig = daoConfigMap.get(ReviewQuestionDao.class).clone();
        reviewQuestionDaoConfig.initIdentityScope(type);

        reviewAnswerDaoConfig = daoConfigMap.get(ReviewAnswerDao.class).clone();
        reviewAnswerDaoConfig.initIdentityScope(type);

        reviewQuestionTranslationDaoConfig = daoConfigMap.get(ReviewQuestionTranslationDao.class).clone();
        reviewQuestionTranslationDaoConfig.initIdentityScope(type);

        reviewAnswerTranslationDaoConfig = daoConfigMap.get(ReviewAnswerTranslationDao.class).clone();
        reviewAnswerTranslationDaoConfig.initIdentityScope(type);

        selectedAnswerDaoConfig = daoConfigMap.get(SelectedAnswerDao.class).clone();
        selectedAnswerDaoConfig.initIdentityScope(type);

        courseDaoConfig = daoConfigMap.get(CourseDao.class).clone();
        courseDaoConfig.initIdentityScope(type);

        chapterDaoConfig = daoConfigMap.get(ChapterDao.class).clone();
        chapterDaoConfig.initIdentityScope(type);

        htmlContentDaoConfig = daoConfigMap.get(HtmlContentDao.class).clone();
        htmlContentDaoConfig.initIdentityScope(type);

        videoDaoConfig = daoConfigMap.get(VideoDao.class).clone();
        videoDaoConfig.initIdentityScope(type);

        attachmentDaoConfig = daoConfigMap.get(AttachmentDao.class).clone();
        attachmentDaoConfig.initIdentityScope(type);

        examDaoConfig = daoConfigMap.get(ExamDao.class).clone();
        examDaoConfig.initIdentityScope(type);

        languageDaoConfig = daoConfigMap.get(LanguageDao.class).clone();
        languageDaoConfig.initIdentityScope(type);

        contentDaoConfig = daoConfigMap.get(ContentDao.class).clone();
        contentDaoConfig.initIdentityScope(type);

        courseAttemptDaoConfig = daoConfigMap.get(CourseAttemptDao.class).clone();
        courseAttemptDaoConfig.initIdentityScope(type);

        attemptDaoConfig = daoConfigMap.get(AttemptDao.class).clone();
        attemptDaoConfig.initIdentityScope(type);

        attemptSectionDaoConfig = daoConfigMap.get(AttemptSectionDao.class).clone();
        attemptSectionDaoConfig.initIdentityScope(type);

        bookmarkFolderDaoConfig = daoConfigMap.get(BookmarkFolderDao.class).clone();
        bookmarkFolderDaoConfig.initIdentityScope(type);

        bookmarkDaoConfig = daoConfigMap.get(BookmarkDao.class).clone();
        bookmarkDaoConfig.initIdentityScope(type);

        contentTypeDaoConfig = daoConfigMap.get(ContentTypeDao.class).clone();
        contentTypeDaoConfig.initIdentityScope(type);

        answerTranslationDaoConfig = daoConfigMap.get(AnswerTranslationDao.class).clone();
        answerTranslationDaoConfig.initIdentityScope(type);

        subjectDaoConfig = daoConfigMap.get(SubjectDao.class).clone();
        subjectDaoConfig.initIdentityScope(type);

        directionDaoConfig = daoConfigMap.get(DirectionDao.class).clone();
        directionDaoConfig.initIdentityScope(type);

        directionTranslationDaoConfig = daoConfigMap.get(DirectionTranslationDao.class).clone();
        directionTranslationDaoConfig.initIdentityScope(type);

        reviewAttemptDao = new ReviewAttemptDao(reviewAttemptDaoConfig, this);
        reviewItemDao = new ReviewItemDao(reviewItemDaoConfig, this);
        reviewQuestionDao = new ReviewQuestionDao(reviewQuestionDaoConfig, this);
        reviewAnswerDao = new ReviewAnswerDao(reviewAnswerDaoConfig, this);
        reviewQuestionTranslationDao = new ReviewQuestionTranslationDao(reviewQuestionTranslationDaoConfig, this);
        reviewAnswerTranslationDao = new ReviewAnswerTranslationDao(reviewAnswerTranslationDaoConfig, this);
        selectedAnswerDao = new SelectedAnswerDao(selectedAnswerDaoConfig, this);
        courseDao = new CourseDao(courseDaoConfig, this);
        chapterDao = new ChapterDao(chapterDaoConfig, this);
        htmlContentDao = new HtmlContentDao(htmlContentDaoConfig, this);
        videoDao = new VideoDao(videoDaoConfig, this);
        attachmentDao = new AttachmentDao(attachmentDaoConfig, this);
        examDao = new ExamDao(examDaoConfig, this);
        languageDao = new LanguageDao(languageDaoConfig, this);
        contentDao = new ContentDao(contentDaoConfig, this);
        courseAttemptDao = new CourseAttemptDao(courseAttemptDaoConfig, this);
        attemptDao = new AttemptDao(attemptDaoConfig, this);
        attemptSectionDao = new AttemptSectionDao(attemptSectionDaoConfig, this);
        bookmarkFolderDao = new BookmarkFolderDao(bookmarkFolderDaoConfig, this);
        bookmarkDao = new BookmarkDao(bookmarkDaoConfig, this);
        contentTypeDao = new ContentTypeDao(contentTypeDaoConfig, this);
        answerTranslationDao = new AnswerTranslationDao(answerTranslationDaoConfig, this);
        subjectDao = new SubjectDao(subjectDaoConfig, this);
        directionDao = new DirectionDao(directionDaoConfig, this);
        directionTranslationDao = new DirectionTranslationDao(directionTranslationDaoConfig, this);

        registerDao(ReviewAttempt.class, reviewAttemptDao);
        registerDao(ReviewItem.class, reviewItemDao);
        registerDao(ReviewQuestion.class, reviewQuestionDao);
        registerDao(ReviewAnswer.class, reviewAnswerDao);
        registerDao(ReviewQuestionTranslation.class, reviewQuestionTranslationDao);
        registerDao(ReviewAnswerTranslation.class, reviewAnswerTranslationDao);
        registerDao(SelectedAnswer.class, selectedAnswerDao);
        registerDao(Course.class, courseDao);
        registerDao(Chapter.class, chapterDao);
        registerDao(HtmlContent.class, htmlContentDao);
        registerDao(Video.class, videoDao);
        registerDao(Attachment.class, attachmentDao);
        registerDao(Exam.class, examDao);
        registerDao(Language.class, languageDao);
        registerDao(Content.class, contentDao);
        registerDao(CourseAttempt.class, courseAttemptDao);
        registerDao(Attempt.class, attemptDao);
        registerDao(AttemptSection.class, attemptSectionDao);
        registerDao(BookmarkFolder.class, bookmarkFolderDao);
        registerDao(Bookmark.class, bookmarkDao);
        registerDao(ContentType.class, contentTypeDao);
        registerDao(AnswerTranslation.class, answerTranslationDao);
        registerDao(Subject.class, subjectDao);
        registerDao(Direction.class, directionDao);
        registerDao(DirectionTranslation.class, directionTranslationDao);
    }
    
    public void clear() {
        reviewAttemptDaoConfig.clearIdentityScope();
        reviewItemDaoConfig.clearIdentityScope();
        reviewQuestionDaoConfig.clearIdentityScope();
        reviewAnswerDaoConfig.clearIdentityScope();
        reviewQuestionTranslationDaoConfig.clearIdentityScope();
        reviewAnswerTranslationDaoConfig.clearIdentityScope();
        selectedAnswerDaoConfig.clearIdentityScope();
        courseDaoConfig.clearIdentityScope();
        chapterDaoConfig.clearIdentityScope();
        htmlContentDaoConfig.clearIdentityScope();
        videoDaoConfig.clearIdentityScope();
        attachmentDaoConfig.clearIdentityScope();
        examDaoConfig.clearIdentityScope();
        languageDaoConfig.clearIdentityScope();
        contentDaoConfig.clearIdentityScope();
        courseAttemptDaoConfig.clearIdentityScope();
        attemptDaoConfig.clearIdentityScope();
        attemptSectionDaoConfig.clearIdentityScope();
        bookmarkFolderDaoConfig.clearIdentityScope();
        bookmarkDaoConfig.clearIdentityScope();
        contentTypeDaoConfig.clearIdentityScope();
        answerTranslationDaoConfig.clearIdentityScope();
        subjectDaoConfig.clearIdentityScope();
        directionDaoConfig.clearIdentityScope();
        directionTranslationDaoConfig.clearIdentityScope();
    }

    public ReviewAttemptDao getReviewAttemptDao() {
        return reviewAttemptDao;
    }

    public ReviewItemDao getReviewItemDao() {
        return reviewItemDao;
    }

    public ReviewQuestionDao getReviewQuestionDao() {
        return reviewQuestionDao;
    }

    public ReviewAnswerDao getReviewAnswerDao() {
        return reviewAnswerDao;
    }

    public ReviewQuestionTranslationDao getReviewQuestionTranslationDao() {
        return reviewQuestionTranslationDao;
    }

    public ReviewAnswerTranslationDao getReviewAnswerTranslationDao() {
        return reviewAnswerTranslationDao;
    }

    public SelectedAnswerDao getSelectedAnswerDao() {
        return selectedAnswerDao;
    }

    public CourseDao getCourseDao() {
        return courseDao;
    }

    public ChapterDao getChapterDao() {
        return chapterDao;
    }

    public HtmlContentDao getHtmlContentDao() {
        return htmlContentDao;
    }

    public VideoDao getVideoDao() {
        return videoDao;
    }

    public AttachmentDao getAttachmentDao() {
        return attachmentDao;
    }

    public ExamDao getExamDao() {
        return examDao;
    }

    public LanguageDao getLanguageDao() {
        return languageDao;
    }

    public ContentDao getContentDao() {
        return contentDao;
    }

    public CourseAttemptDao getCourseAttemptDao() {
        return courseAttemptDao;
    }

    public AttemptDao getAttemptDao() {
        return attemptDao;
    }

    public AttemptSectionDao getAttemptSectionDao() {
        return attemptSectionDao;
    }

    public BookmarkFolderDao getBookmarkFolderDao() {
        return bookmarkFolderDao;
    }

    public BookmarkDao getBookmarkDao() {
        return bookmarkDao;
    }

    public ContentTypeDao getContentTypeDao() {
        return contentTypeDao;
    }

    public AnswerTranslationDao getAnswerTranslationDao() {
        return answerTranslationDao;
    }

    public SubjectDao getSubjectDao() {
        return subjectDao;
    }

    public DirectionDao getDirectionDao() {
        return directionDao;
    }

    public DirectionTranslationDao getDirectionTranslationDao() {
        return directionTranslationDao;
    }

}
