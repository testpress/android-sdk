package in.testpress.network;


import in.testpress.models.AccountActivity;
import in.testpress.models.TestpressApiResponse;

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
    public RetrofitCall<TestpressApiResponse<AccountActivity>> getItems(
            int page, int size) {
        queryParams.put(PAGE, page);
        queryParams.put("filter", "app");
        return apiClient.getAccountActivity(queryParams);
    }
}