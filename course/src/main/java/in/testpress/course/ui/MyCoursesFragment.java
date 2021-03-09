package in.testpress.course.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.fragment.app.FragmentActivity;
import androidx.loader.content.Loader;

import org.greenrobot.greendao.AbstractDao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import in.testpress.core.TestpressException;
import in.testpress.core.TestpressSDKDatabase;
import in.testpress.course.R;
import in.testpress.course.helpers.CourseLastSyncedDate;
import in.testpress.models.greendao.Course;
import in.testpress.models.greendao.CourseDao;
import in.testpress.course.pagers.CoursePager;
import in.testpress.course.api.TestpressCourseApiClient;
import in.testpress.network.TestpressApiClient;
import in.testpress.ui.BaseDataBaseFragment;
import in.testpress.util.SingleTypeAdapter;

public class MyCoursesFragment extends BaseDataBaseFragment<Course, Long> {

    private TestpressCourseApiClient mApiClient;
    private CourseDao courseDao;
    private ArrayList<String> tags = new ArrayList<String>();

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
        parseArguments();
    }

    private void parseArguments() {
        if (getArguments() != null) {
            tags = getArguments().getStringArrayList(TestpressApiClient.TAGS);
        }
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
        return new CourseListAdapter(getActivity(), getCourses(), null);
    }

    private List<Course> getCourses() {
        List<Course> courses = courseDao.queryBuilder()
                .where(CourseDao.Properties.IsMyCourse.eq(true))
                .orderAsc(CourseDao.Properties.Order)
                .list();
        return filteredCourses(courses);
    }

    public List<Course> filteredCourses(List<Course> courses) {
        if (tags == null || tags.isEmpty()) {
            return courses;
        }

        ArrayList<Course> filteredCourses = new ArrayList<Course>();
        for (Course course : courses) {
            if (course.getTags() != null && !Collections.disjoint(course.getTags(), tags)) {
                filteredCourses.add(course);
            }
        }
        return filteredCourses;
    }

    @Override
    protected boolean isItemsEmpty() {
        return getDao().queryBuilder().where(CourseDao.Properties.IsMyCourse.eq(true)).count() == 0;
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
            refreshLastSyncedDate();
            getLoaderManager().destroyLoader(loader.getId());
            return;
        }

        this.exception = null;
        unassignLocalCourses();
        storeCourses(courses);
        updateItems(getCourses());
        if (getCourses().size() == 1) {
            openCourseDetail(getCourses().get(0));
        }
        showList();
    }

    private void openCourseDetail(Course course) {
        Intent intent = ChapterDetailActivity.createIntent(
                course.getTitle(),
                course.getId().toString(), getContext(), null);
        startActivity(intent);
        getActivity().finish();
    }

    private void refreshLastSyncedDate() {
        CourseLastSyncedDate courseLastSyncedDate = new CourseLastSyncedDate(requireContext());
        courseLastSyncedDate.refresh();
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
