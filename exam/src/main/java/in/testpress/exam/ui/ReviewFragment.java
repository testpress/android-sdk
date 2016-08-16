package in.testpress.exam.ui;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.IOException;

import in.testpress.exam.R;
import in.testpress.exam.models.Attempt;
import in.testpress.exam.models.Exam;
import in.testpress.exam.network.AttemptsPager;
import in.testpress.exam.network.TestpressExamApiClient;
import in.testpress.util.CircularProgressDrawable;
import in.testpress.util.SafeAsyncTask;

public class ReviewFragment extends Fragment {

    static final String PRAM_EXAM = "exam";
    static final String PRAM_ATTEMPT = "attempt";
    private ProgressBar progressBar;
    private View emptyView;
    private TextView emptyTitleView;
    private TextView emptyDescView;

    static ReviewFragment getInstance(Exam exam, Attempt attempt) {
        ReviewFragment fragment = new ReviewFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(PRAM_EXAM, exam);
        bundle.putParcelable(PRAM_ATTEMPT, attempt);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.testpress_fragment_review, container, false);
        final Exam exam = getArguments().getParcelable(PRAM_EXAM);
        Attempt attempt = getArguments().getParcelable(PRAM_ATTEMPT);
        progressBar = (ProgressBar) view.findViewById(R.id.pb_loading);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            DisplayMetrics metrics = getResources().getDisplayMetrics();
            float pixelWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, metrics);
            progressBar.setIndeterminateDrawable(new CircularProgressDrawable(
                    getResources().getColor(R.color.testpress_color_primary), pixelWidth));
        }
        if (attempt == null) {
            emptyView = view.findViewById(R.id.empty_container);
            emptyTitleView = (TextView) view.findViewById(R.id.empty_title);
            emptyDescView = (TextView) view.findViewById(R.id.empty_description);
            view.findViewById(R.id.retry_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    progressBar.setVisibility(View.VISIBLE);
                    emptyView.setVisibility(View.GONE);
                    fetchAndRenderAttempt(view, exam);
                }
            });
            fetchAndRenderAttempt(view, exam);
        } else {
            setViewPager(view, exam, attempt);
        }
        return view;
    }

    private void setViewPager(View view, Exam exam, Attempt attempt) {
        progressBar.setVisibility(View.GONE);
        view.findViewById(R.id.tab_container).setVisibility(View.VISIBLE);
        TabLayout tabLayout = (TabLayout) view.findViewById(R.id.tab_layout);
        ViewPager pager = (ViewPager) view.findViewById(R.id.viewpager);
        pager.setAdapter(new ReviewPagerAdapter(this, exam, attempt));
        tabLayout.setupWithViewPager(pager);
    }

    private void fetchAndRenderAttempt(final View view, final Exam exam) {
        final AttemptsPager attemptsPager = new AttemptsPager(exam, new TestpressExamApiClient());
        new SafeAsyncTask<Attempt>() {
            @Override
            public Attempt call() throws Exception {
                attemptsPager.next();
                return attemptsPager.getResources().get(0);
            }

            @Override
            protected void onException(final Exception exception) throws RuntimeException {
                progressBar.setVisibility(View.GONE);
                if((exception.getMessage() != null) && (exception.getMessage()).equals("403 FORBIDDEN")) {
                    setEmptyText(R.string.testpress_authentication_failed, R.string.testpress_please_login);
                } else if (exception.getCause() instanceof IOException) {
                    setEmptyText(R.string.testpress_network_error, R.string.testpress_no_internet_try_again);
                } else {
                    setEmptyText(R.string.testpress_error_loading_questions,
                            R.string.testpress_some_thing_went_wrong_try_again);
                }
            }

            @Override
            protected void onSuccess(final Attempt attempt) throws Exception {
                setViewPager(view, exam, attempt);
            }
        }.execute();
    }

    protected void setEmptyText(final int title, final int description) {
        emptyView.setVisibility(View.VISIBLE);
        emptyTitleView.setText(title);
        emptyDescView.setText(description);
    }
}
