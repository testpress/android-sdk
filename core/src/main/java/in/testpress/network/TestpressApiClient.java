package in.testpress.network;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import in.testpress.core.TestpressSdk;
import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;

public class TestpressApiClient {

    final RestAdapter restAdapter;

    public TestpressApiClient() {
        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();

        restAdapter = new RestAdapter.Builder()
                .setEndpoint(TestpressSdk.BASE_URL)
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setConverter(new GsonConverter(gson))
                .build();
    }

    public AuthenticationService getAuthenticationService() {
        return restAdapter.create(AuthenticationService.class);
    }
}
