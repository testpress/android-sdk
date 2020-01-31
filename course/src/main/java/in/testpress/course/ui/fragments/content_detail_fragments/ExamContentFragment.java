package in.testpress.course.ui.fragments.content_detail_fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;

import java.util.ArrayList;
import java.util.List;

import in.testpress.core.TestpressSDKDatabase;
import in.testpress.course.R;
import in.testpress.course.network.TestpressCourseApiClient;
import in.testpress.course.util.ExoPlayerUtil;
import in.testpress.course.util.ExoplayerFullscreenHelper;
import in.testpress.exam.network.TestpressExamApiClient;
import in.testpress.exam.ui.FolderSpinnerAdapter;
import in.testpress.models.ProfileDetails;
import in.testpress.models.TestpressApiResponse;
import in.testpress.models.greendao.AttachmentDao;
import in.testpress.models.greendao.AttemptDao;
import in.testpress.models.greendao.Bookmark;
import in.testpress.models.greendao.BookmarkFolder;
import in.testpress.models.greendao.Content;
import in.testpress.models.greendao.ContentDao;
import in.testpress.models.greendao.CourseAttempt;
import in.testpress.models.greendao.CourseAttemptDao;
import in.testpress.models.greendao.ExamDao;
import in.testpress.models.greendao.HtmlContent;
import in.testpress.models.greendao.HtmlContentDao;
import in.testpress.models.greendao.Language;
import in.testpress.models.greendao.VideoAttempt;
import in.testpress.models.greendao.VideoDao;
import in.testpress.network.RetrofitCall;
import in.testpress.ui.view.ClosableSpinner;
import in.testpress.util.FullScreenChromeClient;
import in.testpress.util.WebViewUtils;
import in.testpress.v2_4.models.ApiResponse;
import in.testpress.v2_4.models.FolderListResponse;

public class ExamContentFragment extends BaseContentDetailFragment {
    private ExamDao examDao;
    public static final String ACTIONBAR_TITLE = "title";
    public static final String TESTPRESS_CONTENT_SHARED_PREFS = "testpressContentSharedPreferences";
    public static final String FORCE_REFRESH = "forceRefreshContentList";
    public static final String GO_TO_MENU = "gotoMenu";
    public static final String CONTENT_ID = "contentId";
    public static final String CHAPTER_ID = "chapterId";
    public static final String POSITION = "position";

    public ExoplayerFullscreenHelper exoplayerFullscreenHelper;
    private WebView webView;
    private RelativeLayout mContentView;
    private SwipeRefreshLayout swipeRefresh;
    private LinearLayout examContentLayout;
    private LinearLayout examDetailsLayout;
    private RecyclerView attemptList;
    private LinearLayout buttonLayout;
    private LinearLayout emptyContainer;
    private Button startButton;
    private LinearLayout attachmentContentLayout;
    private TextView emptyTitleView;
    private TextView emptyDescView;
    private Button retryButton;
    private TextView titleView;
    private LinearLayout titleLayout;
    private Button previousButton;
    private Button nextButton;
    private List<Content> contents;
    private Content content;
    private String contentId;
    private String attemptsUrl;
    private List<CourseAttempt> courseAttemptsFromDB = new ArrayList<>();
    private List<CourseAttempt> courseAttemptsFromNetwork = new ArrayList<>();
    private int position;
    private ContentDao contentDao;
    private HtmlContentDao htmlContentDao;
    private CourseAttemptDao courseAttemptDao;
    private AttemptDao attemptDao;
    private VideoDao videoDao;
    private AttachmentDao attachmentDao;
    private Long chapterId;
    private TestpressExamApiClient examApiClient;
    private TestpressCourseApiClient courseApiClient;
    private Toast toast;
    private LottieAnimationView animationView;
    private TextView bookmarkButtonText;
    private ImageView bookmarkButtonImage;
    private RelativeLayout bookmarkLayout;
    private LinearLayout bookmarkButtonLayout;
    private ArrayList<BookmarkFolder> bookmarkFolders = new ArrayList<>();
    private ClosableSpinner bookmarkFolderSpinner;
    private FolderSpinnerAdapter folderSpinnerAdapter;
    private WebViewUtils webViewUtils;
    private FullScreenChromeClient fullScreenChromeClient;
    private ExoPlayerUtil exoPlayerUtil;
    private FrameLayout exoPlayerMainFrame;
    private boolean isNonEmbeddableVideo;
    private VideoAttempt videoAttempt;
    private RetrofitCall<CourseAttempt> createAttemptApiRequest;
    private RetrofitCall<Content> updateContentApiRequest;
    private RetrofitCall<ProfileDetails> profileDetailApiRequest;
    private RetrofitCall<TestpressApiResponse<CourseAttempt>> attemptsApiRequest;
    private RetrofitCall<TestpressApiResponse<Language>> languagesApiRequest;
    private RetrofitCall<ApiResponse<FolderListResponse>> bookmarkFoldersApiRequest;
    private RetrofitCall<Bookmark> bookmarkApiRequest;
    private RetrofitCall<Void> deleteBookmarkApiRequest;
    private RetrofitCall<HtmlContent> htmlContentApiRequest;
    private String productSlug;
    private TextView pageNumber;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        examDao = TestpressSDKDatabase.getExamDao(getActivity());
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        examContentLayout = (LinearLayout) view.findViewById(R.id.exam_content_layout);
        examDetailsLayout = (LinearLayout) view.findViewById(R.id.exam_details_layout);
        attemptList = (RecyclerView) view.findViewById(R.id.attempt_list);
    }

    @Override
    void hideContents() {
        examDetailsLayout.setVisibility(View.GONE);
    }

    @Override
    void loadContent() {

    }

    @Override
    void onUpdateContent(Content content) {

    }

    @Override
    void onCreateContentAttempt() {

    }

    private void onExamContent() {
        if (content.getRawExam() == null || content.getAttemptsUrl() == null) {
            updateContent();
            return;
        }

//        setContentTitle(content.getName());
        // forceRefresh if already attempts is listed(courseAttemptsFromDB is populated)
        boolean forceRefresh = !courseAttemptsFromDB.isEmpty();
        courseAttemptsFromDB.clear();
        if (content.getAttemptsCount() > 0) {
            attemptsUrl = content.getAttemptsUrl();
            courseAttemptsFromNetwork.clear();
//            loadAttempts(forceRefresh);
        } else {
//            fetchLanguages(null);
        }
    }
}
