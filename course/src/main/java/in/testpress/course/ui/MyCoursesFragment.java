package in.testpress.course.ui;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.Loader;
import android.util.Log;

import org.greenrobot.greendao.AbstractDao;

import java.util.ArrayList;
import java.util.List;

import in.testpress.core.TestpressException;
import in.testpress.core.TestpressSDKDatabase;
import in.testpress.course.R;
import in.testpress.course.TestpressCourse;
import in.testpress.course.enums.CourseType;
import in.testpress.course.util.ManageCourseStates;
import in.testpress.models.greendao.ChapterDao;
import in.testpress.models.greendao.Course;
import in.testpress.models.greendao.CourseDao;
import in.testpress.course.network.CoursePager;
import in.testpress.course.network.TestpressCourseApiClient;
import in.testpress.ui.BaseDataBaseFragment;
import in.testpress.util.SingleTypeAdapter;

public class MyCoursesFragment extends BaseDataBaseFragment<Course, Long> {

    private TestpressCourseApiClient mApiClient;
    private CourseDao courseDao;

    public static void show(FragmentActivity activity, int containerViewId) {
        activity.getSupportFragmentManager().beginTransaction()
                .replace(containerViewId, new MyCoursesFragment())
                .commitAllowingStateLoss();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mApiClient = new TestpressCourseApiClient(getActivity());
        courseDao = TestpressSDKDatabase.getCourseDao(getActivity());
    }

    @Override
    protected CoursePager getPager() {
        if (pager == null) {
            pager = new CoursePager(mApiClient);
        }
        return (CoursePager)pager;
    }

    @Override
    protected AbstractDao<Course, Long> getDao() {
        return courseDao;
    }

    @Override
    protected SingleTypeAdapter<Course> createAdapter(List<Course> items) {
        List<Course> courses = new ArrayList<>();
        return new CourseListAdapter(getActivity(), courseDao, courses, null);
    }

    @Override
    public void onLoadFinished(Loader<List<Course>> loader, List<Course> courses) {
        final TestpressException exception = getException(loader);
        if (exception != null) {
            this.exception = exception;
            int errorMessage = getErrorMessage(exception);
            if (!isItemsEmpty()) {
                showError(errorMessage);
            }
            showList();
            getLoaderManager().destroyLoader(loader.getId());
            return;
        }

        this.exception = null;
        this.items = courses;
        unassignLocalCourses();
        storeCourses(courses);
        displayDataFromDB();
        showList();
    }

    private void unassignLocalCourses() {
        List<Course> coursesFromDB = courseDao.queryBuilder().where(CourseDao.Properties.IsMyCourse.eq(true)).list();
        for (Course course: coursesFromDB) {
            course.setIsMyCourse(false);
        }
        courseDao.insertOrReplaceInTx(coursesFromDB);
    }

    private void storeCourses(List<Course> courses) {
        for (Course course: courses) {
            course.setIsMyCourse(true);
        }
        courseDao.insertOrReplaceInTx(courses);
    }

    @Override
    protected int getErrorMessage(TestpressException exception) {
        if (exception.isUnauthenticated()) {
            setEmptyText(R.string.testpress_authentication_failed, R.string.testpress_please_login,
                    R.drawable.ic_error_outline_black_18dp);
            return R.string.testpress_authentication_failed;
        } else if (exception.isNetworkError()) {
            setEmptyText(R.string.testpress_network_error, R.string.testpress_no_internet_try_again,
                    R.drawable.ic_error_outline_black_18dp);
            return R.string.testpress_no_internet_try_again;
        } else {
            setEmptyText(R.string.testpress_error_loading_courses,
                    R.string.testpress_some_thing_went_wrong_try_again,
                    R.drawable.ic_error_outline_black_18dp);
        }
        return R.string.testpress_some_thing_went_wrong_try_again;
    }

    @Override
    protected void setEmptyText() {
        setEmptyText(R.string.testpress_no_courses, R.string.testpress_no_courses_description,
                    R.drawable.ic_error_outline_black_18dp);
    }

}
