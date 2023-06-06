package in.testpress.exam.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.MenuItem;

import in.testpress.core.TestpressSDKDatabase;
import in.testpress.core.TestpressSdk;
import in.testpress.exam.R;
import in.testpress.models.InstituteSettings;
import in.testpress.models.greendao.Attempt;
import in.testpress.models.greendao.CourseAttempt;
import in.testpress.models.greendao.CourseAttemptDao;
import in.testpress.models.greendao.Exam;
import in.testpress.models.greendao.ExamDao;
import in.testpress.ui.BaseToolBarActivity;

import static in.testpress.exam.ui.ReviewStatsFragment.PARAM_SHOW_RETAKE_BUTTON;
import static in.testpress.exam.ui.ReviewStatsFragment.SHARE_APP;

public class ReviewStatsActivity extends BaseToolBarActivity {

    static final String PARAM_PREVIOUS_ACTIVITY = "previousActivity";
    static final String PARAM_EXAM = "exam";
    static final String PARAM_ATTEMPT = "attempt";
    static final String PARAM_COURSE_ATTEMPT = "courseAttempt";

    boolean parentIsTestEngine;

    public static Intent createIntent(Activity activity, Exam exam, Attempt attempt) {
        Intent intent = new Intent(activity, ReviewStatsActivity.class);
        intent.putExtra(PARAM_PREVIOUS_ACTIVITY, activity.getClass().getName());
        intent.putExtra(PARAM_EXAM, exam);
        intent.putExtra(PARAM_ATTEMPT, attempt);
        intent.putExtra(PARAM_SHOW_RETAKE_BUTTON, true);
        return intent;
    }

    public static Intent createIntent(Activity activity, Long examId, Long contentAttemptId) {
        ExamDao examDao = TestpressSDKDatabase.getExamDao(activity);
        Exam exam = examDao.queryBuilder().where(ExamDao.Properties.Id.eq(examId)).build().list().get(0);
        CourseAttemptDao courseAttemptDao = TestpressSDKDatabase.getCourseAttemptDao(activity);
        CourseAttempt courseAttempt = courseAttemptDao.queryBuilder()
                .where(CourseAttemptDao.Properties.Id.eq(contentAttemptId)).list().get(0);

        Intent intent = new Intent(activity, ReviewStatsActivity.class);
        intent.putExtra(PARAM_PREVIOUS_ACTIVITY, activity.getClass().getName());
        intent.putExtra(PARAM_EXAM, exam);
        intent.putExtra(PARAM_COURSE_ATTEMPT, courseAttempt);
        intent.putExtra(PARAM_SHOW_RETAKE_BUTTON, false);
        return intent;
    }

    public static Intent createIntent(Activity activity, Exam exam, CourseAttempt courseAttempt) {
        Intent intent = new Intent(activity, ReviewStatsActivity.class);
        intent.putExtra(PARAM_PREVIOUS_ACTIVITY, activity.getClass().getName());
        intent.putExtra(PARAM_EXAM, exam);
        intent.putExtra(PARAM_COURSE_ATTEMPT, courseAttempt);
        intent.putExtra(PARAM_SHOW_RETAKE_BUTTON, false);
        return intent;
    }

    public static Intent createIntent(Activity activity, Attempt attempt) {
        Intent intent = new Intent(activity, ReviewStatsActivity.class);
        intent.putExtra(PARAM_PREVIOUS_ACTIVITY, activity.getClass().getName());
        intent.putExtra(PARAM_ATTEMPT, attempt);
        intent.putExtra(PARAM_SHOW_RETAKE_BUTTON, false);
        return intent;
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.testpress_container_layout);
        String previousActivity = getIntent().getStringExtra(PARAM_PREVIOUS_ACTIVITY);
        parentIsTestEngine = (previousActivity != null) &&
                previousActivity.equals(TestActivity.class.getName());

        //noinspection ConstantConditions
        InstituteSettings instituteSettings =
                TestpressSdk.getTestpressSession(this).getInstituteSettings();

        Fragment fragment;
        CourseAttempt courseAttempt = getIntent().getParcelableExtra(PARAM_COURSE_ATTEMPT);
        if (parentIsTestEngine && instituteSettings.isCoursesFrontend() &&
                instituteSettings.isCoursesGamificationEnabled() && courseAttempt != null) {

            fragment = new TrophiesAchievedFragment();
        } else {
            fragment = new ReviewStatsFragment();
        }
        fragment.setArguments(getIntent().getExtras());
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commitAllowingStateLoss();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == CarouselFragment.TEST_TAKEN_REQUEST_CODE)) {
            setResult(RESULT_OK);
            finish();
        } else if ((requestCode == SHARE_APP) && (resultCode == RESULT_OK)) {
            Exam exam = getIntent().getParcelableExtra(PARAM_EXAM);
            CourseAttempt courseAttempt = getIntent().getParcelableExtra(PARAM_COURSE_ATTEMPT);
            startActivity(
                    ReviewQuestionsActivity.createIntent(this, exam, courseAttempt.getRawAssessment())
            );
        }
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (parentIsTestEngine) {
            // OnBackPressed go to history
            setResult(RESULT_OK);
            finish();
        } else {
            super.onBackPressed();
        }
    }
}
