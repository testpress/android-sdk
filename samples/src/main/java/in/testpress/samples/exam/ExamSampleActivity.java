package in.testpress.samples.exam;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import in.testpress.core.TestpressSdk;
import in.testpress.exam.TestpressExam;
import in.testpress.samples.BaseToolBarActivity;
import in.testpress.samples.R;
import in.testpress.samples.core.TestpressCoreSampleActivity;

import static in.testpress.samples.core.TestpressCoreSampleActivity.AUTHENTICATE_REQUEST_CODE;

public class ExamSampleActivity extends BaseToolBarActivity {

    private int selectedItem;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_exams_as);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        findViewById(R.id.exam).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSDK(view.getId());
            }
        });
        findViewById(R.id.categories).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSDK(view.getId());
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

    }

    @SuppressWarnings("ConstantConditions")
    private void showSDK(int id) {
        selectedItem = id;
        if (TestpressSdk.hasActiveSession(this)) {
            if (id == R.id.exam) {
                TestpressExam.show(this, TestpressSdk.getTestpressSession(this));
            } else {
                TestpressExam.showCategories(this, TestpressSdk.getTestpressSession(this));
            }
        } else {
            Intent intent = new Intent(this, TestpressCoreSampleActivity.class);
            startActivityForResult(intent, AUTHENTICATE_REQUEST_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AUTHENTICATE_REQUEST_CODE && resultCode == RESULT_OK) {
            showSDK(selectedItem);
        }
    }

}
