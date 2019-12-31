package in.testpress.util;

import java.io.IOException;
import java.util.List;

import in.testpress.core.TestpressCallback;
import in.testpress.core.TestpressException;
import in.testpress.core.TestpressRetrofitRequest;
import in.testpress.models.AccountActivity;
import in.testpress.models.TestpressApiResponse;
import in.testpress.network.BaseResourcePager;
import in.testpress.network.RetrofitCall;
import in.testpress.network.TestpressApiClient;
import retrofit2.Response;

import static in.testpress.network.TestpressApiClient.PAGE;

public class SamplePager extends BaseResourcePager<AccountActivity> {

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