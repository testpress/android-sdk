package in.testpress.samples.course;

import android.content.Intent;
import android.view.MenuItem;

import in.testpress.core.TestpressSdk;
import in.testpress.core.TestpressSession;
import in.testpress.course.TestpressCourse;
import in.testpress.samples.BaseNavigationDrawerActivity;
import in.testpress.samples.R;
import in.testpress.samples.core.TestpressCoreSampleActivity;

import static in.testpress.samples.core.TestpressCoreSampleActivity.AUTHENTICATE_REQUEST_CODE;

public class NavigationDrawerActivity extends BaseNavigationDrawerActivity {

    @Override
    protected int getNavigationViewMenu() {
        return R.menu.course_drawer_items;
    }

    @Override
    protected void onDrawerItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.courses:
                showSDK(1);
                break;
            case R.id.leaderboard:
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
                    .setScreenshotDisabled(true)
                    .setDisplayUserEmailOnVideo(false)
                    .setCoursesGamificationEnabled(false)
                    .setAppName(getString(R.string.app_name));
            TestpressSdk.setTestpressSession(this, session);
            if (position == 1) {
                TestpressCourse.show(this, R.id.fragment_container, session);
            } else {
                TestpressCourse.showLeaderboard(this, R.id.fragment_container, session);
            }
        } else {
            Intent intent = new Intent(this, TestpressCoreSampleActivity.class);
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
