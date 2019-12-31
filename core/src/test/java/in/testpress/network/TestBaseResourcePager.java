package in.testpress.network;

import androidx.test.core.app.ApplicationProvider;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.io.IOException;
import java.util.List;

import in.testpress.core.TestpressCallback;
import in.testpress.core.TestpressException;
import in.testpress.core.TestpressRetrofitRequest;
import in.testpress.core.TestpressSdk;
import in.testpress.core.TestpressSession;
import in.testpress.models.AccountActivity;
import in.testpress.models.InstituteSettings;
import in.testpress.models.TestpressApiResponse;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import retrofit2.Response;

import static in.testpress.network.TestpressApiClient.PAGE;

@RunWith(RobolectricTestRunner.class)
public class TestBaseResourcePager {
    private SamplePager pager;
    private TestpressApiClient apiClient;

    private class SamplePager extends BaseResourcePager<AccountActivity> {

        private TestpressApiClient apiClient;;

        public SamplePager(TestpressApiClient apiClient) {
            this.apiClient = apiClient;
        }

        @Override
        protected Object getId(AccountActivity resource) {
            return resource.getId();
        }

        public TestpressRetrofitRequest<AccountActivity> getRetrofitCall(final TestpressApiClient apiClient, final SamplePager pager) {
            return new TestpressRetrofitRequest<AccountActivity>() {
                @Override
                public RetrofitCall<TestpressApiResponse<AccountActivity>> getRetrofitCall(
                        int page, int size) {
                    return apiClient.getAccountActivity(pager.queryParams);
                }
            };
        }

        public TestpressCallback<List<AccountActivity>> testpressCallback() {
            return new TestpressCallback<List<AccountActivity>>() {
                @Override
                public void onSuccess(List<AccountActivity> result) {}

                @Override
                public void onException(TestpressException exception) {}
            };
        }

        @Override
        public Response<TestpressApiResponse<AccountActivity>> getItems(int page, int size) throws IOException {
            queryParams.put(PAGE, page);
            queryParams.put("filter", "app");
            return apiClient.getAccountActivity(queryParams).execute();
        }

    }

    private MockWebServer mockWebServer;
    private static final String USER_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VybmFtZSI6NDYsInVzZXJfaWQiOjQ2LCJlbWFpbCI6IiIsImV4cCI6MTUxOTAzNjUzM30.FUuyJfYNSAw_VcypZsN8_ZHvZra6gHU3njcXmr-TGVU";


    @Before
    public void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        InstituteSettings instituteSettings =
                new InstituteSettings("http://localhost:9200");
        TestpressSdk.setTestpressSession(ApplicationProvider.getApplicationContext(),
                new TestpressSession(instituteSettings, USER_TOKEN));
        mockWebServer.start(9200);

        apiClient = new TestpressApiClient(ApplicationProvider.getApplicationContext(), TestpressSdk.getTestpressSession(ApplicationProvider.getApplicationContext()));
        pager = new SamplePager(apiClient);
    }


    @Test
    public void testRetrofitCall() throws Exception{
        MockResponse successResponse = new MockResponse().setResponseCode(200);
        mockWebServer.enqueue(successResponse);

        pager.fetchItemsAsync(pager.getRetrofitCall(apiClient, pager), pager.testpressCallback());
        mockWebServer.takeRequest();

        assert pager.retrofitCall != null;
        assert pager.page == 1;
        assert !pager.hasMore();
    }

    @After
    public void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

}
