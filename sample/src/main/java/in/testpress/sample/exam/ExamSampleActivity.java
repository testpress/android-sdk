package in.testpress.sample.exam;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import in.testpress.exam.TestpressExam;
import in.testpress.sample.R;

public class ExamSampleActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exam_sample);
    }

    public void showExamsInFragment(View view) {
        Intent intent = new Intent(this, NavigationDrawerActivity.class);
        startActivity(intent);
    }

    public void showNewActivity(View view) {
        TestpressExam.show(this, "http://demo.testpress.in", "testpress", "demo");
    }
}
