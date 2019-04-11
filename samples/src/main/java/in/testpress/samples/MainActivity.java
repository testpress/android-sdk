package in.testpress.samples;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.facebook.FacebookSdk;

import in.testpress.core.TestpressSDKDatabase;
import in.testpress.core.TestpressSdk;
import in.testpress.exam.TestpressExam;
import in.testpress.exam.network.TestpressExamApiClient;
import in.testpress.samples.core.TestpressCoreSampleActivity;
import in.testpress.samples.exam.ExamSampleActivity;
import in.testpress.samples.course.CourseSampleActivity;
import in.testpress.samples.store.StoreSampleActivity;

import static in.testpress.samples.core.TestpressCoreSampleActivity.AUTHENTICATE_REQUEST_CODE;

public class MainActivity extends BaseToolBarActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.core).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, TestpressCoreSampleActivity.class);
                startActivity(intent);
            }
        });
        findViewById(R.id.exam).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ExamSampleActivity.class);
                startActivity(intent);
            }
        });
        findViewById(R.id.course).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, CourseSampleActivity.class);
                startActivity(intent);
            }
        });
        findViewById(R.id.analytics).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAnalytics();
            }
        });
        findViewById(R.id.store).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, StoreSampleActivity.class);
                startActivity(intent);
            }
        });
        findViewById(R.id.logout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TestpressSdk.clearActiveSession(getApplicationContext());
                TestpressSDKDatabase.clearDatabase(getApplicationContext());
                Toast.makeText(getApplicationContext(), "Cleared local database", Toast.LENGTH_LONG);
            }
        });
    }

    private void showAnalytics() {
        if (TestpressSdk.hasActiveSession(MainActivity.this)) {
            //noinspection ConstantConditions
            TestpressExam.showAnalytics(
                    MainActivity.this,
                    TestpressExamApiClient.SUBJECT_ANALYTICS_PATH,
                    TestpressSdk.getTestpressSession(MainActivity.this)
            );
        } else {
            Intent intent = new Intent(MainActivity.this, TestpressCoreSampleActivity.class);
            startActivityForResult(intent, AUTHENTICATE_REQUEST_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AUTHENTICATE_REQUEST_CODE && resultCode == RESULT_OK) {
            showAnalytics();
        }
    }

}
