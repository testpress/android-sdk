package in.testpress.network;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.HashMap;

import in.testpress.core.TestpressSession;
import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;

public class TestpressApiClient {

    private final RestAdapter restAdapter;

    public static final String SOCIAL_AUTH_PATH= "api/v2.2/social-auth/";
    public static final String TESTPRESS_AUTH_PATH= "api/v2.2/auth-token/";

    public TestpressApiClient(String baseUrl) {
        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();

        restAdapter = new RestAdapter.Builder()
                .setEndpoint(baseUrl)
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setConverter(new GsonConverter(gson))
                .build();
    }

    private AuthenticationService getAuthenticationService() {
        return restAdapter.create(AuthenticationService.class);
    }

    public TestpressSession authenticate(String authenticateUrlFrag, HashMap<String, String> arguments) {
        return getAuthenticationService().authenticate(authenticateUrlFrag, arguments);
    }
}
