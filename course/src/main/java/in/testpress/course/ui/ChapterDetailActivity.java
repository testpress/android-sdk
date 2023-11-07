package in.testpress.course.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.Nullable;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import in.testpress.core.TestpressCallback;
import in.testpress.core.TestpressException;
import in.testpress.core.TestpressSDKDatabase;
import in.testpress.core.TestpressSdk;
import in.testpress.course.R;
import in.testpress.course.api.TestpressCourseApiClient;
import in.testpress.course.fragments.CourseContentListFragment;
import in.testpress.database.entities.CourseContentType;
import in.testpress.models.InstituteSettings;
import in.testpress.models.greendao.Chapter;
import in.testpress.models.greendao.ChapterDao;
import in.testpress.models.greendao.Course;
import in.testpress.models.greendao.CourseDao;
import in.testpress.network.RetrofitCall;
import in.testpress.ui.BaseToolBarActivity;
import in.testpress.util.UIUtils;

import static in.testpress.course.TestpressCourse.CHAPTER_URL;
import static in.testpress.course.TestpressCourse.COURSE_ID;
import static in.testpress.course.TestpressCourse.PARENT_ID;
import static in.testpress.course.TestpressCourse.PRODUCT_SLUG;
import static in.testpress.course.fragments.CourseContentListFragment.COURSE_CONTENT_TYPE;
import static in.testpress.course.ui.ContentActivity.FORCE_REFRESH;
import static in.testpress.course.ui.ContentActivity.GO_TO_MENU;
import static in.testpress.course.ui.ContentActivity.TESTPRESS_CONTENT_SHARED_PREFS;
import static in.testpress.store.TestpressStore.PAYMENT_SUCCESS;
import static in.testpress.store.TestpressStore.STORE_REQUEST_CODE;

public class ChapterDetailActivity extends BaseToolBarActivity {

    public static final String CONTENTS_URL_FRAG = "contentsUrlFrag";
    public static final String CHAPTER_ID = "chapterId";
    public static final String TITLE = "title";

    private Chapter chapter;
    private SharedPreferences prefs;
    private LinearLayout emptyView;
    private TextView emptyTitleView;
    private TextView emptyDescView;
    private ProgressBar progressBar;
    private Button retryButton;
    private CourseDao courseDao;

    private RetrofitCall<Chapter> chapterApiRequest;
    private RetrofitCall<Course> courseApiRequest;
    private String productSlug;
    InstituteSettings instituteSettings;

    public static Intent createIntent(String title, String courseId, Context context, String productSlug) {
        Intent intent = new Intent(context, ChapterDetailActivity.class);
        intent.putExtra(ACTIONBAR_TITLE, title);
        intent.putExtra(COURSE_ID, courseId);
        intent.putExtra(PRODUCT_SLUG, productSlug);
        return intent;
    }

