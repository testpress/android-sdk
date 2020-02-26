package in.testpress.samples.exam;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.view.View;
import android.widget.Toast;

import in.testpress.core.TestpressSdk;
import in.testpress.core.TestpressSession;
import in.testpress.exam.TestpressExam;
import in.testpress.samples.BaseToolBarActivity;
import in.testpress.samples.R;
import in.testpress.samples.core.TestpressCoreSampleActivity;
import in.testpress.ui.UserDevicesActivity;
import in.testpress.util.ViewUtils;

import static in.testpress.exam.ui.CarouselFragment.TEST_TAKEN_REQUEST_CODE;
import static in.testpress.samples.core.TestpressCoreSampleActivity.AUTHENTICATE_REQUEST_CODE;

public class ExamSampleActivity extends BaseToolBarActivity {

    private int selectedItem;
    private String examSlug;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_exams_as);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        findViewById(R.id.start_exam).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getExamSlug(R.id.start_exam);
            }
        });
        findViewById(R.id.attempt_state).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getExamSlug(R.id.attempt_state);
            }
        });
        findViewById(R.id.access_code).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSDK(R.id.access_code);
            }
        });
        findViewById(R.id.exam_list).setOnClickListener(new View.OnClickListener() {
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
        findViewById(R.id.bookmarks).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSDK(view.getId());
            }
        });
        findViewById(R.id.login_activity_button).setOnClickListener(new View.OnClickListener() {
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

    private void showSDK(int clickedButtonId) {
        selectedItem = clickedButtonId;
        if (TestpressSdk.hasActiveSession(this)) {
            TestpressSession session = TestpressSdk.getTestpressSession(this);
            //noinspection ConstantConditions
            session.getInstituteSettings()
                    .setBookmarksEnabled(true)
                    .setCommentsVotingEnabled(false)
                    .setCoursesFrontend(false)
                    .setCoursesGamificationEnabled(false);
            TestpressSdk.setTestpressSession(this, session);
            switch (clickedButtonId) {
                case R.id.start_exam:
                    TestpressExam.startExam(this, examSlug, session);
                    break;
                case R.id.attempt_state:
                    TestpressExam.showExamAttemptedState(this, examSlug, session);
                    break;
                case R.id.access_code:
                    TestpressExam.showExamsForAccessCode(this, session);
                    break;
                case R.id.exam_list:
                    TestpressExam.show(this, session);
                    break;
                case R.id.bookmarks:
                    TestpressExam.showBookmarks(this, session);
                    break;
                case R.id.login_activity_button:
                    Intent intent = new Intent(this, UserDevicesActivity.class);
                    this.startActivity(intent);
                    break;
                default:
                    TestpressExam.showCategories(this, false, session);
                    break;
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
        } else if (requestCode == TEST_TAKEN_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "User attempted the exam", Toast.LENGTH_SHORT).show();
            } else if (resultCode == RESULT_CANCELED) {
                if (data.getBooleanExtra(TestpressSdk.ACTION_PRESSED_HOME, false)) {
                    Toast.makeText(this, "User pressed home button", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "User pressed back button", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    void getExamSlug(final int clickedButtonId) {
        ViewUtils.showInputDialogBox(this, "Enter Exam Slug",
                new ViewUtils.OnInputCompletedListener() {
                    @Override
                    public void onInputComplete(String inputText) {
                        examSlug = inputText;
                        showSDK(clickedButtonId);
                    }
        });
    }

}
