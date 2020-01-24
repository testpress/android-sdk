package in.testpress.course.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.Loader;

import java.util.List;

import in.testpress.core.TestpressException;
import in.testpress.core.TestpressSDKDatabase;
import in.testpress.course.R;
import in.testpress.course.network.ContentPager;
import in.testpress.course.network.TestpressCourseApiClient;
import in.testpress.models.greendao.AttachmentDao;
import in.testpress.models.greendao.Chapter;
import in.testpress.models.greendao.Content;
import in.testpress.models.greendao.ContentDao;
import in.testpress.models.greendao.ExamDao;
import in.testpress.models.greendao.HtmlContentDao;
import in.testpress.models.greendao.StreamDao;
import in.testpress.models.greendao.VideoDao;
import in.testpress.ui.BaseListViewFragment;
import in.testpress.util.Assert;
import in.testpress.util.SingleTypeAdapter;
import in.testpress.util.ThrowableLoader;

import static in.testpress.course.TestpressCourse.PRODUCT_SLUG;

public class ContentsListFragment extends BaseListViewFragment<Content> {

    public static final String CONTENTS_URL_FRAG = "contentsUrlFrag";
    public static final String CHAPTER_ID = "chapterId";

    private TestpressCourseApiClient mApiClient;
    private String contentsUrlFrag;
    private ContentDao contentDao;
    private Long chapterId;
    private String productSlug;
    private ContentPager pager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        contentsUrlFrag =  getArguments().getString(CONTENTS_URL_FRAG);
        if (getArguments() == null || contentsUrlFrag == null || contentsUrlFrag.isEmpty()) {
            throw new IllegalArgumentException("CONTENTS_URL_FRAG must not be null or empty");
        }
        mApiClient = new TestpressCourseApiClient(getActivity());
        contentDao = TestpressSDKDatabase.getContentDao(getContext());
        chapterId = getArguments().getLong(CHAPTER_ID);
        Assert.assertNotNull("chapterId must not be null.", chapterId);
        productSlug = getArguments().getString(PRODUCT_SLUG);
        pager = new ContentPager(contentsUrlFrag, mApiClient);
    }

    private void deleteContents(long courseId) {
        contentDao.queryBuilder().where(
                ContentDao.Properties.CourseId.eq(courseId),
                ContentDao.Properties.Active.eq(true)
        ).buildDelete().executeDeleteWithoutDetachingEntities();
        contentDao.detachAll();
    }

    @NonNull
    @Override
    public Loader<List<Content>> onCreateLoader(int id, @Nullable Bundle args) {
        return new ContentLoader(getContext(), pager, contentDao);
    }

    @Override
    public void refreshWithProgress() {
        pager.reset();
        super.refreshWithProgress();
    }

    private static class ContentLoader extends ThrowableLoader<List<Content>> {

        private ContentPager pager;
        private ContentDao contentDao;
        private Context context;

        ContentLoader(Context context, ContentPager pager, ContentDao contentDao) {
            super(context, null);
            this.pager = pager;
            this.contentDao = contentDao;
            this.context = context;
        }

        @Override
        public List<Content> loadData() throws TestpressException {
            do {
                pager.next();
                storeContent();
            } while (pager.hasNext());
            return pager.getResources();
        }

        private void storeContent() {
            ExamDao examDao = TestpressSDKDatabase.getExamDao(context);
            examDao.insertOrReplaceInTx(pager.getListResponse().getExams());

            HtmlContentDao htmlContentDao = TestpressSDKDatabase.getHtmlContentDao(context);
            htmlContentDao.insertOrReplaceInTx(pager.getListResponse().getNotes());

            AttachmentDao attachmentDao = TestpressSDKDatabase.getAttachmentDao(context);
            attachmentDao.insertOrReplaceInTx(pager.getListResponse().getAttachments());

            StreamDao streamDao = TestpressSDKDatabase.getStreamDao(context);
            streamDao.insertOrReplaceInTx(pager.getListResponse().getStreams());

            VideoDao videoDao = TestpressSDKDatabase.getVideoDao(context);
            videoDao.insertOrReplaceInTx(pager.getListResponse().getVideos());
        }
    }

    @Override
    public void onLoadFinished(Loader<List<Content>> loader, List<Content> contents) {
        final TestpressException exception = getException(loader);
        if (exception != null) {
            this.exception = exception;
            int errorMessage = getErrorMessage(exception);
            if (!isItemsEmpty()) {
                showError(errorMessage);
            }
            showList();
            getLoaderManager().destroyLoader(loader.getId());
            return;
        }

        this.exception = null;
        this.items = contents;
        Chapter chapter = Chapter.get(getContext(), chapterId.toString());
        deleteContents(chapter.getCourseId());
        contentDao.insertOrReplaceInTx(contents);
        getListAdapter().notifyDataSetChanged();
        showList();
    }

    @Override
    protected SingleTypeAdapter<Content> createAdapter(List<Content> items) {
        return new ContentsListAdapter(getActivity(), chapterId, productSlug);
    }

    @Override
    protected boolean isItemsEmpty() {
        return contentDao.queryBuilder()
                .where(ContentDao.Properties.ChapterId.eq(chapterId))
                .list().isEmpty();
    }

    @Override
    protected void refresh(final Bundle args) {
        if (!isUsable()) {
            return;
        }
        if (!getLoaderManager().hasRunningLoaders()) {
            super.refresh(args);
        }
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

}
