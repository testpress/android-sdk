package in.testpress.exam;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;

import org.greenrobot.greendao.database.Database;

import junit.framework.Assert;

import in.testpress.core.TestpressSdk;
import in.testpress.core.TestpressSession;
import in.testpress.exam.models.CourseContent;
import in.testpress.exam.models.CourseAttempt;
import in.testpress.exam.models.greendao.DaoMaster;
import in.testpress.exam.models.greendao.DaoSession;
import in.testpress.exam.models.greendao.ReviewAnswerDao;
import in.testpress.exam.models.greendao.ReviewAttemptDao;
import in.testpress.exam.models.greendao.ReviewItemDao;
import in.testpress.exam.models.greendao.ReviewQuestionDao;
import in.testpress.exam.models.greendao.SelectedAnswerDao;
import in.testpress.exam.ui.CarouselFragment;
import in.testpress.exam.ui.CategoriesGridFragment;
import in.testpress.exam.ui.CategoryGridActivity;
import in.testpress.exam.ui.ExamsListActivity;
import in.testpress.exam.ui.TestActivity;
import in.testpress.util.ImageUtils;

public class TestpressExam {

    public static final String ACTION_PRESSED_HOME = "pressedHome";
    private static DaoSession daoSession;
    private static Database database;

    /**
     * Use when testpress exam need to be open in a container as a fragment.
     *
     * <p> Usage example:
     *
     * <p> TestpressSdk.initialize(getActivity(), "baseUrl", "userId", "accessToken", provider,
     * <p>             new TestpressCallback/<TestpressSession>() {
     * <p>             @Override
     * <p>             public void onSuccess(TestpressSession testpressSession) {
     * <p>                 <b>TestpressExam.show(this, R,id.fragment_container, testpressSession);</b>
     * <p>             }
     * <p> });
     *
     * @param activity Activity that has the container
     * @param containerViewId Container view id in which fragment needs to be replace
     * @param testpressSession TestpressSession got from the core module
     */
    public static void show(@NonNull FragmentActivity activity,
                            @NonNull @IdRes Integer containerViewId,
                            @NonNull TestpressSession testpressSession) {
        //noinspection ConstantConditions
        if (activity == null) {
            throw new IllegalArgumentException("Activity must not be null.");
        }
        init(activity, testpressSession);
        CarouselFragment.show(activity, containerViewId);
    }

    /**
     * Use when testpress exam need to be open as a new Activity.
     *
     * <p> Usage example:
     *
     * <p> TestpressSdk.initialize(this, "baseUrl", "userId", "accessToken", provider,
     * <p>             new TestpressCallback/<TestpressSession>() {
     * <p>             @Override
     * <p>             public void onSuccess(TestpressSession testpressSession) {
     * <p>                 <b>TestpressExam.show(this, testpressSession);</b>
     * <p>             }
     * <p> });
     *
     * @param context Context to start the new activity.
     * @param testpressSession TestpressSession got from the core module.
     */
    public static void show(@NonNull Context context, @NonNull TestpressSession testpressSession) {
        //noinspection ConstantConditions
        if (context == null) {
            throw new IllegalArgumentException("Context must not be null.");
        }
        init(context, testpressSession);
        Intent intent = new Intent(context, ExamsListActivity.class);
        context.startActivity(intent);
    }

    /**
     * Use when testpress exam categories need to be open in a container as a fragment.
     *
     * <p> Usage example:
     *
     * <p> TestpressSdk.initialize(getActivity(), "baseUrl", "userId", "accessToken", provider,
     * <p>             new TestpressCallback/<TestpressSession>() {
     * <p>             @Override
     * <p>             public void onSuccess(TestpressSession testpressSession) {
     * <p>                 <b>TestpressExam.showCategories(this, R,id.fragment_container, testpressSession);</b>
     * <p>             }
     * <p> });
     *
     * @param activity Activity that has the container
     * @param containerViewId Container view id in which fragment needs to be replace
     * @param testpressSession TestpressSession got from the core module
     */
    public static void showCategories(@NonNull FragmentActivity activity,
                            @NonNull @IdRes Integer containerViewId,
                            @NonNull TestpressSession testpressSession) {
        //noinspection ConstantConditions
        if (activity == null) {
            throw new IllegalArgumentException("Activity must not be null.");
        }
        init(activity, testpressSession);
        CategoriesGridFragment.show(activity, containerViewId);
    }

