package in.testpress.course.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import in.testpress.core.TestpressCallback;
import in.testpress.core.TestpressException;
import in.testpress.core.TestpressSDKDatabase;
import in.testpress.core.TestpressSdk;
import in.testpress.course.R;
import in.testpress.course.network.ChapterPager;
import in.testpress.course.network.ContentPager;
import in.testpress.course.network.TestpressCourseApiClient;
import in.testpress.models.greendao.Chapter;
import in.testpress.models.greendao.ChapterDao;
import in.testpress.models.greendao.Content;
import in.testpress.models.greendao.ContentDao;
import in.testpress.models.greendao.Course;
import in.testpress.models.greendao.CourseDao;
import in.testpress.network.BaseResourcePager;
import in.testpress.ui.BaseToolBarActivity;
import in.testpress.util.Assert;
import in.testpress.util.PreferenceUtils;
import in.testpress.util.ViewUtils;

import static in.testpress.course.TestpressCourse.CHAPTER_SLUG;
import static in.testpress.course.TestpressCourse.COURSE_ID;
import static in.testpress.course.TestpressCourse.PARENT_CHAPTER_ID;
import static in.testpress.network.TestpressApiClient.MODIFIED_SINCE;
import static in.testpress.network.TestpressApiClient.ORDER;
import static in.testpress.network.TestpressApiClient.UNFILTERED;

public class ExpandableContentsActivity extends BaseToolBarActivity {

    SwipeRefreshLayout swipeRefreshLayout;
    private LinearLayout newContentsAvailableLabel;
    private View emptyView;
    private ImageView emptyViewImage;
    private TextView emptyTitleView;
    private TextView emptyDescView;
    private Button retryButton;

    private Course course;
    private CourseDao courseDao;
    private ChapterDao chapterDao;
    private ContentDao contentDao;
    private TestpressCourseApiClient apiClient;
    private BaseResourcePager currentPager;
    private boolean chaptersModified;
    private long courseId;
    private long parentChapterId;

    public static Intent createIntent(String title, long courseId, Context context) {
        return createIntent(title, courseId, 0, context);
    }

    public static Intent createIntent(String title, long courseId, long parentChapterId,
                                      Context context) {

        Intent intent = new Intent(context, ExpandableContentsActivity.class);
        intent.putExtra(ACTIONBAR_TITLE, title);
        intent.putExtra(COURSE_ID, courseId);
        intent.putExtra(PARENT_CHAPTER_ID, parentChapterId);
        return intent;
    }

    public static Intent createIntent(String chapterSlug, Context context) {
        Intent intent = new Intent(context, ExpandableContentsActivity.class);
        intent.putExtra(CHAPTER_SLUG, chapterSlug);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.testpress_activity_expanable_contents);

