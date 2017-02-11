package in.testpress.course;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;

import junit.framework.Assert;

import org.greenrobot.greendao.database.Database;

import in.testpress.core.TestpressSdk;
import in.testpress.core.TestpressSession;
import in.testpress.course.models.greendao.ChapterDao;
import in.testpress.course.models.greendao.CourseDao;
import in.testpress.course.models.greendao.DaoMaster;
import in.testpress.course.models.greendao.DaoSession;
import in.testpress.course.ui.CourseListActivity;
import in.testpress.course.ui.CourseListFragment;
import in.testpress.util.ImageUtils;

public class TestpressCourse {

    private static DaoSession daoSession;
    private static Database database;

    /**
     * Use when testpress courses need to be open in a container as a fragment.
     *
     * <p> Usage example:
     *
     * <p> TestpressSdk.initialize(getActivity(), "baseUrl", "userId", "accessToken", provider,
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
     * <p> TestpressSdk.initialize(this, "baseUrl", "userId", "accessToken", provider,
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

    private static void init(Context applicationContext, TestpressSession testpressSession) {
        Assert.assertNotNull("TestpressSession must not be null.", testpressSession);

        TestpressSdk.setTestpressSession(applicationContext, testpressSession);
        ImageUtils.initImageLoader(applicationContext);
        initDatabase(applicationContext, testpressSession.getToken());
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