    /**
     * Use when testpress exam categories need to be open as a new Activity.
     *
     * <p> Usage example:
     *
     * <p> TestpressSdk.initialize(this, "baseUrl", "userId", "accessToken", provider,
     * <p>             new TestpressCallback/<TestpressSession>() {
     * <p>             @Override
     * <p>             public void onSuccess(TestpressSession testpressSession) {
     * <p>                 <b>TestpressExam.showCategories(this, testpressSession);</b>
     * <p>             }
     * <p> });
     *
     * @param context Context to start the new activity.
     * @param testpressSession TestpressSession got from the core module.
     */
    public static void showCategories(@NonNull Context context,
                                      @NonNull TestpressSession testpressSession) {
        //noinspection ConstantConditions
        if (context == null) {
            throw new IllegalArgumentException("Context must not be null.");
        }
        init(context, testpressSession);
        Intent intent = new Intent(context, CategoryGridActivity.class);
        context.startActivity(intent);
    }

    /**
     * Use to start a particular exam.
     *
     * <p> Usage example:
     *
     * <p> TestpressSdk.initialize(this, "baseUrl", "userId", "accessToken", provider,
     * <p>             new TestpressCallback/<TestpressSession>() {
     * <p>             @Override
     * <p>             public void onSuccess(TestpressSession testpressSession) {
     * <p>                 <b>TestpressExam.startExam(this, "my-slug", testpressSession);</b>
     * <p>             }
     * <p> });
     *
     * @param activity activity from which exam need to start.
     * @param examSlug Slug of the exam which need to be start.
     * @param testpressSession TestpressSession got from the core module.
     */
    @SuppressWarnings("ConstantConditions")
    public static void startExam(@NonNull Activity activity, @NonNull String examSlug,
                                 @NonNull TestpressSession testpressSession) {
        if (activity == null) {
            throw new IllegalArgumentException("Activity must not be null.");
        }
        if (examSlug == null || examSlug.isEmpty()) {
            throw new IllegalArgumentException("PARAM_EXAM_SLUG must not be null or empty.");
        }
        init(activity, testpressSession);
        Intent intent = new Intent(activity, TestActivity.class);
        intent.putExtra(TestActivity.PARAM_EXAM_SLUG, examSlug);
        activity.startActivityForResult(intent, CarouselFragment.TEST_TAKEN_REQUEST_CODE);
    }

    /**
     * Use to start a particular exam in a course.
     *
     * <p> Usage example:
     *
     * <p> TestpressSdk.initialize(this, "baseUrl", "userId", "accessToken", provider,
     * <p>             new TestpressCallback/<TestpressSession>() {
     * <p>             @Override
     * <p>             public void onSuccess(TestpressSession testpressSession) {
     * <p>                 <b>TestpressExam.startCourseExam(this, courseContent, false,
     * <p>                            testpressSession);</b>
     * <p>             }
     * <p> });
     *
     * @param activity activity from which exam need to start.
     * @param courseContent Course content which has the exam need to be start.
     * @param discardExamDetails True to discard the start exam screen which contains exam details,
     *                           False to show.
     * @param testpressSession TestpressSession got from the core module.
     */
    public static void startCourseExam(@NonNull Activity activity,
                                       @NonNull CourseContent courseContent,
                                       boolean discardExamDetails,
                                       @NonNull TestpressSession testpressSession) {

        handleCourseAttempt(activity, courseContent, null, discardExamDetails, testpressSession,
                false);
    }

    /**
     * Use to resume a particular attempt in a course.
     *
     * <p> Usage example:
     *
     * <p> TestpressSdk.initialize(this, "baseUrl", "userId", "accessToken", provider,
     * <p>             new TestpressCallback/<TestpressSession>() {
     * <p>             @Override
     * <p>             public void onSuccess(TestpressSession testpressSession) {
     * <p>                 <b>TestpressExam.resumeCourseAttempt(this, courseContent, courseAttempt,
     * <p>                            false, testpressSession);</b>
     * <p>             }
     * <p> });
     *
     * @param activity activity from which exam need to start.
     * @param courseContent Course content which has the attempt's exam.
     * @param courseAttempt courseAttempt which need to be resume.
     * @param discardExamDetails True to discard the start exam screen which contains exam details,
     *                           False to show.
     * @param testpressSession TestpressSession got from the core module.
     */
    public static void resumeCourseAttempt(@NonNull Activity activity,
                                           @NonNull CourseContent courseContent,
                                           @NonNull CourseAttempt courseAttempt,
                                           boolean discardExamDetails,
                                           @NonNull TestpressSession testpressSession) {

        Assert.assertNotNull("PARAM_COURSE_ATTEMPT must not be null.", courseAttempt);
        handleCourseAttempt(activity, courseContent, courseAttempt, discardExamDetails,
                testpressSession, false);
    }

