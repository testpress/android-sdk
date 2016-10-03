package in.testpress.exam.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import in.testpress.exam.R;
import in.testpress.exam.models.Exam;
import in.testpress.ui.BaseToolBarActivity;

public class AttemptsListActivity extends BaseToolBarActivity {

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.testpress_container_layout);
        AttemptsListFragment attemptsListFragment = new AttemptsListFragment();
        Bundle bundle = getIntent().getExtras();
        Exam exam = bundle.getParcelable("exam");
        getSupportActionBar().setTitle(exam.getTitle());
        attemptsListFragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, attemptsListFragment).commitAllowingStateLoss();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if ((requestCode == CarouselFragment.TEST_TAKEN_REQUEST_CODE) && (Activity.RESULT_OK == resultCode)) {
            setResult(resultCode);
            finish();
        }
    }
}
