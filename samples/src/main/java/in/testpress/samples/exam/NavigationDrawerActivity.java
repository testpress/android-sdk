package in.testpress.samples.exam;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import in.testpress.core.TestpressSdk;
import in.testpress.core.TestpressSession;
import in.testpress.exam.TestpressExam;
import in.testpress.exam.ui.ReportQuestionFragment;
import in.testpress.samples.BaseNavigationDrawerActivity;
import in.testpress.samples.R;
import in.testpress.samples.core.TestpressCoreSampleActivity;
import in.testpress.util.ViewUtils;

import static in.testpress.samples.core.TestpressCoreSampleActivity.AUTHENTICATE_REQUEST_CODE;

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
            case R.id.report_question:
                showSDK(3);
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
            } else if (position == 2) {
                TestpressExam.showCategories(this, R.id.fragment_container, session);
            } else {
                launchReportQuestionFragment();
            }
        } else {
            Intent intent = new Intent(NavigationDrawerActivity.this,
                    TestpressCoreSampleActivity.class);
            startActivityForResult(intent, AUTHENTICATE_REQUEST_CODE);
        }
    }

    private void launchReportQuestionFragment() {
        ViewUtils.showInputDialogBox(NavigationDrawerActivity.this, "Enter Course ID",
                new ViewUtils.OnInputCompletedListener() {
                    @Override
                    public void onInputComplete(String inputText) {
                        ReportQuestionFragment.Companion.show(
                                NavigationDrawerActivity.this,
                                R.id.fragment_container,
                                10,
                                Long.parseLong(inputText),
                                787
                        );
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