    public static Intent createIntent(String chaptersUrl, Context context, String productSlug) {
        Intent intent = new Intent(context, ChapterDetailActivity.class);
        intent.putExtra(CHAPTER_URL, chaptersUrl);
        intent.putExtra(PRODUCT_SLUG, productSlug);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.testpress_activity_carousal);
        prefs = getSharedPreferences(TESTPRESS_CONTENT_SHARED_PREFS, Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
        courseDao = TestpressSDKDatabase.getCourseDao(this);
        productSlug = getIntent().getStringExtra(PRODUCT_SLUG);
        //noinspection ConstantConditions
        instituteSettings = TestpressSdk.getTestpressSession(this).getInstituteSettings();
        final String chapterUrl = getIntent().getStringExtra(CHAPTER_URL);
        if (chapterUrl != null) {
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
                    loadChapterFromServer(chapterUrl);
                }
            });
            loadChapter(chapterUrl);
        } else {
            setActionBarTitle();
            if (instituteSettings.isCoursesFrontend() && productSlug == null) {
                loadCourseTabLayout();
            } else {
                loadChildChapters();
            }
        }
    }

    private void setActionBarTitle() {
        String title = getIntent().getStringExtra(ACTIONBAR_TITLE);
        if (title != null && !title.isEmpty()) {
            //noinspection ConstantConditions
            getSupportActionBar().setTitle(title);
        } else {
            String courseId = getIntent().getStringExtra(COURSE_ID);
            CourseDao courseDao = TestpressSDKDatabase.getCourseDao(this);
            List<Course> courses = courseDao.queryBuilder()
                    .where(CourseDao.Properties.Id.eq(courseId)).list();

            if (!courses.isEmpty()) {
                //noinspection ConstantConditions
                getSupportActionBar().setTitle(courses.get(0).getTitle());
            }
        }
    }

    void loadChapter(final String chapterUrl) {
        TestpressSDKDatabase.getChapterDao(this).detachAll();
        List<Chapter> chapters = TestpressSDKDatabase.getChapterDao(this).queryBuilder()
                .where(ChapterDao.Properties.Url.eq(chapterUrl)).list();

        if (chapters.isEmpty()) {
            loadChapterFromServer(chapterUrl);
        } else {
            onChapterLoaded(chapters.get(0));
        }
    }

    void loadChapterFromServer(final String chapterUrl) {
        progressBar.setVisibility(View.VISIBLE);

        chapterApiRequest = new TestpressCourseApiClient(this).getChapter(chapterUrl)
                .enqueue(new TestpressCallback<Chapter>() {
                    @Override
                    public void onSuccess(Chapter chapter) {
                        progressBar.setVisibility(View.GONE);
                        deleteExistingChapterAndInsert(chapter);
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

    void deleteExistingChapterAndInsert(Chapter chapter) {
        ChapterDao chapterDao = TestpressSDKDatabase.getChapterDao(this);
        chapterDao.queryBuilder().where(ChapterDao.Properties.Id.eq(chapter.getId())).buildDelete().executeDeleteWithoutDetachingEntities();
        chapterDao.detachAll();
        chapterDao.insertOrReplace(chapter);
    }

    void onChapterLoaded(Chapter chapter) {
        this.chapter = chapter;
        checkCourseAndLoadChaptersOrContents(chapter.getCourseId().toString());
    }

    void checkCourseAndLoadChaptersOrContents(String courseId) {
        List<Course> courses = courseDao.queryBuilder().where(CourseDao.Properties.Id.eq(courseId)).list();

        if (courses.isEmpty()) {
            fetchCourseAndLoadChaptersOrContents(courseId);
        } else {
            loadChaptersOrContents();
        }
    }

    void fetchCourseAndLoadChaptersOrContents(String courseId) {
        progressBar.setVisibility(View.VISIBLE);
        courseApiRequest = new TestpressCourseApiClient(this).getCourse(courseId)
                .enqueue(new TestpressCallback<Course>() {
                    @Override
                    public void onSuccess(Course course) {
                        progressBar.setVisibility(View.GONE);
                        course.setIsMyCourse(true);
                        courseDao.insertOrReplace(course);
                        loadChaptersOrContents();
                    }

                    @Override
                    public void onException(TestpressException exception) {
                        handleException(exception);
                    }
                });
    }

    void handleException(TestpressException exception) {
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

    void loadChaptersOrContents() {
        getSupportActionBar().setTitle(chapter.getName());
        if (chapter.hasChildren()) {
            getIntent().putExtra(COURSE_ID, chapter.getCourseId().toString());
            getIntent().putExtra(PARENT_ID, chapter.getId().toString());
            getIntent().putExtra(PRODUCT_SLUG, productSlug);
            loadChildChapters();
        } else  {
            getIntent().putExtra(CONTENTS_URL_FRAG, chapter.getChapterContentsUrl());
            getIntent().putExtra(CHAPTER_ID, chapter.getId());
            getIntent().putExtra(PRODUCT_SLUG, productSlug);
            loadContents();
        }
    }

    void loadCourseTabLayout() {
        findViewById(R.id.fragment_carousel).setVisibility(View.VISIBLE);
        findViewById(R.id.fragment_container).setVisibility(View.GONE);
        CourseDetailsTabAdapter adapter =
                new CourseDetailsTabAdapter(getSupportFragmentManager(), getFragmentList());

        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setAdapter(adapter);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(viewPager);
    }

    private ArrayList<Fragment> getFragmentList() {
        ArrayList<Fragment> fragments = new ArrayList<>();

        fragments.add(
                createFragment(
                        new ChaptersListFragment(),
                        getIntent().getExtras(),
                        getString(R.string.testpress_learn)
                )
        );
        Bundle runningContentBundle = getIntent().getExtras();
        runningContentBundle.putInt(COURSE_CONTENT_TYPE,CourseContentType.RUNNING_CONTENT.ordinal());
        fragments.add(
                createFragment(
                        new CourseContentListFragment(),
                        runningContentBundle,
                        getString(R.string.testpress_running_contents)
                )
        );
        Bundle upcomingContentBundle = getIntent().getExtras();
        upcomingContentBundle.putInt(COURSE_CONTENT_TYPE,CourseContentType.UPCOMING_CONTENT.ordinal());
        fragments.add(
                createFragment(
                        new CourseContentListFragment(),
                        upcomingContentBundle,
                        getString(R.string.testpress_upcoming_contents)
                )
        );

        if (instituteSettings.isCoursesGamificationEnabled()) {
            fragments.add(
                    createFragment(
                            new RankListFragment(),
                            getIntent().getExtras(),
                            getString(R.string.testpress_leaderboard)
                    )
            );
        }

        return fragments;
    }

    private Fragment createFragment(Fragment fragment, Bundle extras, String title) {
        extras.putString(TITLE, title);
        fragment.setArguments(extras);
        return fragment;
    }

    private void loadChildChapters() {
        ChaptersListFragment fragment = new ChaptersListFragment();
        fragment.setArguments(getIntent().getExtras());
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment)
                .commitAllowingStateLoss();
    }

    private void loadContents() {
        ContentListFragment fragment = new ContentListFragment();
        fragment.setArguments(getIntent().getExtras());
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment)
                .commitAllowingStateLoss();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == STORE_REQUEST_CODE) {
            boolean paymentStatus = data.getBooleanExtra(PAYMENT_SUCCESS, false);
            if (paymentStatus) {
                fetchCourseAndLoadChaptersOrContents(chapter.getCourseId().toString());
            }
        }

        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        fragment.onActivityResult(requestCode, resultCode, data);
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
        if (chapter != null) {
            Long parentId = chapter.getParentId();
            if (parentId != null) {
                data.putString(CHAPTER_URL, chapter.getParentUrl());
            } else {
                data.putInt(COURSE_ID, chapter.getCourseId().intValue());
            }
        }

        if(productSlug != null) {
            data.putString(PRODUCT_SLUG, productSlug);
        }
        return data;
    }

    @Override
    public RetrofitCall[] getRetrofitCalls() {
        return new RetrofitCall[] { chapterApiRequest, courseApiRequest };
    }
}