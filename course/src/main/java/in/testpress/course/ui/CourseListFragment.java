package in.testpress.course.ui;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import java.util.List;

import in.testpress.core.TestpressException;
import in.testpress.course.R;
import in.testpress.course.models.Course;
import in.testpress.course.network.CoursePager;
import in.testpress.course.network.TestpressCourseApiClient;
import in.testpress.ui.PagedItemFragment;
import in.testpress.util.SingleTypeAdapter;

public class CourseListFragment extends PagedItemFragment<Course> {

    private TestpressCourseApiClient mApiClient;

    public static void show(FragmentActivity activity, int containerViewId) {
        activity.getSupportFragmentManager().beginTransaction()
                .replace(containerViewId, new CourseListFragment())
                .commit();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mApiClient = new TestpressCourseApiClient(getActivity());
    }

    @Override
    protected CoursePager getPager() {
        if (pager == null) {
            pager = new CoursePager(mApiClient);
        }
        return (CoursePager)pager;
    }

    @Override
    protected SingleTypeAdapter<Course> createAdapter(List<Course> items) {
        return new CourseListAdapter(getActivity(), items, R.layout.testpress_course_list_item);
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
