package in.testpress.exam.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.Observer;

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
import in.testpress.exam.ui.viewmodel.ExamViewModel;
import in.testpress.exam.util.MultiLanguagesUtil;
import in.testpress.models.TestpressApiResponse;
import in.testpress.models.greendao.Attempt;
import in.testpress.models.greendao.Content;
import in.testpress.models.greendao.ContentDao;
import in.testpress.models.greendao.CourseAttempt;
import in.testpress.models.greendao.Exam;
import in.testpress.models.greendao.Language;
import in.testpress.network.Resource;
import in.testpress.network.RetrofitCall;
import in.testpress.ui.BaseToolBarActivity;
import in.testpress.util.Assert;
import in.testpress.util.FormatDate;
import in.testpress.util.UIUtils;
import in.testpress.util.ViewUtils;
import in.testpress.v2_4.models.ApiResponse;

import static in.testpress.exam.api.TestpressExamApiClient.IS_PARTIAL;
import static in.testpress.exam.ui.AccessCodeExamsFragment.ACCESS_CODE;

/**
 * Activity of Test Engine
 */
public class TestActivity extends BaseToolBarActivity  {

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
    private RetrofitCall<ApiResponse<List<Language>>> languagesApiRequest;
    private ExamViewModel examViewModel;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.testpress_activity_test);
        examViewModel = ExamViewModel.Companion.initializeViewModel(this);
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
        observePermissionResources();
        observeLanguageResources();
        observeContentAttemptResources();
        observeAttemptResources();
    }

    void observePermissionResources(){
        examViewModel.getPermissionResource().observe(this, new Observer<Resource<Permission>>() {
            @Override
            public void onChanged(Resource<Permission> permissionResource) {
                switch (permissionResource.getStatus()){
                    case SUCCESS:{
                        TestActivity.this.permission = permissionResource.getData();
                        checkStartExamScreenState();
                        break;
                    }
                    case LOADING:{
                        progressBar.setVisibility(View.VISIBLE);
                        break;
                    }
                    case ERROR:{
                        handleError(permissionResource.getException(), R.string.testpress_error_loading_permission);
                        retryButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                progressBar.setVisibility(View.VISIBLE);
                                emptyView.setVisibility(View.GONE);
                                checkPermission();
                            }
                        });
                        break;
                    }
                }
            }
        });
    }

    void observeLanguageResources(){
        examViewModel.getLanguageResource().observe(this, new Observer<Resource<List<Language>>>() {
            @Override
            public void onChanged(Resource<List<Language>> listResource) {
                switch (listResource.getStatus()){
                    case SUCCESS:{
                        List<Language> languages = exam.getRawLanguages();
                        languages.addAll(listResource.getData());
                        Map<String, Language> uniqueLanguages = new HashMap<>();
                        for (Language language : languages) {
                            uniqueLanguages.put(language.getCode(), language);
                        }
                        exam.setLanguages(new ArrayList<>(uniqueLanguages.values()));
                        TestActivity.this.languages = exam.getRawLanguages();
                        displayStartExamScreen();
                        break;
                    }
                    case LOADING:{
                        progressBar.setVisibility(View.VISIBLE);
                        break;
                    }
                    case ERROR:{
                        handleError(listResource.getException(), R.string.testpress_error_loading_languages);
                        retryButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                progressBar.setVisibility(View.VISIBLE);
                                emptyView.setVisibility(View.GONE);
                                fetchLanguages();
                            }
                        });
                        break;
                    }
                }
            }
        });
    }

    void observeAttemptResources(){
        examViewModel.getAttemptResource().observe(this, new Observer<Resource<Attempt>>() {
            @Override
            public void onChanged(Resource<Attempt> attemptResource) {
                switch (attemptResource.getStatus()){
                    case SUCCESS:{
                        showFragment();
                        handleSuccessAttempt(attemptResource.getData());
                        break;
                    }
                    case LOADING:{
                        progressBar.setVisibility(View.VISIBLE);
                        break;
                    }
                    case ERROR:{
                        progressBar.setVisibility(View.GONE);
                        handleError(attemptResource.getException(), R.string.testpress_error_loading_attempts);
                        retryButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                progressBar.setVisibility(View.VISIBLE);
                                emptyView.setVisibility(View.GONE);
                                createAttempt();
                            }
                        });
                        break;
                    }
                }
            }
        });
    }

    void observeContentAttemptResources(){
        examViewModel.getContentAttemptResource().observe(this, new Observer<Resource<CourseAttempt>>() {
            @Override
            public void onChanged(Resource<CourseAttempt> courseAttemptResource) {
                switch (courseAttemptResource.getStatus()){
                    case SUCCESS:{
                        showFragment();
                        handleCourseAttemptSuccess(courseAttemptResource.getData());
                        break;
                    }
                    case LOADING:{
                        progressBar.setVisibility(View.VISIBLE);
                        break;
                    }
                    case ERROR:{
                        progressBar.setVisibility(View.GONE);
                        handleError(courseAttemptResource.getException(), R.string.testpress_error_loading_attempts);
                        retryButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                progressBar.setVisibility(View.VISIBLE);
                                emptyView.setVisibility(View.GONE);
                                createContentAttempt();
                            }
                        });
                        break;
                    }
                }
            }
        });
    }

    private void showFragment() {
        progressBar.setVisibility(View.GONE);
        fragmentContainer.setVisibility(View.VISIBLE);
    }

    private void handleCourseAttemptSuccess(CourseAttempt courseAttempt) {
        this.courseAttempt = courseAttempt;
        saveCourseAttemptInDB(courseAttempt, true);
        Attempt attempt = courseAttempt.getRawAssessment();
        if (attempt.getState().equals("Running") && attempt.getRemainingTime().equals("0:00:00")) {
            attempt.setRemainingTime(exam.getDuration());
        }
        handleSuccessAttempt(attempt);
    }

    private void handleSuccessAttempt(Attempt attempt) {
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
            TestpressSession session = TestpressSdk.getTestpressSession(TestActivity.this);
            Assert.assertNotNull("TestpressSession must not be null.", session);
            if (courseAttempt == null) {
                TestpressExam.showAttemptReport(TestActivity.this, exam, attempt, session);
            } else {
                TestpressExam.showCourseAttemptReport(TestActivity.this, exam, courseAttempt, session);
            }
        }
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
        examViewModel.checkPermission(courseContent.getId());
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
        examViewModel.fetchLanguages(exam.getSlug());
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
                startExam(false);
            } else {
                startExam(true);
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
                            showResumeDisabledWarningAlertOrStartExam();
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

    private void showResumeDisabledWarningAlertOrStartExam() {
        if (exam.isAttemptResumeDisabled()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.TestpressAppCompatAlertDialogStyle);
            builder.setTitle(R.string.exam_resume_disable_warning_title);
            builder.setMessage(R.string.exam_resume_disable_warning_description);
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    startExam(false);
                }
            });
            builder.setNegativeButton("Cancel", null);
            AlertDialog dialog = builder.create();
            dialog.show();
        } else {
            startExam(false);
        }
    }

    private void endExam() {
        progressBar.setVisibility(View.VISIBLE);
        if (courseContent != null){
            examViewModel.endContentAttempt(courseAttempt.getEndAttemptUrl());
        } else {
            examViewModel.endAttempt(attempt.getEndUrlFrag());
        }
    }

    private void saveCourseAttemptInDB(CourseAttempt courseAttempt, boolean createdNewAttempt) {
        courseAttempt.saveInDB(this, courseContent);
        if (createdNewAttempt) {
            courseContent.setAttemptsCount(courseContent.getAttemptsCount() + 1);
            ContentDao contentDao = TestpressSDKDatabase.getContentDao(this);
            contentDao.insertOrReplace(courseContent);
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
        if (resumeExam){
            examViewModel.startAttempt(attempt.getStartUrlFrag());
        } else {
            if (courseContent != null){
                createContentAttempt();
            } else {
                createAttempt();
            }
        }
        examDetailsContainer.setVisibility(View.GONE);
    }

    private void createContentAttempt() {
        HashMap<String, Object> data = new HashMap<>();

        if (isPartialQuestions) {
            data.put(IS_PARTIAL, true);
        }
        String attemptsUrl = courseContent.getAttemptsUrl();
        attemptsUrl = attemptsUrl.replace("v2.3", "v2.2.1");
        examViewModel.createContentAttempt(attemptsUrl, data);
    }

    private void createAttempt() {
        HashMap<String, Object> data = new HashMap<>();
        String accessCode = getIntent().getStringExtra(ACCESS_CODE);
        if (accessCode != null) {
            data.put(ACCESS_CODE, accessCode);
        }
        if (isPartialQuestions) {
            data.put(IS_PARTIAL, true);
        }
        examViewModel.createAttempt(exam.getAttemptsFrag(), data);
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
