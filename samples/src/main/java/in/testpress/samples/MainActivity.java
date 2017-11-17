package in.testpress.samples;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.facebook.FacebookSdk;
import com.facebook.stetho.Stetho;

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
        Stetho.initializeWithDefaults(this);
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
