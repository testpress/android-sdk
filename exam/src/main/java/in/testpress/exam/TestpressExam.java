package in.testpress.exam;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;

import in.testpress.core.TestpressSdk;
import in.testpress.core.TestpressSession;
import in.testpress.exam.ui.AccessCodeActivity;
import in.testpress.exam.ui.AccessCodeFragment;
import in.testpress.exam.ui.AnalyticsActivity;
import in.testpress.exam.ui.AttemptsActivity;
import in.testpress.exam.ui.BookmarksActivity;
import in.testpress.exam.ui.CarouselFragment;
import in.testpress.exam.ui.CategoriesGridFragment;
import in.testpress.exam.ui.CategoryGridActivity;
import in.testpress.exam.ui.ExamsListActivity;
import in.testpress.exam.ui.ReviewStatsActivity;
import in.testpress.exam.ui.TestActivity;
import in.testpress.models.greendao.Attempt;
import in.testpress.models.greendao.Content;
import in.testpress.models.greendao.CourseAttempt;
import in.testpress.models.greendao.Exam;
import in.testpress.util.Assert;
import in.testpress.util.ImageUtils;

import static in.testpress.exam.ui.CategoryGridActivity.SHOW_EXAMS_AS_DEFAULT;

public class TestpressExam {

    public static final String PARAM_EXAM_SLUG = "examSlug";

    /**
     * Use when testpress exams need to be open in a container as a fragment.
     *
     * <p> Usage example:
     *
     * <p> TestpressSdk.initialize(getActivity(), instituteSettings, "userId", "accessToken", provider,
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
    public static void show(@NonNull FragmentActivity activity, @IdRes int containerViewId,
                            @NonNull TestpressSession testpressSession) {
        //noinspection ConstantConditions
        if (activity == null) {
            throw new IllegalArgumentException("Activity must not be null.");
        }
        init(activity, testpressSession);
        CarouselFragment.show(activity, containerViewId);
    }

    /**
     * Use when testpress exams need to be open as a new Activity.
     *
     * <p> Usage example:
     *
     * <p> TestpressSdk.initialize(this, instituteSettings, "userId", "accessToken", provider,
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
     * <p> TestpressSdk.initialize(getActivity(), instituteSettings, "userId", "accessToken", provider,
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
    public static void showCategories(@NonNull FragmentActivity activity, @IdRes int containerViewId,
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
     * <p> TestpressSdk.initialize(this, instituteSettings", "userId", "accessToken", provider,
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
    public static void showCategories(@NonNull Context context, boolean showExamsAsDefault,
                                      @NonNull TestpressSession testpressSession) {
        //noinspection ConstantConditions
        if (context == null) {
            throw new IllegalArgumentException("Context must not be null.");
        }
        init(context, testpressSession);
        Intent intent = new Intent(context, CategoryGridActivity.class);
        intent.putExtra(SHOW_EXAMS_AS_DEFAULT, showExamsAsDefault);
        context.startActivity(intent);
    }

    /**
     * Use to start a particular exam.
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
     * Display the attempt report.
     *
     * @param activity activity to which result of review activity need to pass.
     * @param exam Exam object of the attempt.
     * @param attempt Attempt object which report need to be shown.
     * @param testpressSession TestpressSession got from the core module.
     */
    public static void showAttemptReport(@NonNull Activity activity,
                                         @NonNull Exam exam,
                                         @NonNull Attempt attempt,
                                         @NonNull TestpressSession testpressSession) {

        Assert.assertNotNull("Activity must not be null.", activity);
        Assert.assertNotNull("Exam must not be null.", exam);
        Assert.assertNotNull("Attempt must not be null.", attempt);
        init(activity, testpressSession);
        activity.startActivityForResult(ReviewStatsActivity.createIntent(activity, exam, attempt),
                CarouselFragment.TEST_TAKEN_REQUEST_CODE);
    }

    /**
     * Show the exam based on its attempt(s) state.
     *
     * @param activity activity to which result of attempts activity need to pass.
     * @param examSlug Slug of the exam which need to be show.
     * @param testpressSession TestpressSession got from the core module.
     */
    @SuppressWarnings("ConstantConditions")
    public static void showExamAttemptedState(@NonNull Activity activity, @NonNull String examSlug,
                                              @NonNull TestpressSession testpressSession) {

        Assert.assertNotNull("Activity must not be null.", activity);
        if (examSlug == null || examSlug.isEmpty()) {
            throw new IllegalArgumentException("EXAM_SLUG must not be null or empty.");
        }
        init(activity, testpressSession);
        Intent intent = new Intent(activity, AttemptsActivity.class);
        intent.putExtra(PARAM_EXAM_SLUG, examSlug);
        activity.startActivityForResult(intent, CarouselFragment.TEST_TAKEN_REQUEST_CODE);
    }

    /**
     * Get access code from user & display exams which linked to it.
     *
     * @param context Context to start the new activity.
     * @param testpressSession TestpressSession got from the core module.
     */
    @SuppressWarnings("ConstantConditions")
    public static void showExamsForAccessCode(@NonNull Context context,
                                              @NonNull TestpressSession testpressSession) {
        if (context == null) {
            throw new IllegalArgumentException("Activity must not be null.");
        }
        init(context, testpressSession);
        Intent intent = new Intent(context, AccessCodeActivity.class);
        context.startActivity(intent);
    }