    /**
     * Use to end a particular attempt in a course.
     *
     * <p> Usage example:
     *
     * <p> TestpressSdk.initialize(this, "baseUrl", "userId", "accessToken", provider,
     * <p>             new TestpressCallback/<TestpressSession>() {
     * <p>             @Override
     * <p>             public void onSuccess(TestpressSession testpressSession) {
     * <p>                 <b>TestpressExam.endCourseAttempt(this, courseContent, courseAttempt,
     * <p>                            testpressSession);</b>
     * <p>             }
     * <p> });
     *
     * @param activity activity from which exam need to start.
     * @param courseContent Course content which has the attempt's exam.
     * @param courseAttempt courseAttempt which need to be resume.
     * @param testpressSession TestpressSession got from the core module.
     */
    @SuppressWarnings("ConstantConditions")
    public static void endCourseAttempt(@NonNull Activity activity,
                                        @NonNull CourseContent courseContent,
                                        @NonNull CourseAttempt courseAttempt,
                                        @NonNull TestpressSession testpressSession) {

        Assert.assertNotNull("PARAM_COURSE_ATTEMPT must not be null.", courseAttempt);
        handleCourseAttempt(activity, courseContent, courseAttempt, true, testpressSession, true);
    }

    private static void handleCourseAttempt(@NonNull Activity activity,
                                            @NonNull CourseContent courseContent,
                                            CourseAttempt courseAttempt,
                                            boolean discardExamDetails,
                                            @NonNull TestpressSession testpressSession,
                                            boolean endExam) {

        Assert.assertNotNull("Activity must not be null.", activity);
        Assert.assertNotNull("PARAM_COURSE_CONTENT must not be null.", courseContent);
        init(activity, testpressSession);
        Intent intent = new Intent(activity, TestActivity.class);
        intent.putExtra(TestActivity.PARAM_COURSE_CONTENT, courseContent);
        if (courseAttempt != null) {
            intent.putExtra(TestActivity.PARAM_COURSE_ATTEMPT, courseAttempt);
        }
        intent.putExtra(TestActivity.PARAM_DISCARD_EXAM_DETAILS, discardExamDetails);
        if (endExam) {
            intent.putExtra(TestActivity.PARAM_ACTION, TestActivity.PARAM_VALUE_ACTION_END);
        }
        activity.startActivityForResult(intent, CarouselFragment.TEST_TAKEN_REQUEST_CODE);
    }

    private static void init(Context context, TestpressSession testpressSession) {
        if (testpressSession == null) {
            throw new IllegalArgumentException("TestpressSession must not be null.");
        }
        TestpressSdk.setTestpressSession(context, testpressSession);
        ImageUtils.initImageLoader(context);
        initDatabase(context, testpressSession.getToken());
    }

    private static void initDatabase(Context context, String sessionToken) {
        daoSession = getDaoSession(context);
        if (TestpressSdk.isNewExamDBSession(context, sessionToken)) {
            DaoMaster.dropAllTables(database, true);
            DaoMaster.createAllTables(database, true);
            TestpressSdk.setTestpressExamDBSession(context, sessionToken);
        }
    }

    private static DaoSession getDaoSession(Context context) {
        if (daoSession == null) {
            DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(
                    context.getApplicationContext(), TestpressSdk.TESTPRESS_SDK_DATABASE);

            database = helper.getWritableDb();
            daoSession = new DaoMaster(database).newSession();
        }
        return daoSession;
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

    public static SelectedAnswerDao getSelectedAnswerDao(Context context) {
        return getDaoSession(context).getSelectedAnswerDao();
    }
}
