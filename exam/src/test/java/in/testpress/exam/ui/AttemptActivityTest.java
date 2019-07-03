package in.testpress.exam.ui;

import android.content.Intent;

import androidx.test.core.app.ApplicationProvider;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import java.lang.reflect.Field;

import in.testpress.core.TestpressSDKDatabase;
import in.testpress.core.TestpressSdk;
import in.testpress.core.TestpressSession;
import in.testpress.models.InstituteSettings;
import in.testpress.models.greendao.Exam;
import in.testpress.util.CommonTestUtils;

import static in.testpress.exam.ui.TestActivity.PARAM_EXAM;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;


@RunWith(RobolectricTestRunner.class)
public class AttemptActivityTest {

    private static final int NUMBER_OF_RETROFIT_CALLS = 2;
    private AttemptsActivity activity;
    private static final String USER_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VybmFtZSI6NDYsInVzZXJfaWQiOjQ2LCJlbWFpbCI6IiIsImV4cCI6MTUxOTAzNjUzM30.FUuyJfYNSAw_VcypZsN8_ZHvZra6gHU3njcXmr-TGVU";

    private Exam exam;


    @Before
    public void setUp() {
        exam = new Exam();
        exam.setSlug("hello");
        exam.setAttemptsCount(0);
        exam.setPausedAttemptsCount(0);
        Intent intent = new Intent();
        intent.putExtra(PARAM_EXAM, exam);

        InstituteSettings instituteSettings =
                new InstituteSettings("http://sandbox.testpress.in");
        TestpressSdk.setTestpressSession(ApplicationProvider.getApplicationContext(),
                new TestpressSession(instituteSettings, USER_TOKEN));
        TestpressSDKDatabase.getExamDao(ApplicationProvider.getApplicationContext()).insertOrReplace(exam);
        activity = Robolectric.buildActivity(AttemptsActivity.class, intent)
                .create()
                .resume()
                .get();
        activity = spy(activity);
    }

    @Test
    public void testLoadExam() {
        /*
         * If exam details is not fetched then exam should be loaded from API
         * */
        activity.fetchOrCheckExam();
        verify(activity).loadExam(exam.getSlug());

    }

    @Test
    public void testIsDetailsFetched() {
        /*
         * If exam details is fetched then exam should not be loaded from API
         * */
        exam.setIsDetailsFetched(true);
        activity.fetchOrCheckExam();
        verify(activity, never()).loadExam(exam.getSlug());
        verify(activity).checkExamState();
    }

    @Test
    public void testAttemptActivity_getRetrofitCalls_returnCorrectValues() {
        CommonTestUtils.testGetRetrofitCallsReturnCorrectValues(
                new AttemptsActivity(),
                NUMBER_OF_RETROFIT_CALLS
        );
    }

    @After
    public void tearDown() {
        resetSingleton(TestpressSDKDatabase.class, "database");
        resetSingleton(TestpressSDKDatabase.class, "daoSession");

    }


    public static void resetSingleton(Class clazz, String fieldName) {
        Field instance;
        try {
            instance = clazz.getDeclaredField(fieldName);
            instance.setAccessible(true);
            instance.set(null, null);
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }
}
