package in.testpress.exam.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import in.testpress.core.TestpressCallback;
import in.testpress.core.TestpressException;
import in.testpress.core.TestpressSDKDatabase;
import in.testpress.core.TestpressSdk;
import in.testpress.core.TestpressSession;
import in.testpress.exam.R;
import in.testpress.exam.TestpressExam;
import in.testpress.exam.models.Permission;
import in.testpress.exam.api.TestpressExamApiClient;
import in.testpress.exam.util.MultiLanguagesUtil;
import in.testpress.models.TestpressApiResponse;
import in.testpress.models.greendao.Attempt;
import in.testpress.models.greendao.Content;
import in.testpress.models.greendao.ContentDao;
import in.testpress.models.greendao.CourseAttempt;
import in.testpress.models.greendao.Exam;
import in.testpress.models.greendao.Language;
import in.testpress.network.RetrofitCall;
import in.testpress.ui.BaseToolBarActivity;
import in.testpress.util.Assert;
import in.testpress.util.FormatDate;
import in.testpress.util.ThrowableLoader;
import in.testpress.util.UIUtils;
import in.testpress.util.ViewUtils;
import retrofit2.Response;

import static in.testpress.exam.api.TestpressExamApiClient.IS_PARTIAL;
import static in.testpress.exam.ui.AccessCodeExamsFragment.ACCESS_CODE;

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
    static final String PARAM_PERMISSION = "PARAM_PERMISSION";
    static final String PARAM_LANGUAGES = "PARAM_LANGUAGES";
    public static final String PARAM_IS_PARTIAL_QUESTIONS = "isPartialQuestions";
    public static final String PARAM_ACTION = "action";
    public static final String PARAM_VALUE_ACTION_END = "end";
    private static final int START_ATTEMPT_LOADER = 0;
    private static final int RESUME_ATTEMPT_LOADER = 1;
    private static final int END_ATTEMPT_LOADER = 2;
    private TestpressExamApiClient apiClient;
    Exam exam;
    Attempt attempt;
    Content courseContent;
    CourseAttempt courseAttempt;
    Permission permission;
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
    private Button endButton;
    private boolean isPartialQuestions;
    private List<Language> languages;
    private RetrofitCall<Exam> examApiRequest;
    private RetrofitCall<Permission> permissionsApiRequest;
    private RetrofitCall<TestpressApiResponse<Attempt>> attemptsApiRequest;
    private RetrofitCall<TestpressApiResponse<Language>> languagesApiRequest;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.testpress_activity_test);
        examDetailsContainer = findViewById(R.id.exam_details);
        examDetailsContainer.setVisibility(View.GONE);
        fragmentContainer = findViewById(R.id.fragment_container);
        progressBar = findViewById(R.id.pb_loading);
        webOnlyLabel = findViewById(R.id.web_only_label);
        emptyView = findViewById(R.id.empty_container);
        emptyTitleView = findViewById(R.id.empty_title);
        emptyDescView = findViewById(R.id.empty_description);
        retryButton = findViewById(R.id.retry_button);
        startButton = findViewById(R.id.start_exam);
        endButton = findViewById(R.id.end_exam);
        endButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                endExam();
            }
        });
        resumeButton = findViewById(R.id.resume_exam);
        ViewUtils.setTypeface(new TextView[] {startButton, resumeButton, endButton},
                TestpressSdk.getRubikMediumFont(this));
        UIUtils.setIndeterminateDrawable(this, findViewById(R.id.progress_bar), 4);
        apiClient = new TestpressExamApiClient(this);
        final Intent intent = getIntent();
        Bundle data = intent.getExtras();
        assert data != null;
        isPartialQuestions = data.getBoolean(PARAM_IS_PARTIAL_QUESTIONS, false);
        if (savedInstanceState != null && savedInstanceState.get(PARAM_EXAM) != null) {
            exam = savedInstanceState.getParcelable(PARAM_EXAM);
            TestFragment testFragment = getCurrentFragment();
            if (testFragment != null) {
                attempt = testFragment.attempt;
                getSupportFragmentManager().beginTransaction().remove(testFragment)
                        .commitAllowingStateLoss();
            } else {
                attempt = savedInstanceState.getParcelable(PARAM_ATTEMPT);
            }
            permission = savedInstanceState.getParcelable(PARAM_PERMISSION);
            languages = savedInstanceState.getParcelableArrayList(PARAM_LANGUAGES);
        } else {
            discardExamDetails = getIntent().getBooleanExtra(PARAM_DISCARD_EXAM_DETAILS, false);
            exam = data.getParcelable(PARAM_EXAM);
            attempt = data.getParcelable(PARAM_ATTEMPT);
        }
        courseContent = data.getParcelable(PARAM_COURSE_CONTENT);
        courseAttempt = data.getParcelable(PARAM_COURSE_ATTEMPT);
        onDataInitialized();
    }

    void onDataInitialized() {
        if (courseContent != null) {
            if (exam == null) {
                exam = courseContent.getRawExam();
            }
            if (courseAttempt == null && permission == null) {
                checkPermission();
            } else {
                if (courseAttempt != null && attempt != null) {
                    courseAttempt.setAssessment(attempt);
                }
                checkStartExamScreenState();
            }
        } else if (exam != null) {
            checkStartExamScreenState();
        } else {
            String examSlug = getIntent().getStringExtra(PARAM_EXAM_SLUG);
            if (examSlug == null || examSlug.isEmpty()) {
                throw new IllegalArgumentException("PARAM_EXAM_SLUG must not be null or empty.");
            }
            loadExam(examSlug);
        }
    }

    void checkPermission() {
        progressBar.setVisibility(View.VISIBLE);
        permissionsApiRequest = apiClient.checkPermission(courseContent.getId())
                .enqueue(new TestpressCallback<Permission>() {
                    @Override
                    public void onSuccess(Permission permission) {
                        TestActivity.this.permission = permission;
                        checkStartExamScreenState();
                    }

                    @Override
                    public void onException(TestpressException exception) {
                        handleError(exception, R.string.testpress_error_loading_permission);
                        retryButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                progressBar.setVisibility(View.VISIBLE);
                                emptyView.setVisibility(View.GONE);
                                checkPermission();
                            }
                        });
                    }
                });
    }

    void loadExam(final String examSlug) {
        progressBar.setVisibility(View.VISIBLE);
        examApiRequest = apiClient.getExam(examSlug)
                .enqueue(new TestpressCallback<Exam>() {
                    @Override
                    public void onSuccess(Exam exam) {
                        TestActivity.this.exam = exam;
                        if (exam.getPausedAttemptsCount() > 0) {
                            loadAttempts(exam.getAttemptsUrl());
                        } else {
                            checkStartExamScreenState();
                        }
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

    void loadAttempts(final String attemptUrlFrag) {
        progressBar.setVisibility(View.VISIBLE);
        HashMap<String, Object> queryParams = new HashMap<>();
        queryParams.put(PARAM_STATE, STATE_PAUSED);
        attemptsApiRequest = apiClient.getAttempts(attemptUrlFrag, queryParams)
                .enqueue(new TestpressCallback<TestpressApiResponse<Attempt>>() {
                    @Override
                    public void onSuccess(TestpressApiResponse<Attempt> response) {
                        List<Attempt> attempts = response.getResults();
                        if (attempts.isEmpty()) {
                            exam.setPausedAttemptsCount(0);
                        } else {
                            TestActivity.this.attempt = attempts.get(0);
                        }
                        checkStartExamScreenState();
                    }

                    @Override
                    public void onException(TestpressException exception) {
                        handleError(exception, R.string.testpress_error_loading_attempts);
                        retryButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                progressBar.setVisibility(View.VISIBLE);
                                emptyView.setVisibility(View.GONE);
                                loadAttempts(attemptUrlFrag);
                            }
                        });
                    }
                });
    }

    void fetchLanguages() {
        if (languages != null) {
            displayStartExamScreen();
            return;
        }
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
                            TestActivity.this.languages = exam.getRawLanguages();
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

    void checkStartExamScreenState() {
        Button startExam = findViewById(R.id.start_exam);
        LinearLayout attemptActions = findViewById(R.id.attempt_actions);
        if (courseAttempt != null) {
            attempt = courseAttempt.getRawAssessment();
        }
        if (exam.getDeviceAccessControl() != null && exam.getDeviceAccessControl().equals("web")) {
            webOnlyLabel.setVisibility(View.VISIBLE);
        } else if (!exam.hasStarted()) {
            attemptActions.setVisibility(View.GONE);
            startExam.setVisibility(View.GONE);
        } else if (exam.isEnded()) {
            webOnlyLabel.setVisibility(View.VISIBLE);
            webOnlyLabel.setText(R.string.testpress_exam_ended);
            attemptActions.setVisibility(View.GONE);
            startExam.setVisibility(View.GONE);
        } else if (permission != null &&
                (!permission.getHasPermission() || (permission.getNextRetakeTime() != null &&
                        !permission.getNextRetakeTime().equals("0")))) {

            if (!permission.getHasPermission()) {
                webOnlyLabel.setText(R.string.testpress_exam_no_permission);
            } else {
                String time =
                        FormatDate.getTimeDifference(permission.getNextRetakeTime()).toLowerCase();
                webOnlyLabel.setText(getString(R.string.testpress_can_retake_in_few_min, time));
            }
            webOnlyLabel.setVisibility(View.VISIBLE);
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
                assert getSupportActionBar() != null;
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
        } else {
            fetchLanguages();
        }
    }

    @SuppressLint("SetTextI18n")
    void displayStartExamScreen() {
        TextView examTitle = findViewById(R.id.exam_title);
        TextView numberOfQuestions = findViewById(R.id.number_of_questions);
        TextView examDuration = findViewById(R.id.exam_duration);
        TextView markPerQuestion = findViewById(R.id.mark_per_question);
        TextView negativeMarks = findViewById(R.id.negative_marks);
        TextView date = findViewById(R.id.date);
        LinearLayout dateLayout = findViewById(R.id.date_layout);
        LinearLayout description = findViewById(R.id.description);
        LinearLayout marksPerQuestionLayout = findViewById(R.id.mark_per_question_layout);
        LinearLayout negativeMarksLayout = findViewById(R.id.negative_marks_layout);
        TextView descriptionContent = findViewById(R.id.descriptionContent);
        TextView questionsLabel = findViewById(R.id.questions_label);
        TextView durationLabel = findViewById(R.id.duration_label);
        TextView markLabel = findViewById(R.id.mark_per_question_label);
        TextView negativeMarkLabel = findViewById(R.id.negative_marks_label);
        TextView dateLabel = findViewById(R.id.date_label);
        TextView languageLabel = findViewById(R.id.language_label);
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
            if (attempt.getSections().size() > 1) {
                endButton.setVisibility(View.GONE);
            }
        }
        if (isPartialQuestions) {
            findViewById(R.id.questions_info_layout).setVisibility(View.GONE);
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

        if (exam.getVariableMarkPerQuestion()) {
            marksPerQuestionLayout.setVisibility(View.GONE);
            negativeMarksLayout.setVisibility(View.GONE);
        }
    }

    private void endExam() {
        progressBar.setVisibility(View.VISIBLE);
        getSupportLoaderManager().initLoader(END_ATTEMPT_LOADER, null, TestActivity.this);
    }

    @SuppressLint("StaticFieldLeak")
    @NonNull
    @Override
    public Loader<Attempt> onCreateLoader(final int id, final Bundle args) {
        progressBar.setVisibility(View.VISIBLE);
        return new ThrowableLoader<Attempt>(TestActivity.this, attempt) {
            @Override
            public Attempt loadData() throws TestpressException {
                if (courseContent != null && id != RESUME_ATTEMPT_LOADER) {
                    RetrofitCall<CourseAttempt> call = null;
                    boolean createdNewAttempt = false;
                    switch (id) {
                        case START_ATTEMPT_LOADER:
                            Map<String, Object> data = new HashMap<>();
                            if (isPartialQuestions) {
                                data.put(IS_PARTIAL, true);
                            }
                            String attemptsUrl = courseContent.getAttemptsUrl();
                            attemptsUrl = attemptsUrl.replace("v2.3", "v2.2.1");
                            call = apiClient
                                    .createContentAttempt(attemptsUrl, data);
                            createdNewAttempt = true;
                            break;
                        case END_ATTEMPT_LOADER:
                            call = apiClient.endContentAttempt(courseAttempt.getEndAttemptUrl());
                            createdNewAttempt = false;
                            break;
                    }
                    courseAttempt = executeRetrofitCall(call);
                    saveCourseAttemptInDB(courseAttempt, createdNewAttempt);
                    Attempt attempt = courseAttempt.getRawAssessment();
                    if (id == START_ATTEMPT_LOADER && attempt.getRemainingTime().equals("0:00:00")) {
                        attempt.setRemainingTime(exam.getDuration());
                    }
                    return attempt;
                } else {
                    RetrofitCall<Attempt> call = null;
                    switch (id) {
                        case START_ATTEMPT_LOADER:
                            Map<String, Object> data = new HashMap<>();
                            String accessCode = getIntent().getStringExtra(ACCESS_CODE);
                            if (accessCode != null) {
                                data.put(ACCESS_CODE, accessCode);
                            }
                            if (isPartialQuestions) {
                                data.put(IS_PARTIAL, true);
                            }
                            call = apiClient.createAttempt(exam.getAttemptsFrag(), data);
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

    private void saveCourseAttemptInDB(CourseAttempt courseAttempt, boolean createdNewAttempt) {
        courseAttempt.saveInDB(this, courseContent);
        if (createdNewAttempt) {
            courseContent.setAttemptsCount(courseContent.getAttemptsCount() + 1);
            ContentDao contentDao = TestpressSDKDatabase.getContentDao(this);
            contentDao.insertOrReplace(courseContent);
        }
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

    public void onLoadFinished(@NonNull final Loader<Attempt> loader, final Attempt attempt) {
        if (progressBar.getVisibility() == View.GONE) {
            return;
        }
        getSupportLoaderManager().destroyLoader(loader.getId());
        progressBar.setVisibility(View.GONE);
        //noinspection ThrowableResultOfMethodCallIgnored
        TestpressException exception = ((ThrowableLoader<Attempt>) loader).clearException();
        if(exception == null) {
            fragmentContainer.setVisibility(View.VISIBLE);
            if (attempt.getState().equals("Running")) {
                Bundle bundle = new Bundle();
                bundle.putParcelable(TestFragment.PARAM_ATTEMPT, attempt);
                bundle.putParcelable(TestFragment.PARAM_EXAM, exam);
                if (courseContent != null) {
                    bundle.putParcelable(TestActivity.PARAM_COURSE_CONTENT, courseContent);
                    bundle.putParcelable(TestActivity.PARAM_COURSE_ATTEMPT, courseAttempt);
                }
                bundle.putBoolean(PARAM_DISCARD_EXAM_DETAILS, discardExamDetails);
                displayTestFragment(bundle);
            } else {
                TestpressSession session = TestpressSdk.getTestpressSession(this);
                Assert.assertNotNull("TestpressSession must not be null.", session);
                if (courseAttempt == null) {
                    TestpressExam.showAttemptReport(this, exam, attempt, session);
                } else {
                    TestpressExam.showCourseAttemptReport(this, exam, courseAttempt, session);
                }
            }
        } else {
            handleError(exception, R.string.testpress_error_loading_attempts);
            retryButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    progressBar.setVisibility(View.VISIBLE);
                    emptyView.setVisibility(View.GONE);
                    getSupportLoaderManager().restartLoader(loader.getId(), null, TestActivity.this);
                }
            });
        }
    }

    private void displayTestFragment(Bundle arguments) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        if (exam.hasInstructions()){
            displayExamInstruction(arguments);
            return;
        }

        startExam(arguments);
    }

    private void displayExamInstruction(Bundle arguments){

        ExamInstructions instructions = ExamInstructions.Companion.createInstance(exam.getInstructions(), exam.getTitle(), () -> {
            startExam(arguments);
            return null;
        });
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, instructions).commitAllowingStateLoss();
    }

    private void startExam(Bundle arguments){
        TestFragment testFragment = new TestFragment();
        testFragment.setArguments(arguments);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, testFragment).commitAllowingStateLoss();
    }

    @Override
    public void onLoaderReset(@NonNull final Loader<Attempt> loader) {
        
    }

    @Override
    public void onBackPressed() {
        TestFragment testFragment = getCurrentFragment();
        if (testFragment != null) {
            if (testFragment.slidingPaneLayout.isOpen()) {
                testFragment.slidingPaneLayout.closePane();
            } else {
                testFragment.showPauseExamAlert();
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
        retryButton.setVisibility(View.GONE);
        examDetailsContainer.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if ((requestCode == CarouselFragment.TEST_TAKEN_REQUEST_CODE) && (Activity.RESULT_OK == resultCode)) {
            setResult(resultCode);
            finish();
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
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
        } else if (exception.isClientError()) {
            setEmptyText(R.string.testpress_cannot_attempt_exam,
                    R.string.testpress_some_thing_went_wrong_try_again);
            try {
                JSONObject jsonObject = new JSONObject(exception.getResponse().errorBody().string());
                emptyDescView.setText(jsonObject.getString("detail"));
            } catch (JSONException | IOException e) {
                e.printStackTrace();
            }
        } else {
            setEmptyText(errorMessage, R.string.testpress_some_thing_went_wrong_try_again);
        }
    }

    private TestFragment getCurrentFragment() {
        return (TestFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(PARAM_EXAM, exam);
        outState.putParcelable(PARAM_ATTEMPT, attempt);
        outState.putParcelable(PARAM_PERMISSION, permission);
        if (languages != null) {
            outState.putParcelableArrayList(PARAM_LANGUAGES, new ArrayList<>(languages));
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public RetrofitCall[] getRetrofitCalls() {
        return new RetrofitCall[] {
                examApiRequest, languagesApiRequest, attemptsApiRequest, permissionsApiRequest
        };
    }

}
