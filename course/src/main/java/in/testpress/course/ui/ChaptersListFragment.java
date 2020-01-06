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
import in.testpress.core.TestpressSdk;
import in.testpress.course.R;
import in.testpress.course.network.ChapterPager;
import in.testpress.course.network.ContentPager;
import in.testpress.course.network.TestpressCourseApiClient;
import in.testpress.models.InstituteSettings;
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
    private BaseResourcePager currentPager;

    public static ChaptersListFragment getInstance(long courseId, Long parentChapterId) {
        ChaptersListFragment fragment = new ChaptersListFragment();
        Bundle bundle = new Bundle();
        bundle.putLong(COURSE_ID, courseId);
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
        if (getArguments().getString(PARENT_CHAPTER_ID) != null) {
            parentChapterId = getArguments().getString(PARENT_CHAPTER_ID);
        }

        initDao();
        initPager();
    }

    private void initDao() {
        apiClient = new TestpressCourseApiClient(getActivity());
        chapterDao = TestpressSDKDatabase.getChapterDao(getActivity());
        courseDao = TestpressSDKDatabase.getCourseDao(getContext());
        chapterDao = TestpressSDKDatabase.getChapterDao(getContext());
        contentDao = TestpressSDKDatabase.getContentDao(getContext());
    }

    private void initPager() {
        currentPager = new ChapterPager(String.valueOf(courseId), apiClient);
        List<Course> courses = courseDao.queryBuilder()
                .where(CourseDao.Properties.Id.eq(courseId)).list();
        course = courses.get(0);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        firstCallBack = false;
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onViewCreated(View view,
                              Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        swipeRefreshLayout.setEnabled(false);
        setListShown(!isItemsEmpty());
        fetchChapters();
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
        return new ChaptersListAdapter(getActivity(), String.valueOf(courseId), parentChapterId, this);
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
        if (chapter.hasSubChapters(getContext())) {
            displayChildChapters(chapter);
        } else {
            displayContents(chapter);
        }
    }

    void openChaptersFromActivity(Chapter chapter) {
        Intent intent = ChapterDetailActivity.createIntent(
                chapter.getSlug(),
                getContext());
        intent.putExtra(COURSE_ID, courseId);
        intent.putExtra(CHAPTER_SLUG, chapter.getSlug());
        intent.putExtra(PARENT_CHAPTER_ID, chapter.getId().toString());
        getActivity().startActivity(intent);
    }

    void showChaptersInFragment(Long parentId) {
        this.parentChapterId = String.valueOf(parentId);
        ChaptersListAdapter chaptersListAdapter = (ChaptersListAdapter) getListAdapter().getWrappedAdapter();
        chaptersListAdapter.setParentId(String.valueOf(parentId));
        getListAdapter().getWrappedAdapter().notifyDataSetChanged();
        setActionBarTitle(parentChapterId);
    }

    void displayChildChapters(Chapter chapter) {
        InstituteSettings instituteSettings =
                TestpressSdk.getTestpressSession(getContext()).getInstituteSettings();

        if(instituteSettings.isCoursesFrontend() &&
                instituteSettings.isCoursesGamificationEnabled()) {
            openChaptersFromActivity(chapter);
        } else {
            showChaptersInFragment(chapter.getId());
        }
    }

    void showContentsFromActivity(Chapter chapter) {
        Intent intent = ChapterDetailActivity.createIntent(
                chapter.getSlug(),
                getContext());
        intent.putExtra(COURSE_ID, courseId);
        intent.putExtra(CHAPTER_SLUG, chapter.getSlug());
        getActivity().startActivity(intent);
    }

    void showContentsInFragment(Chapter chapter) {
        setActionBarTitle(chapter.getId().toString());
        ContentsListFragment fragment = new ContentsListFragment();
        Bundle bundle = new Bundle();
        bundle.putLong(CHAPTER_ID, chapter.getId());
        fragment.setArguments(bundle);
        assert getFragmentManager() != null;
        getFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();
    }

    private void displayContents(Chapter chapter) {
        setActionBarTitle(chapter.getId().toString());
        InstituteSettings instituteSettings =
                TestpressSdk.getTestpressSession(getContext()).getInstituteSettings();

        if(instituteSettings.isCoursesFrontend() && instituteSettings.isCoursesGamificationEnabled()) {
            showContentsFromActivity(chapter);
        } else {
            showContentsInFragment(chapter);
        }

    }

    private void detachExistingChaptersFromDb() {
        chapterDao.queryBuilder()
                .where(ChapterDao.Properties.CourseId.eq(courseId))
                .buildDelete().executeDeleteWithoutDetachingEntities();

        chapterDao.detachAll();
    }

    private void detachExistingContentsFromDb() {
        contentDao.queryBuilder()
                .where(ContentDao.Properties.CourseId.eq(course.getId()))
                .buildDelete().executeDeleteWithoutDetachingEntities();

        contentDao.detachAll();
    }

    private void fetchChapters() {
        ((ChapterPager) currentPager).fetchItemsAsync(((ChapterPager) currentPager).getRetrofitCall(), new TestpressCallback<List<Chapter>>() {
            @Override
            public void onSuccess(List<Chapter> chapters) {
                if (getListAdapter() != null) {
                    getListAdapter().notifyDataSetChanged();
                }

                if (currentPager.hasMore()) {
                    fetchChapters();
                } else {
                    detachExistingChaptersFromDb();
                    chapterDao.insertOrReplaceInTx(chapters);
                    setListShown(true);
                    fetchContents();
                }
            }

            @Override
            public void onException(TestpressException exception) {
                setListShown(isItemsEmpty());
                handleError(exception);
            }
        });
    }

    private void fetchContents() {
        ((ContentPager) currentPager).fetchItemsAsync(((ContentPager) currentPager).getRetrofitCall(), new TestpressCallback<List<Content>>() {
            @Override
            public void onSuccess(List<Content> contents) {
                if (currentPager.hasMore()) {
                    fetchContents();
                } else {
                    detachExistingContentsFromDb();
                    contentDao.insertOrReplaceInTx(contents);
                    if (!course.getChildItemsLoaded()) {
                        course.setChildItemsLoaded(true);
                        courseDao.insertOrReplace(course);
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
                    R.drawable.ic_error_outline_black_18dp);
        } else if (exception.isNetworkError()) {
            displayError(R.string.testpress_network_error,
                    R.string.testpress_no_internet_try_again,
                    R.drawable.ic_error_outline_black_18dp);
            retryButton.setVisibility(View.VISIBLE);
        } else if (exception.isPageNotFound()) {
            displayError(R.string.testpress_content_not_available,
                    R.string.testpress_content_not_available_description,
                    R.drawable.ic_error_outline_black_18dp);
        } else  {
            displayError(R.string.testpress_error_loading_contents,
                    R.string.testpress_some_thing_went_wrong_try_again,
                    R.drawable.ic_error_outline_black_18dp);
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
