package in.testpress.samples.exam;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import in.testpress.core.TestpressFont;
import in.testpress.core.TestpressSdk;
import in.testpress.exam.TestpressExam;
import in.testpress.samples.BaseToolBarActivity;
import in.testpress.samples.R;
import in.testpress.samples.core.TestpressCoreSampleActivity;

public class ExamSampleActivity extends BaseToolBarActivity {

    public static final int AUTHENTICATE_REQUEST_CODE = 1111;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exam_sample);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        findViewById(R.id.new_activity_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TestpressSdk.hasActiveSession(ExamSampleActivity.this)) {
                    displayExams();
                } else {
                    Intent intent = new Intent(ExamSampleActivity.this, TestpressCoreSampleActivity.class);
                    startActivityForResult(intent, AUTHENTICATE_REQUEST_CODE);
                }
            }
        });
        findViewById(R.id.fragment_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ExamSampleActivity.this, NavigationDrawerActivity.class);
                startActivity(intent);
            }
        });
    }

    private void displayExams() {
        //noinspection ConstantConditions
        TestpressExam.show(this, TestpressSdk.getTestpressSession(this),
                new TestpressFont(TestpressFont.TestpressTypeface.SERIF));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AUTHENTICATE_REQUEST_CODE && resultCode == RESULT_OK) {
            displayExams();
        }
    }

}
