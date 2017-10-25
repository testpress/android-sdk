package in.testpress.samples.course;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.Toast;

import in.testpress.core.TestpressSdk;
import in.testpress.core.TestpressSession;
import in.testpress.course.TestpressCourse;
import in.testpress.samples.BaseToolBarActivity;
import in.testpress.samples.R;
import in.testpress.samples.core.TestpressCoreSampleActivity;
import in.testpress.samples.util.ViewUtils;

import static in.testpress.core.TestpressSdk.COURSE_CHAPTER_REQUEST_CODE;
import static in.testpress.core.TestpressSdk.COURSE_CONTENT_DETAIL_REQUEST_CODE;
import static in.testpress.core.TestpressSdk.COURSE_CONTENT_LIST_REQUEST_CODE;
import static in.testpress.course.TestpressCourse.CHAPTER_URL;
import static in.testpress.course.TestpressCourse.COURSE_ID;
import static in.testpress.course.TestpressCourse.PARENT_ID;
import static in.testpress.samples.core.TestpressCoreSampleActivity.AUTHENTICATE_REQUEST_CODE;

public class CourseSampleActivity extends BaseToolBarActivity {

    private int selectedItem;
    private String contentId;
    private TestpressSession session;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_in);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        findViewById(R.id.simple_course).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSDK(R.id.simple_course);
            }
        });
        findViewById(R.id.gamified_course).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSDK(R.id.gamified_course);
            }
        });
        findViewById(R.id.leaderboard).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSDK(R.id.leaderboard);
            }
        });
        findViewById(R.id.content_detail).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ViewUtils.showInputDialogBox(CourseSampleActivity.this, "Enter Course Id",
                        new ViewUtils.OnInputCompletedListener() {
                            @Override
                            public void onInputComplete(String inputText) {
                                contentId = inputText;
                                showSDK(R.id.content_detail);
                            }
                });
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
    private void showSDK(int clickedButtonId) {
        selectedItem = clickedButtonId;
        if (TestpressSdk.hasActiveSession(this)) {
            session = TestpressSdk.getTestpressSession(this);
            switch (clickedButtonId) {
                case R.id.simple_course:
                    session.getInstituteSettings()
                            .setCommentsVotingEnabled(false)
                            .setCoursesFrontend(true)
                            .setCoursesGamificationEnabled(false);
                    break;
                case R.id.gamified_course:
                case R.id.leaderboard:
                case R.id.content_detail:
                    session.getInstituteSettings()
                            .setCommentsVotingEnabled(false)
                            .setCoursesFrontend(true)
                            .setCoursesGamificationEnabled(true);
                    break;
            }
            TestpressSdk.setTestpressSession(this, session);
            switch (clickedButtonId) {
                case R.id.simple_course:
                case R.id.gamified_course:
                    TestpressCourse.show(this, session);
                    break;
                case R.id.leaderboard:
                    TestpressCourse.showLeaderboard(this, session);
                    break;
                case R.id.content_detail:
                    TestpressCourse.showContentDetail(this, contentId, session);
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
        switch (requestCode) {
            case AUTHENTICATE_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    showSDK(selectedItem);
                }
                break;
            case COURSE_CONTENT_DETAIL_REQUEST_CODE:
            case COURSE_CONTENT_LIST_REQUEST_CODE:
            case COURSE_CHAPTER_REQUEST_CODE:
                if (resultCode == RESULT_CANCELED) {
                    if (data.getBooleanExtra(TestpressSdk.ACTION_PRESSED_HOME, false)) {
                        switch (requestCode) {
                            case COURSE_CONTENT_DETAIL_REQUEST_CODE:
                                String chapterUrl = data.getStringExtra(CHAPTER_URL);
                                ViewUtils.toast(this, "User pressed home button " + chapterUrl);
                                if (chapterUrl != null) {
                                    TestpressCourse.showContents(this, chapterUrl, session);
                                }
                                break;
                            case COURSE_CONTENT_LIST_REQUEST_CODE:
                            case COURSE_CHAPTER_REQUEST_CODE:
                                String courseId = data.getStringExtra(COURSE_ID);
                                String parentId = data.getStringExtra(PARENT_ID);
                                ViewUtils.toast(this,
                                        "User pressed home button " + courseId + " - " + parentId);

                                if (courseId != null) {
                                    TestpressCourse.showChapters(this, courseId, parentId, session);
                                }
                                break;
                        }
                    } else {
                        ViewUtils.toast(this, "User pressed back button");
                    }
                }
                break;
        }
    }

}
