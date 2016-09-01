package in.testpress.exam.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import in.testpress.exam.R;
import in.testpress.exam.models.ExamCourse;
import in.testpress.exam.network.TestpressExamApiClient;
import in.testpress.util.CircularProgressDrawable;
import in.testpress.util.SafeAsyncTask;

public class CarouselFragment extends Fragment {

    public static final int TEST_TAKEN_REQUEST_CODE = 9999;
    public static final String CURRENT_ITEM = "currentItem";
    private ExamPagerAdapter adapter;
    private ViewPager viewPager;
    private ProgressBar progressBar;
    private View tabContainer;
    private View emptyView;
    private TextView emptyTitleView;
    private TextView emptyDescView;
    private TabLayout tabLayout;

    public static void show(FragmentActivity activity, int containerViewId) {
        activity.getSupportFragmentManager().beginTransaction()
                .replace(containerViewId, new CarouselFragment())
                .commit();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.testpress_fragment_carousel, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewPager = (ViewPager) view.findViewById(R.id.viewpager);
        progressBar = (ProgressBar) view.findViewById(R.id.progress_bar);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            DisplayMetrics metrics = getResources().getDisplayMetrics();
            float pixelWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, metrics);
            progressBar.setIndeterminateDrawable(new CircularProgressDrawable(
                    getResources().getColor(R.color.testpress_color_primary), pixelWidth));
        }
        tabContainer = view.findViewById(R.id.tab_container);
        emptyView = view.findViewById(R.id.empty_container);
        emptyTitleView = (TextView) view.findViewById(R.id.empty_title);
        emptyDescView = (TextView) view.findViewById(R.id.empty_description);
        tabLayout = (TabLayout) view.findViewById(R.id.tab_layout);
        view.findViewById(R.id.retry_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadExamCategories();
            }
        });
        loadExamCategories();
    }

    void loadExamCategories() {
        emptyView.setVisibility(View.GONE);
        tabContainer.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        new SafeAsyncTask<List<ExamCourse>>() {
            public List<ExamCourse> call() throws Exception {
                return new TestpressExamApiClient(getActivity()).getExamCourses().getResults();
            }

            @Override
            protected void onException(final Exception exception) throws RuntimeException {
                if((exception.getMessage() != null) && (exception.getMessage()).equals("403 FORBIDDEN")) {
                    setEmptyText(R.string.testpress_authentication_failed, R.string.testpress_please_login,
                            R.drawable.ic_error_outline_black_18dp);
                } else if (exception.getCause() instanceof IOException) {
                    setEmptyText(R.string.testpress_network_error, R.string.testpress_no_internet_try_again,
                            R.drawable.ic_error_outline_black_18dp);
                } else {
                    setEmptyText(R.string.testpress_error_loading_exams,
                            R.string.testpress_some_thing_went_wrong_try_again,
                            R.drawable.ic_error_outline_black_18dp);
                }
            }

            @Override
            public void onSuccess(final List<ExamCourse> categoryList) {
                Bundle bundle = getArguments();
                if (bundle == null) {
                    bundle = new Bundle();
                }
                bundle.putParcelableArrayList(ExamsListFragment.COURSES,
                        new ArrayList<Parcelable>(categoryList));
                adapter = new ExamPagerAdapter(getResources(), getChildFragmentManager(), bundle);
                viewPager.setAdapter(adapter);
                tabContainer.setVisibility(View.VISIBLE);
                tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
                tabLayout.setupWithViewPager(viewPager);
                viewPager.setCurrentItem(bundle.getInt(CarouselFragment.CURRENT_ITEM, 0));
            }

            @Override
            protected void onFinally() throws RuntimeException {
                super.onFinally();
                progressBar.setVisibility(View.GONE);
            }
        }.execute();
    }

    protected void setEmptyText(final int title, final int description, final int left) {
        emptyView.setVisibility(View.VISIBLE);
        emptyTitleView.setText(title);
        emptyTitleView.setCompoundDrawablesWithIntrinsicBounds(left, 0, 0, 0);
        emptyDescView.setText(description);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if ((requestCode == TEST_TAKEN_REQUEST_CODE) && (Activity.RESULT_OK == resultCode)) {
            if ((viewPager.getCurrentItem() == 0)) {
                // If current tab is available exams then refresh it & move to history tab
                getFragmentByPosition(0).refreshWithProgress();
                viewPager.setCurrentItem(2);
            }
            if (getFragmentByPosition(2).getListView() != null) {
                // Refresh history tab
                getFragmentByPosition(2).refreshWithProgress();
            }
        }
    }

    public ExamsListFragment getFragmentByPosition(int position) {
        return (ExamsListFragment) getChildFragmentManager().findFragmentByTag("android:switcher:"
                + viewPager.getId() + ":" + adapter.getItemId(position));
    }
}
