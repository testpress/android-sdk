package in.testpress.samples.course;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.view.View;

import in.testpress.core.TestpressSdk;
import in.testpress.core.TestpressSession;
import in.testpress.course.TestpressCourse;
import in.testpress.course.ui.CustomTestGenerationActivity;
import in.testpress.course.ui.OfflineExamListActivity;
import in.testpress.samples.BaseToolBarActivity;
import in.testpress.samples.R;
import in.testpress.samples.core.TestpressCoreSampleActivity;
import in.testpress.ui.WebViewWithSSOActivity;
import in.testpress.ui.DiscussionActivity;
//import in.testpress.ui.WebViewWithSSOActivity;
import in.testpress.util.Permission;
import in.testpress.util.ViewUtils;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;

import static in.testpress.core.TestpressSdk.COURSE_CHAPTER_REQUEST_CODE;
import static in.testpress.core.TestpressSdk.COURSE_CONTENT_DETAIL_REQUEST_CODE;
import static in.testpress.core.TestpressSdk.COURSE_CONTENT_LIST_REQUEST_CODE;
import static in.testpress.course.TestpressCourse.CHAPTER_URL;
import static in.testpress.course.TestpressCourse.COURSE_ID;
import static in.testpress.samples.core.TestpressCoreSampleActivity.AUTHENTICATE_REQUEST_CODE;
import static in.testpress.util.extension.ActivityKt.performActionIfPermissionsGranted;

import java.util.ArrayList;
import java.util.List;

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
        findViewById(R.id.discussions_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TestpressSdk.hasActiveSession(CourseSampleActivity.this)) {
                    Intent intent = DiscussionActivity.createIntent(CourseSampleActivity.this);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(CourseSampleActivity.this, TestpressCoreSampleActivity.class);
                    startActivity(intent);
                }
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
        findViewById(R.id.downloads).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSDK(R.id.downloads);
            }
        });
        findViewById(R.id.course_contents).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ViewUtils.showInputDialogBox(CourseSampleActivity.this, "Enter Course ID",
                        new ViewUtils.OnInputCompletedListener() {
                            @Override
                            public void onInputComplete(String inputText) {
                                text = inputText;
                                showSDK(R.id.course_contents);
                            }
                        });
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
        findViewById(R.id.bookmarks).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSDK(R.id.bookmarks);
            }
        });

        findViewById(R.id.premission_check_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                performActionIfPermissionsGranted(CourseSampleActivity.this,
                        Permission.Companion.getAllPermissions(),
                        new Function0<Unit>() {
                            @Override
                            public Unit invoke() {
                                ViewUtils.toast(CourseSampleActivity.this,"Permission Granted");
                                return null;
                            }
                        });
            }
        });

        findViewById(R.id.web_view_with_sso).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ViewUtils.showInputDialogBox(CourseSampleActivity.this, "Enter Url",
                        new ViewUtils.OnInputCompletedListener() {
                            @Override
                            public void onInputComplete(String inputText) {
                                text = inputText;
                                launchWebViewWithSSOActivity(text);
                            }
                        });
            }
        });

        findViewById(R.id.custom_test_generation).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchCustomTestGenerationActivity();
            }
        });

        findViewById(R.id.offline_exam_download).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openOfflineExamActivity();
            }
        });

        findViewById(R.id.sample_offline_exam_download).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openSampleOfflineExamActivity();
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
                case R.id.course_contents:
                case R.id.chapter_contents:
                case R.id.bookmarks:
                case R.id.content_detail:
                    session.getInstituteSettings()
                            .setBookmarksEnabled(true)
                            .setCommentsVotingEnabled(false)
                            .setCoursesFrontend(true)
                            .setCoursesGamificationEnabled(true);
                    break;
            }
            session.getInstituteSettings().setDisplayUserEmailOnVideo(true);
            session.getInstituteSettings().setVideoDownloadEnabled(true);
            session.getInstituteSettings().setScreenshotDisabled(false);
            session.getInstituteSettings().setDisableStudentAnalytics(false);
            session.getInstituteSettings().setEnableCustomTest(true);
            session.getInstituteSettings().setStoreLabel("Available Courses");
            session.getInstituteSettings().setStoreEnabled(true);
            session.getInstituteSettings().setDisableStoreInApp(false);
            session.getInstituteSettings().setEnableOfflineExam(true);
            session.getInstituteSettings().setShowOfflineExamEndingAlert(true);
            session.getInstituteSettings().setAppToolbarLogo("https://media.testpress.in/institute/elixir/institutes/elixir-neet-pg-preparation-app/9571158ee51b4c36b9dd14b9dae17452.png");
            session.getInstituteSettings().setAndroidSentryDns("https://186f9948d5294b4c9c03aa9d2a11c982@sentry.testpress.in/3");

            TestpressSdk.setTestpressSession(this, session);
            switch (clickedButtonId) {
                case R.id.simple_course:
                case R.id.gamified_course:
                    TestpressCourse.show(this, session);
                    break;
                case R.id.leaderboard:
                    TestpressCourse.showLeaderboard(this, session);
                    break;
                case R.id.course_contents:
                    TestpressCourse.showChapters(this, "Course Detail",
                            Integer.parseInt(text), session);
                    break;
                case R.id.chapter_contents:
                    String url = session.getInstituteSettings().getBaseUrl() +
                            "/api/v2.2.1/chapters/" + text + "/";

                    TestpressCourse.showChapterContents(this, url, session);
                    break;
                case R.id.bookmarks:
                    TestpressCourse.showBookmarks(this, session);
                    break;
                case R.id.content_detail:
                    TestpressCourse.showContentDetail(this, text, session);
                    break;
                case R.id.downloads:
                    TestpressCourse.showDownloads(this, session);
                    break;
            }
        } else {
            Intent intent = new Intent(this, TestpressCoreSampleActivity.class);
            startActivityForResult(intent, AUTHENTICATE_REQUEST_CODE);
        }
    }

    private void launchWebViewWithSSOActivity(String urlPath) {
        startActivity(WebViewWithSSOActivity.Companion.createIntent(
                        CourseSampleActivity.this,
                        "Test WebView",
                        session.getInstituteSettings().getDomainUrl() + urlPath,
                        true,
                        WebViewWithSSOActivity.class
                )
        );
    }

    private void launchCustomTestGenerationActivity() {
        ViewUtils.showInputDialogBox(CourseSampleActivity.this, "Enter Chapter ID",
                new ViewUtils.OnInputCompletedListener() {
                    @Override
                    public void onInputComplete(String inputText) {
                        text = inputText;
                        List<Permission> permissions = new ArrayList<>();
                        permissions.add(Permission.CAMERA);
                        permissions.add(Permission.MICROPHONE);
                        performActionIfPermissionsGranted(CourseSampleActivity.this,
                                permissions,
                                new Function0<Unit>() {
                                    @Override
                                    public Unit invoke() {
                                        startActivity(
                                                CustomTestGenerationActivity.Companion.createIntent(
                                                        CourseSampleActivity.this,
                                                        "Custom Module",
                                                        session.getInstituteSettings().getDomainUrl()+"/courses/custom_test_generation/?course_id="+inputText+"&testpress_app=android",
                                                        true,
                                                        CustomTestGenerationActivity.class
                                                )
                                        );
                                        return null;
                                    }
                                });
                    }
                });
    }

    private void openOfflineExamActivity() {
        startActivity(new Intent(this, OfflineExamListActivity.class));
    }

    private void openSampleOfflineExamActivity() {
        startActivity(new Intent(this,OfflineExamSampleActivity.class));
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
