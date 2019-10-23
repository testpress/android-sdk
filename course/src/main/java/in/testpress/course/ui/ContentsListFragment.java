package in.testpress.course.ui;

import android.os.Bundle;
import androidx.loader.content.Loader;

import org.greenrobot.greendao.AbstractDao;

import java.util.List;

import in.testpress.core.TestpressException;
import in.testpress.core.TestpressSDKDatabase;
import in.testpress.course.R;
import in.testpress.course.network.ContentPager;
import in.testpress.course.network.TestpressCourseApiClient;
import in.testpress.models.greendao.Attachment;
import in.testpress.models.greendao.AttachmentDao;
import in.testpress.models.greendao.Content;
import in.testpress.models.greendao.ContentDao;
import in.testpress.models.greendao.Exam;
import in.testpress.models.greendao.ExamDao;
import in.testpress.models.greendao.Language;
import in.testpress.models.greendao.LanguageDao;
import in.testpress.models.greendao.Video;
import in.testpress.models.greendao.VideoDao;
import in.testpress.ui.BaseDataBaseFragment;
import in.testpress.util.Assert;
import in.testpress.util.SingleTypeAdapter;

public class ContentsListFragment extends BaseDataBaseFragment<Content, Long> {

    public static final String CONTENTS_URL_FRAG = "contentsUrlFrag";
    public static final String CHAPTER_ID = "chapterId";

    private TestpressCourseApiClient mApiClient;
    private String contentsUrlFrag;
    private ContentDao contentDao;
    private Long chapterId;

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
    }

    @Override
    protected ContentPager getPager() {
        if (pager == null) {
            pager = new ContentPager(contentsUrlFrag, mApiClient);
        }
        return (ContentPager) pager;
    }

    @Override
    protected AbstractDao<Content, Long> getDao() {
        return contentDao;
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
        if (!contents.isEmpty()) {
            for(Content content : contents) {
                Video video = content.getRawVideo();
                Attachment attachment = content.getRawAttachment();
                Exam exam = content.getRawExam();
                if (attachment != null) {
                    AttachmentDao attachmentDao = TestpressSDKDatabase.getAttachmentDao(getContext());
                    attachmentDao.insertOrReplace(attachment);
                    content.setAttachmentId(attachment.getId());
                } else if (video != null) {
                    VideoDao videoDao = TestpressSDKDatabase.getVideoDao(getContext());
                    videoDao.insertOrReplace(video);
                    content.setVideoId(video.getId());
                } else if (exam != null) {
                    List<Language> languages = exam.getRawLanguages();
                    for (Language language : languages) {
                        language.setExamId(exam.getId());
                    }
                    LanguageDao languageDao = TestpressSDKDatabase.getLanguageDao(getContext());
                    languageDao.insertOrReplaceInTx(languages);
                    ExamDao examDao = TestpressSDKDatabase.getExamDao(getContext());
                    examDao.insertOrReplace(exam);
                    content.setExamId(exam.getId());
                } else if (content.getHtmlContentTitle() != null) {
                    List<Content> contentsFromDB = getDao().queryBuilder()
                            .where(ContentDao.Properties.Id.eq(content.getId())).list();

                    if (!contentsFromDB.isEmpty()) {
                        Content contentFromDB = contentsFromDB.get(0);
                        if (contentFromDB.getHtmlId() != null) {
                            content.setHtmlId(contentFromDB.getHtmlId());
                        }
                    }
                }
                getDao().insertOrReplace(content);
            }
        }
        displayDataFromDB();
        showList();
    }

    @Override
    protected SingleTypeAdapter<Content> createAdapter(List<Content> items) {
        return new ContentsListAdapter(getActivity(), items, R.layout.testpress_content_list_item,
                contentDao, chapterId);
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
