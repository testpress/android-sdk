package in.testpress.exam.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import junit.framework.Assert;

import java.util.ArrayList;
import java.util.List;

import in.testpress.core.TestpressCallback;
import in.testpress.core.TestpressException;
import in.testpress.core.TestpressSdk;
import in.testpress.exam.R;
import in.testpress.exam.models.Attempt;
import in.testpress.models.greendao.Exam;
import in.testpress.exam.network.AttemptsPager;
import in.testpress.exam.network.TestpressExamApiClient;
import in.testpress.exam.util.MultiLanguagesUtil;
import in.testpress.models.greendao.ExamDao;
import in.testpress.models.greendao.TestpressSDK;
import in.testpress.ui.BaseToolBarActivity;
import in.testpress.util.ThrowableLoader;
import in.testpress.util.UIUtils;
import in.testpress.util.ViewUtils;

import static in.testpress.exam.TestpressExam.PARAM_EXAM_SLUG;
import static in.testpress.exam.network.TestpressExamApiClient.STATE_PAUSED;
import static in.testpress.exam.ui.CarouselFragment.TEST_TAKEN_REQUEST_CODE;

public class AttemptsActivity extends BaseToolBarActivity
        implements LoaderManager.LoaderCallbacks<List<Attempt>> {

    public static final String EXAM = "exam";
    public static final String STATE_COMPLETED = "Completed";

    private ScrollView scrollView;
    private LinearLayout examDetailsLayout;
    private LinearLayout emptyView;
    private FrameLayout testReportLayout;
    private LinearLayout startButtonLayout;
    private Button startButton;
    private TextView emptyTitleView;
    private TextView emptyDescView;
    private ProgressBar progressBar;
    private Button retryButton;
    private Exam exam;
    private List<Attempt> attempts = new ArrayList<>();
    private AttemptsPager pager;
    private Activity activity;

    @SuppressWarnings({"ConstantConditions", "deprecation"})
    @SuppressLint({"SetJavaScriptEnabled", "AddJavascriptInterface", "DefaultLocale"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.testpress_activity_attempts);
        activity = this;
        scrollView = (ScrollView) findViewById(R.id.scroll_view);
        examDetailsLayout = (LinearLayout) findViewById(R.id.exam_details_layout);
        testReportLayout = (FrameLayout) findViewById(R.id.fragment_container);
        startButtonLayout = (LinearLayout) findViewById(R.id.start_button_layout);
        emptyView = (LinearLayout) findViewById(R.id.empty_container);
        startButton = (Button) findViewById(R.id.start_exam);
        emptyTitleView = (TextView) findViewById(R.id.empty_title);
        emptyDescView = (TextView) findViewById(R.id.empty_description);
        retryButton = (Button) findViewById(R.id.retry_button);
        progressBar = (ProgressBar) findViewById(R.id.pb_loading);
        UIUtils.setIndeterminateDrawable(this, progressBar, 4);
        startButton.setTypeface(TestpressSdk.getRubikMediumFont(this));
        //noinspection ConstantConditions
        getSupportActionBar().hide();
        //exam = getIntent().getLongExtra(EXAM, 0);
        if (exam == null) {
            String examSlug = getIntent().getStringExtra(PARAM_EXAM_SLUG);
            // Throw exception if both exam & exam slug is null
            Assert.assertNotNull("EXAM must not be null.", examSlug);
            loadExam(examSlug);
        } else {
            checkExamState();
        }
    }

    void checkExamState() {
        if ((exam.getAttemptsCount() == 0) ||
                (exam.getAttemptsCount() == 1 && exam.getPausedAttemptsCount() == 1)) {
            // Show start exam screen with exam details if still exam is not taken or only one
            // paused attempt exist
            displayStartExamScreen();
        } else {
            retryButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getSupportLoaderManager().restartLoader(0, null, AttemptsActivity.this);
                }
            });
            pager = new AttemptsPager(exam.getAttemptsUrl(), new TestpressExamApiClient(this));
            getSupportLoaderManager().initLoader(0, null, this);
        }
    }

    @SuppressLint("SetTextI18n")
    private void displayStartExamScreen() {
        TextView titleView = (TextView) findViewById(R.id.title);
        TextView numberOfQuestions = (TextView) findViewById(R.id.number_of_questions);
        TextView examDuration = (TextView) findViewById(R.id.exam_duration);
        TextView markPerQuestion = (TextView) findViewById(R.id.mark_per_question);
        TextView negativeMarks = (TextView) findViewById(R.id.negative_marks);
        TextView date = (TextView) findViewById(R.id.date);
        LinearLayout dateLayout = (LinearLayout) findViewById(R.id.date_layout);
        LinearLayout description = (LinearLayout) findViewById(R.id.description);
        TextView descriptionContent = (TextView) findViewById(R.id.descriptionContent);
        TextView questionsLabel = (TextView) findViewById(R.id.questions_label);
        TextView durationLabel = (TextView) findViewById(R.id.duration_label);
        TextView markLabel = (TextView) findViewById(R.id.mark_per_question_label);
        TextView negativeMarkLabel = (TextView) findViewById(R.id.negative_marks_label);
        TextView dateLabel = (TextView) findViewById(R.id.date_label);
        TextView languageLabel = (TextView) findViewById(R.id.language_label);
        ViewUtils.setTypeface(new TextView[] {numberOfQuestions, examDuration, markPerQuestion,
                negativeMarks, titleView, date}, TestpressSdk.getRubikMediumFont(this));
        ViewUtils.setTypeface(
                new TextView[] {
                        descriptionContent, questionsLabel, durationLabel, markLabel,
                        negativeMarkLabel, dateLabel, languageLabel
                },
                TestpressSdk.getRubikRegularFont(this)
        );
        int buttonTextResId = (exam.getPausedAttemptsCount() > 0) ?
                R.string.testpress_resume_exam : R.string.testpress_start_exam;

        startButton.setText(buttonTextResId);
        //noinspection ConstantConditions
        getSupportActionBar().setTitle(buttonTextResId);
        getSupportActionBar().show();
        titleView.setText(exam.getTitle());
        numberOfQuestions.setText(exam.getNumberOfQuestions().toString());
        examDuration.setText(exam.getDuration());
        markPerQuestion.setText(exam.getMarkPerQuestion());
        negativeMarks.setText(exam.getNegativeMarks());
        if (exam.getFormattedStartDate().equals("forever")) {
            dateLayout.setVisibility(View.GONE);
        } else {
            date.setText(exam.getFormattedStartDate() + " - " + exam.getFormattedEndDate());
            dateLayout.setVisibility(View.VISIBLE);
        }
        if ((exam.getDescription() != null) && !exam.getDescription().trim().isEmpty()) {
            description.setVisibility(View.VISIBLE);
            descriptionContent.setText(exam.getDescription());
        }
        if (canAttemptExam()) {
            MultiLanguagesUtil.supportMultiLanguage(this, exam, startButton,
                    new MultiLanguagesUtil.LanguageSelectionListener() {
                        @Override
                        public void onLanguageSelected() {
                            startExam(true);
                        }});
            startButtonLayout.setVisibility(View.VISIBLE);
        } else {
            startButtonLayout.setVisibility(View.GONE);
        }
        examDetailsLayout.setVisibility(View.VISIBLE);
        scrollView.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
    }

    private boolean canAttemptExam() {
        if (exam.getAttemptsCount() == 0 || exam.getPausedAttemptsCount() != 0 ||
                ((exam.getAllowRetake()) &&
                        (exam.getAttemptsCount() <= exam.getMaxRetakes() ||
                                exam.getMaxRetakes() < 0))) {

            if (exam.getDeviceAccessControl() != null &&
                    exam.getDeviceAccessControl().equals("web")) {

                TextView webOnlyLabel;
                if (attempts.isEmpty()) {
                    webOnlyLabel = (TextView) findViewById(R.id.web_only_label);
                } else {
                    webOnlyLabel = (TextView) findViewById(R.id.attempt_web_only_label);
                }
                webOnlyLabel.setTypeface(TestpressSdk.getRubikRegularFont(this));
                webOnlyLabel.setVisibility(View.VISIBLE);
                return false;
            } else if (exam.isEnded()) {
                if (attempts.isEmpty()) {
                    TextView webOnlyLabel = (TextView) findViewById(R.id.web_only_label);
                    webOnlyLabel.setTypeface(TestpressSdk.getRubikRegularFont(this));
                    webOnlyLabel.setVisibility(View.VISIBLE);
                }
                return false;
            } else {
                return exam.hasStarted();
            }
        } else {
            return false;
        }
    }

    void loadExam(final String examSlug) {
        List<Exam> exams = TestpressSDK.getExamDao(activity).queryBuilder().where(ExamDao.Properties.Slug.eq(examSlug)).list();
        if(exams.size() > 0) {
            AttemptsActivity.this.exam = exams.get(0);
            checkExamState();
        } else {
            progressBar.setVisibility(View.VISIBLE);
            new TestpressExamApiClient(this).getExam(examSlug)
                    .enqueue(new TestpressCallback<Exam>() {
                        @Override
                        public void onSuccess(Exam exam) {
                            AttemptsActivity.this.exam = exam;
                            saveExamInDB(exam);
                            checkExamState();
                        }

                        @Override
                        public void onException(TestpressException exception) {
                            if (exception.isUnauthenticated()) {
                                setEmptyText(R.string.testpress_authentication_failed,
                                        R.string.testpress_exam_no_permission);
                                retryButton.setVisibility(View.GONE);
                            } else if (exception.isNetworkError()) {
                                setEmptyText(R.string.testpress_network_error,
                                        R.string.testpress_no_internet_try_again);
                                retryButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        progressBar.setVisibility(View.VISIBLE);
                                        emptyView.setVisibility(View.GONE);
                                        loadExam(examSlug);
                                    }
                                });
                            } else if (exception.getResponse().code() == 404) {
                                setEmptyText(R.string.testpress_exam_not_available,
                                        R.string.testpress_exam_not_available_description);
                                retryButton.setVisibility(View.GONE);
                            } else {
                                setEmptyText(R.string.testpress_error_loading_exam,
                                        R.string.testpress_some_thing_went_wrong_try_again);
                                retryButton.setVisibility(View.GONE);
                            }
                        }
                    });
        }
    }

    void saveExamInDB(Exam exam) {
        TestpressSDK.getExamDao(activity).insertOrReplace(exam);
    }

    @SuppressLint("StaticFieldLeak")
    @Override
    public Loader<List<Attempt>> onCreateLoader(int id, Bundle args) {
        progressBar.setVisibility(View.VISIBLE);
        scrollView.setVisibility(View.GONE);
        emptyView.setVisibility(View.GONE);
        return new ThrowableLoader<List<Attempt>>(this, attempts) {
            @Override
            public List<Attempt> loadData() throws TestpressException {
                do {
                    pager.next();
                    attempts = pager.getResources();
                } while (pager.hasNext());
                return attempts;
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<List<Attempt>> loader, List<Attempt> data) {
        //noinspection ThrowableResultOfMethodCallIgnored
        TestpressException exception = ((ThrowableLoader<List<Attempt>>) loader).clearException();
        if(exception != null) {
            if (exception.isUnauthenticated()) {
                setEmptyText(R.string.testpress_authentication_failed,
                        R.string.testpress_please_login);
            } else if (exception.isNetworkError()) {
                setEmptyText(R.string.testpress_network_error,
                        R.string.testpress_no_internet_try_again);
            } else {
                setEmptyText(R.string.testpress_error_loading_attempts,
                        R.string.testpress_some_thing_went_wrong_try_again);
            }
            return;
        }

        if (attempts.size() == 1 && attempts.get(0).getState().equals(STATE_COMPLETED)) {
            // if only one attempt exist then show the test report of that attempt
            testReportLayout.setVisibility(View.VISIBLE);
            ReviewStatsFragment.showReviewStatsFragment(this, exam, attempts.get(0), true);
            progressBar.setVisibility(View.GONE);
        } else {
            displayAttemptsList();
        }
    }

    private void displayAttemptsList() {
        //noinspection ConstantConditions
        getSupportActionBar().setTitle(exam.getTitle());
        getSupportActionBar().show();
        if (canAttemptExam()) {
            final List<Attempt> pausedAttempts = new ArrayList<>();
            if (exam.getPausedAttemptsCount() > 0) {
                for (Attempt attempt : attempts) {
                    if (attempt.getState().equals(STATE_PAUSED)) {
                        pausedAttempts.add(attempt);
                    }
                }
            }
            if (pausedAttempts.isEmpty()) {
                startButton.setText(R.string.testpress_retake);
                startButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startExam(false);
                    }
                });
            } else {
                startButton.setText(R.string.testpress_resume);
                startButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(AttemptsActivity.this, TestActivity.class);
                        if (getIntent().getExtras() != null) {
                            intent.putExtras(getIntent().getExtras());
                        }
                        intent.putExtra(TestActivity.PARAM_EXAM, exam);
                        intent.putExtra(TestActivity.PARAM_ATTEMPT,
                                pausedAttempts.get(pausedAttempts.size() - 1));
                        startActivityForResult(intent, CarouselFragment.TEST_TAKEN_REQUEST_CODE);
                    }
                });
            }
            startButtonLayout.setVisibility(View.VISIBLE);
        } else {
            startButtonLayout.setVisibility(View.GONE);
        }
        RecyclerView attemptList = (RecyclerView) findViewById(R.id.attempt_list);
        attemptList.setHasFixedSize(true);
        attemptList.setLayoutManager(new LinearLayoutManager(this));
        attemptList.setAdapter(new AttemptListAdapter(this, exam, attempts));
        examDetailsLayout.setVisibility(View.GONE);
        scrollView.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == TEST_TAKEN_REQUEST_CODE) && (Activity.RESULT_OK == resultCode)) {
            setResult(resultCode);
            finish();
        }
    }

    private void startExam(boolean discardExamDetails) {
        Intent intent = new Intent(this, TestActivity.class);
        if (getIntent().getExtras() != null) {
            intent.putExtras(getIntent().getExtras());
        }
        intent.putExtra(TestActivity.PARAM_EXAM, exam);
        intent.putExtra(TestActivity.PARAM_DISCARD_EXAM_DETAILS, discardExamDetails);
        startActivityForResult(intent, CarouselFragment.TEST_TAKEN_REQUEST_CODE);
    }

    protected void setEmptyText(final int title, final int description) {
        emptyView.setVisibility(View.VISIBLE);
        emptyTitleView.setText(title);
        emptyDescView.setText(description);
        scrollView.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onLoaderReset(Loader<List<Attempt>> loader) {
    }
}