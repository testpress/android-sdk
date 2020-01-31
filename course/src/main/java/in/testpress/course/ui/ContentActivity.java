package in.testpress.course.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import in.testpress.core.TestpressSDKDatabase;
import in.testpress.course.R;
import in.testpress.course.ui.fragments.ContentDetailFragment;
import in.testpress.course.ui.fragments.content_detail_fragments.AttachmentContentFragment;
import in.testpress.course.ui.fragments.content_detail_fragments.ExamContentFragment;
import in.testpress.course.ui.fragments.content_detail_fragments.HtmlContentFragment;
import in.testpress.course.ui.fragments.content_detail_fragments.VideoContentFragment;
import in.testpress.models.greendao.Content;
import in.testpress.models.greendao.ContentDao;
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
        Bundle bundle = getIntent().getExtras();

        if (getContent() != null && getContent().getExamId() != null) {
            ExamContentFragment examContentFragment = new ExamContentFragment();
            examContentFragment.setArguments(bundle);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, examContentFragment).commitAllowingStateLoss();

        } else if (getContent() != null && getContent().getHtmlId() != null) {
            HtmlContentFragment htmlContentFragment = new HtmlContentFragment();
            htmlContentFragment.setArguments(bundle);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, htmlContentFragment).commitAllowingStateLoss();
        } else if (getContent() != null && getContent().getAttachmentId() != null) {
            AttachmentContentFragment fragment = new AttachmentContentFragment();
            fragment.setArguments(bundle);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment).commitAllowingStateLoss();
        } else if (getContent() != null && getContent().getVideoId() != null) {
            VideoContentFragment fragment = new VideoContentFragment();
            fragment.setArguments(bundle);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment).commitAllowingStateLoss();
        }
        else {
            contentDetailFragment = new ContentDetailFragment();
            contentDetailFragment.setArguments(bundle);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, contentDetailFragment).commitAllowingStateLoss();
        }
    }

    private Content getContent() {
        List<Content> contents = new ArrayList<>();
        long chapterId = getIntent().getLongExtra(CHAPTER_ID, -1);
        int position = getIntent().getIntExtra(POSITION, -1);
        if (chapterId != -1 && position != -1 ) {
            contents = getContentsFromDB(chapterId);
            if (!contents.isEmpty()) {
                return contents.get(position);
            }
        }

        if (contents == null || contents.isEmpty()) {
            String contentId = getIntent().getStringExtra(CONTENT_ID);
        }

        return null;
    }

    List<Content> getContentsFromDB(long chapterId) {
        ContentDao contentDao = TestpressSDKDatabase.getContentDao(this);

        return contentDao.queryBuilder()
                .where(
                        ContentDao.Properties.ChapterId.eq(chapterId),
                        ContentDao.Properties.Active.eq(true)
                )
                .orderAsc(ContentDao.Properties.Order).list();
    }


    @Override
    public void onBackPressed() {
//        contentDetailFragment.updateVideoContentAttempt();
        super.onBackPressed();
    }
}