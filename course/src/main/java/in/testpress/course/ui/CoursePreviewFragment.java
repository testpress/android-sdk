package in.testpress.course.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.content.Loader;

import java.util.ArrayList;
import java.util.List;

import in.testpress.core.TestpressException;
import in.testpress.core.TestpressSDKDatabase;
import in.testpress.course.R;
import in.testpress.course.util.ProductUtils;
import in.testpress.models.greendao.Course;
import in.testpress.models.greendao.CourseDao;
import in.testpress.models.greendao.ProductDao;
import in.testpress.store.ui.ProductDetailsActivity;
import in.testpress.ui.BaseListViewFragment;
import in.testpress.util.SingleTypeAdapter;
import in.testpress.util.ThrowableLoader;

import static in.testpress.course.TestpressCourse.COURSE_IDS;
import static in.testpress.course.TestpressCourse.PRODUCT_SLUG;

public class CoursePreviewFragment extends BaseListViewFragment<Course> {
    private CourseDao courseDao;
    private List<Course> courses = new ArrayList<Course>();
    private ArrayList<Integer> courseIds;
    private String productSlug;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        courseDao = TestpressSDKDatabase.getCourseDao(getActivity());
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            courseIds = bundle.getIntegerArrayList(COURSE_IDS);
            productSlug = bundle.getString(PRODUCT_SLUG);
        }

    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.course_preview_layout, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        displayBuyNowButton();
    }

    private void displayBuyNowButton() {
        Button buyButton = requireView().findViewById(R.id.buy_button);
        buyButton.setVisibility(View.VISIBLE);

        if (ProductUtils.getPriceForProduct(productSlug, requireContext()) > 0.0) {
            buyButton.setText(R.string.buy_now);
        } else {
            buyButton.setText(R.string.get_it_for_free);
        }

        buyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(requireContext(), ProductDetailsActivity.class);
                intent.putExtra(ProductDetailsActivity.PRODUCT_SLUG, productSlug);
                requireActivity().startActivity(intent);
            }
        });
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
        courses = courseDao.queryBuilder().where(CourseDao.Properties.Id.in(courseIds)).list();
        return new CourseListAdapter(getActivity(), courseDao, courses, productSlug);
    }

    @NonNull
    @Override
    public Loader<List<Course>> onCreateLoader(int id, @Nullable Bundle args) {
        return new CourseLoader(getContext(), courseDao, courseIds);
    }

    private static class CourseLoader extends ThrowableLoader<List<Course>> {
        private CourseDao courseDao;
        private ArrayList<Integer> courseIds;

        CourseLoader(Context context, CourseDao courseDao, ArrayList<Integer> courseIds) {
            super(context, null);
            this.courseDao = courseDao;
            this.courseIds = courseIds;
        }

        @Override
        public List<Course> loadData() throws TestpressException {
            return courseDao.queryBuilder().where(CourseDao.Properties.Id.in(courseIds)).list();
        }
    }

}
