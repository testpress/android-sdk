package in.testpress.course.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import org.greenrobot.greendao.query.QueryBuilder;

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
import pl.openrnd.multilevellistview.ItemInfo;
import pl.openrnd.multilevellistview.MultiLevelListView;
import pl.openrnd.multilevellistview.OnItemClickListener;

import static in.testpress.course.TestpressCourse.COURSE;
import static in.testpress.course.TestpressCourse.COURSE_ID;
import static in.testpress.network.TestpressApiClient.MODIFIED_SINCE;
import static in.testpress.network.TestpressApiClient.ORDER;
import static in.testpress.network.TestpressApiClient.UNFILTERED;

public class ExpandableContentsActivity extends BaseToolBarActivity {

    private SwipeRefreshLayout swipeRefreshLayout;
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
    private ExpandableContentsAdapter adapter;
    private ListView listView;
    private boolean chaptersModified;

    public static Intent createIntent(String title, long courseId, Context context) {
        Intent intent = new Intent(context, ExpandableContentsActivity.class);
        intent.putExtra(ACTIONBAR_TITLE, title);
        intent.putExtra(COURSE_ID, courseId);
        return intent;
    }

    public static Intent createIntent(Course course, Context context) {
        Intent intent = new Intent(context, ExpandableContentsActivity.class);
        intent.putExtra(COURSE, course);
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

        final MultiLevelListView multiLevelListView = findViewById(R.id.listView);
        adapter = new ExpandableContentsAdapter(this);
        multiLevelListView.setAdapter(adapter);
        listView = findViewById(android.R.id.list);
        multiLevelListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClicked(MultiLevelListView parent, View view, Object item,
                                      ItemInfo itemInfo) {

                if (item instanceof Chapter) {
                    Snackbar.make(swipeRefreshLayout, R.string.testpress_no_content_description,
                            Snackbar.LENGTH_SHORT).show();
                } else {
                    Content content = (Content) item;
                    startActivity(ContentActivity.createIntent(
                            content.getId(),
                            content.getChapterId(),
                            ExpandableContentsActivity.this)
                    );
                }
            }

            @Override
            public void onGroupItemClicked(MultiLevelListView parent, View view, Object item,
                                           ItemInfo itemInfo) {

                Chapter chapter = (Chapter) item;
                if (chapter.getRawChildrenCount(getBaseContext()) == 1) {
                    Chapter childChapter = chapterDao.queryBuilder()
                            .where(ChapterDao.Properties.ParentId.eq(chapter.getId())).list().get(0);

                    int activePosition = listView.getPositionForView(view);
                    if (adapter.isExpandable(childChapter) && activePosition != -1) {
                        activePosition++;
                        listView.performItemClick(
                                listView.getAdapter().getView(activePosition, null, null),
                                activePosition,
                                listView.getAdapter().getItemId(activePosition)
                        );
                    }
                }
            }
        });
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                                 int totalItemCount) {

                if (listView.getChildAt(0) != null) {
                    swipeRefreshLayout.setEnabled(listView.getFirstVisiblePosition() == 0
                            && listView.getChildAt(0).getTop() == 0);
                }
            }
        });

        courseDao = TestpressSDKDatabase.getCourseDao(this);
        chapterDao = TestpressSDKDatabase.getChapterDao(this);
        contentDao = TestpressSDKDatabase.getContentDao(getBaseContext());
        apiClient = new TestpressCourseApiClient(this);
        course = getIntent().getParcelableExtra(COURSE);
        assert getSupportActionBar() != null;
        if (course == null) {
            long courseId = getIntent().getLongExtra(COURSE_ID, 0);
            String title = getIntent().getStringExtra(ACTIONBAR_TITLE);
            List<Course> courses = courseDao.queryBuilder()
                    .where(CourseDao.Properties.Id.eq(courseId)).list();

            if (!courses.isEmpty()) {
                course = courses.get(0);
                onCourseAvailable();
            } else {
                if (title != null && !title.isEmpty()) {
                    getSupportActionBar().setTitle(title);
                }
                // loadCourse()
            }
        } else {
            onCourseAvailable();
        }
    }

    private void onCourseAvailable() {
        //noinspection ConstantConditions
        getSupportActionBar().setTitle(course.getTitle());
        if (course.getChildItemsLoaded()) {
            displayChapters();
            currentPager = new ChapterPager(course.getId(), apiClient);
            if (getParentChaptersQueryBuilder().count() != 0) {
                Chapter chapter = getAllChaptersQueryBuilder()
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

    private QueryBuilder<Chapter> getParentChaptersQueryBuilder() {
        return getAllChaptersQueryBuilder().where(
                ChapterDao.Properties.Active.eq(true),
                ChapterDao.Properties.ParentId.isNull()
        ).orderAsc(ChapterDao.Properties.Order);
    }

    void displayChapters() {
        newContentsAvailableLabel.setVisibility(View.GONE);
        List<Chapter> chapters = getParentChaptersQueryBuilder().list();
        if (chapters.isEmpty()) {
            setEmptyText(R.string.testpress_no_content, R.string.testpress_no_content_description,
                    R.drawable.testpress_learn_flat_icon);
        } else {
            emptyView.setVisibility(View.GONE);
            adapter.setDataItems(chapters);
            if (chapters.size() == 1 && adapter.isExpandable(chapters.get(0))) {
                listView.performItemClick(
                        listView.getAdapter().getView(0, null, null),
                        0,
                        listView.getAdapter().getItemId(0)
                );
            }
        }
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
        if (exception.isNetworkError()) {
            displayError(R.string.testpress_network_error,
                    R.string.testpress_no_internet_try_again,
                    R.drawable.testpress_no_wifi);
        } else {
            displayError(R.string.testpress_error_loading_analytics,
                    R.string.testpress_some_thing_went_wrong_try_again,
                    R.drawable.testpress_alert_warning);

            retryButton.setVisibility(View.GONE);
        }
    }

    private boolean isItemsEmpty() {
        return getParentChaptersQueryBuilder().count() == 0;
    }

    private void restartLoading() {
        swipeRefreshLayout.setRefreshing(true);
        if (currentPager instanceof ChapterPager) {
            fetchChapters();
        } else {
            fetchContents();
        }
    }

    private QueryBuilder<Chapter> getAllChaptersQueryBuilder() {
        return chapterDao.queryBuilder().where(ChapterDao.Properties.CourseId.eq(course.getId()));
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
        retryButton.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        currentPager.cancelAsyncRequest();
        super.onDestroy();
    }
}