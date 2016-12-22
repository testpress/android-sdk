package in.testpress.samples.exam;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.MenuItem;

import com.facebook.login.LoginManager;

import in.testpress.core.TestpressSdk;
import in.testpress.exam.TestpressExam;
import in.testpress.samples.BaseNavigationDrawerActivity;
import in.testpress.samples.HomeFragment;
import in.testpress.samples.R;
import in.testpress.samples.core.TestpressCoreSampleActivity;

import static in.testpress.samples.core.TestpressCoreSampleActivity.AUTHENTICATE_REQUEST_CODE;

public class NavigationDrawerActivity extends BaseNavigationDrawerActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_drawer);
        navigationView.getMenu().getItem(2).setVisible(true);
    }

    @Override
    protected void onDrawerItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.exams:
                showSDK(1);
                break;
            case R.id.exams_categories:
                showSDK(2);
                break;
            case R.id.logout:
                TestpressSdk.clearActiveSession(this);
                LoginManager.getInstance().logOut();
                finish();
                break;
        }
        super.onDrawerItemSelected(menuItem);
    }

    @Override
    protected void displayHome() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new HomeFragment())
                .commit();
    }

    private void showSDK(int position) {
        selectedItem = position;
        if (TestpressSdk.hasActiveSession(this)) {
            if (position == 1) {
                //noinspection ConstantConditions
                TestpressExam.show(this, R.id.fragment_container,
                        TestpressSdk.getTestpressSession(this));
            } else {
                //noinspection ConstantConditions
                TestpressExam.showCategories(this, R.id.fragment_container,
                        TestpressSdk.getTestpressSession(this));
            }
        } else {
            Intent intent = new Intent(NavigationDrawerActivity.this,
                    TestpressCoreSampleActivity.class);
            startActivityForResult(intent, AUTHENTICATE_REQUEST_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AUTHENTICATE_REQUEST_CODE && resultCode == RESULT_OK) {
            showSDK(selectedItem);
            logoutMenu.setVisible(true);
        }
    }

}
