package in.testpress.exam.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.IOException;

import in.testpress.exam.R;
import in.testpress.exam.models.Attempt;
import in.testpress.exam.models.Exam;
import in.testpress.exam.network.TestpressExamApiClient;

import in.testpress.exam.util.ThrowableLoader;
import in.testpress.util.CircularProgressDrawable;

/**
 * Activity of Test Engine
 */
public class TestActivity extends FragmentActivity implements LoaderManager.LoaderCallbacks<Attempt>  {

    static final String PARAM_EXAM = "exam";
    static final String PARAM_ATTEMPT = "attempt";
    static final String PARAM_ACTION = "action";
    static final String PARAM_VALUE_ACTION_END = "end";
    private TestpressExamApiClient apiClient;
    private Exam exam;
    private Attempt attempt;
    private RelativeLayout progressBar;
    private LinearLayout examDetailsContainer;
    private LinearLayout fragmentContainer;
    private View emptyView;
    private TextView emptyTitleView;
    private TextView emptyDescView;
    private Button retryButton;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.testpress_activity_test);
        examDetailsContainer = (LinearLayout) findViewById(R.id.exam_details);
        fragmentContainer = (LinearLayout) findViewById(R.id.fragment_container);
        progressBar = (RelativeLayout) findViewById(R.id.pb_loading);
        emptyView = findViewById(R.id.empty_container);
        emptyTitleView = (TextView) findViewById(R.id.empty_title);
        emptyDescView = (TextView) findViewById(R.id.empty_description);
        retryButton = (Button) findViewById(R.id.retry_button);
        Button startExam = (Button) findViewById(R.id.start_exam);
        LinearLayout attemptActions = (LinearLayout) findViewById(R.id.attempt_actions);
        TextView examTitle = (TextView) findViewById(R.id.exam_title);
        TextView numberOfQuestions = (TextView) findViewById(R.id.number_of_questions);
        TextView examDuration = (TextView) findViewById(R.id.exam_duration);
        TextView markPerQuestion = (TextView) findViewById(R.id.mark_per_question);
        TextView negativeMarks = (TextView) findViewById(R.id.negative_marks);
        LinearLayout description = (LinearLayout) findViewById(R.id.description);
        TextView descriptionContent = (TextView) findViewById(R.id.descriptionContent);
        findViewById(R.id.exam_back_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        findViewById(R.id.start_exam).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSupportLoaderManager().initLoader(0, null, TestActivity.this);
                progressBar.setVisibility(View.VISIBLE);
            }
        });
        findViewById(R.id.end_exam).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                endExam();
            }
        });
        findViewById(R.id.resume_exam).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSupportLoaderManager().initLoader(1, null, TestActivity.this);
                progressBar.setVisibility(View.VISIBLE);
            }
        });
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            DisplayMetrics metrics = getResources().getDisplayMetrics();
            float pixelWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, metrics);
            ((ProgressBar) findViewById(R.id.progress_bar)).setIndeterminateDrawable(
                    new CircularProgressDrawable(getResources().getColor(
                            R.color.testpress_color_primary), pixelWidth));
        }
        apiClient = new TestpressExamApiClient();
        final Intent intent = getIntent();
        Bundle data = intent.getExtras();
        exam = data.getParcelable(PARAM_EXAM);
        attempt = data.getParcelable(PARAM_ATTEMPT);
        if (attempt != null) {
            attemptActions.setVisibility(View.VISIBLE);
        } else {
            startExam.setVisibility(View.VISIBLE);
        }
        examTitle.setText(exam.getTitle());
        numberOfQuestions.setText(exam.getNumberOfQuestions().toString());
        examDuration.setText(exam.getDuration());
        markPerQuestion.setText(exam.getMarkPerQuestion());
        negativeMarks.setText(exam.getNegativeMarks());
        if ((exam.getDescription() != null) && !exam.getDescription().trim().isEmpty()) {
            description.setVisibility(View.VISIBLE);
            descriptionContent.setText(exam.getDescription());
        }
        String action = data.getString(PARAM_ACTION);
        if (action != null && action.equals(PARAM_VALUE_ACTION_END)) {
            endExam();
        }
    }

    private void endExam() {
        getSupportLoaderManager().initLoader(2, null, TestActivity.this);
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public Loader<Attempt> onCreateLoader(final int id, final Bundle args) {
        return new ThrowableLoader<Attempt>(TestActivity.this, attempt) {
            @Override
            public Attempt loadData() throws Exception {
                switch (id) {
                    case 0:
                        return apiClient.createAttempt(exam.getAttemptsFrag());
                    case 1:
                        return apiClient.startAttempt(attempt.getStartUrlFrag());
                    case 2:
                        return apiClient.endAttempt(attempt.getEndUrlFrag());
                    default:
                        return null;
                }
            }
        };
    }

    public void onLoadFinished(final Loader<Attempt> loader, final Attempt attempt) {
        progressBar.setVisibility(View.GONE);
        examDetailsContainer.setVisibility(View.GONE);
        Exception exception = ((ThrowableLoader<Attempt>) loader).clearException();
        if(exception == null) {
            fragmentContainer.setVisibility(View.VISIBLE);
            if (attempt.getState().equals("Running")) {
                TestFragment testFragment = new TestFragment();
                Bundle bundle = new Bundle();
                bundle.putParcelable(TestFragment.PARAM_ATTEMPT, attempt);
                bundle.putParcelable(TestFragment.PARAM_EXAM, exam);
                testFragment.setArguments(bundle);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, testFragment).commitAllowingStateLoss();
            } else {
                startActivityForResult(ReviewActivity.createIntent(this, exam, attempt),
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
            if((exception.getMessage() != null) && (exception.getMessage()).equals("403 FORBIDDEN")) {
                setEmptyText(R.string.testpress_authentication_failed, R.string.testpress_please_login);
            } else if (exception.getCause() instanceof IOException) {
                setEmptyText(R.string.testpress_network_error, R.string.testpress_no_internet_try_again);
            } else {
                setEmptyText(R.string.testpress_error_loading_questions,
                        R.string.testpress_some_thing_went_wrong_try_again);
            }
        }
    }

    @Override
    public void onLoaderReset(final Loader<Attempt> loader) {
    }

    @Override
    public void onBackPressed() {
        TestFragment testFragment = null;
        try {
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

    protected void setEmptyText(final int title, final int description) {
        emptyView.setVisibility(View.VISIBLE);
        emptyTitleView.setText(title);
        emptyDescView.setText(description);
        retryButton.setVisibility(View.VISIBLE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if ((requestCode == CarouselFragment.TEST_TAKEN_REQUEST_CODE) && (Activity.RESULT_OK == resultCode)) {
            setResult(resultCode);
            finish();
        }
    }

}
