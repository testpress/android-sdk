package in.testpress.core;

import android.support.v7.app.AlertDialog;
import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowAlertDialog;

import java.io.IOException;

import in.testpress.models.InstituteSettings;
import in.testpress.network.AccountActivityPager;
import in.testpress.network.TestpressApiClient;
import in.testpress.ui.UserDevicesActivity;
import junit.framework.Assert;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

@RunWith(RobolectricTestRunner.class)
public class TestpressApiClientTest {

    private static final String USER_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VybmFtZSI6NDYsInVzZXJfaWQiOjQ2LCJlbWFpbCI6IiIsImV4cCI6MTUxOTAzNjUzM30.FUuyJfYNSAw_VcypZsN8_ZHvZra6gHU3njcXmr-TGVU";
    private MockWebServer mockWebServer;
    private UserDevicesActivity activity;

    @Before
    public void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        InstituteSettings instituteSettings =
                new InstituteSettings("http://localhost:9200");
        TestpressSdk.setTestpressSession(ApplicationProvider.getApplicationContext(),
                new TestpressSession(instituteSettings, USER_TOKEN));
        mockWebServer.start(9200);
    }


    @Test
    public void testConstructor_withNullBaseUrl() throws Exception {
        try {
            Context context = mock(Context.class);
            new TestpressApiClient(null, context);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("BaseUrl must not be null or Empty.", e.getMessage());
        }
    }

    @Test
    public void testConstructor_withNullContext() throws Exception {
        try {
            new TestpressApiClient("dummyBaseUrl", null);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Context must not be null.", e.getMessage());
        }
        try {
            TestpressSession session = mock(TestpressSession.class);
            new TestpressApiClient(null, session);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Context must not be null.", e.getMessage());
        }
    }

    @Test
    public void testConstructor_withNullSession() throws Exception {
        try {
            Context context = mock(Context.class);
            new TestpressApiClient(context, null);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("TestpressSession must not be null.", e.getMessage());
        }
    }

    @Test
    public void testUnAuthorizedUserError() throws Exception {

        TestpressApiClient apiClient = new TestpressApiClient(ApplicationProvider.getApplicationContext(), TestpressSdk.getTestpressSession(ApplicationProvider.getApplicationContext()));
        AccountActivityPager pager = new AccountActivityPager(apiClient);
        MockResponse successResponse = new MockResponse().setResponseCode(401);
        mockWebServer.enqueue(successResponse);
        pager.getItems(1, 1);
        mockWebServer.takeRequest();

        AlertDialog alertDialog = (AlertDialog) ShadowAlertDialog.getShownDialogs().get(0);

        Assert.assertNotNull(alertDialog);
    }


    @After
    public void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

}
