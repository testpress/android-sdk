package in.testpress.samples.exam;

import android.content.Intent;
import android.view.MenuItem;

import in.testpress.core.TestpressSdk;
import in.testpress.core.TestpressSession;
import in.testpress.exam.TestpressExam;
import in.testpress.samples.BaseNavigationDrawerActivity;
import in.testpress.samples.R;
import in.testpress.samples.core.LoginActivity;

import static in.testpress.samples.core.LoginActivity.AUTHENTICATE_REQUEST_CODE;

public class NavigationDrawerActivity extends BaseNavigationDrawerActivity {

    @Override
    protected int getNavigationViewMenu() {
        return R.menu.exam_drawer_items;
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
        }
        super.onDrawerItemSelected(menuItem);
    }

    private void showSDK(int position) {
        selectedItem = position;
        if (TestpressSdk.hasActiveSession(this)) {
            TestpressSession session = TestpressSdk.getTestpressSession(this);
            //noinspection ConstantConditions
            session.getInstituteSettings()
                    .setBookmarksEnabled(false)
                    .setCommentsVotingEnabled(false)
                    .setCoursesFrontend(false)
                    .setCoursesGamificationEnabled(false);
            TestpressSdk.setTestpressSession(this, session);
            if (position == 1) {
                TestpressExam.show(this, R.id.fragment_container, session);
            } else {
                TestpressExam.showCategories(this, R.id.fragment_container, session);
            }
        } else {
            Intent intent = new Intent(NavigationDrawerActivity.this,
                    LoginActivity.class);
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
