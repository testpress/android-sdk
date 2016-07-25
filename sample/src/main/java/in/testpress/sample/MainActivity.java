package in.testpress.sample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import java.io.IOException;

import in.testpress.core.TestpressSessionManager;
import in.testpress.models.AuthToken;
import in.testpress.util.Callback;
import in.testpress.util.Toaster;

public class MainActivity extends AppCompatActivity {

    EditText usernameEditText;
    EditText passwordEditText;
    View loginView;
    View clearButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        loginView = findViewById(R.id.scroll_view);
        usernameEditText = (EditText) findViewById(R.id.username);
        passwordEditText = (EditText) findViewById(R.id.password);
        clearButton = findViewById(R.id.clear_token_button);
        if (TestpressSessionManager.getInstance(this).hasActiveSession()) {
            // Already had authorization
            loginView.setVisibility(View.GONE);
            clearButton.setVisibility(View.VISIBLE);
        } else {
            // Need to authenticate
            loginView.setVisibility(View.VISIBLE);
            clearButton.setVisibility(View.GONE);
        }
    }

    public void authenticate(View view) {
        Callback<AuthToken> callback = new Callback<AuthToken>() {
            @Override
            public void onSuccess(AuthToken response) {
                loginView.setVisibility(View.GONE);
                clearButton.setVisibility(View.VISIBLE);
                Toaster.showShort(MainActivity.this, "Token Generated Successfully");
            }

            @Override
            public void onException(Exception e) {
                if (e.getCause() instanceof IOException) {
                    Toaster.showShort(MainActivity.this, R.string.no_internet_try_again);
                } else {
                    Toaster.showShort(MainActivity.this, "Token Generation Failed");
                }
            }
        };
        TestpressSessionManager.getInstance(this).createSession(usernameEditText.getText()
                .toString().trim(), passwordEditText.getText().toString().trim(), callback);
    }

    public void clearToken(View view) {
        TestpressSessionManager.getInstance(this).clearActiveSession();
        loginView.setVisibility(View.VISIBLE);
        clearButton.setVisibility(View.GONE);
    }
}
