package in.testpress.core;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.util.TypedValue;

import java.io.IOException;
import java.util.HashMap;

import in.testpress.network.AuthorizationErrorResponse;
import in.testpress.network.TestpressApiClient;
import in.testpress.R;
import in.testpress.util.CircularProgressDrawable;
import in.testpress.util.SafeAsyncTask;
import retrofit.RetrofitError;

public final class TestpressSdk {

    private static SharedPreferences pref;
    private static SharedPreferences.Editor editor;
    private static final String KEY_TESTPRESS_AUTH_TOKEN = "testpressAuthToken";
    private static final String KEY_TESTPRESS_SHARED_PREFS = "testpressSharedPreferences";
    private static final String KEY_USER_ID = "userId";
    public enum Provider { FACEBOOK, GOOGLE }

    private static SharedPreferences getPreferences(Context context) {
        if (pref == null) {
            validateContext(context);
            pref = context.getSharedPreferences(KEY_TESTPRESS_SHARED_PREFS, Context.MODE_PRIVATE);
        }
        return pref;
    }

    private static SharedPreferences.Editor getPreferenceEditor(Context context) {
        if (editor == null) {
            validateContext(context);
            editor = getPreferences(context).edit();
        }
        return editor;
    }

    public static void setTestpressSession(@NonNull Context context,
                                            @NonNull TestpressSession testpressSession) {
        if (testpressSession == null) {
            throw new IllegalArgumentException("TestpressSession must not be null.");
        }
        SharedPreferences.Editor editor = getPreferenceEditor(context);
        editor.putString(KEY_TESTPRESS_AUTH_TOKEN, TestpressSession.serialize(testpressSession));
        editor.apply();
    }

    @Nullable
    public static TestpressSession getTestpressSession(@NonNull Context context) {
        SharedPreferences pref = getPreferences(context);
        return TestpressSession.deserialize(pref.getString(KEY_TESTPRESS_AUTH_TOKEN, ""));
    }

    public static void clearActiveSession(@NonNull Context context) {
        SharedPreferences.Editor editor = getPreferenceEditor(context);
        editor.remove(KEY_TESTPRESS_AUTH_TOKEN).apply();
    }

    public static boolean hasActiveSession(@NonNull Context context) {
        return getTestpressSession(context) != null;
    }

    /**
     * Initialize the Sdk, it will authorize the user & store the token
     *
     * @param context Context
     * @param baseUrl Base url of institute
     * @param userId User social account id
     * @param accessToken User social account access token
     * @param provider Provider.FACEBOOK or Provider.GOOGLE
     */
    public static void initialize(@NonNull Context context, @NonNull String baseUrl,
                                  @NonNull String userId, @NonNull String accessToken,
                                  @NonNull Provider provider) {
        initialize(context, baseUrl, userId, accessToken, provider, null);
    }

    /**
     * Initialize the Sdk, it will authorize the user & store the token
     *
     * @param context Context
     * @param baseUrl Base url of institute
     * @param userId User social account id
     * @param accessToken User social account access token
     * @param provider Provider.FACEBOOK or Provider.GOOGLE
     * @param callback Callback which will be call on success or failure
     */
    public static void initialize(@NonNull final Context context, final @NonNull String baseUrl,
                                  @NonNull final String userId, @NonNull String accessToken,
                                  @NonNull Provider provider,
                                  final TestpressCallback<TestpressSession> callback) {
        validateContext(context);
        if (userId == null || accessToken == null || provider == null) {
            throw new IllegalArgumentException("UserId & AccessToken & Provider must not be null.");
        }
        if (baseUrl == null || baseUrl.isEmpty()) {
            throw new IllegalArgumentException("BaseUrl must not be null or Empty.");
        }
        if (userId.equals(getPreferences(context).getString(KEY_USER_ID, null)) &&
                hasActiveSession(context)) {
            if (callback != null) {
                callback.onSuccess(getTestpressSession(context));
            }
            return;
        }
        final ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(context.getString(R.string.testpress_please_wait));
        progressDialog.setCancelable(false);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            DisplayMetrics metrics = context.getResources().getDisplayMetrics();
            float pixelWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, metrics);
            progressDialog.setIndeterminateDrawable(new CircularProgressDrawable(
                    context.getResources().getColor(R.color.testpress_color_primary), pixelWidth));
        }
        progressDialog.show();
        final HashMap<String, String> credentials = new HashMap<String, String>();
        credentials.put("provider", provider.name());
        credentials.put("user_id", userId);
        credentials.put("access_token", accessToken);
        new SafeAsyncTask<TestpressSession>() {
            @Override
            public TestpressSession call() throws Exception {
                return new TestpressApiClient(baseUrl).getAuthenticationService().authenticate(credentials);
            }

            @Override
            protected void onException(Exception exception) throws RuntimeException {
                super.onException(exception);
                progressDialog.dismiss();
                if (callback != null) {
                    TestpressException  testpressException = new TestpressException(exception.getCause());
                    if (exception.getCause() instanceof IOException) {
                        testpressException.setStatusCode(TestpressException.NETWORK_ERROR);
                    } else if((exception instanceof RetrofitError) &&
                            ((RetrofitError) exception).getResponse().getStatus() ==
                                    TestpressException.BAD_REQUEST) {

                        AuthorizationErrorResponse errorResponse = (AuthorizationErrorResponse)
                                ((RetrofitError) exception).getBodyAs(AuthorizationErrorResponse.class);
                        String message = "";
                        if(!errorResponse.getUserId().isEmpty()) {
                            message = errorResponse.getUserId().get(0);
                        } else if(!errorResponse.getAccessToken().isEmpty()) {
                            message = errorResponse.getAccessToken().get(0);
                        } else if(!errorResponse.getProvider().isEmpty()) {
                            message = errorResponse.getProvider().get(0);
                        } else if (!errorResponse.getNonFieldErrors().isEmpty()) {
                            message = errorResponse.getNonFieldErrors().get(0);
                        }
                        testpressException = new TestpressException(message, exception.getCause());
                        testpressException.setStatusCode(TestpressException.BAD_REQUEST);
                    }
                    callback.onException(testpressException);
                }
            }

            @Override
            protected void onSuccess(TestpressSession testpressSession) throws Exception {
                super.onSuccess(testpressSession);
                testpressSession.setBaseUrl(baseUrl);
                setTestpressSession(context, testpressSession);
                SharedPreferences.Editor editor = getPreferenceEditor(context);
                editor.putString(KEY_USER_ID, userId);
                editor.apply();
                progressDialog.dismiss();
                if (callback != null) {
                    callback.onSuccess(testpressSession);
                }
            }
        }.execute();
    }

    private static void validateContext(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("Context must not be null.");
        }
    }
}
