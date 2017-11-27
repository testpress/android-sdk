package in.testpress.exam.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import in.testpress.core.TestpressCallback;
import in.testpress.core.TestpressException;
import in.testpress.core.TestpressSdk;
import in.testpress.exam.R;
import in.testpress.models.greendao.Attempt;
import in.testpress.models.greendao.CourseContent;
import in.testpress.models.greendao.CourseAttempt;
import in.testpress.models.greendao.Exam;
import in.testpress.exam.network.TestpressExamApiClient;

import in.testpress.exam.util.MultiLanguagesUtil;
import in.testpress.models.TestpressApiResponse;
import in.testpress.models.greendao.ExamDao;
import in.testpress.core.TestpressSDKDatabase;
import in.testpress.util.ThrowableLoader;
import in.testpress.ui.BaseToolBarActivity;
import in.testpress.util.UIUtils;
import in.testpress.network.RetrofitCall;
import in.testpress.util.ViewUtils;
import retrofit2.Response;

import static in.testpress.exam.ui.TestFragment.PARAM_CONTENT_ATTEMPT_END_URL;

/**
 * Activity of Test Engine
 */
public class TestActivity extends BaseToolBarActivity implements LoaderManager.LoaderCallbacks<Attempt>  {

    public static final String PARAM_EXAM_SLUG = "slug";
    public static final String PARAM_COURSE_CONTENT = "courseContent";
    public static final String PARAM_COURSE_ATTEMPT = "courseAttempt";
    public static final String PARAM_DISCARD_EXAM_DETAILS = "showExamDetails";
    static final String PARAM_EXAM = "exam";
    static final String PARAM_ATTEMPT = "attempt";
    static final String PARAM_STATE = "state";
    static final String STATE_PAUSED = "paused";
    public static final String PARAM_ACTION = "action";
    public static final String PARAM_VALUE_ACTION_END = "end";
    private static final int START_ATTEMPT_LOADER = 0;
    private static final int RESUME_ATTEMPT_LOADER = 1;
    private static final int END_ATTEMPT_LOADER = 2;
    private TestpressExamApiClient apiClient;
    private Exam exam;
    private Attempt attempt;
    private CourseContent courseContent;
    private CourseAttempt courseAttempt;
    private boolean discardExamDetails;
    private RelativeLayout progressBar;
    private View examDetailsContainer;
    private LinearLayout fragmentContainer;
    private TextView webOnlyLabel;
    private View emptyView;
    private TextView emptyTitleView;
    private TextView emptyDescView;
    private Button retryButton;
    private Button resumeButton;
    private Button startButton;
    private Activity activity;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.testpress_activity_test);
        activity = this;
        examDetailsContainer = findViewById(R.id.exam_details);
        fragmentContainer = (LinearLayout) findViewById(R.id.fragment_container);
        progressBar = (RelativeLayout) findViewById(R.id.pb_loading);
        webOnlyLabel = (TextView) findViewById(R.id.web_only_label);
        emptyView = findViewById(R.id.empty_container);
        emptyTitleView = (TextView) findViewById(R.id.empty_title);
        emptyDescView = (TextView) findViewById(R.id.empty_description);
        retryButton = (Button) findViewById(R.id.retry_button);
        examDetailsContainer.setVisibility(View.GONE);
        startButton = (Button) findViewById(R.id.start_exam);
        Button endButton = (Button) findViewById(R.id.end_exam);
        endButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                endExam();
            }
        });
        resumeButton = (Button) findViewById(R.id.resume_exam);
        ViewUtils.setTypeface(new TextView[] {startButton, resumeButton, endButton},
                TestpressSdk.getRubikMediumFont(this));
        UIUtils.setIndeterminateDrawable(this, findViewById(R.id.progress_bar), 4);
        apiClient = new TestpressExamApiClient(this);
        final Intent intent = getIntent();
        Bundle data = intent.getExtras();

        //Currently I am passing parcelled exam from ContentAttempt
        exam = data.getParcelable(PARAM_EXAM);

        //If coming through exam list then only Id will be passed
        if(exam == null && data.getLong(PARAM_EXAM) != 0) {
            exam = TestpressSDKDatabase.getExamDao(activity).queryBuilder()
                    .where(ExamDao.Properties.Id.eq(data.getLong(PARAM_EXAM))).list().get(0);
        }

        attempt = data.getParcelable(PARAM_ATTEMPT);
        discardExamDetails = getIntent().getBooleanExtra(PARAM_DISCARD_EXAM_DETAILS, false);
        if (exam == null) {
            courseContent = data.getParcelable(PARAM_COURSE_CONTENT);
            courseAttempt = data.getParcelable(PARAM_COURSE_ATTEMPT);
            if (courseContent != null) {
                exam = courseContent.exam;
                displayStartExamScreen();
                return;
            }
            String examSlug = exam.getSlug();
            if (examSlug == null || examSlug.isEmpty()) {
                throw new IllegalArgumentException("PARAM_EXAM_SLUG must not be null or empty.");
            }
            return;
        }
        displayStartExamScreen();
    }

    void loadAttempts(final String attemptUrlFrag) {
        progressBar.setVisibility(View.VISIBLE);
        HashMap<String, Object> queryParams = new HashMap<>();
        queryParams.put(PARAM_STATE, STATE_PAUSED);
        new TestpressExamApiClient(this).getAttempts(attemptUrlFrag, queryParams)
                .enqueue(new TestpressCallback<TestpressApiResponse<Attempt>>() {
                    @Override
                    public void onSuccess(TestpressApiResponse<Attempt> response) {
                        List<Attempt> attempts = response.getResults();
                        if (attempts.isEmpty()) {
                            exam.setPausedAttemptsCount(0);
                        } else {
                            TestActivity.this.attempt = attempts.get(0);
                        }
                        displayStartExamScreen();
                    }

                    @Override
                    public void onException(TestpressException exception) {
                        if (exception.isUnauthenticated()) {
                            setEmptyText(R.string.testpress_authentication_failed,
                                    R.string.testpress_please_login);
                            retryButton.setVisibility(View.GONE);
                        } else if (exception.isNetworkError()) {
                            setEmptyText(R.string.testpress_network_error,
                                    R.string.testpress_no_internet_try_again);
                            retryButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    progressBar.setVisibility(View.VISIBLE);
                                    emptyView.setVisibility(View.GONE);
                                    loadAttempts(attemptUrlFrag);
                                }
                            });
                        } else {
                            setEmptyText(R.string.testpress_error_loading_attempts,
                                    R.string.testpress_some_thing_went_wrong_try_again);
                            retryButton.setVisibility(View.GONE);
                        }
                    }
                });
    }

    @SuppressWarnings("ConstantConditions")
    @SuppressLint("SetTextI18n")
    void displayStartExamScreen() {
        Button startExam = (Button) findViewById(R.id.start_exam);
        LinearLayout attemptActions = (LinearLayout) findViewById(R.id.attempt_actions);
        if (courseAttempt != null) {
            attempt = courseAttempt.assessment;
        }
        if (exam.getDeviceAccessControl() != null && exam.getDeviceAccessControl().equals("web")) {
            webOnlyLabel.setVisibility(View.VISIBLE);
        } else if (!exam.hasStarted()) {
            attemptActions.setVisibility(View.GONE);
            startExam.setVisibility(View.GONE);
        } else if (exam.getPausedAttemptsCount() > 0) {
            if (attempt == null) {
                if (courseContent != null) {
                    startExam.setVisibility(View.VISIBLE);
                } else {
                    loadAttempts(exam.getAttemptsUrl());
                    return;
                }
            } else {
                String action = getIntent().getStringExtra(PARAM_ACTION);
                if (action != null && action.equals(PARAM_VALUE_ACTION_END)) {
                    getSupportActionBar().setTitle(getString(R.string.testpress_end_exam));
                    endExam();
                    return;
                } else {
                    getSupportActionBar().setTitle(getString(R.string.testpress_resume_exam));
                    attemptActions.setVisibility(View.VISIBLE);
                }
            }
        } else {
            startExam.setVisibility(View.VISIBLE);
        }
        if (discardExamDetails) {
            if (attempt == null) {
                getSupportLoaderManager().initLoader(START_ATTEMPT_LOADER, null, TestActivity.this);
            } else {
                getSupportLoaderManager().initLoader(RESUME_ATTEMPT_LOADER, null, TestActivity.this);
            }
            return;
        }
        TextView examTitle = (TextView) findViewById(R.id.exam_title);
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
                negativeMarks, examTitle, date}, TestpressSdk.getRubikMediumFont(this));
        ViewUtils.setTypeface(new TextView[] {descriptionContent, questionsLabel, webOnlyLabel,
                durationLabel, markLabel, negativeMarkLabel, dateLabel, languageLabel },
                TestpressSdk.getRubikRegularFont(this));
        examTitle.setText(exam.getTitle());
        numberOfQuestions.setText(exam.getNumberOfQuestions().toString());
        if (attempt == null) {
            MultiLanguagesUtil.supportMultiLanguage(this, exam, startButton,
                    new MultiLanguagesUtil.LanguageSelectionListener() {
                        @Override
                        public void onLanguageSelected() {
                            startExam(false);
                        }});
            examDuration.setText(exam.getDuration());
        } else {
            MultiLanguagesUtil.supportMultiLanguage(this, exam, resumeButton,
                    new MultiLanguagesUtil.LanguageSelectionListener() {
                        @Override
                        public void onLanguageSelected() {
                            startExam(true);
                        }});
            durationLabel.setText(getString(R.string.testpress_time_remaining));
            examDuration.setText(attempt.getRemainingTime());
        }
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
        progressBar.setVisibility(View.GONE);
        examDetailsContainer.setVisibility(View.VISIBLE);
    }

    private void endExam() {
        progressBar.setVisibility(View.VISIBLE);
        getSupportLoaderManager().initLoader(END_ATTEMPT_LOADER, null, TestActivity.this);
    }

    @SuppressLint("StaticFieldLeak")
    @Override
    public Loader<Attempt> onCreateLoader(final int id, final Bundle args) {
        progressBar.setVisibility(View.VISIBLE);
        return new ThrowableLoader<Attempt>(TestActivity.this, attempt) {
            @Override
            public Attempt loadData() throws TestpressException {
                if (courseContent != null && id != RESUME_ATTEMPT_LOADER) {
                    RetrofitCall<CourseAttempt> call = null;
                    switch (id) {
                        case START_ATTEMPT_LOADER:
                            call = apiClient.createContentAttempt(courseContent.getAttemptsUrl());
                            break;
                        case END_ATTEMPT_LOADER:
                            call = apiClient.endContentAttempt(courseAttempt.getEndAttemptUrl());
                            break;
                    }
                    courseAttempt = executeRetrofitCall(call);
                    return courseAttempt.assessment;
                } else {
                    RetrofitCall<Attempt> call = null;
                    switch (id) {
                        case START_ATTEMPT_LOADER:
                            call = apiClient.createAttempt(exam.getAttemptsFrag());
                            break;
                        case RESUME_ATTEMPT_LOADER:
                            call = apiClient.startAttempt(attempt.getStartUrlFrag());
                            break;
                        case END_ATTEMPT_LOADER:
                            call = apiClient.endAttempt(attempt.getEndUrlFrag());
                            break;
                    }
                    return executeRetrofitCall(call);
                }
            }
        };
    }

    private <T> T executeRetrofitCall(RetrofitCall<T> call) {
        try {
            if (call != null) {
                Response<T> response = call.execute();
                if (response.isSuccessful()) {
                    return response.body();
                } else {
                    throw TestpressException.httpError(response);
                }
            } else {
                throw new IllegalStateException("Invalid loader id");
            }
        } catch (TestpressException e) {
            throw e;
        } catch (IOException e) {
            throw TestpressException.networkError(e);
        } catch (Exception e) {
            throw TestpressException.unexpectedError(e);
        }
    }

    public void onLoadFinished(final Loader<Attempt> loader, final Attempt attempt) {
        progressBar.setVisibility(View.GONE);
        //noinspection ThrowableResultOfMethodCallIgnored
        TestpressException exception = ((ThrowableLoader<Attempt>) loader).clearException();
        if(exception == null) {
            fragmentContainer.setVisibility(View.VISIBLE);
            if (attempt.getState().equals("Running")) {
                if (getSupportActionBar() != null) {
                    getSupportActionBar().hide();
                }
                TestFragment testFragment = new TestFragment();
                Bundle bundle = new Bundle();
                bundle.putParcelable(TestFragment.PARAM_ATTEMPT, attempt);
                bundle.putLong(TestFragment.PARAM_EXAM, exam.getId());
                if (courseAttempt != null) {
                    bundle.putString(PARAM_CONTENT_ATTEMPT_END_URL, courseAttempt.getEndAttemptUrl());
                }
                bundle.putBoolean(PARAM_DISCARD_EXAM_DETAILS, discardExamDetails);
                testFragment.setArguments(bundle);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, testFragment).commitAllowingStateLoss();
            } else {
                startActivityForResult(ReviewStatsActivity.createIntent(this, exam, attempt),
                        CarouselFragment.TEST_TAKEN_REQUEST_CODE);
            }
        } else {
            retryButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    progressBar.setVisibility(View.VISIBLE);
                    emptyView.setVisibility(View.GONE);
                    getSupportLoaderManager().restartLoader(loader.getId(), null, TestActivity.this);
                }
            });
            if (exception.isUnauthenticated()) {
                setEmptyText(R.string.testpress_authentication_failed, R.string.testpress_please_login);
            } else if (exception.isNetworkError()) {
                setEmptyText(R.string.testpress_network_error, R.string.testpress_no_internet_try_again);
            } else if (exception.isClientError()) {
                setEmptyText(R.string.testpress_cannot_attempt_exam,
                        R.string.testpress_some_thing_went_wrong_try_again);
                try {
                    JSONObject jsonObject = new JSONObject(exception.getResponse().errorBody().string());
                    emptyDescView.setText(jsonObject.getString("detail"));
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
                retryButton.setVisibility(View.GONE);
            } else {
                setEmptyText(R.string.testpress_error_loading_attempts,
                        R.string.testpress_some_thing_went_wrong_try_again);
                retryButton.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onLoaderReset(final Loader<Attempt> loader) {
        
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void onBackPressed() {
        TestFragment testFragment = null;
        try {
            //noinspection RestrictedApi
            testFragment = (TestFragment) getSupportFragmentManager().getFragments().get(0);
        } catch (Exception e) {
        }
        if(testFragment != null) {
            if (testFragment.slidingPaneLayout.isOpen()) {
                testFragment.slidingPaneLayout.closePane();
            } else {
                testFragment.pauseExam();
            }
        } else {
            super.onBackPressed();
        }
    }

    private void startExam(boolean resumeExam) {
        int loaderId = resumeExam ? RESUME_ATTEMPT_LOADER : START_ATTEMPT_LOADER;
        getSupportLoaderManager().initLoader(loaderId, null, TestActivity.this);
        examDetailsContainer.setVisibility(View.GONE);
    }

    protected void setEmptyText(final int title, final int description) {
        emptyView.setVisibility(View.VISIBLE);
        emptyTitleView.setText(title);
        emptyDescView.setText(description);
        retryButton.setVisibility(View.VISIBLE);
        examDetailsContainer.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if ((requestCode == CarouselFragment.TEST_TAKEN_REQUEST_CODE) && (Activity.RESULT_OK == resultCode)) {
            setResult(resultCode);
            finish();
        }
    }

}
