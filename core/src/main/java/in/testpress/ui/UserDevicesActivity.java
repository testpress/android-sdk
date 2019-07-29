package in.testpress.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import in.testpress.R;
import in.testpress.core.TestpressCallback;
import in.testpress.core.TestpressException;
import in.testpress.core.TestpressSdk;
import in.testpress.network.TestpressApiClient;

public class UserDevicesActivity extends BaseToolBarActivity {

    private static final String TAG = "UserDevicesActivity";
    private Button logoutDevicesButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_devices);
        final UserActivityFragment accountActivityFragment = new UserActivityFragment();
        Bundle bundle = getIntent().getExtras();
        accountActivityFragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, accountActivityFragment).commitAllowingStateLoss();

        getSupportActionBar().setTitle("Login Activity");

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
    }

}
