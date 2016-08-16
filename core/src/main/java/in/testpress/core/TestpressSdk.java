package in.testpress.core;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;

import java.util.HashMap;

import in.testpress.network.TestpressApiClient;
import in.testpress.R;
import in.testpress.util.CircularProgressDrawable;
import in.testpress.util.SafeAsyncTask;

public final class TestpressSdk {

    private static SharedPreferences pref;
    private static SharedPreferences.Editor editor;
    private static final String KEY_TESTPRESS_AUTH_TOKEN = "testpressAuthToken";
    private static final String KEY_TESTPRESS_SHARED_PREFS = "testpressSharedPreferences";

    public static String BASE_URL;

    public static void setAuthToken(String authToken) {
        isSdkInitialized();
        editor.putString(KEY_TESTPRESS_AUTH_TOKEN, authToken);
        editor.commit();
    }

    public static String getAuthToken() {
        isSdkInitialized();
        return pref.getString(KEY_TESTPRESS_AUTH_TOKEN, null);
    }

    public static void clearActiveSession() {
        isSdkInitialized();
        editor.remove(KEY_TESTPRESS_AUTH_TOKEN).commit();
    }

    public static boolean hasActiveSession() {
        return getAuthToken() != null;
    }

    private static boolean isSdkInitialized() {
        if (pref == null) {
            throw new IllegalStateException("Initialize the sdk first");
        } else {
            return true;
        }
    }

    /**
     * Initialize the Sdk, it will authorize the user & store the token
     *
     * @param context Context
     * @param baseUrl Base url of institute
     * @param username Username
     * @param password Password
     */
    public static void initialize(@NonNull Context context, @NonNull String baseUrl,
                                  @NonNull String username, @NonNull String password) {
        initialize(context, baseUrl, username, password, null);
    }

    /**
     * Initialize the Sdk, it will authorize the user & store the token
     *
     * @param context Context
     * @param baseUrl Base url of institute
     * @param username Username
     * @param password Password
     * @param callback Callback which will be call on success or failure
     */
    // ToDo: Use params & hash for authentication instead of username & password
    public static void initialize(@NonNull Context context, @NonNull String baseUrl,
                                  @NonNull String username, @NonNull String password,
                                  final TestpressCallback<TestpressAuthToken> callback) {
        final ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(context.getResources().getString(R.string.testpress_please_wait));
        progressDialog.setCancelable(false);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            DisplayMetrics metrics = context.getResources().getDisplayMetrics();
            float pixelWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, metrics);
            progressDialog.setIndeterminateDrawable(new CircularProgressDrawable(
                    context.getResources().getColor(R.color.testpress_color_primary), pixelWidth));
        }
        progressDialog.show();
        BASE_URL = baseUrl;
        pref = context.getSharedPreferences(KEY_TESTPRESS_SHARED_PREFS, Context.MODE_PRIVATE);
        editor = pref.edit();
        final HashMap<String, String> credentials = new HashMap<String, String>();
        credentials.put("username", username);
        credentials.put("password", password);
        new SafeAsyncTask<TestpressAuthToken>() {
            @Override
            public TestpressAuthToken call() throws Exception {
                return new TestpressApiClient().getAuthenticationService().authenticate(credentials);
            }

            @Override
            protected void onException(Exception exception) throws RuntimeException {
                super.onException(exception);
                progressDialog.dismiss();
                if (callback != null) {
                    callback.onException(exception);
                }
            }

            @Override
            protected void onSuccess(TestpressAuthToken response) throws Exception {
                super.onSuccess(response);
                Log.e("authToken:", response.getToken());
                setAuthToken(response.getToken());
                progressDialog.dismiss();
                if (callback != null) {
                    callback.onSuccess(response);
                }
            }
        }.execute();
    }
}
