package in.testpress.exam.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.StringRes;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import junit.framework.Assert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import in.testpress.core.TestpressCallback;
import in.testpress.core.TestpressException;
import in.testpress.core.TestpressSDKDatabase;
import in.testpress.core.TestpressSdk;
import in.testpress.exam.R;
import in.testpress.models.greendao.ExamDao;
import in.testpress.exam.pager.AttemptsPager;
import in.testpress.exam.api.TestpressExamApiClient;
import in.testpress.exam.util.MultiLanguagesUtil;
import in.testpress.exam.util.RetakeExamUtil;
import in.testpress.models.TestpressApiResponse;
import in.testpress.models.greendao.Attempt;
import in.testpress.models.greendao.Exam;
import in.testpress.models.greendao.Language;
import in.testpress.network.RetrofitCall;
import in.testpress.ui.BaseToolBarActivity;
import in.testpress.util.ThrowableLoader;
import in.testpress.util.UIUtils;
import in.testpress.util.ViewUtils;

import static in.testpress.exam.TestpressExam.PARAM_EXAM_SLUG;
import static in.testpress.exam.api.TestpressExamApiClient.STATE_PAUSED;
import static in.testpress.exam.ui.CarouselFragment.TEST_TAKEN_REQUEST_CODE;
import static in.testpress.exam.ui.TestActivity.PARAM_EXAM;

