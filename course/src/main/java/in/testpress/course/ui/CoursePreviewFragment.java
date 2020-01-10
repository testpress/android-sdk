package in.testpress.course.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;

import in.testpress.core.TestpressException;
import in.testpress.core.TestpressSDKDatabase;
import in.testpress.course.R;
import in.testpress.models.greendao.Course;
import in.testpress.models.greendao.CourseDao;
import in.testpress.store.ui.ProductDetailsActivity;
import in.testpress.ui.BaseListViewFragment;
import in.testpress.util.SingleTypeAdapter;
import in.testpress.util.ThrowableLoader;

import static in.testpress.store.TestpressStore.STORE_REQUEST_CODE;

public class CoursePreviewFragment extends BaseListViewFragment<Course> {
    private CourseDao courseDao;
    private List<Course> courses = new ArrayList<Course>();
    private ArrayList<Integer> course_ids;
    private String productSlug;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        courseDao = TestpressSDKDatabase.getCourseDao(getActivity());
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            course_ids = bundle.getIntegerArrayList("course_ids");
            productSlug = bundle.getString("productSlug");
        }

    }

    @Override
    protected int getErrorMessage(TestpressException exception) {
        return 0;
    }

    @Override
    protected void setEmptyText() {
        setEmptyText(R.string.testpress_no_courses, R.string.testpress_no_courses_description,
                R.drawable.ic_error_outline_black_18dp);
    }

    @Override
    protected SingleTypeAdapter<Course> createAdapter(
            List<Course> items) {
        courses = courseDao.queryBuilder().where(CourseDao.Properties.Id.in(course_ids)).list();
        return new CourseListAdapter(getActivity(), courseDao, courses, productSlug);
    }

    @NonNull
    @Override
    public Loader<List<Course>> onCreateLoader(int id, @Nullable Bundle args) {
        return new CourseLoader(getContext(), courseDao, course_ids);
    }

    private static class CourseLoader extends ThrowableLoader<List<Course>> {
        private CourseDao courseDao;
        private ArrayList<Integer> course_ids;

        CourseLoader(Context context, CourseDao courseDao, ArrayList<Integer> course_ids) {
            super(context, null);
            this.courseDao = courseDao;
            this.course_ids = course_ids;
        }

        @Override
        public List<Course> loadData() throws TestpressException {
            return courseDao.queryBuilder().where(CourseDao.Properties.Id.in(course_ids)).list();
        }
    }

}
