package in.testpress.samples.course;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.view.View;

import in.testpress.core.TestpressSdk;
import in.testpress.core.TestpressSession;
import in.testpress.course.TestpressCourse;
import in.testpress.samples.BaseToolBarActivity;
import in.testpress.samples.R;
import in.testpress.samples.core.TestpressCoreSampleActivity;
import in.testpress.util.ViewUtils;

import static in.testpress.core.TestpressSdk.COURSE_CHAPTER_REQUEST_CODE;
import static in.testpress.core.TestpressSdk.COURSE_CONTENT_DETAIL_REQUEST_CODE;
import static in.testpress.core.TestpressSdk.COURSE_CONTENT_LIST_REQUEST_CODE;
import static in.testpress.course.TestpressCourse.CHAPTER_SLUG;
import static in.testpress.course.TestpressCourse.COURSE_ID;
import static in.testpress.samples.core.TestpressCoreSampleActivity.AUTHENTICATE_REQUEST_CODE;

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
        setClickListenerToShowInputDialogBox(R.id.course_detail, "Enter Course Id");
        setClickListenerToShowInputDialogBox(R.id.chapter_contents, "Enter Chapter Slug");
        setClickListenerToShowInputDialogBox(R.id.content_detail, "Enter Content Id");
        findViewById(R.id.fragment_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CourseSampleActivity.this, NavigationDrawerActivity.class);
                startActivity(intent);
            }
        });
    }
    
    void setClickListenerToShowInputDialogBox(@IdRes final int viewId, final String title) {
        findViewById(viewId).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ViewUtils.showInputDialogBox(CourseSampleActivity.this, title,
                        new ViewUtils.OnInputCompletedListener() {
                            @Override
                            public void onInputComplete(String inputText) {
                                text = inputText;
                                showSDK(viewId);
                            }
                        });
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
                case R.id.course_detail:
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
            TestpressSdk.setTestpressSession(this, session);
            switch (clickedButtonId) {
                case R.id.simple_course:
                case R.id.gamified_course:
                    TestpressCourse.show(this, session);
                    break;
                case R.id.leaderboard:
                    TestpressCourse.showLeaderboard(this, session);
                    break;
                case R.id.course_detail:
                    TestpressCourse.showChapters(this, null, Long.parseLong(text), session);
                    break;
                case R.id.chapter_contents:
                    TestpressCourse.showChapterContents(this, text, session);
                    break;
                case R.id.content_detail:
                    TestpressCourse.showContentDetail(this, text, session);
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
                            case COURSE_CONTENT_LIST_REQUEST_CODE:
                            case COURSE_CHAPTER_REQUEST_CODE:
                                int courseId = data.getIntExtra(COURSE_ID, 0);
                                String chapterSlug = data.getStringExtra(CHAPTER_SLUG);
                                if (chapterSlug != null) {
                                    ViewUtils.toast(this,
                                            "User pressed home button chapterSlug:" + chapterSlug);
                                    TestpressCourse.showChapterContents(this, chapterSlug, session);
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
