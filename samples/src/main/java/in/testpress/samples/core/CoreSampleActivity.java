package in.testpress.samples.core;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import in.testpress.core.TestpressSdk;
import in.testpress.core.TestpressSession;
import in.testpress.samples.BaseToolBarActivity;
import in.testpress.samples.R;
import in.testpress.ui.UserDevicesActivity;

public class CoreSampleActivity extends BaseToolBarActivity {
    private int selectedItem;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_core);

        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        findViewById(R.id.login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CoreSampleActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
        findViewById(R.id.login_activity_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSDK(view.getId());
            }
        });
    }

    private void showSDK(int clickedButtonId) {
        selectedItem = clickedButtonId;
        if (TestpressSdk.hasActiveSession(this)) {
            TestpressSession session = TestpressSdk.getTestpressSession(this);
            //noinspection ConstantConditions
            session.getInstituteSettings()
                    .setBookmarksEnabled(true)
                    .setCommentsVotingEnabled(false)
                    .setCoursesFrontend(false)
                    .setCoursesGamificationEnabled(false);
            TestpressSdk.setTestpressSession(this, session);
            switch (clickedButtonId) {
                case R.id.login_activity_button:
                    Intent intent = new Intent(this, UserDevicesActivity.class);
                    this.startActivity(intent);
                default:
                    break;
            }
        }
    }
}
