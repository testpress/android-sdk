package in.testpress.network;

import android.content.Context;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import in.testpress.core.TestpressSession;
import in.testpress.util.UserAgentProvider;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class TestpressApiClient {

    public static final String SOCIAL_AUTH_PATH= "api/v2.2/social-auth/";
    public static final String TESTPRESS_AUTH_PATH= "api/v2.2/auth-token/";

    protected final Retrofit retrofit;

    /**
     * Used to make the network calls without
     * authentication header.
     *
     * @param baseUrl
     * @param context
     */
    public TestpressApiClient(String baseUrl, final Context context) {
        this(context, baseUrl, null);
    }

    /**
     * Use when testpress session is available, to add the authentication header
     *
     * @param testpressSession
     * @param context
     */
    public TestpressApiClient(final Context context, TestpressSession testpressSession) {
        this(context, checkTestpressSessionIsNull(testpressSession).getBaseUrl(), testpressSession);
    }

    private TestpressApiClient(final Context context, String baseUrl,
                              final TestpressSession testpressSession) {
        if (context == null) {
            throw new IllegalArgumentException("Context must not be null.");
        }
        if (baseUrl == null || baseUrl.isEmpty()) {
            throw new IllegalArgumentException("BaseUrl must not be null or Empty.");
        }
        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();

        // Set headers for all network requests
        Interceptor interceptor = new Interceptor() {
            @Override
            public okhttp3.Response intercept(Interceptor.Chain chain) throws IOException {
                Request.Builder header = chain.request().newBuilder()
                        .addHeader("User-Agent", UserAgentProvider.get(context));
                if (testpressSession != null) {
                    header.addHeader("Authorization", "JWT " + testpressSession.getToken());
                }
                return chain.proceed(header.build());
            }
        };
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.connectTimeout(2, TimeUnit.MINUTES).readTimeout(2, TimeUnit.MINUTES);
        httpClient.addInterceptor(interceptor);

        // Set log level
        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        httpClient.addInterceptor(httpLoggingInterceptor);

        retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addCallAdapterFactory(new ErrorHandlingCallAdapterFactory())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(httpClient.build())
                .build();
    }

    public static TestpressSession checkTestpressSessionIsNull(TestpressSession testpressSession) {
        if (testpressSession == null) {
            throw new IllegalArgumentException("TestpressSession must not be null.");
        }
        return testpressSession;
    }

    private AuthenticationService getAuthenticationService() {
        return retrofit.create(AuthenticationService.class);
    }

    public RetrofitCall<TestpressSession> authenticate(String authenticateUrlFrag,
                                                       HashMap<String, String> arguments) {
        return getAuthenticationService().authenticate(authenticateUrlFrag, arguments);
    }

}
