package in.testpress.samples.course;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import in.testpress.core.TestpressSdk;
import in.testpress.core.TestpressSession;
import in.testpress.course.TestpressCourse;
import in.testpress.course.fragments.CourseContentListFragment;
import in.testpress.course.ui.LeaderboardFragment;
import in.testpress.database.entities.CourseContentType;
import in.testpress.samples.BaseNavigationDrawerActivity;
import in.testpress.samples.R;
import in.testpress.samples.core.TestpressCoreSampleActivity;
import in.testpress.util.ViewUtils;

import static in.testpress.course.fragments.CourseContentListFragment.COURSE_CONTENT_TYPE;
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
            case R.id.running_content:
                showSDK(3);
                break;
            case R.id.upcoming_content:
                showSDK(4);
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
                    .setMaxAllowedDownloadedVideos(null)
                    .setWhiteLabeledHostUrl(session.getInstituteSettings().getBaseUrl())
                    .setAndroidSentryDns("https://186f9948d5294b4c9c03aa9d2a11c982@sentry.testpress.in/3")
                    .setAppName(getString(R.string.app_name));
            TestpressSdk.setTestpressSession(this, session);
            if (position == 1) {
                TestpressCourse.show(this, R.id.fragment_container, session);
            } else if (position == 2){
                TestpressCourse.showLeaderboard(this, R.id.fragment_container, session);
            } else if (position == 3) {
                launchCourseContentFragment(CourseContentType.RUNNING_CONTENT.ordinal());
            }else {
                launchCourseContentFragment(CourseContentType.UPCOMING_CONTENT.ordinal());
            }
        } else {
            Intent intent = new Intent(this, TestpressCoreSampleActivity.class);
            startActivityForResult(intent, AUTHENTICATE_REQUEST_CODE);
        }
    }
    private void launchCourseContentFragment(int type){
        ViewUtils.showInputDialogBox(NavigationDrawerActivity.this, "Enter Course ID",
                new ViewUtils.OnInputCompletedListener() {
                    @Override
                    public void onInputComplete(String inputText) {
                        Bundle bundle = new Bundle();
                        bundle.putInt(COURSE_CONTENT_TYPE,type);
                        bundle.putString("courseId",inputText);
                        CourseContentListFragment fragment = new CourseContentListFragment();
                        fragment.setArguments(bundle);
                        NavigationDrawerActivity.this.getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container,fragment)
                                .commitAllowingStateLoss();
                    }
                });

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
