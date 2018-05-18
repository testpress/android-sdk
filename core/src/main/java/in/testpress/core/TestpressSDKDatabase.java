package in.testpress.core;

import android.content.Context;
import android.support.annotation.NonNull;

import org.greenrobot.greendao.database.Database;

import in.testpress.models.greendao.AnswerTranslationDao;
import in.testpress.models.greendao.AttachmentDao;
import in.testpress.models.greendao.AttemptDao;
import in.testpress.models.greendao.BookmarkDao;
import in.testpress.models.greendao.BookmarkFolderDao;
import in.testpress.models.greendao.ChapterDao;
import in.testpress.models.greendao.ContentDao;
import in.testpress.models.greendao.ContentTypeDao;
import in.testpress.models.greendao.CourseAttemptDao;
import in.testpress.models.greendao.CourseDao;
import in.testpress.models.greendao.DaoMaster;
import in.testpress.models.greendao.DaoSession;
import in.testpress.models.greendao.DirectionDao;
import in.testpress.models.greendao.DirectionTranslationDao;
import in.testpress.models.greendao.ExamDao;
import in.testpress.models.greendao.HtmlContentDao;
import in.testpress.models.greendao.LanguageDao;
import in.testpress.models.greendao.ReviewAnswerDao;
import in.testpress.models.greendao.ReviewAnswerTranslationDao;
import in.testpress.models.greendao.ReviewAttemptDao;
import in.testpress.models.greendao.ReviewItemDao;
import in.testpress.models.greendao.ReviewQuestionDao;
import in.testpress.models.greendao.ReviewQuestionTranslationDao;
import in.testpress.models.greendao.SelectedAnswerDao;
import in.testpress.models.greendao.SubjectDao;
import in.testpress.models.greendao.VideoDao;

public class TestpressSDKDatabase {

    private static DaoSession daoSession;
    private static Database database;

    public static Database getDatabase(@NonNull Context context) {
        if (database == null) {
            DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(
                    context.getApplicationContext(), TestpressSdk.TESTPRESS_SDK_DATABASE);

            database = helper.getWritableDb();
        }
        return database;
    }

    public static DaoSession getDaoSession(Context context) {
        if (daoSession == null) {
            database = getDatabase(context);
            daoSession = new DaoMaster(database).newSession();
        }
        return daoSession;
    }

    public static void clearDatabase(@NonNull Context context) {
        Database database = getDatabase(context);
        DaoMaster.dropAllTables(database, true);
        DaoMaster.createAllTables(database, true);
    }

    public static ExamDao getExamDao(Context context) {
        return getDaoSession(context).getExamDao();
    }

    public static LanguageDao getLanguageDao(Context context) {
        return getDaoSession(context).getLanguageDao();
    }

    public static ContentDao getContentDao(Context context) {
        return getDaoSession(context).getContentDao();
    }

    public static VideoDao getVideoDao(Context context) {
        return getDaoSession(context).getVideoDao();
    }

    public static AttachmentDao getAttachmentDao(Context context) {
        return getDaoSession(context).getAttachmentDao();
    }

    public static HtmlContentDao getHtmlContentDao(Context context) {
        return getDaoSession(context).getHtmlContentDao();
    }

    public static CourseAttemptDao getCourseAttemptDao(Context context) {
        return getDaoSession(context).getCourseAttemptDao();
    }

    public static AttemptDao getAttemptDao(Context context) {
        return getDaoSession(context).getAttemptDao();
    }

    public static CourseDao getCourseDao(Context context) {
        return getDaoSession(context).getCourseDao();
    }

    public static ChapterDao getChapterDao(Context context) {
        return getDaoSession(context).getChapterDao();
    }

    public static ReviewAttemptDao getReviewAttemptDao(Context context) {
        return getDaoSession(context).getReviewAttemptDao();
    }

    public static ReviewItemDao getReviewItemDao(Context context) {
        return getDaoSession(context).getReviewItemDao();
    }

    public static ReviewQuestionDao getReviewQuestionDao(Context context) {
        return getDaoSession(context).getReviewQuestionDao();
    }

    public static ReviewAnswerDao getReviewAnswerDao(Context context) {
        return getDaoSession(context).getReviewAnswerDao();
    }

    public static ReviewQuestionTranslationDao getReviewQuestionTranslationDao(Context context) {
        return getDaoSession(context).getReviewQuestionTranslationDao();
    }

    public static ReviewAnswerTranslationDao getReviewAnswerTranslationDao(Context context) {
        return getDaoSession(context).getReviewAnswerTranslationDao();
    }

    public static SelectedAnswerDao getSelectedAnswerDao(Context context) {
        return getDaoSession(context).getSelectedAnswerDao();
    }

    public static BookmarkFolderDao getBookmarkFolderDao(Context context) {
        return getDaoSession(context).getBookmarkFolderDao();
    }

    public static AnswerTranslationDao getAnswerTranslationDao(Context context) {
        return getDaoSession(context).getAnswerTranslationDao();
    }

    public static DirectionDao getDirectionDao(Context context) {
        return getDaoSession(context).getDirectionDao();
    }

    public static DirectionTranslationDao getDirectionTranslationDao(Context context) {
        return getDaoSession(context).getDirectionTranslationDao();
    }

    public static SubjectDao getSubjectDao(Context context) {
        return getDaoSession(context).getSubjectDao();
    }

    public static BookmarkDao getBookmarkDao(Context context) {
        return getDaoSession(context).getBookmarkDao();
    }

    public static ContentTypeDao getContentTypeDao(Context context) {
        return getDaoSession(context).getContentTypeDao();
    }

}
