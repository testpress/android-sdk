package in.testpress.samples.course;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import in.testpress.core.TestpressSdk;
import in.testpress.core.TestpressSession;
import in.testpress.course.TestpressCourse;
import in.testpress.samples.BaseToolBarActivity;
import in.testpress.samples.R;
import in.testpress.samples.core.TestpressCoreSampleActivity;

import static in.testpress.samples.core.TestpressCoreSampleActivity.AUTHENTICATE_REQUEST_CODE;

public class CourseSampleActivity extends BaseToolBarActivity {

    private int selectedItem;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_in);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        findViewById(R.id.simple_course).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayCourses(R.id.simple_course);
            }
        });
        findViewById(R.id.gamified_course).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayCourses(R.id.gamified_course);
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

    @SuppressWarnings("ConstantConditions")
    private void displayCourses(int clickedButtonId) {
        selectedItem = clickedButtonId;
        if (TestpressSdk.hasActiveSession(this)) {
            TestpressSession session = TestpressSdk.getTestpressSession(this);
            switch (clickedButtonId) {
                case R.id.simple_course:
                    session.getInstituteSettings()
                            .setCoursesFrontend(true)
                            .setCoursesGamificationEnabled(false);
                    break;
                case R.id.gamified_course:
                    session.getInstituteSettings()
                            .setCoursesFrontend(true)
                            .setCoursesGamificationEnabled(true);
                    break;
            }
            TestpressSdk.setTestpressSession(this, session);
            TestpressCourse.show(this, session);
        } else {
            Intent intent = new Intent(this, TestpressCoreSampleActivity.class);
            startActivityForResult(intent, AUTHENTICATE_REQUEST_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AUTHENTICATE_REQUEST_CODE && resultCode == RESULT_OK) {
            displayCourses(selectedItem);
        }
    }

}
