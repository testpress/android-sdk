package in.testpress.course.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

import in.testpress.core.TestpressException;
import in.testpress.core.TestpressSDKDatabase;
import in.testpress.course.R;
import in.testpress.course.network.CoursePager;
import in.testpress.models.greendao.Course;
import in.testpress.models.greendao.CourseDao;
import in.testpress.models.greendao.Product;
import in.testpress.store.network.ProductsPager;
import in.testpress.store.ui.ProductDetailsActivity;
import in.testpress.ui.BaseListViewFragment;
import in.testpress.ui.HeaderFooterListAdapter;
import in.testpress.util.SingleTypeAdapter;
import in.testpress.util.ThrowableLoader;

import static in.testpress.store.TestpressStore.STORE_REQUEST_CODE;

public class CoursePreviewFragment extends BaseListViewFragment<Course> {
    private CourseDao courseDao;
    private List<Course> courses;
    private ArrayList<Integer> course_ids;
    private String product_slug;
    private Button buyNowButton;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        courseDao = TestpressSDKDatabase.getCourseDao(getActivity());
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            course_ids = bundle.getIntegerArrayList("course_ids");
            product_slug = bundle.getString("product_slug");
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
    public void onViewCreated(View view,
                              Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RelativeLayout ll_Main  = new RelativeLayout(getActivity());
        buyNowButton = new Button(getActivity());
        buyNowButton.setText("Buy Now");
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        rlp.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 0);
        rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP,0);
        rlp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        ll_Main.setLayoutParams(rlp);
        buyNowButton.setLayoutParams(lp);
        buyNowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), ProductDetailsActivity.class);
                intent.putExtra(ProductDetailsActivity.PRODUCT_SLUG, product_slug);
                getActivity().startActivityForResult(intent, STORE_REQUEST_CODE);
            }
        });
        ll_Main.addView(buyNowButton);
        buyNowButton.setVisibility(View.INVISIBLE);
        listView.addFooterView(ll_Main);
    }

    @Override
    protected SingleTypeAdapter<Course> createAdapter(
            List<Course> items) {
        courses = courseDao.queryBuilder().where(CourseDao.Properties.Id.in(course_ids)).list();
        return new CourseListAdapter(getActivity(), courseDao, courses, product_slug);
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

    @Override
    public void onLoadFinished(Loader<List<Course>> loader,
                               List<Course> items) {
        super.onLoadFinished(loader, items);
        listView.addFooterView(new View(getContext()));
        buyNowButton.setVisibility(View.VISIBLE);
    }
}
