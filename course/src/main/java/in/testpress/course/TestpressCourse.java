package in.testpress.course;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;

import in.testpress.core.TestpressSdk;
import in.testpress.core.TestpressSession;
import in.testpress.course.ui.ChapterDetailActivity;
import in.testpress.course.ui.ContentActivity;
import in.testpress.course.ui.CourseListActivity;
import in.testpress.course.ui.MyCoursesFragment;
import in.testpress.course.ui.LeaderboardActivity;
import in.testpress.course.ui.LeaderboardFragment;
import in.testpress.util.Assert;
import in.testpress.util.ImageUtils;

import static in.testpress.core.TestpressSdk.COURSE_CHAPTER_REQUEST_CODE;
import static in.testpress.core.TestpressSdk.COURSE_CONTENT_DETAIL_REQUEST_CODE;
import static in.testpress.core.TestpressSdk.COURSE_CONTENT_LIST_REQUEST_CODE;

public class TestpressCourse {

    public static final String COURSE_ID = "courseId";
    public static final String PARENT_ID = "parentId";
    public static final String CHAPTER_URL = "chapterUrl";
    public static final String PRODUCT_SLUG = "productSlug";
    public static final String COURSE_IDS = "courseIds";

    /**
     * Use when testpress courses need to be open in a container as a fragment.
     *
     * <p> Usage example:
     *
     * <p> TestpressSdk.initialize(getActivity(), instituteSettings, "userId", "accessToken", provider,
     * <p>             new TestpressCallback/<TestpressSession>() {
     * <p>             @Override
     * <p>             public void onSuccess(TestpressSession testpressSession) {
     * <p>                 <b>TestpressCourse.show(this, R,id.fragment_container, testpressSession);</b>
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

        Assert.assertNotNull("Activity must not be null.", activity);

        init(activity.getApplicationContext(), testpressSession);
        MyCoursesFragment.show(activity, containerViewId);
    }

    /**
     * Use when testpress courses need to be open as a new Activity.
     *
     * <p> Usage example:
     *
     * <p> TestpressSdk.initialize(this, instituteSettings, "userId", "accessToken", provider,
     * <p>             new TestpressCallback/<TestpressSession>() {
     * <p>             @Override
     * <p>             public void onSuccess(TestpressSession testpressSession) {
     * <p>                 <b>TestpressCourse.show(this, testpressSession);</b>
     * <p>             }
     * <p> });
     *
     * @param context Context to start the new activity.
     * @param testpressSession TestpressSession got from the core module.
     */
    public static void show(@NonNull Context context, @NonNull TestpressSession testpressSession) {
        Assert.assertNotNull("Context must not be null.", context);

        init(context.getApplicationContext(), testpressSession);
        Intent intent = new Intent(context, CourseListActivity.class);
        context.startActivity(intent);
    }

    /**
     * Get instance of Course list fragment.
     *
     * @param context Context
     * @param testpressSession TestpressSession got from the core module
     */
    public static MyCoursesFragment getCoursesListFragment(@NonNull Context context,
                                                           @NonNull TestpressSession testpressSession) {

        Assert.assertNotNull("Context must not be null.", context);

        init(context.getApplicationContext(), testpressSession);
        return new MyCoursesFragment();
    }

    /**
     * Show chapters of a specific course as new Activity.
     *
     * @param activity Activity from which chapters needs to show.
     * @param courseName Course name to be displayed in the action bar of new activity.
     * @param courseId Id of the Course which chapters need to be display.
     * @param testpressSession TestpressSession got from the core module.
     */
    public static void showChapters(@NonNull Activity activity,
                                    String courseName,
                                    @NonNull Integer courseId,
                                    @NonNull TestpressSession testpressSession) {

        Assert.assertNotNull("Activity must not be null.", activity);
        Assert.assertNotNull("courseId must not be null.", courseId);

        init(activity.getApplicationContext(), testpressSession);
        activity.startActivityForResult(
                ChapterDetailActivity.createIntent(courseName, courseId.toString(), activity, null),
                COURSE_CHAPTER_REQUEST_CODE
        );
    }

    /**
     * Load chapter & display child chapters or contents of the chapter in new Activity.
     *
     * @param activity activity from which child list needs to show.
     * @param chapterUrl Url of the chapter which children needs to show.
     * @param testpressSession TestpressSession got from the core module.
     */
    public static void showChapterContents(@NonNull Activity activity,
                                           @NonNull String chapterUrl,
                                           @NonNull TestpressSession testpressSession) {

        Assert.assertNotNull("Activity must not be null.", activity);
        Assert.assertNotNullAndNotEmpty("chapterUrl must not be null or empty.", chapterUrl);

        init(activity.getApplicationContext(), testpressSession);
        activity.startActivityForResult(
                ChapterDetailActivity.createIntent(chapterUrl, activity, null),
                COURSE_CONTENT_LIST_REQUEST_CODE
        );
    }

    /**
     * Load content from given content id & show in new Activity.
     *
     * @param activity activity from which content detail need to show.
     * @param contentId id of the content.
     * @param testpressSession TestpressSession got from the core module.
     */
    public static void showContentDetail(@NonNull Activity activity,
                                         @NonNull String contentId,
                                         @NonNull TestpressSession testpressSession) {

        Assert.assertNotNull("Activity must not be null.", activity);
        Assert.assertNotNullAndNotEmpty("contentId must not be null or empty.", contentId);

        init(activity.getApplicationContext(), testpressSession);
        activity.startActivityForResult(
                ContentActivity.createIntent(contentId, activity),
                COURSE_CONTENT_DETAIL_REQUEST_CODE
        );
    }

    /**
     * Use when overall leaderboard need to be open in a container as a fragment.
     *
     * Usage example:
     *
     * TestpressCourse.showLeaderboard(this, R,id.fragment_container, testpressSession);
     *
     * @param activity Activity that has the container
     * @param containerViewId Container view id in which fragment needs to be replace
     * @param testpressSession TestpressSession got from the core module
     */
    public static void showLeaderboard(@NonNull FragmentActivity activity,
                                       @NonNull @IdRes Integer containerViewId,
                                       @NonNull TestpressSession testpressSession) {

        Assert.assertNotNull("Activity must not be null.", activity);

        init(activity.getApplicationContext(), testpressSession);
        LeaderboardFragment.show(activity, containerViewId);
    }

    /**
     * Use when overall leaderboard need to be open as a new Activity.
     *
     * @param context Context to start the new activity.
     * @param testpressSession TestpressSession got from the core module.
     */
    public static void showLeaderboard(@NonNull Context context,
                                       @NonNull TestpressSession testpressSession) {

        Assert.assertNotNull("Context must not be null.", context);

        init(context.getApplicationContext(), testpressSession);
        Intent intent = new Intent(context, LeaderboardActivity.class);
        context.startActivity(intent);
    }

    /**
     * Get instance of Leaderboard fragment.
     *
     * @param context Context
     * @param testpressSession TestpressSession got from the core module
     */
    public static LeaderboardFragment getLeaderboardFragment(@NonNull Context context,
                                                             @NonNull TestpressSession testpressSession) {

        Assert.assertNotNull("Context must not be null.", context);

        init(context.getApplicationContext(), testpressSession);
        return new LeaderboardFragment();
    }

    private static void init(Context applicationContext, TestpressSession testpressSession) {
        Assert.assertNotNull("TestpressSession must not be null.", testpressSession);

        TestpressSdk.setTestpressSession(applicationContext, testpressSession);
        ImageUtils.initImageLoader(applicationContext);
    }

}
