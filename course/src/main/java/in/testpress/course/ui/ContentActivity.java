package in.testpress.course.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import in.testpress.course.R;
import in.testpress.course.ui.fragments.ContentDetailFragment;
import in.testpress.ui.BaseToolBarActivity;

import static in.testpress.course.TestpressCourse.PRODUCT_SLUG;

public class ContentActivity extends BaseToolBarActivity {

    public static final String ACTIONBAR_TITLE = "title";
    public static final String TESTPRESS_CONTENT_SHARED_PREFS = "testpressContentSharedPreferences";
    public static final String FORCE_REFRESH = "forceRefreshContentList";
    public static final String GO_TO_MENU = "gotoMenu";
    public static final String CONTENT_ID = "contentId";
    public static final String CHAPTER_ID = "chapterId";
    public static final String POSITION = "position";

    private ContentDetailFragment contentDetailFragment;

    public static Intent createIntent(int position, long chapterId, AppCompatActivity activity, String productSlug) {
        Intent intent = new Intent(activity, ContentActivity.class);
        intent.putExtra(POSITION, position);
        intent.putExtra(ACTIONBAR_TITLE, activity.getSupportActionBar().getTitle());
        intent.putExtra(CHAPTER_ID, chapterId);
        intent.putExtra(PRODUCT_SLUG, productSlug);
        return intent;
    }

    public static Intent createIntent(String contentId, Context context) {
        Intent intent = new Intent(context, ContentActivity.class);
        intent.putExtra(CONTENT_ID, contentId);
        return intent;
    }

    @SuppressWarnings({"ConstantConditions", "deprecation"})
    @SuppressLint({"SetJavaScriptEnabled", "AddJavascriptInterface", "DefaultLocale", "ShowToast"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.content_detail_activity);
        contentDetailFragment = new ContentDetailFragment();
        Bundle bundle = getIntent().getExtras();
        contentDetailFragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, contentDetailFragment).commitAllowingStateLoss();
    }

    @Override
    public void onBackPressed() {
        contentDetailFragment.updateVideoContentAttempt();
        super.onBackPressed();
    }
}