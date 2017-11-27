package in.testpress.exam.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import junit.framework.Assert;

import java.util.HashMap;

import in.testpress.core.TestpressCallback;
import in.testpress.core.TestpressException;
import in.testpress.core.TestpressSdk;
import in.testpress.exam.R;
import in.testpress.models.greendao.Attempt;
import in.testpress.models.greendao.Exam;
import in.testpress.exam.network.TestpressExamApiClient;
import in.testpress.models.InstituteSettings;
import in.testpress.models.TestpressApiResponse;
import in.testpress.models.greendao.ExamDao;
import in.testpress.core.TestpressSDKDatabase;
import in.testpress.util.UIUtils;
import in.testpress.util.ViewUtils;

import static in.testpress.exam.ui.CarouselFragment.TEST_TAKEN_REQUEST_CODE;
import static in.testpress.exam.ui.ReviewStatsActivity.PARAM_ATTEMPT;
import static in.testpress.exam.ui.ReviewStatsActivity.PARAM_EXAM;
import static in.testpress.exam.ui.ReviewStatsActivity.PARAM_PREVIOUS_ACTIVITY;

public class ReviewStatsFragment extends Fragment {

    private TextView examTitle;
    private TextView attemptDate;
    private TextView timeTaken;
    private TextView score;
    private TextView rank;
    private TextView maxRank;
    private TextView correct;
    private TextView incorrect;
    private TextView accuracy;
    private LinearLayout rankLayout;
    private LinearLayout reviewStatLayout;
    private ProgressBar progressBar;
    private View emptyView;
    private ImageView emptyViewImage;
    private TextView emptyTitleView;
    private TextView emptyDescView;
    private Button retryButton;
    private TextView analyticsButton;
    private TextView retakeButton;
    private LinearLayout retakeButtonLayout;
    private TextView reviewQuestionsButton;
    private TextView emailPdfButton;
    private LinearLayout emailPdfButtonLayout;
    private Attempt attempt;
    private Exam exam;