        swipeRefreshLayout = findViewById(R.id.swipe_container);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshWithProgress();
            }
        });
        swipeRefreshLayout.setColorSchemeResources(R.color.testpress_color_primary);
        newContentsAvailableLabel = findViewById(R.id.new_items_available_label);
        newContentsAvailableLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayChapters();
            }
        });
        emptyView = findViewById(R.id.empty_container);
        emptyViewImage = findViewById(R.id.image_view);
        emptyTitleView = findViewById(R.id.empty_title);
        emptyDescView = findViewById(R.id.empty_description);
        emptyTitleView.setTypeface(TestpressSdk.getRubikMediumFont(this));
        emptyDescView.setTypeface(TestpressSdk.getRubikRegularFont(this));
        retryButton = findViewById(R.id.retry_button);
        retryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                emptyView.setVisibility(View.GONE);
                restartLoading();
            }
        });

        courseDao = TestpressSDKDatabase.getCourseDao(this);
        chapterDao = TestpressSDKDatabase.getChapterDao(this);
        contentDao = TestpressSDKDatabase.getContentDao(getBaseContext());

        apiClient = new TestpressCourseApiClient(this);

        courseId = getIntent().getLongExtra(COURSE_ID, 0);
        parentChapterId = getIntent().getLongExtra(PARENT_CHAPTER_ID, 0);
        String title = getIntent().getStringExtra(ACTIONBAR_TITLE);
        if (title != null && !title.isEmpty()) {
            assert getSupportActionBar() != null;
            getSupportActionBar().setTitle(title);
        }
        if (courseId != 0) {
            checkCourseAvailableInDB();
        } else {
            String chapterSlug = getIntent().getStringExtra(CHAPTER_SLUG);
            Assert.assertNotNullAndNotEmpty("courseId/chapterSlug must need to pass.", chapterSlug);
            loadChapter(chapterSlug);
        }
    }

    void checkCourseAvailableInDB() {
        List<Course> coursesFromDB = courseDao.queryBuilder()
                .where(CourseDao.Properties.Id.eq(courseId)).list();

        if (!coursesFromDB.isEmpty()) {
            onCourseAvailable(coursesFromDB.get(0));
        } else {
            loadCourse(courseId);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.testpress_list_grid, menu);
        MenuItem list = menu.findItem(R.id.list);
        MenuItem grid = menu.findItem(R.id.grid);
        ViewUtils.setMenuIconsColor(this, new MenuItem[] { list, grid });
        Fragment currentFragment = getCurrentFragment();
        if (currentFragment != null) {
            if (currentFragment instanceof ExpandableContentsFragment) {
                list.setVisible(false);
                grid.setVisible(true);
            } else {
                list.setVisible(true);
                grid.setVisible(false);
            }
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (R.id.list == item.getItemId()) {
            setLastCourseUiUsedIsToc(true);
            Fragment fragment = getCurrentFragment();
            if (fragment instanceof NewChaptersGridFragment) {
                NewChaptersGridFragment gridFragment = (NewChaptersGridFragment) fragment;
                try {
                    parentChapterId = Long.parseLong(gridFragment.parentChapterId);
                } catch (NumberFormatException e) {
                     parentChapterId = 0;
                }
            } else if (fragment instanceof ContentsListFragment) {
                ContentsListFragment contentsListFragment = (ContentsListFragment) fragment;
                parentChapterId = contentsListFragment.chapterId;
            }
            setActionBarTitle(course.getTitle());
            showFragment(ExpandableContentsFragment.getInstance(courseId, parentChapterId));
            return true;
        } else if (R.id.grid == item.getItemId()) {
            setLastCourseUiUsedIsToc(false);
            ExpandableContentsFragment expandableContentsFragment =
                    (ExpandableContentsFragment) getCurrentFragment();

            parentChapterId = expandableContentsFragment.parentChapterId;
            displayChapterDetailView();
            return true;
        } else if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadCourse(long courseId) {
        swipeRefreshLayout.setRefreshing(true);
        apiClient.getCourse(courseId).enqueue(new TestpressCallback<Course>() {
            @Override
            public void onSuccess(Course course) {
                courseDao.insertOrReplace(course);
                swipeRefreshLayout.setRefreshing(false);
                onCourseAvailable(course);
            }

            @Override
            public void onException(TestpressException exception) {
                handleError(exception);
            }
        });
    }

    private void onCourseAvailable(Course courseFromDB) {
        course = courseFromDB;
        //noinspection ConstantConditions
        getSupportActionBar().setTitle(course.getTitle());
        if (course.getChildItemsLoaded()) {
            displayChapters();
            currentPager = new ChapterPager(course.getId(), apiClient);
            if (!isItemsEmpty()) {
                Chapter chapter = Chapter.getAllChaptersQueryBuilder(this, courseId)
                        .orderDesc(ChapterDao.Properties.ModifiedDate).list().get(0);

                currentPager.setQueryParams(MODIFIED_SINCE, chapter.getModified());
                currentPager.setQueryParams(ORDER, "modified");
            } else {
                swipeRefreshLayout.setRefreshing(true);
            }
            fetchChapters();
        } else {
            refreshWithProgress();
        }
    }

    void loadChapter(String chapterSlug) {
        List<Chapter> chapters = chapterDao.queryBuilder()
                .where(ChapterDao.Properties.Slug.eq(chapterSlug)).list();

        if (!chapters.isEmpty()) {
            Chapter chapter = chapters.get(0);
            if ((chapter.getRawChildrenCount(this) != 0 || chapter.getRawContentsCount(this) != 0)) {
                onChapterLoaded(chapter);
                return;
            }
            //noinspection ConstantConditions
            getSupportActionBar().setTitle(chapter.getName());
        }
        loadChapterFromServer(chapterSlug);
    }

    void loadChapterFromServer(String chapterSlug) {
        swipeRefreshLayout.setRefreshing(true);
        apiClient.getChapter(chapterSlug)
                .enqueue(new TestpressCallback<Chapter>() {
                    @Override
                    public void onSuccess(Chapter chapter) {
                        swipeRefreshLayout.setRefreshing(false);
                        onChapterLoaded(chapter);
                    }

                    @Override
                    public void onException(TestpressException exception) {
                        handleError(exception);
                    }
                });
    }

    void onChapterLoaded(Chapter chapter) {
        courseId = chapter.getCourseId();
        parentChapterId = chapter.getId();
        // noinspection ConstantConditions
        getSupportActionBar().setTitle(chapter.getName());
        checkCourseAvailableInDB();
    }

    void displayChapters() {
        newContentsAvailableLabel.setVisibility(View.GONE);
        if (parentChapterId == 0 && isItemsEmpty()) {
            setEmptyText(R.string.testpress_no_content, R.string.testpress_no_content_description,
                    R.drawable.testpress_learn_flat_icon);
            return;
        }
        emptyView.setVisibility(View.GONE);
        Fragment fragment = getCurrentFragment();
        boolean isTocUi = false;
        if (fragment == null) {
            isTocUi = (course.getIsTocUi() == null) ?
                    PreferenceUtils.isTocUsedAsLastCourseUi(this) : course.getIsTocUi();
        }
        if ((fragment == null && isTocUi) || fragment instanceof ExpandableContentsFragment) {
            showFragment(ExpandableContentsFragment.getInstance(courseId, parentChapterId));
        } else if (fragment == null) {
            displayChapterDetailView();
        } else if (fragment instanceof NewChaptersGridFragment) {
            showFragment(NewChaptersGridFragment.getInstance(courseId, parentChapterId));
        } else if (fragment instanceof ContentsListFragment) {
            ContentsListFragment contentsListFragment = (ContentsListFragment) fragment;
            parentChapterId = contentsListFragment.chapterId;
            showFragment(ContentsListFragment.getInstance(parentChapterId));
        }
    }

    private void displayChapterDetailView() {
        if (parentChapterId != 0) {
            List<Chapter> chaptersFromDB = chapterDao.queryBuilder()
                    .where(ChapterDao.Properties.Id.eq(parentChapterId))
                    .list();

            if (chaptersFromDB.isEmpty() || chaptersFromDB.get(0).getRawChildrenCount(this) == 0) {
                showFragment(ContentsListFragment.getInstance(parentChapterId));
                return;
            }
        }
        showFragment(NewChaptersGridFragment.getInstance(courseId, parentChapterId));
    }

    private void showFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment)
                .commitAllowingStateLoss();
    }

    private void fetchChapters() {
        ((ChapterPager) currentPager).enqueueNext(new TestpressCallback<List<Chapter>>() {
            @Override
            public void onSuccess(List<Chapter> chapters) {
                if (currentPager.hasMore()) {
                    fetchChapters();
                } else {
                    boolean loadModifiedContentsOnly =
                            currentPager.getQueryParams(MODIFIED_SINCE) != null;

                    currentPager = new ContentPager(course.getId(), apiClient);
                    if (loadModifiedContentsOnly) {
                        List<Content> contents = contentDao.queryBuilder()
                                .where(ContentDao.Properties.CourseId.eq(course.getId()))
                                .orderDesc(ContentDao.Properties.ModifiedDate).list();

                        if (!contents.isEmpty()) {
                            Content content = contents.get(0);
                            currentPager.setQueryParams(MODIFIED_SINCE, content.getModified());
                            currentPager.setQueryParams(UNFILTERED, true);
                        }
                    } else {
                        chapterDao.queryBuilder()
                                .where(ChapterDao.Properties.CourseId.eq(course.getId()))
                                .buildDelete().executeDeleteWithoutDetachingEntities();

                        chapterDao.detachAll();
                    }
                    chapterDao.insertOrReplaceInTx(chapters);
                    chaptersModified = !chapters.isEmpty() && !swipeRefreshLayout.isRefreshing();
                    fetchContents();
                }
            }

            @Override
            public void onException(TestpressException exception) {
                handleError(exception);
            }
        });
    }

    private void fetchContents() {
        ((ContentPager) currentPager).enqueueNext(new TestpressCallback<List<Content>>() {
            @Override
            public void onSuccess(List<Content> contents) {
                if (currentPager.hasMore()) {
                    fetchContents();
                } else {
                    if (currentPager.getQueryParams(MODIFIED_SINCE) == null) {
                        contentDao.queryBuilder()
                                .where(ContentDao.Properties.CourseId.eq(course.getId()))
                                .buildDelete().executeDeleteWithoutDetachingEntities();

                        contentDao.detachAll();
                    }
                    contentDao.insertOrReplaceInTx(contents);
                    if (!course.getChildItemsLoaded()) {
                        course.setChildItemsLoaded(true);
                        courseDao.insertOrReplace(course);
                    }
                    if (swipeRefreshLayout.isRefreshing()) {
                        swipeRefreshLayout.setRefreshing(false);
                        displayChapters();
                    } else if (!contents.isEmpty() || chaptersModified) {
                        chaptersModified = false;
                        newContentsAvailableLabel.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onException(TestpressException exception) {
                handleError(exception);
            }
        });
    }

    public void refreshWithProgress() {
        swipeRefreshLayout.setRefreshing(true);
        currentPager = new ChapterPager(course.getId(), apiClient);
        fetchChapters();
    }

    private void handleError(TestpressException exception) {
        if (exception.isCancelled()) {
            return;
        }
        if (exception.isUnauthenticated()) {
            displayError(R.string.testpress_authentication_failed,
                    R.string.testpress_no_permission,
                    R.drawable.testpress_alert_warning);
        } else if (exception.isNetworkError()) {
            displayError(R.string.testpress_network_error,
                    R.string.testpress_no_internet_try_again,
                    R.drawable.testpress_no_wifi);
            retryButton.setVisibility(View.VISIBLE);
        } else if (exception.isPageNotFound()) {
            displayError(R.string.testpress_content_not_available,
                    R.string.testpress_content_not_available_description,
                    R.drawable.testpress_alert_warning);
        } else  {
            displayError(R.string.testpress_error_loading_contents,
                    R.string.testpress_some_thing_went_wrong_try_again,
                    R.drawable.testpress_alert_warning);
        }
    }

    @Override
    public void onBackPressed() {
        Fragment fragment = getCurrentFragment();
        if (fragment instanceof NewChaptersGridFragment) {
            NewChaptersGridFragment gridFragment = (NewChaptersGridFragment) fragment;
            if (gridFragment.parentChapterId.equals("null")) {
                super.onBackPressed();
            } else {
                gridFragment.displayChildChapters(getParentChapterId(gridFragment.parentChapterId));
            }
        } else if (fragment instanceof ContentsListFragment) {
            ContentsListFragment contentsListFragment = (ContentsListFragment) fragment;
            showFragment(NewChaptersGridFragment.getInstance(
                    courseId,
                    getParentChapterId(contentsListFragment.chapterId)
            ));
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

    private boolean isItemsEmpty() {
        return Chapter.getRootChaptersQueryBuilder(this, courseId).count() == 0;
    }

    private void restartLoading() {
        swipeRefreshLayout.setRefreshing(true);
        if (currentPager == null) {
            loadCourse(courseId);
        } else if (currentPager instanceof ChapterPager) {
            fetchChapters();
        } else {
            fetchContents();
        }
    }

    void displayError(int title, int description, int imageRes) {
        if (isItemsEmpty()) {
            setEmptyText(title, description, imageRes);
        } else {
            Snackbar.make(swipeRefreshLayout, description, Snackbar.LENGTH_SHORT).show();
        }
        swipeRefreshLayout.setRefreshing(false);
    }

    private void setEmptyText(int title, int description, int imageRes) {
        emptyView.setVisibility(View.VISIBLE);
        emptyTitleView.setText(title);
        emptyViewImage.setImageResource(imageRes);
        emptyDescView.setText(description);
        retryButton.setVisibility(View.GONE);
    }

    private Fragment getCurrentFragment() {
        return getSupportFragmentManager().findFragmentById(R.id.fragment_container);
    }

    private void setLastCourseUiUsedIsToc(boolean isTocUi) {
        course.setIsTocUi(isTocUi);
        course.update();
        PreferenceUtils.setLastCourseUiUsedIsToc(this, isTocUi);
    }

    @Override
    protected void onDestroy() {
        if (currentPager != null) {
            currentPager.cancelAsyncRequest();
        }
        super.onDestroy();
    }
}