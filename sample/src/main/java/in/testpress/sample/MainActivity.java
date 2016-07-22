package in.testpress.sample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import in.testpress.core.TestpressAccountAuthenticator;
import in.testpress.models.AuthToken;
import in.testpress.util.Toaster;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    EditText usernameEditText;
    EditText passwordEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        usernameEditText = (EditText) findViewById(R.id.username);
        passwordEditText = (EditText) findViewById(R.id.password);
    }

    public void authenticate(View view) {
        Callback<AuthToken> callback = new Callback<AuthToken>() {
            @Override
            public void onResponse(Call<AuthToken> call, Response<AuthToken> response) {
                Toaster.showShort(MainActivity.this, "Token Generated Successfully");
            }

            @Override
            public void onFailure(Call<AuthToken> call, Throwable throwable) {
                Toaster.showShort(MainActivity.this, "Token Generation Failed");
            }
        };
        new TestpressAccountAuthenticator().authenticate(this, usernameEditText.getText().toString().trim(),
                passwordEditText.getText().toString().trim(), callback);
    }
}
