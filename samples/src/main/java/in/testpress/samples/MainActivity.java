package in.testpress.samples;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.facebook.FacebookSdk;

import in.testpress.samples.core.TestpressCoreSampleActivity;
import in.testpress.samples.exam.ExamSampleActivity;
import in.testpress.samples.course.CourseSampleActivity;

public class MainActivity extends BaseToolBarActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FacebookSdk.sdkInitialize(getApplicationContext());
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
        findViewById(R.id.gamified_exam).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, CourseSampleActivity.class);
                startActivity(intent);
            }
        });
    }

}