    public static void showReviewStatsFragment(FragmentActivity activity, Exam exam, Attempt attempt) {
        ReviewStatsFragment fragment = new ReviewStatsFragment();
        Bundle bundle = new Bundle();
        bundle.putLong(PARAM_EXAM, exam.getId());
        bundle.putParcelable(PARAM_ATTEMPT, attempt);
        fragment.setArguments(bundle);
        activity.getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commitAllowingStateLoss();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        exam = TestpressSDKDatabase.getExamDao(getActivity()).queryBuilder().where(ExamDao.Properties.Id.eq(getArguments().getLong(PARAM_EXAM))).list().get(0);
        // TODO exam coming as null
        Assert.assertNotNull("PARAM_EXAM must not be null.", exam);
        attempt = getArguments().getParcelable(PARAM_ATTEMPT);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.testpress_fragment_review_stats, container, false);
        progressBar = (ProgressBar) view.findViewById(R.id.pb_loading);
        UIUtils.setIndeterminateDrawable(getActivity(), progressBar, 4);
        emptyView = view.findViewById(R.id.empty_container);
        emptyViewImage = (ImageView) view.findViewById(R.id.image_view);
        emptyTitleView = (TextView) view.findViewById(R.id.empty_title);
        emptyDescView = (TextView) view.findViewById(R.id.empty_description);
        retryButton = (Button) view.findViewById(R.id.retry_button);
        examTitle = (TextView) view.findViewById(R.id.exam_title);
        attemptDate = (TextView) view.findViewById(R.id.attempt_date);
        timeTaken = (TextView) view.findViewById(R.id.time_taken);
        score = (TextView) view.findViewById(R.id.score);
        rank = (TextView) view.findViewById(R.id.rank);
        maxRank = (TextView) view.findViewById(R.id.max_rank);
        correct = (TextView) view.findViewById(R.id.correct_count);
        incorrect = (TextView) view.findViewById(R.id.incorrect_count);
        accuracy = (TextView) view.findViewById(R.id.accuracy);
        rankLayout = (LinearLayout) view.findViewById(R.id.rank_layout);
        reviewStatLayout = (LinearLayout) view.findViewById(R.id.review_statistics_layout);
        reviewStatLayout.setVisibility(View.GONE);
        analyticsButton = (TextView) view.findViewById(R.id.analytics);
        retakeButton = (TextView) view.findViewById(R.id.retake);
        emailPdfButton = (TextView) view.findViewById(R.id.email_mcqs);
        retakeButtonLayout = (LinearLayout) view.findViewById(R.id.retake_button_layout);
        emailPdfButtonLayout = (LinearLayout) view.findViewById(R.id.email_mcqs_layout);
        reviewQuestionsButton = (TextView) view.findViewById(R.id.review);
        ViewUtils.setTypeface(
                new TextView[] {
                        score, rank, correct, incorrect, timeTaken, accuracy, reviewQuestionsButton,
                        analyticsButton, emailPdfButton, retakeButton, emptyTitleView, retryButton
                },
                TestpressSdk.getRubikMediumFont(getContext())
        );
        TextView timeTakenLabel = (TextView) view.findViewById(R.id.time_taken_label);
        TextView scoreLabel = (TextView) view.findViewById(R.id.score_label);
        TextView rankLabel = (TextView) view.findViewById(R.id.rank_label);
        TextView correctLabel = (TextView) view.findViewById(R.id.correct_label);
        TextView incorrectLabel = (TextView) view.findViewById(R.id.incorrect_label);
        TextView accuracyLabel = (TextView) view.findViewById(R.id.accuracy_label);
        ViewUtils.setTypeface(
                new TextView[] {
                        scoreLabel, rankLabel, correctLabel, incorrectLabel, timeTakenLabel,
                        accuracyLabel, examTitle, attemptDate, emptyDescView, maxRank
                },
                TestpressSdk.getRubikRegularFont(getContext()));
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (attempt == null) {
            loadAttempt();
        } else {
            displayTestReport();
        }
    }

    @SuppressLint("SetTextI18n")
    private void displayTestReport() {
        String previousActivity = getArguments().getString(PARAM_PREVIOUS_ACTIVITY);
        if((previousActivity != null) && previousActivity.equals(TestActivity.class.getName())) {
            attemptDate.setText(attempt.getShortDate());
        } else {
            examTitle.setText(exam.getTitle());
            attemptDate.setVisibility(View.VISIBLE);
        }
        timeTaken.setText(attempt.getTimeTaken());
        correct.setText(attempt.getCorrectCount().toString());
        incorrect.setText(attempt.getIncorrectCount().toString());
        if (attempt.getRank().equals("NA")) {
            rankLayout.setVisibility(View.GONE);
        } else {
            rank.setText(attempt.getRank());
            maxRank.setText(attempt.getMaxRank());
        }
        score.setText(attempt.getScore());
        accuracy.setText(attempt.getAccuracy().toString());
        if (exam.getShowAnswers()) {
            reviewQuestionsButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getActivity().startActivity(
                            ReviewQuestionsActivity.createIntent(getActivity(), exam, attempt)
                    );
                }
            });
            reviewQuestionsButton.setVisibility(View.VISIBLE);
            analyticsButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getActivity().startActivity(
                            AnalyticsActivity.createIntent(getActivity(), attempt.getUrlFrag() +
                                    TestpressExamApiClient.ATTEMPT_SUBJECT_ANALYTICS_PATH, null, null)
                    );
                }
            });
            analyticsButton.setVisibility(View.VISIBLE);
        } else {
            reviewQuestionsButton.setVisibility(View.GONE);
            analyticsButton.setVisibility(View.GONE);
        }
        if (exam.getAllowPdf()) {
            emailPdfButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new EmailPdfDialog(getActivity(), R.style.TestpressAppCompatAlertDialogStyle,
                            true, attempt.getUrlFrag()).show();
                }
            });
        } else {
            emailPdfButtonLayout.setVisibility(View.GONE);
        }
        //noinspection ConstantConditions
        InstituteSettings settings =
                TestpressSdk.getTestpressSession(getContext()).getInstituteSettings();

        if (canAttemptExam() && !settings.isCoursesFrontend()) {
            retakeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startExam();
                }
            });
        } else {
            retakeButtonLayout.setVisibility(View.GONE);
        }
        reviewStatLayout.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
    }

    private void loadAttempt() {
        new TestpressExamApiClient(getActivity()).getAttempts(exam.getAttemptsUrl(),
                new HashMap<String, Object>())
                .enqueue(new TestpressCallback<TestpressApiResponse<Attempt>>() {
                    @Override
                    public void onSuccess(TestpressApiResponse<Attempt> response) {
                        if (getActivity() == null) {
                        }
                        attempt = response.getResults().get(0);
                        if (exam.getAllowPdf()) {
                            getActivity().invalidateOptionsMenu();
                        }
                        displayTestReport();
                    }

                    @Override
                    public void onException(TestpressException exception) {
                        progressBar.setVisibility(View.GONE);
                        retryButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                progressBar.setVisibility(View.VISIBLE);
                                emptyView.setVisibility(View.GONE);
                                loadAttempt();
                            }
                        });
                        if(exception.isUnauthenticated()) {
                            setEmptyText(R.string.testpress_authentication_failed,
                                    R.string.testpress_please_login,
                                    R.drawable.testpress_alert_warning);
                        } else if (exception.isNetworkError()) {
                            setEmptyText(R.string.testpress_network_error,
                                    R.string.testpress_no_internet_try_again,
                                    R.drawable.testpress_no_wifi);
                        } else {
                            setEmptyText(R.string.testpress_error_loading_questions,
                                    R.string.testpress_some_thing_went_wrong_try_again,
                                    R.drawable.testpress_alert_warning);
                        }
                    }
                });
    }

    private boolean canAttemptExam() {
        return (exam.getPausedAttemptsCount() == 0) && exam.canRetake() && exam.hasStarted() &&
                !exam.getDeviceAccessControl().equals("web");
    }

    private void startExam() {
        Intent intent = new Intent(getActivity(), TestActivity.class);
        intent.putExtra(TestActivity.PARAM_EXAM, exam.getId());
        startActivityForResult(intent, CarouselFragment.TEST_TAKEN_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == TEST_TAKEN_REQUEST_CODE) && (Activity.RESULT_OK == resultCode)) {
            getActivity().setResult(resultCode);
            getActivity().finish();
        }
    }

    protected void setEmptyText(final int title, final int description, int imageRes) {
        emptyView.setVisibility(View.VISIBLE);
        emptyTitleView.setText(title);
        emptyDescView.setText(description);
        emptyViewImage.setImageResource(imageRes);
    }
}
