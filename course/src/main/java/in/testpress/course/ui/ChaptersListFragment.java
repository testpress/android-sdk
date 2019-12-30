package in.testpress.course.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;

import org.greenrobot.greendao.AbstractDao;

import java.util.List;

import in.testpress.core.TestpressCallback;
import in.testpress.core.TestpressException;
import in.testpress.core.TestpressSDKDatabase;
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
import in.testpress.ui.BaseDataBaseFragment;
import in.testpress.util.SingleTypeAdapter;

import static in.testpress.course.TestpressCourse.CHAPTER_ID;
import static in.testpress.course.TestpressCourse.CHAPTER_SLUG;
import static in.testpress.course.TestpressCourse.COURSE_ID;
import static in.testpress.course.TestpressCourse.PARENT_CHAPTER_ID;
import static in.testpress.course.TestpressCourse.PRODUCT_SLUG;
import static in.testpress.network.TestpressApiClient.MODIFIED_SINCE;
import static in.testpress.network.TestpressApiClient.ORDER;
import static in.testpress.network.TestpressApiClient.UNFILTERED;


public class ChaptersListFragment extends BaseDataBaseFragment<Chapter, Long> implements ChaptersListAdapter.OnChapterClickListener {

    private TestpressCourseApiClient apiClient;
    private long courseId;
    String parentChapterId = "null";
    private ChapterDao chapterDao;
    private CourseDao courseDao;
    private ContentDao contentDao;
    private Course course;
    private boolean chaptersModified;
    private BaseResourcePager currentPager;
    private String product_slug;


    public static ChaptersListFragment getInstance(long courseId, Long parentChapterId, String product_slug) {
        ChaptersListFragment fragment = new ChaptersListFragment();
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

        apiClient = new TestpressCourseApiClient(getActivity());
        chapterDao = TestpressSDKDatabase.getChapterDao(getActivity());
        courseDao = TestpressSDKDatabase.getCourseDao(getContext());
        chapterDao = TestpressSDKDatabase.getChapterDao(getContext());
        contentDao = TestpressSDKDatabase.getContentDao(getContext());
        currentPager = new ChapterPager(String.valueOf(courseId), apiClient);
        List<Course> courses = courseDao.queryBuilder()
                .where(CourseDao.Properties.Id.eq(courseId)).list();
        course = courses.get(0);

        if (!isItemsEmpty()) {
            Chapter chapter = Chapter.getAllChaptersQueryBuilder(getContext(), courseId)
                    .orderDesc(ChapterDao.Properties.ModifiedDate).list().get(0);

            currentPager.setQueryParams(MODIFIED_SINCE, chapter.getModified());
            currentPager.setQueryParams(ORDER, "modified");
        }
        fetchChapters();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        firstCallBack = false;
        listShown = true;
        super.onActivityCreated(savedInstanceState);
        swipeRefreshLayout.setEnabled(false);
    }

    @Override
    protected int getErrorMessage(TestpressException exception) {
        if (exception.isUnauthenticated()) {
            setEmptyText(R.string.testpress_authentication_failed, R.string.testpress_please_login,
                    R.drawable.ic_error_outline_black_18dp);
            return R.string.testpress_authentication_failed;
        } else if (exception.isNetworkError()) {
            setEmptyText(R.string.testpress_network_error, R.string.testpress_no_internet_try_again,
                    R.drawable.ic_error_outline_black_18dp);
            return R.string.testpress_no_internet_try_again;
        } else {
            setEmptyText(R.string.testpress_error_loading_contents,
                    R.string.testpress_some_thing_went_wrong_try_again,
                    R.drawable.ic_error_outline_black_18dp);
        }
        return R.string.testpress_some_thing_went_wrong_try_again;
    }

    @Override
    protected void setEmptyText() {
        setEmptyText(R.string.testpress_no_content, R.string.testpress_no_content_description,
                R.drawable.ic_error_outline_black_18dp);
    }

    private void setActionBarTitle(String chapterId) {

        if (chapterId != null && !chapterId.isEmpty() && !chapterId.equals("null")) {
            List<Chapter> chapters = TestpressSDKDatabase.getChapterDao(getContext()).queryBuilder()
                    .where(ChapterDao.Properties.Id.eq(chapterId))
                    .list();
            ((ChapterDetailActivity) getActivity()).setActionBarTitle(chapters.get(0).getName());
        } else {
            List<Course> courses = TestpressSDKDatabase.getCourseDao(getContext()).queryBuilder()
                    .where(CourseDao.Properties.Id.eq(courseId))
                    .list();

            if (!courses.isEmpty()) {
                //noinspection ConstantConditions
                ((ChapterDetailActivity) getActivity()).setActionBarTitle(courses.get(0).getTitle());
            }
        }
    }

    @Override
    protected boolean isItemsEmpty() {
        setActionBarTitle(parentChapterId);
        return Chapter.getParentChaptersQueryBuilder(getActivity(), String.valueOf(courseId), parentChapterId).listLazy().isEmpty();
    }


    @Override
    protected SingleTypeAdapter<Chapter> createAdapter(
            List<Chapter> items) {
        return new ChaptersListAdapter(getActivity(), items, R.layout.testpress_chapters_list_item,
                String.valueOf(courseId), parentChapterId, this);
    }

    @Override
    protected BaseResourcePager<Chapter> getPager() {
        return null;
    }

    @Override
    protected AbstractDao<Chapter, Long> getDao() {
        return chapterDao;
    }

    @Override
    public void onChapterClick(Chapter chapter) {
        if (chapter.getRawChildrenCount(getContext()) > 0) {
            displayChildChapters(chapter.getId());
        } else {
            displayContents(chapter);
        }
    }

    void displayChildChapters(Long parentId) {
        this.parentChapterId = String.valueOf(parentId);
        ChaptersListAdapter chaptersListAdapter = (ChaptersListAdapter) getListAdapter().getWrappedAdapter();
        chaptersListAdapter.setParentId(String.valueOf(parentId));
        getListAdapter().getWrappedAdapter().notifyDataSetChanged();
        setActionBarTitle(parentChapterId);
    }

    private void displayContents(Chapter chapter) {
        setActionBarTitle(chapter.getId().toString());
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

    private void fetchChapters() {
        ((ChapterPager) currentPager).enqueueNext(new TestpressCallback<List<Chapter>>() {
            @Override
            public void onSuccess(List<Chapter> chapters) {
                if (currentPager.hasMore()) {
                    fetchChapters();
                } else {
                    boolean loadModifiedContentsOnly =
                            currentPager.getQueryParams(MODIFIED_SINCE) != null;

                    currentPager = new ContentPager(courseId, apiClient);
                    if (loadModifiedContentsOnly) {
                        List<Content> contents = contentDao.queryBuilder()
                                .where(ContentDao.Properties.CourseId.eq(courseId))
                                .orderDesc(ContentDao.Properties.ModifiedDate).list();

                        if (!contents.isEmpty()) {
                            Content content = contents.get(0);
                            currentPager.setQueryParams(MODIFIED_SINCE, content.getModified());
                            currentPager.setQueryParams(UNFILTERED, true);
                        }
                    } else {
                        chapterDao.queryBuilder()
                                .where(ChapterDao.Properties.CourseId.eq(courseId))
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
}
