package in.testpress.core;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;

import java.util.HashMap;

import in.testpress.R;
import in.testpress.models.AuthToken;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TestpressAccountAuthenticator {

    public static final String AUTH_TOKEN_TYPE = "in.testpress.testpressAuthToken";

    /*
    * Use if no account for user already exist for your app
    */
    public void authenticate(Context context, String username, String password,
                             Callback<AuthToken> callback) {
        authenticate(null, context, username, password, callback);
    }

    /*
    * Use if already a account exist for user in your app
    */
    public void authenticate(final Account account, final Context context, String username,
                             String password, final Callback<AuthToken> callback) {
        // ToDo: Use params & hash for authentication instead of username & password
        final ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(context.getResources().getString(R.string.please_wait));
        progressDialog.setCancelable(false);
        progressDialog.show();
        HashMap<String, String> credentials = new HashMap<String, String>();
        credentials.put("username", username);
        credentials.put("password", password);
        Callback<AuthToken> authTokenCallback = new Callback<AuthToken>() {
            @Override
            public void onResponse(Call<AuthToken> call, Response<AuthToken> response) {
                Log.e("authToken:", response.body().getToken());
                AccountManager accountManager = AccountManager.get(context);
                if (account == null) {
                    // ToDo: Add account in mobile
                } else {
                    accountManager.setAuthToken(account, AUTH_TOKEN_TYPE, response.body().getToken());
                }
                progressDialog.dismiss();
                callback.onResponse(call, response);
            }

            @Override
            public void onFailure(Call<AuthToken> call, Throwable throwable) {
                throwable.printStackTrace();
                progressDialog.dismiss();
                callback.onFailure(call, throwable);
            }
        };
        Call<AuthToken> authTokenCall = new TestpressApiClient().getAuthenticationService()
                .authenticate(credentials);

        authTokenCall.enqueue(authTokenCallback);
    }

}
