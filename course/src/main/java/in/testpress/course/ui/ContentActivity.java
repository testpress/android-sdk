package in.testpress.course.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import org.jetbrains.annotations.NotNull;

import in.testpress.course.R;
import in.testpress.course.domain.DomainContent;
import in.testpress.course.fragments.ContentFragmentChangeListener;
import in.testpress.course.fragments.ContentFragmentFactory;
import in.testpress.course.fragments.ContentLoadingFragment;
import in.testpress.ui.BaseToolBarActivity;

import static in.testpress.course.TestpressCourse.CONTENT_TYPE;
import static in.testpress.course.TestpressCourse.PRODUCT_SLUG;

public class ContentActivity extends BaseToolBarActivity implements ContentFragmentChangeListener {

    public static final String ACTIONBAR_TITLE = "title";
    public static final String TESTPRESS_CONTENT_SHARED_PREFS = "testpressContentSharedPreferences";
    public static final String FORCE_REFRESH = "forceRefreshContentList";
    public static final String GO_TO_MENU = "gotoMenu";
    public static final String CONTENT_ID = "contentId";
    public static final String CHAPTER_ID = "chapterId";
    public static final String POSITION = "position";

    public static Intent createIntent(int position, long chapterId, AppCompatActivity activity, String productSlug) {
        Intent intent = new Intent(activity, ContentActivity.class);
        intent.putExtra(POSITION, position);
        intent.putExtra(ACTIONBAR_TITLE, activity.getSupportActionBar().getTitle());
        intent.putExtra(CHAPTER_ID, chapterId);
        intent.putExtra(PRODUCT_SLUG, productSlug);
        return intent;
    }

    public static Intent createIntent(Long contentId, Context context) {
        Intent intent = new Intent(context, ContentActivity.class);
        intent.putExtra(CONTENT_ID, contentId);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_detail_activity);
        Bundle bundle = getIntent().getExtras();
        ContentLoadingFragment fragment = new ContentLoadingFragment();
        fragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment).commitAllowingStateLoss();
    }

    @Override
    public void changeFragment(@NotNull DomainContent content) {
        Fragment fragment = ContentFragmentFactory.Companion.getFragment(content);
        Bundle bundle = new Bundle();
        bundle.putLong(CONTENT_ID, content.getId());
        bundle.putString(CONTENT_TYPE, content.getContentType());
        fragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment).commitAllowingStateLoss();
    }
}