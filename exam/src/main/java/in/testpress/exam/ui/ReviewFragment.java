package in.testpress.exam.ui;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.HashMap;

import in.testpress.core.TestpressCallback;
import in.testpress.core.TestpressException;
import in.testpress.exam.R;
import in.testpress.exam.models.Attempt;
import in.testpress.exam.models.Exam;
import in.testpress.exam.network.TestpressExamApiClient;
import in.testpress.model.TestpressApiResponse;
import in.testpress.util.UIUtils;

public class ReviewFragment extends Fragment {

    static final String PRAM_EXAM = "exam";
    static final String PRAM_ATTEMPT = "attempt";
    private ProgressBar progressBar;
    private View emptyView;
    private TextView emptyTitleView;
    private TextView emptyDescView;
    private Attempt attempt;
    private Exam exam;
    private MenuItem emailPdfMenu;

    static ReviewFragment getInstance(Exam exam, Attempt attempt) {
        ReviewFragment fragment = new ReviewFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(PRAM_EXAM, exam);
        bundle.putParcelable(PRAM_ATTEMPT, attempt);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.testpress_email_pdf, menu);
        emailPdfMenu = menu.findItem(R.id.email_pdf);
        Drawable emailPdfIcon = emailPdfMenu.getIcon();
        emailPdfIcon.mutate().setColorFilter(ContextCompat.getColor(getActivity(),
                R.color.testpress_actionbar_text), PorterDuff.Mode.SRC_IN);
        emailPdfMenu.setIcon(emailPdfIcon);
        if (!exam.getAllowPdf() || attempt == null) {
            emailPdfMenu.setVisible(false);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.testpress_fragment_review, container, false);
        exam = getArguments().getParcelable(PRAM_EXAM);
        attempt = getArguments().getParcelable(PRAM_ATTEMPT);
        progressBar = (ProgressBar) view.findViewById(R.id.pb_loading);
        UIUtils.setIndeterminateDrawable(getActivity(), progressBar, 4);
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
        new TestpressExamApiClient(getActivity()).getAttempts(exam.getAttemptsFrag(),
                new HashMap<String, Object>())
                .enqueue(new TestpressCallback<TestpressApiResponse<Attempt>>() {
                    @Override
                    public void onSuccess(TestpressApiResponse<Attempt> response) {
                        attempt = response.getResults().get(0);
                        if (exam.getAllowPdf()) {
                            emailPdfMenu.setVisible(true);
                        }
                        setViewPager(view, exam, attempt);
                    }

                    @Override
                    public void onException(TestpressException exception) {
                        progressBar.setVisibility(View.GONE);
                        if(exception.isUnauthenticated()) {
                            setEmptyText(R.string.testpress_authentication_failed, R.string.testpress_please_login);
                        } else if (exception.isNetworkError()) {
                            setEmptyText(R.string.testpress_network_error, R.string.testpress_no_internet_try_again);
                        } else {
                            setEmptyText(R.string.testpress_error_loading_questions,
                                    R.string.testpress_some_thing_went_wrong_try_again);
                        }
                    }
                });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (R.id.email_pdf == item.getItemId()) {
            new EmailPdfDialog(getActivity(), R.style.TestpressAppCompatAlertDialogStyle, true,
                    attempt.getUrlFrag()).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void setEmptyText(final int title, final int description) {
        emptyView.setVisibility(View.VISIBLE);
        emptyTitleView.setText(title);
        emptyDescView.setText(description);
    }
}
