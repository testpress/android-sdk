package in.testpress.samples.exam;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import in.testpress.exam.TestpressExam;
import in.testpress.samples.BaseToolBarActivity;
import in.testpress.samples.R;

public class ExamSampleActivity extends BaseToolBarActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exam_sample);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        findViewById(R.id.new_activity_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TestpressExam.show(ExamSampleActivity.this, "http://demo.testpress.in", "testpress",
                        "demo");
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

}
