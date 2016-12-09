package in.testpress.samples.course;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import in.testpress.core.TestpressSdk;
import in.testpress.course.TestpressCourse;
import in.testpress.samples.BaseToolBarActivity;
import in.testpress.samples.R;
import in.testpress.samples.core.TestpressCoreSampleActivity;

import static in.testpress.samples.core.TestpressCoreSampleActivity.AUTHENTICATE_REQUEST_CODE;

public class CourseSampleActivity extends BaseToolBarActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_in);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        findViewById(R.id.new_activity_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TestpressSdk.hasActiveSession(CourseSampleActivity.this)) {
                    displayExams();
                } else {
                    Intent intent = new Intent(CourseSampleActivity.this, TestpressCoreSampleActivity.class);
                    startActivityForResult(intent, AUTHENTICATE_REQUEST_CODE);
                }
            }
        });
        findViewById(R.id.fragment_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CourseSampleActivity.this, NavigationDrawerActivity.class);
                startActivity(intent);
            }
        });
    }

    private void displayExams() {
        //noinspection ConstantConditions
        TestpressCourse.show(this, TestpressSdk.getTestpressSession(this));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AUTHENTICATE_REQUEST_CODE && resultCode == RESULT_OK) {
            displayExams();
        }
    }

}
