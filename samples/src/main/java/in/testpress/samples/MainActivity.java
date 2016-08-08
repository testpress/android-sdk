package in.testpress.samples;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import in.testpress.samples.core.LoginActivity;
import in.testpress.samples.exam.ExamSampleActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void core(View view) {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    public void exam(View view) {
        Intent intent = new Intent(this, ExamSampleActivity.class);
        startActivity(intent);
    }
}
