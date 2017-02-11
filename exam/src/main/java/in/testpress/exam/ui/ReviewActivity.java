package in.testpress.exam.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import in.testpress.exam.R;
import in.testpress.exam.models.Attempt;
import in.testpress.exam.models.Exam;
import in.testpress.ui.BaseToolBarActivity;

public class ReviewActivity extends BaseToolBarActivity {

    static final String PARAM_PREVIOUS_ACTIVITY = "previousActivity";
    static final String PARAM_EXAM = "exam";
    static final String PARAM_ATTEMPT = "attempt";

    public static Intent createIntent(Activity activity, Exam exam, Attempt attempt) {
        Intent intent = new Intent(activity, ReviewActivity.class);
        intent.putExtra(ReviewActivity.PARAM_PREVIOUS_ACTIVITY, activity.getClass().getName());
        intent.putExtra(ReviewActivity.PARAM_EXAM, exam);
        intent.putExtra(ReviewActivity.PARAM_ATTEMPT, attempt);
        return intent;
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.testpress_container_layout);
        Exam exam = getIntent().getParcelableExtra(PARAM_EXAM);
        Attempt attempt = getIntent().getParcelableExtra(PARAM_ATTEMPT);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, ReviewFragment.getInstance(exam, attempt))
                .commitAllowingStateLoss();
    }

    @Override
    public void onBackPressed() {
        String previousActivity = getIntent().getStringExtra(PARAM_PREVIOUS_ACTIVITY);
        if((previousActivity != null) && previousActivity.equals(TestActivity.class.getName())) {
            // OnBackPressed go to history
            setResult(RESULT_OK);
            finish();
        } else {
            super.onBackPressed();
        }
    }
}
