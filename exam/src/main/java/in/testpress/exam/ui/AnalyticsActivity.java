package in.testpress.exam.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import in.testpress.exam.R;
import in.testpress.ui.BaseToolBarActivity;

import static in.testpress.exam.ui.AnalyticsFragment.ANALYTICS_URL_FRAG;
import static in.testpress.exam.ui.AnalyticsFragment.PARENT_SUBJECT_ID;

public class AnalyticsActivity extends BaseToolBarActivity {

    public static final String ACTIONBAR_TITLE = "actionbar_title";

    public static Intent createIntent(Activity activity, String analyticsUrlFrag,
                                      String parentSubjectId, String actionbarTitle) {
        Intent intent = new Intent(activity, AnalyticsActivity.class);
        intent.putExtra(ANALYTICS_URL_FRAG, analyticsUrlFrag);
        intent.putExtra(ACTIONBAR_TITLE, actionbarTitle);
        intent.putExtra(PARENT_SUBJECT_ID, parentSubjectId);
        return intent;
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.testpress_container_layout);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        AnalyticsFragment analyticsFragment = new AnalyticsFragment();
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            analyticsFragment.setArguments(bundle);
            String title = bundle.getString(ACTIONBAR_TITLE);
            if (title != null) {
                getSupportActionBar().setTitle(title);
            }
        }
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, analyticsFragment).commitAllowingStateLoss();
    }

}
