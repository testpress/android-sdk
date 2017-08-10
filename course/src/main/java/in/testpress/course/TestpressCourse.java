package in.testpress.course;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;

import org.greenrobot.greendao.database.Database;

import in.testpress.core.TestpressSdk;
import in.testpress.core.TestpressSession;
import in.testpress.course.models.greendao.ChapterDao;
import in.testpress.course.models.greendao.CourseDao;
import in.testpress.course.models.greendao.DaoMaster;
import in.testpress.course.models.greendao.DaoSession;
import in.testpress.course.ui.ChaptersGridActivity;
import in.testpress.course.ui.ContentsListActivity;
import in.testpress.course.ui.CourseListActivity;
import in.testpress.course.ui.CourseListFragment;
import in.testpress.course.ui.LeaderboardActivity;
import in.testpress.course.ui.LeaderboardFragment;
import in.testpress.util.Assert;
import in.testpress.util.ImageUtils;

public class TestpressCourse {

    private static DaoSession daoSession;
    private static Database database;

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
        CourseListFragment.show(activity, containerViewId);
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
    public static CourseListFragment getCoursesListFragment(@NonNull Context context,
                                                            @NonNull TestpressSession testpressSession) {

        Assert.assertNotNull("Context must not be null.", context);

        init(context.getApplicationContext(), testpressSession);
        return new CourseListFragment();
    }

    /**
     * Show chapters of a specific course as new Activity.
     *
     * @param context Context to start the new activity.
     * @param courseName Course name to be displayed in the action bar of new activity.
     * @param courseId Id of the Course which chapters need to be display.
     * @param testpressSession TestpressSession got from the core module.
     */
    public static void showChapters(@NonNull Context context,
                                    @NonNull String courseName,
                                    @NonNull Integer courseId,
                                    @NonNull TestpressSession testpressSession) {

        Assert.assertNotNull("Context must not be null.", context);
        Assert.assertNotNullAndNotEmpty("courseName must not be null or empty.", courseName);
        Assert.assertNotNull("courseId must not be null.", courseId);

        init(context.getApplicationContext(), testpressSession);
        context.startActivity(
                ChaptersGridActivity.createIntent(courseName, courseId.toString(), null, context));
    }

    /**
     * Load contents from given url & show in new Activity.
     *
     * @param context Context to start the new activity.
     * @param title Text to be displayed in the action bar of new activity.
     * @param contentsUrl Url from which contents can be load.
     * @param testpressSession TestpressSession got from the core module.
     */
    public static void showContents(@NonNull Context context,
                                    @NonNull String title,
                                    @NonNull String contentsUrl,
                                    @NonNull TestpressSession testpressSession) {

        Assert.assertNotNull("Context must not be null.", context);
        Assert.assertNotNullAndNotEmpty("title must not be null or empty.", title);
        Assert.assertNotNullAndNotEmpty("contentsUrl must not be null or empty.", contentsUrl);

        init(context.getApplicationContext(), testpressSession);
        context.startActivity(ContentsListActivity.createIntent(title, contentsUrl, context));
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
        initDatabase(applicationContext, testpressSession.getToken());
    }

    private static DaoSession getDaoSession(Context context) {
        if (daoSession == null) {
            DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(
                    context.getApplicationContext(), TestpressSdk.TESTPRESS_COURSE_SDK_DATABASE);

            database = helper.getWritableDb();
            daoSession = new DaoMaster(database).newSession();
        }
        return daoSession;
    }

    private static void initDatabase(Context context, String sessionToken) {
        daoSession = getDaoSession(context);
        if (TestpressSdk.isNewCourseDBSession(context, sessionToken)) {
            DaoMaster.dropAllTables(database, true);
            DaoMaster.createAllTables(database, true);
            TestpressSdk.setTestpressCourseDBSession(context, sessionToken);
        }
    }

    public static CourseDao getCourseDao(Context context) {
        return getDaoSession(context).getCourseDao();
    }

    public static ChapterDao getChapterDao(Context context) {
        return getDaoSession(context).getChapterDao();
    }

}
