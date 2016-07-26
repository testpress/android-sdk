package in.testpress.sample;

import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import java.io.IOException;

import in.testpress.core.TestpressSdk;
import in.testpress.core.TestpressAuthToken;
import in.testpress.core.TestpressCallback;

public class MainActivity extends AppCompatActivity {

    EditText usernameEditText;
    EditText passwordEditText;
    View loginView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        loginView = findViewById(R.id.scroll_view);
        usernameEditText = (EditText) findViewById(R.id.username);
        passwordEditText = (EditText) findViewById(R.id.password);
    }

    public void authenticate(View view) {
        TestpressSdk.initialize(this, "http://demo.testpress.in", usernameEditText.getText().toString().trim(),
                passwordEditText.getText().toString().trim(), new TestpressCallback<TestpressAuthToken>() {
            @Override
            public void onSuccess(TestpressAuthToken response) {
                Snackbar.make(loginView, "Token Generated Successfully", Snackbar.LENGTH_LONG).show();
            }

            @Override
            public void onException(Exception e) {
                if (e.getCause() instanceof IOException) {
                    Snackbar.make(loginView, R.string.no_internet_try_again,
                            Snackbar.LENGTH_LONG).show();
                } else {
                    Snackbar.make(loginView, "Token Generation Failed", Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }

}
