package in.testpress.core;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.HashMap;

import in.testpress.R;
import in.testpress.models.AuthToken;
import in.testpress.util.Callback;
import in.testpress.util.SafeAsyncTask;

public class TestpressSessionManager {

    SharedPreferences pref;
    SharedPreferences.Editor editor;
    Context context;
    private static final String KEY_TESTPRESS_AUTH_TOKEN = "testpressAuthToken";
    private static final String KEY_TESTPRESS_SHARED_PREFS = "testpressSharedPreferences";

    public TestpressSessionManager(Context context) {
        this.context = context;
        pref = this.context.getSharedPreferences(KEY_TESTPRESS_SHARED_PREFS, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    public static TestpressSessionManager getInstance(Context context) {
        return new TestpressSessionManager(context);
    }

    public void setAuthToken(String authToken) {
        editor.putString(KEY_TESTPRESS_AUTH_TOKEN, authToken);
        editor.commit();
    }

    public String getAuthToken() {
        return pref.getString(KEY_TESTPRESS_AUTH_TOKEN, null);
    }

    public void clearActiveSession() {
        editor.remove(KEY_TESTPRESS_AUTH_TOKEN).commit();
    }

    public boolean hasActiveSession() {
        return getAuthToken() != null;
    }

    // ToDo: Use params & hash for authentication instead of username & password
    public void createSession(String username, String password, final Callback<AuthToken> callback) {
        final ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(context.getResources().getString(R.string.please_wait));
        progressDialog.setCancelable(false);
        progressDialog.show();
        final HashMap<String, String> credentials = new HashMap<String, String>();
        credentials.put("username", username);
        credentials.put("password", password);
        new SafeAsyncTask<AuthToken>() {
            @Override
            public AuthToken call() throws Exception {
                return new TestpressApiClient().getAuthenticationService().authenticate(credentials);
            }

            @Override
            protected void onException(Exception exception) throws RuntimeException {
                super.onException(exception);
                progressDialog.dismiss();
                callback.onException(exception);
            }

            @Override
            protected void onSuccess(AuthToken response) throws Exception {
                super.onSuccess(response);
                Log.e("authToken:", response.getToken());
                setAuthToken(response.getToken());
                progressDialog.dismiss();
                callback.onSuccess(response);
            }
        }.execute();
    }
}
