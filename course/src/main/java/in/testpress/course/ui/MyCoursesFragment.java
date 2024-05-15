package in.testpress.course.ui;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.loader.content.Loader;

import org.greenrobot.greendao.AbstractDao;

import java.util.ArrayList;
import java.util.List;

import in.testpress.core.TestpressException;
import in.testpress.core.TestpressSDKDatabase;
import in.testpress.core.TestpressSdk;
import in.testpress.course.R;
import in.testpress.course.helpers.CourseLastSyncedDate;
import in.testpress.models.InstituteSettings;
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
    private InstituteSettings instituteSettings;

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
        instituteSettings = TestpressSdk.getTestpressSession(requireContext()).getInstituteSettings();
        initializeTags();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(instituteSettings.getEnableCustomTest());
    }

    private void initializeTags() {
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
        return Course.filterByTags(courses, tags);
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
        showList();
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

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.custom_test_generation, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.custom_test_icon) {
            openCustomTestGenerationActivity();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void openCustomTestGenerationActivity() {
        startActivity(
                CustomTestGenerationActivity.Companion.createIntent(
                        requireContext(),
                        "Custom Module",
                        instituteSettings.getBaseUrl()+"/courses/custom_test_generation/?"+constrictQueryParamForAvailableCourses()+"%26testpress_app=android",
                        true,
                        CustomTestGenerationActivity.class
                )
        );
    }

    private String constrictQueryParamForAvailableCourses(){
        StringBuilder queryParam = new StringBuilder();
        for (Course course : getCourses()) {
            queryParam.append("course_id=").append(course.getId()).append("%26");
        }
        return queryParam.toString();
    }

}
