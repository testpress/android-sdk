package in.testpress.course.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

import in.testpress.core.TestpressCallback;
import in.testpress.core.TestpressException;
import in.testpress.core.TestpressSDKDatabase;
import in.testpress.core.TestpressSdk;
import in.testpress.course.R;
import in.testpress.course.network.TestpressCourseApiClient;
import in.testpress.models.InstituteSettings;
import in.testpress.models.greendao.Chapter;
import in.testpress.models.greendao.ChapterDao;
import in.testpress.models.greendao.Course;
import in.testpress.models.greendao.CourseDao;
import in.testpress.network.RetrofitCall;
import in.testpress.ui.BaseToolBarActivity;
import in.testpress.util.UIUtils;

import static in.testpress.course.TestpressCourse.CHAPTER_SLUG;
import static in.testpress.course.TestpressCourse.CHAPTER_URL;
import static in.testpress.course.TestpressCourse.COURSE_ID;
import static in.testpress.course.TestpressCourse.PARENT_ID;
import static in.testpress.course.ui.ContentActivity.FORCE_REFRESH;
import static in.testpress.course.ui.ContentActivity.GO_TO_MENU;
import static in.testpress.course.ui.ContentActivity.TESTPRESS_CONTENT_SHARED_PREFS;

public class ChapterDetailActivity extends BaseToolBarActivity {

    public static final String CONTENTS_URL_FRAG = "contentsUrlFrag";
    public static final String CHAPTER_ID = "chapterId";

    private Chapter chapter;
    private SharedPreferences prefs;
    private LinearLayout emptyView;
    private TextView emptyTitleView;
    private TextView emptyDescView;
    private ProgressBar progressBar;
    private Button retryButton;
    private ChapterDao chapterDao;
    private String chapterSlug;

    private RetrofitCall<Chapter> chapterApiRequest;

    public static Intent createIntent(String title, Long courseId, Context context) {
        Intent intent = new Intent(context, ChapterDetailActivity.class);
        intent.putExtra(ACTIONBAR_TITLE, title);
        intent.putExtra(COURSE_ID, courseId);
        return intent;
    }

