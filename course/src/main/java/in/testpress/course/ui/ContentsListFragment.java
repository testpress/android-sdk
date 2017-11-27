package in.testpress.course.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.Loader;

import org.greenrobot.greendao.AbstractDao;

import java.util.List;

import in.testpress.core.TestpressException;
import in.testpress.course.R;
import in.testpress.models.greendao.Attachment;
import in.testpress.models.greendao.AttachmentDao;
import in.testpress.models.greendao.Content;
import in.testpress.course.network.ContentPager;
import in.testpress.course.network.TestpressCourseApiClient;
import in.testpress.models.greendao.ContentDao;
import in.testpress.models.greendao.Exam;
import in.testpress.models.greendao.ExamDao;
import in.testpress.core.TestpressSDKDatabase;
import in.testpress.models.greendao.Video;
import in.testpress.models.greendao.VideoDao;
import in.testpress.ui.BaseDataBaseFragment;
import in.testpress.util.SingleTypeAdapter;
import in.testpress.util.ThrowableLoader;

import static in.testpress.course.ui.ContentsListActivity.CONTENTS_URL_FRAG;

public class ContentsListFragment extends BaseDataBaseFragment<Content, Long> {

    private TestpressCourseApiClient mApiClient;
    private String contentsUrlFrag;
    private ContentDao contentDao;
    private final String CHAPTER_ID = "chapterId";
    private long chapterId;

    public static void show(FragmentActivity activity, int containerViewId) {
        activity.getSupportFragmentManager().beginTransaction()
                .replace(containerViewId, new ContentsListFragment())
                .commitAllowingStateLoss();
    }

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
    }

    @Override
    protected ContentPager getPager() {
        if (pager == null) {
            pager = new ContentPager(contentsUrlFrag, mApiClient);
            if(contentDao.count() > 0) {
                Content latest = contentDao.queryBuilder()
                        //.where(ContentDao.Properties.ChapterId.eq())
                        .orderAsc(ContentDao.Properties.Start)
                        .list().get(0);
            }
        }
        return (ContentPager)pager;
    }

    @Override
    protected AbstractDao<Content, Long> getDao() {
        return contentDao;
    }

    @Override
    public void onLoadFinished(Loader<List<Content>> loader, List<Content> contents) {
        final TestpressException exception = getException(loader);
        Video video;
        Attachment attachment;
        Exam exam;
        VideoDao videoDao = TestpressSDKDatabase.getVideoDao(getContext());
        AttachmentDao attachmentDao = TestpressSDKDatabase.getAttachmentDao(getContext());
        ExamDao examDao = TestpressSDKDatabase.getExamDao(getContext());
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
        if (!contents.isEmpty()) {
            for(Content content : contents) {
                video = content.video;
                attachment = content.attachment;
                exam = content.exam;
                if (attachment != null) {
                    attachmentDao.insertOrReplace(attachment);
                    content.setAttachmentId(attachment.getId());
                }
                if (video != null) {
                    videoDao.insertOrReplace(video);
                    content.setVideoId(video.getId());
                }
                if (exam != null) {
                    examDao.insertOrReplace(exam);
                    content.setExamId(exam.getId());
                }
                getDao().insertOrReplace(content);
            }
        }
        displayDataFromDB();
        showList();
    }

    @Override
    protected SingleTypeAdapter<Content> createAdapter(List<Content> items) {
        return new ContentsListAdapter(getActivity(), items, R.layout.testpress_content_list_item, contentDao, chapterId);
    }

    @Override
    protected boolean isItemsEmpty() {
        return contentDao.queryBuilder()
                .where(
                        ContentDao.Properties.ChapterId.eq(chapterId)
                )
                .list().isEmpty();
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