public class AttemptsActivity extends BaseToolBarActivity
        implements LoaderManager.LoaderCallbacks<List<Attempt>> {

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

    private TestpressExamApiClient apiClient;
    private Exam exam;
    private List<Attempt> attempts = new ArrayList<>();
    private AttemptsPager pager;
    private RetrofitCall<Exam> examApiRequest;
    private RetrofitCall<TestpressApiResponse<Language>> languagesApiRequest;

    @SuppressWarnings({"ConstantConditions", "deprecation"})
    @SuppressLint({"SetJavaScriptEnabled", "AddJavascriptInterface", "DefaultLocale"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.testpress_activity_attempts);
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
        apiClient = new TestpressExamApiClient(this);
        fetchOrCheckExam();
    }

    void fetchOrCheckExam() {
        exam = getIntent().getParcelableExtra(PARAM_EXAM);
        String examSlug = getIntent().getStringExtra(PARAM_EXAM_SLUG);
        Boolean isDetailsFetched = false;
        Exam examFromDb;
        if (exam != null){
            examFromDb = TestpressSDKDatabase.getExamDao(this).queryBuilder().where(ExamDao.Properties.Slug.eq(exam.getSlug())).limit(1).unique();
        } else {
            examFromDb = TestpressSDKDatabase.getExamDao(this).queryBuilder().where(ExamDao.Properties.Slug.eq(examSlug)).limit(1).unique();
        }

        if (examFromDb != null) {
            isDetailsFetched = examFromDb.getIsDetailsFetched();
            exam = examFromDb;
        }

        if (exam == null) {
            // Throw exception if both exam & exam slug is null
            Assert.assertNotNull("EXAM must not be null.", examSlug);
            loadExam(examSlug);
        } else if (isDetailsFetched == null || !isDetailsFetched){
            loadExam(exam.getSlug());
        } else {
            checkExamState();
        }
    }

    void checkExamState() {
        setActionBarTitle(exam.getTitle());
        if ((exam.getAttemptsCount() == 0) ||
                (exam.getAttemptsCount() == 1 && exam.getPausedAttemptsCount() == 1)) {
            // Show start exam screen with exam details if still exam is not taken or only one
            // paused attempt exist
            fetchLanguages();
        } else {
            retryButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getSupportLoaderManager().restartLoader(0, null, AttemptsActivity.this);
                }
            });
            pager = new AttemptsPager(exam.getAttemptsUrl(), apiClient);
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
        setActionBarTitle(buttonTextResId);
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
                            startExam(true, false);
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
        progressBar.setVisibility(View.VISIBLE);
        examApiRequest = apiClient.getExam(examSlug)
                .enqueue(new TestpressCallback<Exam>() {
                    @Override
                    public void onSuccess(Exam exam) {
                        AttemptsActivity.this.exam = exam;
                        exam.setIsDetailsFetched(true);
                        TestpressSDKDatabase.getExamDao(AttemptsActivity.this)
                                .insertOrReplace(exam);
                        checkExamState();

                    }

                    @Override
                    public void onException(TestpressException exception) {
                        handleError(exception, R.string.testpress_error_loading_exam);
                        retryButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                progressBar.setVisibility(View.VISIBLE);
                                emptyView.setVisibility(View.GONE);
                                loadExam(examSlug);
                            }
                        });
                    }
                });
    }

    void fetchLanguages() {
        progressBar.setVisibility(View.VISIBLE);
        languagesApiRequest = apiClient.getLanguages(exam.getSlug())
                .enqueue(new TestpressCallback<TestpressApiResponse<Language>>() {
                    @Override
                    public void onSuccess(TestpressApiResponse<Language> apiResponse) {
                        List<Language> languages = exam.getRawLanguages();
                        languages.addAll(apiResponse.getResults());
                        Map<String, Language> uniqueLanguages = new HashMap<>();
                        for (Language language : languages) {
                            uniqueLanguages.put(language.getCode(), language);
                        }
                        exam.setLanguages(new ArrayList<>(uniqueLanguages.values()));
                        if (apiResponse.hasMore()) {
                            fetchLanguages();
                        } else {
                            displayStartExamScreen();
                        }
                    }

                    @Override
                    public void onException(TestpressException exception) {
                        handleError(exception, R.string.testpress_error_loading_languages);
                        retryButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                progressBar.setVisibility(View.VISIBLE);
                                emptyView.setVisibility(View.GONE);
                                fetchLanguages();
                            }
                        });
                    }
                });
    }

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
        getSupportLoaderManager().destroyLoader(loader.getId());
        if(exception != null) {
            handleError(exception, R.string.testpress_error_loading_attempts);
            return;
        }

        if (attempts.size() == 1 && attempts.get(0).getState().equals(STATE_COMPLETED)) {
            // if only one attempt exist then show the test report of that attempt
            testReportLayout.setVisibility(View.VISIBLE);
            setActionBarTitle(R.string.testpress_test_report);
            ReviewStatsFragment.showReviewStatsFragment(this, exam, attempts.get(0), true);
            progressBar.setVisibility(View.GONE);
        } else {
            displayAttemptsList();
        }
    }

    private void displayAttemptsList() {
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
                        RetakeExamUtil.showRetakeOptions(AttemptsActivity.this,
                                new RetakeExamUtil.SelectionListener() {
                                    @Override
                                    public void onOptionSelected(boolean isPartial) {
                                        startExam(false, isPartial);
                                    }
                                });

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

    private void startExam(boolean discardExamDetails, boolean isPartial) {
        Intent intent = new Intent(this, TestActivity.class);
        if (getIntent().getExtras() != null) {
            intent.putExtras(getIntent().getExtras());
        }
        intent.putExtra(TestActivity.PARAM_IS_PARTIAL_QUESTIONS, isPartial);
        intent.putExtra(TestActivity.PARAM_EXAM, exam);
        intent.putExtra(TestActivity.PARAM_DISCARD_EXAM_DETAILS, discardExamDetails);
        startActivityForResult(intent, CarouselFragment.TEST_TAKEN_REQUEST_CODE);
    }

    protected void setEmptyText(final int title, final int description) {
        emptyView.setVisibility(View.VISIBLE);
        emptyTitleView.setText(title);
        emptyDescView.setText(description);
        retryButton.setVisibility(View.GONE);
        scrollView.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
    }

    void handleError(TestpressException exception, @StringRes int errorMessage) {
        if (exception.isUnauthenticated()) {
            setEmptyText(R.string.testpress_authentication_failed,
                    R.string.testpress_exam_no_permission);
        } else if (exception.isNetworkError()) {
            setEmptyText(R.string.testpress_network_error, R.string.testpress_no_internet_try_again);
            retryButton.setVisibility(View.VISIBLE);
        } else if (exception.getResponse().code() == 404) {
            setEmptyText(R.string.testpress_exam_not_available,
                    R.string.testpress_exam_not_available_description);
        } else {
            setEmptyText(errorMessage, R.string.testpress_some_thing_went_wrong_try_again);
        }
    }

    @Override
    public RetrofitCall[] getRetrofitCalls() {
        return new RetrofitCall[] {
                examApiRequest, languagesApiRequest
        };
    }

    @Override
    public void onLoaderReset(Loader<List<Attempt>> loader) {
    }
}