package in.testpress.course;

import androidx.test.core.app.ApplicationProvider;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import in.testpress.core.TestpressSdk;
import in.testpress.core.TestpressSession;
import in.testpress.course.network.TestpressCourseApiClient;
import in.testpress.models.InstituteSettings;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@RunWith(RobolectricTestRunner.class)
public class TestpressCourseApiClientTest {

    @Test
    public void testConstructor_withNullContext() throws Exception {
        try {
            new TestpressCourseApiClient(null);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Context must not be null.", e.getMessage());
        }
    }

    private static final String USER_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VybmFtZSI6NDYsInVzZXJfaWQiOjQ2LCJlbWFpbCI6IiIsImV4cCI6MTUxOTAzNjUzM30.FUuyJfYNSAw_VcypZsN8_ZHvZra6gHU3njcXmr-TGVU";
    private MockWebServer mockWebServer;

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
    public void testGetContents() throws Exception {
        int courseId = 1;
        MockResponse successResponse = new MockResponse().setResponseCode(200).setBody("{}");
        mockWebServer.enqueue(successResponse);
        TestpressCourseApiClient apiClient = new TestpressCourseApiClient(ApplicationProvider.getApplicationContext());
        apiClient.getContents(courseId, new LinkedHashMap<String, Object>()).execute();
        RecordedRequest request = mockWebServer.takeRequest();

        String expected = String.format("/api/v2.4/courses/%s/contents/", courseId);
        assertEquals(expected, request.getPath());
    }


    @Test
    public void testGetChapters() throws Exception {
        int courseId = 1;
        Map<String, Object> queryParams = new LinkedHashMap<String, Object>();
        MockResponse successResponse = new MockResponse().setResponseCode(200).setBody("{}");
        mockWebServer.enqueue(successResponse);
        TestpressCourseApiClient apiClient = new TestpressCourseApiClient(ApplicationProvider.getApplicationContext());
        apiClient.getChapters(String.valueOf(courseId), queryParams, null).execute();
        RecordedRequest request = mockWebServer.takeRequest();

        String expected = String.format("/api/v2.4/courses/%s/chapters/", courseId);
        assertEquals(expected, request.getPath());
    }

    @Test
    public void testGetCourses() throws Exception {
        Map<String, Object> queryParams = new LinkedHashMap<String, Object>();
        MockResponse successResponse = new MockResponse().setResponseCode(200).setBody("{}");
        mockWebServer.enqueue(successResponse);
        TestpressCourseApiClient apiClient = new TestpressCourseApiClient(ApplicationProvider.getApplicationContext());
        apiClient.getCourses(queryParams, null).execute();
        RecordedRequest request = mockWebServer.takeRequest();

        assertEquals("/api/v2.4/courses/", request.getPath());
    }

    @After
    public void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

}
