package in.testpress.exam;

import android.app.Dialog;

import androidx.test.core.app.ApplicationProvider;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowAlertDialog;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import in.testpress.core.TestpressSdk;
import in.testpress.core.TestpressSession;
import in.testpress.exam.network.TestpressExamApiClient;
import in.testpress.models.InstituteSettings;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;


@RunWith(RobolectricTestRunner.class)
public class TestpressExamApiClientTest {

    private static final String USER_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VybmFtZSI6NDYsInVzZXJfaWQiOjQ2LCJlbWFpbCI6IiIsImV4cCI6MTUxOTAzNjUzM30.FUuyJfYNSAw_VcypZsN8_ZHvZra6gHU3njcXmr-TGVU";
    private MockWebServer mockWebServer;
    private TestpressExamApiClient apiClient;
    private Map<String, Object> queryParams;

    @Before
    public void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        InstituteSettings instituteSettings =
                new InstituteSettings("http://localhost:9000");
        TestpressSdk.setTestpressSession(ApplicationProvider.getApplicationContext(),
                new TestpressSession(instituteSettings, USER_TOKEN));
        apiClient = new TestpressExamApiClient(ApplicationProvider.getApplicationContext());
        queryParams = new LinkedHashMap<>();
        mockWebServer.start(9000);
    }


    @Test
    public void testResponseCodeInterceptionForError() throws IOException, InterruptedException {
        /*
        * For Response code between 400 - 500 alert dialog should be displayed with error message.
        *
        * */
        String testDataJson = "{\"error_code\":\"parallel_login_exceeded\",\"detail\":\"Maximum number of parallel logins exceeded. Logout from one or more devices to continue.\"}";
        MockResponse successResponse = new MockResponse().setBody(testDataJson);
        successResponse.setResponseCode(401);
        mockWebServer.enqueue(successResponse);
        apiClient.getExams(queryParams).execute();
        mockWebServer.takeRequest();
        Dialog dialog  = ShadowAlertDialog.getLatestDialog();

        assertTrue(dialog.isShowing());
    }

    @Test
    public void testResponseCodeInterceptionForValidResponse() throws IOException, InterruptedException {
        /*
         * For Response code other than 400 - 500 alert dialog should not be displayed.
         *
         * */
        String testDataJson = "{\"error_code\":\"parallel_login_exceeded\",\"detail\":\"Maximum number of parallel logins exceeded. Logout from one or more devices to continue.\"}";
        MockResponse successResponse = new MockResponse().setBody(testDataJson);
        successResponse.setResponseCode(200);
        mockWebServer.enqueue(successResponse);
        apiClient.getExams(queryParams).execute();
        mockWebServer.takeRequest();
        Dialog dialog  = ShadowAlertDialog.getLatestDialog();

        assertNull(dialog);
    }


    @After
    public void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

}