    public static Intent createIntent(String chapterSlug, Context context) {
        Intent intent = new Intent(context, ChapterDetailActivity.class);
        intent.putExtra(CHAPTER_SLUG, chapterSlug);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.testpress_activity_carousal);
        prefs = getSharedPreferences(TESTPRESS_CONTENT_SHARED_PREFS, Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
        chapterDao = TestpressSDKDatabase.getChapterDao(this);
        chapterSlug = getIntent().getStringExtra(CHAPTER_SLUG);
        if (chapterSlug != null) {
            emptyView = (LinearLayout) findViewById(R.id.empty_container);
            emptyTitleView = (TextView) findViewById(R.id.empty_title);
            emptyDescView = (TextView) findViewById(R.id.empty_description);
            retryButton = (Button) findViewById(R.id.retry_button);
            progressBar = (ProgressBar) findViewById(R.id.pb_loading);
            UIUtils.setIndeterminateDrawable(this, progressBar, 4);
            retryButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    emptyView.setVisibility(View.GONE);
                    loadChapterFromServer(chapterSlug);
                }
            });
            loadChapter(chapterSlug);
        } else {
            String title = getIntent().getStringExtra(ACTIONBAR_TITLE);
            if (title != null && !title.isEmpty()) {
                //noinspection ConstantConditions
                getSupportActionBar().setTitle(title);
            } else {
                long courseId = getIntent().getLongExtra(COURSE_ID, -1);
                CourseDao courseDao = TestpressSDKDatabase.getCourseDao(this);
                List<Course> courses = courseDao.queryBuilder()
                        .where(CourseDao.Properties.Id.eq(courseId)).list();

                if (!courses.isEmpty()) {
                    //noinspection ConstantConditions
                    getSupportActionBar().setTitle(courses.get(0).getTitle());
                }
            }
            //noinspection ConstantConditions
            InstituteSettings instituteSettings =
                    TestpressSdk.getTestpressSession(this).getInstituteSettings();

            if (instituteSettings.isCoursesFrontend() &&
                    instituteSettings.isCoursesGamificationEnabled()) {

                findViewById(R.id.fragment_carousel).setVisibility(View.VISIBLE);
                findViewById(R.id.fragment_container).setVisibility(View.GONE);
                CourseDetailsTabAdapter adapter = new CourseDetailsTabAdapter(getResources(),
                        getSupportFragmentManager(), getIntent().getExtras());

                ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
                viewPager.setAdapter(adapter);
                TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
                tabLayout.setupWithViewPager(viewPager);
            } else {
                loadChildChapters();
            }
        }
    }

    void loadChapter(final String chapterSlug) {
        List<Chapter> chapters = TestpressSDKDatabase.getChapterDao(this).queryBuilder()
                .where(ChapterDao.Properties.Slug.eq(chapterSlug)).list();

        if (chapters.isEmpty() ||
                (chapters.get(0).getRawChildrenCount(this) == 0 && chapters.get(0).getRawContentsCount(this) == 0)) {

            if (!chapters.isEmpty()) {
                //noinspection ConstantConditions
                getSupportActionBar().setTitle(chapters.get(0).getName());
            }
            loadChapterFromServer(chapterSlug);
        } else {
            onChapterLoaded(chapters.get(0));
        }
    }

    void loadChapterFromServer(final String chapterSlug) {
        progressBar.setVisibility(View.VISIBLE);
        chapterApiRequest = new TestpressCourseApiClient(this).getChapter(chapterSlug)
                .enqueue(new TestpressCallback<Chapter>() {
                    @Override
                    public void onSuccess(Chapter chapter) {
                        progressBar.setVisibility(View.GONE);
                        onChapterLoaded(chapter);
                    }

                    @Override
                    public void onException(TestpressException exception) {
                        if (exception.isUnauthenticated()) {
                            setEmptyText(R.string.testpress_authentication_failed,
                                    R.string.testpress_no_permission);
                            retryButton.setVisibility(View.GONE);
                        } else if (exception.isNetworkError()) {
                            setEmptyText(R.string.testpress_network_error,
                                    R.string.testpress_no_internet_try_again);
                        } else if (exception.getResponse().code() == 404) {
                            setEmptyText(R.string.testpress_chapter_not_available,
                                    R.string.testpress_chapter_not_available_description);
                            retryButton.setVisibility(View.GONE);
                        } else  {
                            setEmptyText(R.string.testpress_error_loading_chapters,
                                    R.string.testpress_some_thing_went_wrong_try_again);
                            retryButton.setVisibility(View.GONE);
                        }
                    }
                });
    }

    private Fragment getCurrentFragment() {
        return getSupportFragmentManager().findFragmentById(R.id.fragment_container);
    }

    private void showFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment)
                .commitAllowingStateLoss();
    }

    public void handleBackpressOnChaptersList() {
        Fragment fragment = getCurrentFragment();
        ChaptersListFragment chaptersListFragment = (ChaptersListFragment) fragment;

        if (chaptersListFragment.parentChapterId.equals("null")) {
            super.onBackPressed();
        } else {
            chaptersListFragment.showChaptersInFragment(getParentChapterId(chaptersListFragment.parentChapterId));
        }
    }

    public void handleBackpressOnContentsList() {
        long courseId = getIntent().getLongExtra(COURSE_ID, -1);
        Fragment fragment = getCurrentFragment();

        ContentsListFragment contentsListFragment = (ContentsListFragment) fragment;
        showFragment(ChaptersListFragment.getInstance(
                courseId,
                getParentChapterId(contentsListFragment.chapterId)
        ));
    }

    public void handleBackpressForGamifiedInstitute() {
        Fragment fragment = getCurrentFragment();

        if (fragment instanceof ContentsListFragment && chapter.hasSubChapters(this)) {
            handleBackpressOnContentsList();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onBackPressed() {
        Fragment fragment = getCurrentFragment();
        InstituteSettings instituteSettings =
                TestpressSdk.getTestpressSession(this).getInstituteSettings();

        if (instituteSettings.isCoursesFrontend() && instituteSettings.isCoursesGamificationEnabled()) {
            handleBackpressForGamifiedInstitute();
        } else if (fragment instanceof ChaptersListFragment) {
            handleBackpressOnChaptersList();
        } else if (fragment instanceof ContentsListFragment) {
            handleBackpressOnContentsList();
        } else {
            super.onBackPressed();
        }
    }


    Long getParentChapterId(Object chapterId) {
        List<Chapter> chapters = chapterDao.queryBuilder()
                .where(ChapterDao.Properties.Id.eq(chapterId))
                .list();
        return chapters.get(0).getParentId();
    }

    void onChapterLoaded(Chapter chapter) {
        this.chapter = chapter;
        //noinspection ConstantConditions
        getSupportActionBar().setTitle(chapter.getName());
        if (chapter.getActive() && chapter.hasSubChapters(this)) {
            getIntent().putExtra(COURSE_ID, Long.valueOf(chapter.getCourseId()));
            getIntent().putExtra(PARENT_ID, chapter.getId().toString());
            loadChildChapters();
        } else if (chapter.getActive() && chapter.hasContents(this)) {
            getIntent().putExtra(CONTENTS_URL_FRAG, chapter.getContentUrl());
            getIntent().putExtra(CHAPTER_ID, chapter.getId());
            loadContents();
        } else {
            setEmptyText(R.string.testpress_no_content,
                    R.string.testpress_no_content_description);
        }
    }

    private void loadChildChapters() {
        ChaptersListFragment fragment = new ChaptersListFragment();
        fragment.setArguments(getIntent().getExtras());
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment)
                .commitAllowingStateLoss();
    }

    private void loadContents() {
        ContentsListFragment fragment = new ContentsListFragment();
        fragment.setArguments(getIntent().getExtras());
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment)
                .commitAllowingStateLoss();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (prefs.getBoolean(GO_TO_MENU, false)) {
            prefs.edit().putBoolean(GO_TO_MENU, false).apply();
            finish();
        } else if (prefs.getBoolean(FORCE_REFRESH, false) &&
                getIntent().getStringExtra(CONTENTS_URL_FRAG) != null) {

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
        if (chapter != null && chapter.getActive()) {
            Long parentId = chapter.getParentId();
            if (parentId != null) {
                data.putString(CHAPTER_SLUG, chapter.getParentSlug());
            } else {
                data.putLong(COURSE_ID, Long.valueOf(chapter.getCourseId()));
            }
        }
        return data;
    }

    @Override
    public RetrofitCall[] getRetrofitCalls() {
        return new RetrofitCall[] { chapterApiRequest };
    }
}
