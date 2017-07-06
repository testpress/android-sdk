package in.testpress.exam.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import in.testpress.exam.R;
import in.testpress.exam.models.Attempt;
import in.testpress.exam.models.Exam;

public class ReviewStatsActivity extends AppCompatActivity {

    static final String PARAM_PREVIOUS_ACTIVITY = "previousActivity";
    static final String PARAM_EXAM = "exam";
    static final String PARAM_ATTEMPT = "attempt";

    public static Intent createIntent(Activity activity, Exam exam, Attempt attempt) {
        Intent intent = new Intent(activity, ReviewStatsActivity.class);
        intent.putExtra(PARAM_PREVIOUS_ACTIVITY, activity.getClass().getName());
        intent.putExtra(PARAM_EXAM, exam);
        intent.putExtra(PARAM_ATTEMPT, attempt);
        return intent;
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.testpress_container_layout_without_toolbar);
        ReviewStatsFragment fragment = new ReviewStatsFragment();
        fragment.setArguments(getIntent().getExtras());
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commitAllowingStateLoss();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == CarouselFragment.TEST_TAKEN_REQUEST_CODE)) {
            String previousActivity = getIntent().getStringExtra(PARAM_PREVIOUS_ACTIVITY);
            if((previousActivity != null) && previousActivity.equals(TestActivity.class.getName())) {
                // OnBackPressed go to history
                setResult(RESULT_OK);
                finish();
            } else {
                finish();
            }
        }
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
