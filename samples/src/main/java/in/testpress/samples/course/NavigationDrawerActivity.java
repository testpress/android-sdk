package in.testpress.samples.course;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.MenuItem;

import com.facebook.login.LoginManager;

import in.testpress.core.TestpressSdk;
import in.testpress.course.TestpressCourse;
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
        navigationView.getMenu().getItem(1).setTitle(getString(R.string.courses));
    }

    @Override
    protected void onDrawerItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.exams:
                if (TestpressSdk.hasActiveSession(this)) {
                    displayExams();
                } else {
                    Intent intent = new Intent(NavigationDrawerActivity.this,
                            TestpressCoreSampleActivity.class);
                    startActivityForResult(intent, AUTHENTICATE_REQUEST_CODE);
                }
                selectedItem = 1;
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

    private void displayExams() {
        //noinspection ConstantConditions
        TestpressCourse.show(this, R.id.fragment_container, TestpressSdk.getTestpressSession(this));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AUTHENTICATE_REQUEST_CODE && resultCode == RESULT_OK) {
            displayExams();
            logoutMenu.setVisible(true);
        }
    }

}
