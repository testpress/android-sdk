package in.testpress.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import in.testpress.R;
import in.testpress.core.TestpressCallback;
import in.testpress.core.TestpressException;
import in.testpress.core.TestpressSdk;
import in.testpress.core.TestpressSession;
import in.testpress.network.TestpressApiClient;

import static in.testpress.network.TestpressApiClient.PARAM_SHOW_PARALLEL_LOGIN_INFO;

public class UserDevicesActivity extends BaseToolBarActivity {

    private static final String TAG = "UserDevicesActivity";
    private Button logoutDevicesButton;
    private Button cancelButton;
    private TextView parallelLoginRestrictionInfo;
    private UserActivityFragment accountActivityFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_devices);
        accountActivityFragment = new UserActivityFragment();
        Bundle bundle = getIntent().getExtras();
        accountActivityFragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, accountActivityFragment).commitAllowingStateLoss();

        getSupportActionBar().setTitle("Login Activity");
        configureButtons();
        setInfoText();
    }

    public void configureButtons() {
        logoutDevicesButton = (Button) findViewById(R.id.logout_devices_button);
        logoutDevicesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TestpressApiClient apiClient = new TestpressApiClient(getApplicationContext(), TestpressSdk.getTestpressSession(getApplicationContext()));
                apiClient.logoutDevices().enqueue(new TestpressCallback<Void>() {

                    @Override
                    public void onSuccess(Void result) {
                        Toast.makeText(getApplicationContext(), "Logged out successfully", Toast.LENGTH_LONG).show();
                        accountActivityFragment.refreshAdapter();
                    }

                    @Override
                    public void onException(TestpressException testpressException) {
                        Toast.makeText(getApplicationContext(), "Error Occurred. Try again later.", Toast.LENGTH_LONG).show();
                    }
                });

            }
        });

        cancelButton = (Button) findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    public void setInfoText() {
        parallelLoginRestrictionInfo = (TextView) findViewById(R.id.parallel_login_restriction_note);
        TestpressSession session = TestpressSdk.getTestpressSession(this);
        String info = getString(R.string.lockout_limit_info);

        Intent intent = getIntent();

        Bundle extras = intent.getExtras();
        if (extras != null) {
            if (extras.containsKey(PARAM_SHOW_PARALLEL_LOGIN_INFO)) {
                boolean showParallelLoginRestrictionInfo = extras.getBoolean("isNewItem", false);
                if (showParallelLoginRestrictionInfo) {
                    info = getString(R.string.parallel_login_restriction_message);
                    parallelLoginRestrictionInfo.setVisibility(View.VISIBLE);
                    parallelLoginRestrictionInfo.setText(info);
                }
            }
        }


        if (session.getInstituteSettings().getLockoutLimit() != null) {
            parallelLoginRestrictionInfo.setVisibility(View.VISIBLE);
            parallelLoginRestrictionInfo.setText(String.format(info, session.getInstituteSettings().getLockoutLimit()));
        }
    }

}
