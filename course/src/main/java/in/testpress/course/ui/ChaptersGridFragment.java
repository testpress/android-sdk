package in.testpress.course.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TableRow;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.greenrobot.greendao.query.QueryBuilder;
import org.greenrobot.greendao.query.WhereCondition;

import java.util.List;

import in.testpress.core.TestpressCallback;
import in.testpress.core.TestpressException;
import in.testpress.core.TestpressSDKDatabase;
import in.testpress.core.TestpressSdk;
import in.testpress.course.R;
import in.testpress.course.network.ContentPager;
import in.testpress.models.greendao.Chapter;
import in.testpress.models.greendao.ChapterDao;
import in.testpress.course.network.ChapterPager;
import in.testpress.course.network.TestpressCourseApiClient;
import in.testpress.models.greendao.Content;
import in.testpress.models.greendao.ContentDao;
import in.testpress.models.greendao.Course;
import in.testpress.models.greendao.CourseDao;
import in.testpress.network.BaseResourcePager;
import in.testpress.ui.BaseGridFragment;
import in.testpress.util.ImageUtils;
import in.testpress.util.UIUtils;

import static in.testpress.course.TestpressCourse.CHAPTER_SLUG;
import static in.testpress.course.TestpressCourse.COURSE_ID;
import static in.testpress.course.TestpressCourse.FROM_PRODUCT;
import static in.testpress.course.TestpressCourse.PARENT_ID;

import static in.testpress.course.TestpressCourse.CHAPTER_ID;
import static in.testpress.course.TestpressCourse.COURSE_ID;
import static in.testpress.course.TestpressCourse.PARENT_CHAPTER_ID;
import static in.testpress.course.TestpressCourse.PRODUCT_SLUG;
import static in.testpress.network.TestpressApiClient.MODIFIED_SINCE;
import static in.testpress.network.TestpressApiClient.ORDER;
import static in.testpress.network.TestpressApiClient.UNFILTERED;

public class ChaptersGridFragment extends BaseGridFragment<Chapter> {

    private long courseId;
    String parentChapterId = "null";
    private ImageLoader mImageLoader;
    private BaseResourcePager currentPager;
    private TestpressCourseApiClient apiClient;
    private CourseDao courseDao;
    private ChapterDao chapterDao;
    private ContentDao contentDao;
    private Course course;
    private boolean chaptersModified;
    private String product_slug;


    public static ChaptersGridFragment getInstance(long courseId, Long parentChapterId, String product_slug) {
        ChaptersGridFragment fragment = new ChaptersGridFragment();
        Bundle bundle = new Bundle();
        bundle.putLong(COURSE_ID, courseId);
        bundle.putString(PRODUCT_SLUG, product_slug);
        if (parentChapterId != null && parentChapterId != 0) {
            bundle.putString(PARENT_CHAPTER_ID, parentChapterId.toString());
        }
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        assert getArguments() != null;
        courseId =  getArguments().getLong(COURSE_ID);
        product_slug = getArguments().getString(PRODUCT_SLUG);
        if (getArguments().getString(PARENT_CHAPTER_ID) != null) {
            parentChapterId = getArguments().getString(PARENT_CHAPTER_ID);
        }
        mImageLoader = ImageUtils.initImageLoader(getActivity());


        courseDao = TestpressSDKDatabase.getCourseDao(getContext());
        chapterDao = TestpressSDKDatabase.getChapterDao(getContext());
        contentDao = TestpressSDKDatabase.getContentDao(getContext());
        apiClient = new TestpressCourseApiClient(getContext());

        List<Course> courses = courseDao.queryBuilder()
                .where(CourseDao.Properties.Id.eq(courseId)).list();
        course = courses.get(0);
        currentPager = new ChapterPager(String.valueOf(courseId), apiClient);

        if (!isItemsEmpty()) {
            Chapter chapter = Chapter.getAllChaptersQueryBuilder(getContext(), courseId)
                    .orderDesc(ChapterDao.Properties.ModifiedDate).list().get(0);

            currentPager.setQueryParams(MODIFIED_SINCE, chapter.getModified());
            currentPager.setQueryParams(ORDER, "modified");
        }
        fetchChapters();

    }

    @Override
    protected void initItems() {
        if (getParentChaptersQueryBuilder().count() != 0) {
            showGrid();
        }
        displayItems();
        firstCallBack = false;
        swipeRefreshLayout.setEnabled(false);
    }

    private QueryBuilder<Chapter> getParentChaptersQueryBuilder() {
        WhereCondition parentCondition;
        if (parentChapterId.equals("null")) {
            parentCondition = ChapterDao.Properties.ParentId.isNull();
        } else {
            parentCondition = ChapterDao.Properties.ParentId.eq(parentChapterId);
        }
        return Chapter.getAllActiveChaptersQueryBuilder(getContext(), courseId)
                .where(parentCondition);
    }

