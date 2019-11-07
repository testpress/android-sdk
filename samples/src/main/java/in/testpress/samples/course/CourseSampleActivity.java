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
import in.testpress.samples.core.LoginActivity;
import in.testpress.util.ViewUtils;

import static in.testpress.core.TestpressSdk.COURSE_CHAPTER_REQUEST_CODE;
import static in.testpress.core.TestpressSdk.COURSE_CONTENT_DETAIL_REQUEST_CODE;
import static in.testpress.core.TestpressSdk.COURSE_CONTENT_LIST_REQUEST_CODE;
import static in.testpress.course.TestpressCourse.CHAPTER_URL;
import static in.testpress.course.TestpressCourse.COURSE_ID;
import static in.testpress.samples.core.LoginActivity.AUTHENTICATE_REQUEST_CODE;

public class CourseSampleActivity extends BaseToolBarActivity {

    private int selectedItem;
    private String text;
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
        findViewById(R.id.chapter_contents).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ViewUtils.showInputDialogBox(CourseSampleActivity.this, "Enter Chapter Slug",
                        new ViewUtils.OnInputCompletedListener() {
                            @Override
                            public void onInputComplete(String inputText) {
                                text = inputText;
                                showSDK(R.id.chapter_contents);
                            }
                        });
            }
        });
        findViewById(R.id.content_detail).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ViewUtils.showInputDialogBox(CourseSampleActivity.this, "Enter Content Id",
                        new ViewUtils.OnInputCompletedListener() {
                            @Override
                            public void onInputComplete(String inputText) {
                                text = inputText;
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
                            .setBookmarksEnabled(true)
                            .setCommentsVotingEnabled(false)
                            .setCoursesFrontend(true)
                            .setCoursesGamificationEnabled(false);
                    break;
                case R.id.gamified_course:
                case R.id.leaderboard:
                case R.id.chapter_contents:
                case R.id.content_detail:
                    session.getInstituteSettings()
                            .setBookmarksEnabled(true)
                            .setCommentsVotingEnabled(false)
                            .setCoursesFrontend(true)
                            .setCoursesGamificationEnabled(true);
                    break;
            }
            session.getInstituteSettings().setDisplayUserEmailOnVideo(true);
            session.getInstituteSettings().setScreenshotDisabled(false);
            session.getInstituteSettings().setDisableStudentAnalytics(false);

            TestpressSdk.setTestpressSession(this, session);
            switch (clickedButtonId) {
                case R.id.simple_course:
                case R.id.gamified_course:
                    TestpressCourse.show(this, session);
                    break;
                case R.id.leaderboard:
                    TestpressCourse.showLeaderboard(this, session);
                    break;
                case R.id.chapter_contents:
                    String url = session.getInstituteSettings().getBaseUrl() +
                            "/api/v2.2.1/chapters/" + text + "/";

                    TestpressCourse.showChapterContents(this, url, session);
                    break;
                case R.id.content_detail:
                    TestpressCourse.showContentDetail(this, text, session);
                    break;
            }
        } else {
            Intent intent = new Intent(this, LoginActivity.class);
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
                            case COURSE_CONTENT_LIST_REQUEST_CODE:
                            case COURSE_CHAPTER_REQUEST_CODE:
                                int courseId = data.getIntExtra(COURSE_ID, 0);
                                String chapterUrl = data.getStringExtra(CHAPTER_URL);
                                if (chapterUrl != null) {
                                    ViewUtils.toast(this,
                                            "User pressed home button chapterUrl:" + chapterUrl);
                                    TestpressCourse.showChapterContents(this, chapterUrl, session);
                                } else if (courseId != 0) {
                                    ViewUtils.toast(this,
                                            "User pressed home button courseId:" + courseId);
                                    TestpressCourse.showChapters(this, null, courseId, session);
                                } else {
                                    ViewUtils.toast(this, "User pressed home button");
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