    /**
     * Get instance of AccessCodeFragment
     * Use to get access code from user & display exams which linked to it.
     *
     * @param context Context
     * @param testpressSession TestpressSession got from the core module.
     */
    @SuppressWarnings("ConstantConditions")
    public static AccessCodeFragment getAccessCodeFragment(@NonNull Context context,
                                                           @NonNull TestpressSession testpressSession) {

        Assert.assertNotNull("Activity must not be null.", context);
        init(context.getApplicationContext(), testpressSession);
        return new AccessCodeFragment();
    }

    /**
     * Display the subject wise analytics.
     *
     * @param activity activity to which result of analytics activity need to pass.
     * @param analyticsUrlFrag Analytics url fragment.
     * @param testpressSession TestpressSession got from the core module.
     */
    public static void showAnalytics(@NonNull Activity activity,
                                     @NonNull String analyticsUrlFrag,
                                     @NonNull TestpressSession testpressSession) {

        Assert.assertNotNull("Activity must not be null.", activity);
        //noinspection ConstantConditions
        if (analyticsUrlFrag == null || analyticsUrlFrag.trim().isEmpty()) {
            throw new IllegalArgumentException("analyticsUrl must not be null or empty.");
        }
        init(activity, testpressSession);
        activity.startActivity(AnalyticsActivity.createIntent(activity, analyticsUrlFrag, null, null));
    }

    /**
     * Use to start a particular exam in a course.
     *
     * @param activity activity from which exam need to start.
     * @param courseContent Course content which has the exam need to be start.
     * @param discardExamDetails True to discard the start exam screen which contains exam details,
     *                           False to show.
     * @param testpressSession TestpressSession got from the core module.
     */
    public static void startCourseExam(@NonNull Activity activity,
                                       @NonNull Content courseContent,
                                       boolean discardExamDetails,
                                       boolean isPartialQuestions,
                                       @NonNull TestpressSession testpressSession) {

        handleCourseAttempt(activity, courseContent, null, discardExamDetails, isPartialQuestions,
                testpressSession, false);
    }

    /**
     * Use to resume a particular attempt in a course.
     *
     * @param activity activity to which result of test activity need to pass.
     * @param courseContent Course content which has the attempt's exam.
     * @param courseAttempt courseAttempt which need to be resume.
     * @param discardExamDetails True to discard the start exam screen which contains exam details,
     *                           False to show.
     * @param testpressSession TestpressSession got from the core module.
     */
    public static void resumeCourseAttempt(@NonNull Activity activity,
                                           @NonNull Content courseContent,
                                           @NonNull CourseAttempt courseAttempt,
                                           boolean discardExamDetails,
                                           @NonNull TestpressSession testpressSession) {

        Assert.assertNotNull("PARAM_COURSE_ATTEMPT must not be null.", courseAttempt);
        handleCourseAttempt(activity, courseContent, courseAttempt, discardExamDetails, false,
                testpressSession, false);
    }

    /**
     * Use to end a particular attempt in a course.
     *
     * @param activity activity to which result of test activity need to pass.
     * @param courseContent Course content which has the attempt's exam.
     * @param courseAttempt courseAttempt which need to be resume.
     * @param testpressSession TestpressSession got from the core module.
     */
    @SuppressWarnings("ConstantConditions")
    public static void endCourseAttempt(@NonNull Activity activity,
                                        @NonNull Content courseContent,
                                        @NonNull CourseAttempt courseAttempt,
                                        @NonNull TestpressSession testpressSession) {

        Assert.assertNotNull("PARAM_COURSE_ATTEMPT must not be null.", courseAttempt);
        handleCourseAttempt(activity, courseContent, courseAttempt, true, false, testpressSession,
                true);
    }

    /**
     * Display the content attempt report.
     *
     * @param activity activity to which result of review activity need to pass.
     * @param exam Exam object of the attempt.
     * @param courseAttempt Attempt object which report need to be shown.
     * @param testpressSession TestpressSession got from the core module.
     */
    public static void showCourseAttemptReport(@NonNull Activity activity,
                                               @NonNull Exam exam,
                                               @NonNull CourseAttempt courseAttempt,
                                               @NonNull TestpressSession testpressSession) {

        Assert.assertNotNull("Activity must not be null.", activity);
        Assert.assertNotNull("Exam must not be null.", exam);
        Assert.assertNotNull("CourseAttempt must not be null.", courseAttempt);
        init(activity, testpressSession);
        activity.startActivityForResult(
                ReviewStatsActivity.createIntent(activity, exam, courseAttempt),
                CarouselFragment.TEST_TAKEN_REQUEST_CODE
        );
    }

    /**
     * Use when bookmarked items need to be open as a new Activity.
     *
     * @param context Context to start the new activity.
     * @param testpressSession TestpressSession got from the core module.
     */
    public static void showBookmarks(@NonNull Context context,
                                     @NonNull TestpressSession testpressSession) {

        //noinspection ConstantConditions
        if (context == null) {
            throw new IllegalArgumentException("Context must not be null.");
        }
        init(context, testpressSession);
        Intent intent = new Intent(context, BookmarksActivity.class);
        context.startActivity(intent);
    }

    private static void handleCourseAttempt(@NonNull Activity activity,
                                            @NonNull Content courseContent,
                                            CourseAttempt courseAttempt,
                                            boolean discardExamDetails,
                                            boolean isPartialQuestions,
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
        intent.putExtra(TestActivity.PARAM_IS_PARTIAL_QUESTIONS, isPartialQuestions);
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
    }

}
