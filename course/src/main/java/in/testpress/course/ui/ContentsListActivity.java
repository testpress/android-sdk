package in.testpress.course.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import junit.framework.Assert;

import in.testpress.core.TestpressCallback;
import in.testpress.core.TestpressException;
import in.testpress.core.TestpressSDKDatabase;
import in.testpress.course.R;
import in.testpress.models.greendao.Chapter;
import in.testpress.course.network.TestpressCourseApiClient;
import in.testpress.models.greendao.ChapterDao;
import in.testpress.ui.BaseToolBarActivity;
import in.testpress.util.UIUtils;

import static in.testpress.course.TestpressCourse.CHAPTER_URL;
import static in.testpress.course.TestpressCourse.COURSE_ID;
import static in.testpress.course.TestpressCourse.PARENT_ID;
import static in.testpress.course.ui.ContentActivity.FORCE_REFRESH;
import static in.testpress.course.ui.ContentActivity.GO_TO_MENU;
import static in.testpress.course.ui.ContentActivity.TESTPRESS_CONTENT_SHARED_PREFS;
import static in.testpress.exam.ui.CarouselFragment.TEST_TAKEN_REQUEST_CODE;

public class ContentsListActivity extends BaseToolBarActivity {

    public static final String CONTENTS_URL_FRAG = "contentsUrlFrag";
    public static final String ACTIONBAR_TITLE = "title";
    public static final String CHAPTER_ID = "chapterId";

    private Chapter chapter;
    private SharedPreferences prefs;
    private LinearLayout emptyView;
    private TextView emptyTitleView;
    private TextView emptyDescView;
    private ProgressBar progressBar;
    private Button retryButton;
    private Activity activity;

    public static Intent createIntent(String title, String contentsUrlFrag, Context context, Long idOfChapter) {
        Intent intent = new Intent(context, ContentsListActivity.class);
        intent.putExtra(ACTIONBAR_TITLE, title);
        intent.putExtra(CONTENTS_URL_FRAG, contentsUrlFrag);
        intent.putExtra(CHAPTER_ID, idOfChapter);
        return intent;
    }

    public static Intent createIntent(String title, String contentsUrlFrag, Context context) {
        Intent intent = new Intent(context, ContentsListActivity.class);
        intent.putExtra(ACTIONBAR_TITLE, title);
        intent.putExtra(CONTENTS_URL_FRAG, contentsUrlFrag);
        return intent;
    }

    public static Intent createIntent(String chaptersUrl, Context context) {
        Intent intent = new Intent(context, ContentsListActivity.class);
        intent.putExtra(CHAPTER_URL, chaptersUrl);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.testpress_container_layout);
        prefs = getSharedPreferences(TESTPRESS_CONTENT_SHARED_PREFS, Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
        String chapterUrl = getIntent().getStringExtra(CHAPTER_URL);
        activity = this;
        if (chapterUrl != null) {
            emptyView = (LinearLayout) findViewById(R.id.empty_container);
            emptyTitleView = (TextView) findViewById(R.id.empty_title);
            emptyDescView = (TextView) findViewById(R.id.empty_description);
            retryButton = (Button) findViewById(R.id.retry_button);
            progressBar = (ProgressBar) findViewById(R.id.pb_loading);
            UIUtils.setIndeterminateDrawable(this, progressBar, 4);
            loadChapter(chapterUrl);
        } else {
            String title = getIntent().getStringExtra(ACTIONBAR_TITLE);
            Assert.assertNotNull("ACTIONBAR_TITLE must not be null.", title);
            //noinspection ConstantConditions
            getSupportActionBar().setTitle(title);
            loadContents();
        }
    }

    private void loadContents() {
        ContentsListFragment fragment = new ContentsListFragment();
        fragment.setArguments(getIntent().getExtras());
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment)
                .commit();
    }

    void loadChapter(final String chapterUrl) {
        progressBar.setVisibility(View.VISIBLE);
        new TestpressCourseApiClient(this).getChapter(chapterUrl)
                .enqueue(new TestpressCallback<Chapter>() {
                    @Override
                    public void onSuccess(Chapter chapter) {
                        progressBar.setVisibility(View.GONE);
                        ContentsListActivity.this.chapter = chapter;
                        saveChapterInDB(chapter);
                        //noinspection ConstantConditions
                        getSupportActionBar().setTitle(chapter.getName());
                        getIntent().putExtra(CONTENTS_URL_FRAG, chapter.getContentUrl());
                        loadContents();
                    }

                    @Override
                    public void onException(TestpressException exception) {
                        if (exception.isUnauthenticated()) {
                            setEmptyText(R.string.testpress_authentication_failed,
                                    R.string.testpress_exam_no_permission);
                            retryButton.setVisibility(View.GONE);
                        } else if (exception.isNetworkError()) {
                            setEmptyText(R.string.testpress_network_error,
                                    R.string.testpress_no_internet_try_again);
                            retryButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    emptyView.setVisibility(View.GONE);
                                    loadChapter(chapterUrl);
                                }
                            });
                        } else if (exception.getResponse().code() == 404) {
                            setEmptyText(R.string.testpress_chapter_not_available,
                                    R.string.testpress_chapter_not_available_description);
                            retryButton.setVisibility(View.GONE);
                        } else  {
                            setEmptyText(R.string.testpress_error_loading_exam,
                                    R.string.testpress_some_thing_went_wrong_try_again);
                            retryButton.setVisibility(View.GONE);
                        }
                    }
                });
    }

    private void saveChapterInDB(Chapter chapter) {
        ChapterDao chapterDao = TestpressSDKDatabase.getChapterDao(activity);
        chapterDao.insertOrReplace(chapter);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == TEST_TAKEN_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                loadContents();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (prefs.getBoolean(GO_TO_MENU, false)) {
            prefs.edit().putBoolean(GO_TO_MENU, false).apply();
            finish();
        } else if (prefs.getBoolean(FORCE_REFRESH, false)) {
            prefs.edit().putBoolean(FORCE_REFRESH, false).apply();
            loadContents();
        }
    }

    protected void setEmptyText(final int title, final int description) {
        emptyView.setVisibility(View.VISIBLE);
        emptyTitleView.setText(title);
        emptyDescView.setText(description);
        progressBar.setVisibility(View.GONE);
    }

    @Override
    protected Bundle getDataToSetResult() {
        Bundle data = super.getDataToSetResult();
        if (chapter != null) {
            // Pass chapter's parent id & course id
            data.putString(COURSE_ID, chapter.getCourseId().toString());
            data.putString(PARENT_ID, String.valueOf(chapter.getParentId()));
        }
        return data;
    }

}