    @Override
    protected List<Chapter> getItems() {
        return getParentChaptersQueryBuilder().orderAsc(ChapterDao.Properties.Order).list();
    }

    @Override
    protected boolean isItemsEmpty() {
        if (parentChapterId.equals("null")) {
            List<Course> courses = TestpressSDKDatabase.getCourseDao(getContext()).queryBuilder()
                    .where(CourseDao.Properties.Id.eq(courseId))
                    .list();

            if (!courses.isEmpty()) {
                //noinspection ConstantConditions
                ((ChapterDetailActivity) getActivity())
                        .setActionBarTitle(courses.get(0).getTitle());
            }
        } else {
            List<Chapter> chapters = TestpressSDKDatabase.getChapterDao(getContext()).queryBuilder()
                    .where(ChapterDao.Properties.Id.eq(parentChapterId))
                    .list();

            if (!chapters.isEmpty()) {
                //noinspection ConstantConditions
                ((ChapterDetailActivity) getActivity())
                        .setActionBarTitle(chapters.get(0).getName());
            }
        }

        return getParentChaptersQueryBuilder().count() == 0;
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
                    chaptersModified = !chapters.isEmpty();
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
                    if (!contents.isEmpty() || chaptersModified) {
                        chaptersModified = false;
                    }
                }
            }

            @Override
            public void onException(TestpressException exception) {
                handleError(exception);
            }
        });
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

    void displayError(int title, int description, int imageRes) {
        if (isItemsEmpty()) {
            setEmptyText(title, description, imageRes);
        } else {
            Snackbar.make(swipeRefreshLayout, description, Snackbar.LENGTH_SHORT).show();
        }
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    protected View getChildView(final Chapter chapter, ViewGroup parent) {
        View view = getLayoutInflater().inflate(R.layout.testpress_chapter_grid_item, parent, false);
        TextView name = view.findViewById(R.id.title);
        ImageView thumbnailImage = view.findViewById(R.id.thumbnail_image);
        name.setText(chapter.getName());
        if (chapter.getImage() == null || chapter.getImage().isEmpty()) {
            thumbnailImage.setVisibility(View.GONE);
        } else {
            thumbnailImage.setVisibility(View.VISIBLE);
            mImageLoader.displayImage(chapter.getImage(), thumbnailImage);
        }
        View lock = view.findViewById(R.id.lock);
        View whiteForeground = view.findViewById(R.id.white_foreground);
        if (chapter.getIsLocked()) {
            lock.setVisibility(View.VISIBLE);
            whiteForeground.setVisibility(View.VISIBLE);
        } else {
            lock.setVisibility(View.GONE);
            whiteForeground.setVisibility(View.GONE);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("ChaptersGridFragment", "onClick: ");
                    if (chapter.getRawChildrenCount(getContext()) > 0) {
                        displayChildChapters(chapter.getId());
                    } else {
                        displayContents(chapter);
                    }
                }
            });
        }
        assert getContext() != null;
        name.setTypeface(TestpressSdk.getRubikMediumFont(getContext()));
        return view;
    }

    void displayChildChapters(Long parentId) {
        this.parentChapterId = String.valueOf(parentId);
        initItems();
    }

    private void displayContents(Chapter chapter) {
        ContentsListFragment fragment = new ContentsListFragment();
        Bundle bundle = new Bundle();
        bundle.putLong(CHAPTER_ID, chapter.getId());
        bundle.putString(PRODUCT_SLUG, product_slug);
        fragment.setArguments(bundle);
        assert getFragmentManager() != null;

        if (getFragmentManager().getFragments().size() > 1) {
            Intent intent = ChapterDetailActivity.createIntent(
                    chapter.getSlug(),
                    getContext());
            intent.putExtra(COURSE_ID, courseId);
            intent.putExtra(CHAPTER_SLUG, chapter.getSlug());
            getActivity().startActivity(intent);
        } else  {
            getFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();
        }

    }

    @Override
    protected TableRow.LayoutParams getLayoutParams() {
        return new TableRow.LayoutParams(getChildColumnWidth(), TableRow.LayoutParams.WRAP_CONTENT);
    }

    @Override
    protected int getChildColumnWidth() {
        assert getContext() != null;
        return (int) UIUtils.getPixelFromDp(getContext(), 118);
    }

    @Override
    protected int getErrorMessage(TestpressException exception) {
        return R.string.testpress_network_error;
    }

    @Override
    protected void setEmptyText() {
        setEmptyText(R.string.testpress_no_chapter, R.string.testpress_no_chapter_description,
                R.drawable.ic_error_outline_black_18dp);
    }

    @Override
    protected BaseResourcePager<Chapter> getPager() {
        return null;
    }
}
