package in.testpress.network;


import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

import in.testpress.models.AccountActivity;
import in.testpress.models.TestpressApiResponse;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.QueryMap;

public interface AccountActivityService {
    @GET(TestpressApiClient.ACCOUNT_ACTIVITY_PATH)
    RetrofitCall<TestpressApiResponse<AccountActivity>> getAccountActivity(@QueryMap Map<String, Object> options);

    @POST(TestpressApiClient.LOGOUT_DEVICES)
    RetrofitCall<Void> logoutDevices();


}
