package in.testpress.exam.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.RemoteException;
import androidx.annotation.RequiresApi;
import androidx.test.InstrumentationRegistry;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.IdlingResource;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;
import androidx.test.uiautomator.UiDevice;
import android.view.WindowManager;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import in.testpress.core.TestpressSdk;
import in.testpress.core.TestpressSession;
import in.testpress.exam.util.ElapsedTimeIdlingResource;
import in.testpress.models.InstituteSettings;
import in.testpress.models.greendao.Exam;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
public class ReviewStatsActivityTest extends ActivityTestRule<ReviewStatsActivity> {

    private static final int WAITING_TIME = 15000;
    private static final String USER_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VybmFtZSI6NDYsInVzZXJfaWQiOjQ2LCJlbWFpbCI6IiIsImV4cCI6MTUxOTAzNjUzM30.FUuyJfYNSAw_VcypZsN8_ZHvZra6gHU3njcXmr-TGVU";

    @Rule
    public final ActivityTestRule<ReviewStatsActivity> mActivityRule =
            new ActivityTestRule<ReviewStatsActivity>(ReviewStatsActivity.class, true, false) {
                @Override
                protected void beforeActivityLaunched() {
                    super.beforeActivityLaunched();
                    InstituteSettings instituteSettings =
                            new InstituteSettings("http://sandbox.testpress.in");
                    instituteSettings.setDisableStudentAnalytics(false);

                    TestpressSdk.setTestpressSession(InstrumentationRegistry.getTargetContext(),
                            new TestpressSession(instituteSettings, USER_TOKEN));
                }
            };

    public ReviewStatsActivityTest() {
        super(ReviewStatsActivity.class);
    }

    protected Intent getActivityIntent() {
        String examJson = "{\n" +
                "url: \"https://sandbox.testpress.in/api/v2.2/exams/android-app-test-case-exam/\",\n" +
                "id: 60,\n" +
                "title: \"Android App Test Case Exam\",\n" +
                "number_of_questions: 200,\n" +
                "template_type: 1,\n" +
                "max_retakes: -1,\n" +
                "attempts_url: \"https://sandbox.testpress.in/api/v2.2/exams/android-app-test-case-exam/attempts/\",\n" +
                "attempts_count: 1,\n" +
                "paused_attempts_count: 0,\n" +
                "allow_pdf: true,\n" +
                "allow_question_pdf: true," +
                "device_access_control: \"both\",\n" +
                "allow_retake: true,\n" +
                "show_answers: true,\n" +
                "show_percentile: true,\n" +
                "show_score: true,\n" +
                "languages: [{ \"code\": \"en\",\"title\": \"English\" }]" +
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
        UiDevice uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        try {
            if (!uiDevice.isScreenOn()) {
                uiDevice.wakeUp();
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        assertReviewStatsDisplayed();
        return activity;
    }

    private void assertReviewStatsDisplayed() {
        IdlingResource idlingResource = new ElapsedTimeIdlingResource(WAITING_TIME);
        Espresso.registerIdlingResources(idlingResource);

        onView(withText("Android App Test Case Exam")).check(matches(isDisplayed()));

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
