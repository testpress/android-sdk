package in.testpress.network;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.List;

import in.testpress.models.AccountActivity;
import in.testpress.models.TestpressApiResponse;
import in.testpress.models.greendao.Attempt;
import in.testpress.models.greendao.Exam;
import in.testpress.network.BaseResourcePager;
import in.testpress.network.TestpressApiClient;
import retrofit2.Response;

import static in.testpress.network.TestpressApiClient.PAGE;


public class AccountActivityPager extends BaseResourcePager<AccountActivity> {

    private TestpressApiClient apiClient;;

    public AccountActivityPager(TestpressApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    protected Object getId(AccountActivity accountActivity) {
        return accountActivity.getId();
    }

    @Override
    public Response<TestpressApiResponse<AccountActivity>> getItems(int page, int size) throws IOException {
        queryParams.put(PAGE, page);
        queryParams.put("filter", "app");
        return apiClient.getAccountActivity(queryParams).execute();
    }

}