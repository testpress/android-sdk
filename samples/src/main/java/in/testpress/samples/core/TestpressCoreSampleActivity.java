package in.testpress.samples.core;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.IOException;

import in.testpress.core.TestpressException;
import in.testpress.core.TestpressSdk;
import in.testpress.core.TestpressSession;
import in.testpress.core.TestpressCallback;
import in.testpress.samples.BaseToolBarActivity;
import in.testpress.samples.R;

public class TestpressCoreSampleActivity extends BaseToolBarActivity {

    public static final int REQUEST_CODE_GOOGLE_SIGN_IN = 2222;
    private View loginView;
    private CallbackManager callbackManager;
    private GoogleApiClient googleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        if (getCallingActivity() != null) {
            // Activity started by startActivityForResult(), i.e by ExamSampleActivity or NavigationDrawerActivity
            getSupportActionBar().hide();
        } else {
            // Activity started by startActivity, i.e by MainActivity
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        callbackManager = CallbackManager.Factory.create();
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(
                GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken(getString(R.string.server_client_id))
                .build();
        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        if (connectionResult.getErrorMessage() != null) {
                            Snackbar.make(loginView, connectionResult.getErrorMessage(),
                                    Snackbar.LENGTH_LONG).show();
                        } else {
                            Snackbar.make(loginView, connectionResult.toString(),
                                    Snackbar.LENGTH_LONG).show();
                        }
                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, googleSignInOptions)
                .build();
        findViewById(R.id.google_sign_in_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
                startActivityForResult(signInIntent, REQUEST_CODE_GOOGLE_SIGN_IN);
            }
        });
        LoginButton loginButton = (LoginButton) findViewById(R.id.fb_login_button);
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                authenticate(loginResult.getAccessToken().getUserId(),
                        loginResult.getAccessToken().getToken(), TestpressSdk.Provider.FACEBOOK);
            }

            @Override
            public void onCancel() {
            }

            @Override
            public void onError(FacebookException error) {
                if (error.getCause() instanceof IOException) {
                    Snackbar.make(loginView, R.string.no_internet_try_again, Snackbar.LENGTH_LONG).show();
                } else {
                    Snackbar.make(loginView, "Facebook sign in error, please check the key hashes",
                            Snackbar.LENGTH_LONG).show();
                }
            }
        });
        final EditText usernameEditText = (EditText) findViewById(R.id.username);
        final EditText passwordEditText = (EditText) findViewById(R.id.password);
        findViewById(R.id.testpress_login_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!usernameEditText.getText().toString().trim().isEmpty() &&
                        !passwordEditText.getText().toString().trim().isEmpty()) {
                    authenticate(usernameEditText.getText().toString().trim(),
                            passwordEditText.getText().toString().trim(), TestpressSdk.Provider.TESTPRESS);
                }
            }
        });
        loginView = findViewById(R.id.scroll_view);
    }

    private void authenticate(String userId, String accessToken, TestpressSdk.Provider provider) {
        TestpressSdk.initialize(this, "http://demo.testpress.in", userId, accessToken, provider,
                new TestpressCallback<TestpressSession>() {
                    @Override
                    public void onSuccess(TestpressSession response) {
                        if (getCallingActivity() != null) {
                            setResult(RESULT_OK);
                            finish();
                        } else {
                            Snackbar.make(loginView, "Token Generated Successfully",
                                    Snackbar.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onException(TestpressException e) {
                        if (e.isNetworkError()) {
                            Snackbar.make(loginView, R.string.no_internet_try_again,
                                    Snackbar.LENGTH_LONG).show();
                        } else if (e.isClientError()) {
                            Snackbar.make(loginView, e.getMessage(), Snackbar.LENGTH_LONG).show();
                        } else {
                            Snackbar.make(loginView, "Token Generation Failed",
                                    Snackbar.LENGTH_LONG).show();
                        }
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_GOOGLE_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                authenticate(result.getSignInAccount().getId(), result.getSignInAccount().getIdToken(),
                        TestpressSdk.Provider.GOOGLE);
            } else if (result.getStatus().getStatusCode() == 10) {
                Snackbar.make(loginView, "Google sign in error, please check the hashes",
                        Snackbar.LENGTH_LONG).show();
            } else {
                Log.e("Google sign in error",result.getStatus().toString());
            }
        } else {
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

}
