package in.testpress.exam.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.IOException;

import in.testpress.core.TestpressException;
import in.testpress.exam.R;
import in.testpress.exam.models.Attempt;
import in.testpress.exam.models.Exam;
import in.testpress.exam.network.TestpressExamApiClient;

import in.testpress.util.FontUtils;
import in.testpress.util.ThrowableLoader;
import in.testpress.ui.BaseToolBarActivity;
import in.testpress.util.UIUtils;
import in.testpress.network.RetrofitCall;
import retrofit2.Response;

/**
 * Activity of Test Engine
 */
public class TestActivity extends BaseToolBarActivity implements LoaderManager.LoaderCallbacks<Attempt>  {

    static final String PARAM_EXAM = "exam";
    static final String PARAM_ATTEMPT = "attempt";
    static final String PARAM_ACTION = "action";
    static final String PARAM_VALUE_ACTION_END = "end";
    private TestpressExamApiClient apiClient;
    private Exam exam;
    private Attempt attempt;
    private RelativeLayout progressBar;
    private View examDetailsContainer;
    private LinearLayout fragmentContainer;
    private View emptyView;
    private TextView emptyTitleView;
    private TextView emptyDescView;
    private Button retryButton;

    @SuppressWarnings("ConstantConditions")
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.testpress_activity_test);
        examDetailsContainer = findViewById(R.id.exam_details);
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
        UIUtils.setIndeterminateDrawable(this, findViewById(R.id.progress_bar), 4);
        apiClient = new TestpressExamApiClient(this);
        final Intent intent = getIntent();
        Bundle data = intent.getExtras();
        exam = data.getParcelable(PARAM_EXAM);
        attempt = data.getParcelable(PARAM_ATTEMPT);
        if (attempt != null) {
            String action = data.getString(PARAM_ACTION);
            if (action != null && action.equals(PARAM_VALUE_ACTION_END)) {
                getSupportActionBar().setTitle(getString(R.string.testpress_end_exam));
                endExam();
                return;
            } else {
                getSupportActionBar().setTitle(getString(R.string.testpress_resume_exam));
                attemptActions.setVisibility(View.VISIBLE);
            }
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
            descriptionContent.setText("    " + exam.getDescription());
        }
        FontUtils.applyTestpressFont(this, (ViewGroup) findViewById(android.R.id.content));
    }

    private void endExam() {
        progressBar.setVisibility(View.VISIBLE);
        getSupportLoaderManager().initLoader(2, null, TestActivity.this);
    }

    @Override
    public Loader<Attempt> onCreateLoader(final int id, final Bundle args) {
        return new ThrowableLoader<Attempt>(TestActivity.this, attempt) {
            @Override
            public Attempt loadData() throws TestpressException {
                RetrofitCall<Attempt> call = null;
                switch (id) {
                    case 0:
                        call = apiClient.createAttempt(exam.getAttemptsFrag());
                        break;
                    case 1:
                        call = apiClient.startAttempt(attempt.getStartUrlFrag());
                        break;
                    case 2:
                        call = apiClient.endAttempt(attempt.getEndUrlFrag());
                        break;
                }
                try {
                    if (call != null) {
                        Response<Attempt> response = call.execute();
                        if (response.isSuccessful()) {
                            return response.body();
                        } else {
                            throw TestpressException.httpError(response);
                        }
                    } else {
                        throw new IllegalStateException("Invalid loader id");
                    }
                } catch (IOException e) {
                    throw TestpressException.networkError(e);
                } catch (Exception e) {
                    throw TestpressException.unexpectedError(e);
                }
            }
        };
    }

    public void onLoadFinished(final Loader<Attempt> loader, final Attempt attempt) {
        progressBar.setVisibility(View.GONE);
        examDetailsContainer.setVisibility(View.GONE);
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
            if (exception.isUnauthenticated()) {
                setEmptyText(R.string.testpress_authentication_failed, R.string.testpress_please_login);
            } else if (exception.isNetworkError()) {
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
