package in.testpress.network;

import android.content.Context;
import android.content.Intent;
import android.os.Looper;
import android.support.v7.app.AlertDialog;
import android.os.Handler;
import android.os.Looper;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import in.testpress.R;
import in.testpress.core.TestpressSession;
import in.testpress.models.AccountActivity;
import in.testpress.models.FileDetails;
import in.testpress.models.ProfileDetails;
import in.testpress.models.TestpressApiResponse;
import in.testpress.models.greendao.AttemptSection;
import in.testpress.ui.UserDevicesActivity;
import in.testpress.util.UIUtils;
import in.testpress.util.UserAgentProvider;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class TestpressApiClient {

    public static final String SOCIAL_AUTH_PATH= "api/v2.2/social-auth/";
    public static final String TESTPRESS_AUTH_PATH= "api/v2.2/auth-token/";

    public static final String PROFILE_DETAILS_PATH= "api/v2.2/me/";
    public static final String ACCOUNT_ACTIVITY_PATH = "api/v2.3/me/login_activity/";
    public static final String LOGOUT_DEVICES = "api/v2.4/auth/logout_devices/";
    public static final String LOGOUT_PATH = "api/v2.4/auth/logout/";

    /**
     * Query Params
     */
    public static final String PAGE = "page";
    public static final String PAGE_SIZE = "page_size";
    public static final String PARENT = "parent";

    public static final String SINCE = "since";
    public static final String UNTIL = "until";
    public static final String ORDER = "order";
    public static final String UNFILTERED = "unfiltered";
    public static final String FOLDER = "folder";
    public static final String MODIFIED_SINCE = "modified_since";
    public static final String CREATED_SINCE = "created_since";
    public static final String CREATED_UNTIL = "created_until";

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
        this(context, null, checkTestpressSessionIsNull(testpressSession));
    }
    

    private TestpressApiClient(final Context context, String baseUrl,
                              final TestpressSession testpressSession) {
        if (context == null) {
            throw new IllegalArgumentException("Context must not be null.");
        }
        if (testpressSession != null) {
            baseUrl = testpressSession.getInstituteSettings().getBaseUrl();
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
        httpClient.connectTimeout(1, TimeUnit.MINUTES).readTimeout(1, TimeUnit.MINUTES);
        httpClient.addNetworkInterceptor(interceptor);

        // Set log level
        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        httpClient.addInterceptor(httpLoggingInterceptor);

        Interceptor responseCodeInterceptor = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request request = chain.request();
                Response response = chain.proceed(request);
                final String rawJson = response.body().string();

                if (response.code() >= 400 && response.code() <= 500) {
                    try {
                        JSONObject json = new JSONObject(rawJson);
                        String error_code = json.getString("error_code");
                        Handler handler = new Handler(Looper.getMainLooper());

                        if (error_code.equals("parallel_login_restriction")) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Intent intent = new Intent(context, UserDevicesActivity.class);
                                    context.startActivity(intent);
                                }
                            });
                        } else if (error_code.equals("max_login_exceeded")) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                String message = context.getString(R.string.max_login_limit_exceeded_error);

                                if (testpressSession.getInstituteSettings().getCooloffTime() != null) {
                                    message += context.getString(R.string.account_unlock_info) + " %s hours";
                                    message = String.format(message, testpressSession.getInstituteSettings().getCooloffTime());
                                }

                                UIUtils.showAlert(context, "Account Locked", message);
                                }
                            });
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                return response.newBuilder()
                        .body(ResponseBody.create(response.body().contentType(), rawJson)).build();
            }
        };

        httpClient.addInterceptor(responseCodeInterceptor);

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

    public AccountActivityService getAccountActivityService() {
        return retrofit.create(AccountActivityService.class);
    }

    public RetrofitCall<TestpressApiResponse<AccountActivity>> getAccountActivity(Map<String, Object> queryParams) {
        return getAccountActivityService().getAccountActivity(queryParams);
    }

    public RetrofitCall<Void> logoutDevices() {
        return getAccountActivityService().logoutDevices();
    }


    public String getBaseUrl() {
        return retrofit.baseUrl().toString();
    }

    private AuthenticationService getAuthenticationService() {
        return retrofit.create(AuthenticationService.class);
    }

    private FileUploadService getFileUploadService() {
        return retrofit.create(FileUploadService.class);
    }

    public RetrofitCall<TestpressSession> authenticate(String authenticateUrlFrag,
                                                       HashMap<String, String> arguments) {
        return getAuthenticationService().authenticate(authenticateUrlFrag, arguments);
    }

    public RetrofitCall<ProfileDetails> getProfileDetails() {
        return getAuthenticationService().getProfileDetails();
    }

    public RetrofitCall<Void> logout() {
        return getAuthenticationService().logout();
    }

    public RetrofitCall<FileDetails> upload(String filePath) {
        File file = new File(filePath);
        RequestBody reqFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), reqFile);
        return getFileUploadService().upload(body);
    }

}
