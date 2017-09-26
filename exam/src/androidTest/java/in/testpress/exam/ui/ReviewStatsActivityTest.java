package in.testpress.exam.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.IdlingResource;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.WindowManager;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import in.testpress.core.TestpressSdk;
import in.testpress.core.TestpressSession;
import in.testpress.exam.models.Exam;
import in.testpress.exam.util.ElapsedTimeIdlingResource;
import in.testpress.model.InstituteSettings;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
public class ReviewStatsActivityTest extends ActivityTestRule<ReviewStatsActivity> {

    private static final int WAITING_TIME = 15000;

    @Rule
    public final ActivityTestRule<ReviewStatsActivity> mActivityRule =
            new ActivityTestRule<ReviewStatsActivity>(ReviewStatsActivity.class, true, false) {
                @Override
                protected void beforeActivityLaunched() {
                    super.beforeActivityLaunched();
                    InstituteSettings instituteSettings = new InstituteSettings("http://demo.testpress.in");
                    TestpressSdk.setTestpressSession(InstrumentationRegistry.getTargetContext(),
                            new TestpressSession(instituteSettings, "eyJhbGciOiJIUzI1Ni" +
                                    "IsInR5cCI6IkpXVCJ9.eyJ1c2VybmFtZSI6MTg4LCJ1c2VyX2lkIjoxODgs" +
                                    "ImVtYWlsIjoiZHVtbXlAbGFja21haWwucnUiLCJleHAiOjE0NzcxNDA5MzR" +
                                    "9.C1Mt3r5pxKprSKOvD-1W9IU_WgZjRHCLEM-m0jcFJY4"));
                }
            };

    public ReviewStatsActivityTest() {
        super(ReviewStatsActivity.class);
    }

    protected Intent getActivityIntent() {
        String examJson = "{\n" +
                "url: \"http://demo.testpress.in/api/v2.2/exams/ias-demo/\",\n" +
                "id: 60,\n" +
                "title: \"Science & Technology\",\n" +
                "number_of_questions: 100,\n" +
                "template_type: 1,\n" +
                "max_retakes: -1,\n" +
                "attempts_url: \"http://demo.testpress.in/api/v2.2/exams/ias-demo/attempts/\",\n" +
                "attempts_count: 1,\n" +
                "paused_attempts_count: 0,\n" +
                "allow_pdf: true,\n" +
                "allow_question_pdf: true" +
                "}";
        Intent intent = new Intent(InstrumentationRegistry.getTargetContext(), ReviewStatsActivity.class);
        Gson gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();
        intent.putExtra(ReviewStatsActivity.PARAM_EXAM, gson.fromJson(examJson, Exam.class));
        return intent;
    }

    @Test
    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    public void testReviewActivity_recreate() throws Exception {
        final ReviewStatsActivity activity = launchNewActivity();
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.recreate();
            }
        });
        // Wait for activity to create completely
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        assertReviewStatsDisplayed();
    }

    private ReviewStatsActivity launchNewActivity() {
        ReviewStatsActivity activity = mActivityRule.launchActivity(getActivityIntent());
        unlockScreen(activity);
        assertReviewStatsDisplayed();
        return activity;
    }

    private void assertReviewStatsDisplayed() {
        IdlingResource idlingResource = new ElapsedTimeIdlingResource(WAITING_TIME);
        Espresso.registerIdlingResources(idlingResource);

        onView(withText("Score")).check(matches(isDisplayed()));

        Espresso.unregisterIdlingResources(idlingResource);
    }

    private void unlockScreen(final Activity activity) {
        activity.runOnUiThread(new Runnable() {
            public void run() {
                activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                );
            }
        });
    }

}
