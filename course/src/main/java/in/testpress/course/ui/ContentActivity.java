package in.testpress.course.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import in.testpress.core.TestpressCallback;
import in.testpress.core.TestpressException;
import in.testpress.core.TestpressSDKDatabase;
import in.testpress.core.TestpressSdk;
import in.testpress.core.TestpressSession;
import in.testpress.core.TestpressUserDetails;
import in.testpress.course.R;
import in.testpress.course.network.TestpressCourseApiClient;
import in.testpress.course.util.ExoPlayerUtil;
import in.testpress.exam.TestpressExam;
import in.testpress.exam.network.TestpressExamApiClient;
import in.testpress.exam.ui.FolderSpinnerAdapter;
import in.testpress.exam.util.MultiLanguagesUtil;
import in.testpress.exam.util.RetakeExamUtil;
import in.testpress.models.ProfileDetails;
import in.testpress.models.TestpressApiResponse;
import in.testpress.models.greendao.Attachment;
import in.testpress.models.greendao.AttachmentDao;
import in.testpress.models.greendao.Attempt;
import in.testpress.models.greendao.AttemptDao;
import in.testpress.models.greendao.Bookmark;
import in.testpress.models.greendao.BookmarkFolder;
import in.testpress.models.greendao.Chapter;
import in.testpress.models.greendao.ChapterDao;
import in.testpress.models.greendao.Content;
import in.testpress.models.greendao.ContentDao;
import in.testpress.models.greendao.CourseAttempt;
import in.testpress.models.greendao.CourseAttemptDao;
import in.testpress.models.greendao.Exam;
import in.testpress.models.greendao.ExamDao;
import in.testpress.models.greendao.HtmlContent;
import in.testpress.models.greendao.HtmlContentDao;
import in.testpress.models.greendao.Language;
import in.testpress.models.greendao.Video;
import in.testpress.models.greendao.VideoAttempt;
import in.testpress.models.greendao.VideoDao;
import in.testpress.network.RetrofitCall;
import in.testpress.ui.BaseToolBarActivity;
import in.testpress.ui.ZoomableImageActivity;
import in.testpress.ui.view.ClosableSpinner;
import in.testpress.util.FormatDate;
import in.testpress.util.FullScreenChromeClient;
import in.testpress.util.ViewUtils;
import in.testpress.util.WebViewUtils;
import in.testpress.v2_4.models.ApiResponse;
import in.testpress.v2_4.models.FolderListResponse;

import static in.testpress.core.TestpressSdk.ACTION_PRESSED_HOME;
import static in.testpress.course.TestpressCourse.CHAPTER_ID;
import static in.testpress.course.network.TestpressCourseApiClient.EMBED_CODE;
import static in.testpress.course.network.TestpressCourseApiClient.EMBED_DOMAIN_RESTRICTED_VIDEO_PATH;
import static in.testpress.course.TestpressCourse.CHAPTER_SLUG;
import static in.testpress.course.network.TestpressCourseApiClient.CONTENTS_PATH_V2_4;
import static in.testpress.exam.network.TestpressExamApiClient.BOOKMARK_FOLDERS_PATH;
import static in.testpress.exam.network.TestpressExamApiClient.STATE_PAUSED;
import static in.testpress.exam.ui.CarouselFragment.TEST_TAKEN_REQUEST_CODE;
import static in.testpress.models.greendao.BookmarkFolder.UNCATEGORIZED;
import static in.testpress.models.greendao.Content.ATTACHMENT_TYPE;
import static in.testpress.models.greendao.Content.EXAM_TYPE;
import static in.testpress.models.greendao.Content.HTML_TYPE;
import static in.testpress.models.greendao.Content.VIDEO_TYPE;

public class ContentActivity extends BaseToolBarActivity {

    public static final String ACTIONBAR_TITLE = "title";
    public static final String TESTPRESS_CONTENT_SHARED_PREFS = "testpressContentSharedPreferences";
    public static final String FORCE_REFRESH = "forceRefreshContentList";
    public static final String GO_TO_MENU = "gotoMenu";
    public static final String CONTENT_ID = "contentId";

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
    private long contentId;
    private String attemptsUrl;
    private List<CourseAttempt> courseAttemptsFromDB = new ArrayList<>();
    private List<CourseAttempt> courseAttemptsFromNetwork = new ArrayList<>();
    private int position;
    private ContentDao contentDao;
    private HtmlContentDao htmlContentDao;
    private CourseAttemptDao courseAttemptDao;
    private AttemptDao attemptDao;
    private ExamDao examDao;
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
    private boolean isEmbeddableVideo;
    private VideoAttempt videoAttempt;
    private RetrofitCall<CourseAttempt> createAttemptApiRequest;
    private RetrofitCall<Content> updateContentApiRequest;
    private RetrofitCall<ProfileDetails> profileDetailApiRequest;
    private RetrofitCall<TestpressApiResponse<CourseAttempt>> attemptsApiRequest;
    private RetrofitCall<TestpressApiResponse<Language>> languagesApiRequest;
    private RetrofitCall<ApiResponse<FolderListResponse>> bookmarkFoldersApiRequest;
    private RetrofitCall<Bookmark> bookmarkApiRequest;
    private RetrofitCall<Void> deleteBookmarkApiRequest;

    public static Intent createIntent(long contentId, long chapterId, AppCompatActivity activity) {
        Intent intent = new Intent(activity, ContentActivity.class);
        intent.putExtra(CONTENT_ID, contentId);
        //noinspection ConstantConditions
        intent.putExtra(ACTIONBAR_TITLE, activity.getSupportActionBar().getTitle());
        intent.putExtra(CHAPTER_ID, chapterId);
        return intent;
    }

    public static Intent createIntent(String contentId, Context context) {
        Intent intent = new Intent(context, ContentActivity.class);
        intent.putExtra(CONTENT_ID, Long.parseLong(contentId));
        return intent;
    }

    @SuppressWarnings({"ConstantConditions", "deprecation"})
    @SuppressLint({"SetJavaScriptEnabled", "AddJavascriptInterface", "DefaultLocale", "ShowToast"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.testpress_activity_content_detail);
        contentDao = TestpressSDKDatabase.getContentDao(this);
        htmlContentDao = TestpressSDKDatabase.getHtmlContentDao(this);
        courseAttemptDao = TestpressSDKDatabase.getCourseAttemptDao(this);
        attemptDao = TestpressSDKDatabase.getAttemptDao(this);
        videoDao = TestpressSDKDatabase.getVideoDao(this);
        examDao = TestpressSDKDatabase.getExamDao(this);
        attachmentDao = TestpressSDKDatabase.getAttachmentDao(this);
        webView = (WebView) findViewById(R.id.web_view);
        mContentView = (RelativeLayout) findViewById(R.id.main_content);
        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        examContentLayout = (LinearLayout) findViewById(R.id.exam_content_layout);
        examDetailsLayout = (LinearLayout) findViewById(R.id.exam_details_layout);
        attemptList = (RecyclerView) findViewById(R.id.attempt_list);
        attachmentContentLayout = (LinearLayout) findViewById(R.id.attachment_content_layout);
        emptyContainer = (LinearLayout) findViewById(R.id.empty_container);
        startButton = (Button) findViewById(R.id.start_exam);
        emptyTitleView = (TextView) findViewById(R.id.empty_title);
        emptyDescView = (TextView) findViewById(R.id.empty_description);
        titleView = (TextView) findViewById(R.id.title);
        titleLayout = (LinearLayout) findViewById(R.id.title_layout);
        buttonLayout = (LinearLayout) findViewById(R.id.button_layout);
        previousButton = (Button) findViewById(R.id.previous);
        nextButton = (Button) findViewById(R.id.next);
        retryButton = (Button) findViewById(R.id.retry_button);
        exoPlayerMainFrame = findViewById(R.id.exo_player_main_frame);
        toast = Toast.makeText(this, R.string.testpress_no_internet_try_again, Toast.LENGTH_SHORT);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        TextView pageNumber = (TextView) findViewById(R.id.page_number);
        ViewUtils.setTypeface(
                new TextView[] {titleView, previousButton, nextButton, startButton, pageNumber},
                TestpressSdk.getRubikMediumFont(this)
        );
        bookmarkLayout = findViewById(R.id.bookmark_layout);
        bookmarkButtonLayout = findViewById(R.id.bookmark_button_layout);
        bookmarkButtonImage = findViewById(R.id.bookmark_button_image);
        bookmarkButtonText = findViewById(R.id.bookmark_text);
        bookmarkButtonText.setTypeface(TestpressSdk.getRubikRegularFont(this));
        bookmarkButtonLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (content.getBookmarkId() != null) {
                    deleteBookmark(content.getBookmarkId());
                } else {
                    String baseUrl = TestpressSdk.getTestpressSession(ContentActivity.this)
                            .getInstituteSettings().getBaseUrl();

                    bookmarkFolders.clear();
                    loadBookmarkFolders(baseUrl + BOOKMARK_FOLDERS_PATH);
                }
            }
        });
        bookmarkFolderSpinner = findViewById(R.id.bookmark_folder_spinner);
        folderSpinnerAdapter = new FolderSpinnerAdapter(this, getResources(),
                new ViewUtils.OnInputCompletedListener() {
                    @Override
                    public void onInputComplete(String folderName) {
                        bookmarkFolderSpinner.dismissPopUp();
                        bookmark(folderName);
                    }
                });
        folderSpinnerAdapter.hideSpinner(true);
        bookmarkFolderSpinner.setAdapter(folderSpinnerAdapter);
        bookmarkFolderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> spinner, View view, int position, long itemId) {
                if (position == 0) {
                    return;
                }
                bookmark(folderSpinnerAdapter.getTag(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        animationView = findViewById(R.id.bookmark_loader);
        animationView.playAnimation();
        examApiClient = new TestpressExamApiClient(this);
        courseApiClient = new TestpressCourseApiClient(this);
        swipeRefresh.setColorSchemeResources(R.color.testpress_color_primary);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateContent();
            }
        });
        webViewUtils = new WebViewUtils(webView) {
            @Override
            protected void onPageStarted() {
                super.onPageStarted();
                swipeRefresh.setRefreshing(true);
                emptyContainer.setVisibility(View.GONE);
                swipeRefresh.setVisibility(View.VISIBLE);
            }

            @Override
            protected void onLoadFinished() {
                super.onLoadFinished();
                swipeRefresh.setRefreshing(false);
                webView.setVisibility(View.VISIBLE);
                createContentAttempt();
            }

            @Override
            public String getHeader() {
                return super.getHeader() + getBookmarkHandlerScript();
            }

            @Override
            protected void onNetworkError() {
                setEmptyText(R.string.testpress_network_error,
                        R.string.testpress_no_internet_try_again,
                        R.drawable.ic_error_outline_black_18dp);

                retryButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        updateContent();
                    }
                });
            }

            @Override
            protected boolean shouldOverrideUrlLoading(Activity activity, String url) {
                if (url.contains(EMBED_DOMAIN_RESTRICTED_VIDEO_PATH)) {
                    return false;
                }
                return super.shouldOverrideUrlLoading(activity, url);
            }
        };
        webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        fullScreenChromeClient = new FullScreenChromeClient(this);
        contentId = getIntent().getLongExtra(CONTENT_ID, 0);
        if (contentId == 0) {
            throw new IllegalArgumentException("contentId must not be null.");
        }
        content = getContentFromDB();
        chapterId = getIntent().getLongExtra(CHAPTER_ID, 0);
        if (chapterId == 0) {
            if (content == null) {
                updateContent();
            } else {
                checkContentType();
            }
            buttonLayout.setVisibility(View.GONE);
        } else {
            if (content == null) {
                updateContent();
            } else {
                contents = getContentsFromDB();
                position = content.getOrder();
                pageNumber.setText(String.format("%d/%d", position + 1, contents.size()));
                checkContentType();
                validateAdjacentNavigationButton();
            }
        }
    }

    private Content getContentFromDB() {
        List<Content> contents = contentDao.queryBuilder()
                .where(ContentDao.Properties.Id.eq(contentId)).list();

        if (contents.isEmpty()) {
            return null;
        }
        return contents.get(0);
    }

    private void setChapterNameInActionBarTitle(long chapterId) {
        List<Chapter> chapters = TestpressSDKDatabase.getChapterDao(this).queryBuilder()
                .where(ChapterDao.Properties.Id.eq(chapterId)).list();

        if (!chapters.isEmpty()) {
            //noinspection ConstantConditions
            getSupportActionBar().setTitle(chapters.get(0).getName());
        }
    }

    private void checkContentType() {
        setChapterNameInActionBarTitle(content.getChapterId());
        setContentTitle(content.getTitle());
        hideContents();
        switch (content.getContentType()) {
            case HTML_TYPE:
                displayHtmlContent();
                break;
            case VIDEO_TYPE:
                break;
            case EXAM_TYPE:
                onExamContent();
                break;
            case ATTACHMENT_TYPE:
                displayAttachmentContent();
                break;
            default:
                setEmptyText(R.string.testpress_error_loading_contents,
                        R.string.testpress_some_thing_went_wrong_try_again,
                        R.drawable.ic_error_outline_black_18dp);
                break;
        }
    }

    void displayHtmlContent() {
        if (content.getRawHtmlContent() == null) {
            updateContent();
            return;
        }
        String html = "<div style='padding-left: 20px; padding-right: 20px;'>" +
                content.getRawHtmlContent().getTextHtml() + "</div>";

        webViewUtils.initWebView(html, this);
    }

    void displayVideoContent() {
        Video video = content.getRawVideo();
        if (video == null) {
            updateContent();
            return;
        }
        if (video.getIsDomainRestricted()) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty(EMBED_CODE, video.getEmbedCode());
            String url = courseApiClient.getBaseUrl() + EMBED_DOMAIN_RESTRICTED_VIDEO_PATH;
            webViewUtils.initWebViewAndPostUrl(url, jsonObject.toString(), this);
            webView.setWebChromeClient(fullScreenChromeClient);
        } else if (!content.isNonEmbeddableVideo()) {
            String html = "<div style='margin-top: 15px; padding-left: 20px; padding-right: 20px;'" +
                    "class='videoWrapper'>" + video.getEmbedCode() + "</div>";

            webViewUtils.initWebView(html, this);
            webView.setWebChromeClient(fullScreenChromeClient);
        } else {
            TestpressSession session = TestpressSdk.getTestpressSession(this);
            if (session != null && session.getInstituteSettings().isDisplayUserEmailOnVideo()) {
                checkProfileDetailExist(video.getUrl());
            } else {
                initExoPlayer(video.getUrl());
            }
        }
    }

    private void checkProfileDetailExist(final String videoUrl) {
        ProfileDetails profileDetails = TestpressUserDetails.getInstance().getProfileDetails();
        if (profileDetails != null) {
            initExoPlayer(videoUrl);
        } else {
            showLoadingProgress();
            profileDetailApiRequest = TestpressUserDetails.getInstance()
                    .load(this, new TestpressCallback<ProfileDetails>() {
                        @Override
                        public void onSuccess(ProfileDetails userDetails) {
                            swipeRefresh.setRefreshing(false);
                            initExoPlayer(videoUrl);
                        }

                        @Override
                        public void onException(TestpressException exception) {
                            handleError(exception, false);
                        }
                    });
        }
    }

    private void initExoPlayer(String videoUrl) {
        if (videoAttempt == null) {
            createContentAttempt();
        } else {
            float startPosition;
            try {
                startPosition = Float.parseFloat(videoAttempt.getLastPosition());
            } catch (NumberFormatException e) {
                startPosition = 0;
            }
            exoPlayerUtil = new ExoPlayerUtil(this, exoPlayerMainFrame, videoUrl, startPosition);
            exoPlayerUtil.setVideoAttemptParameters(videoAttempt.getId(), content);
            exoPlayerMainFrame.setVisibility(View.VISIBLE);
            exoPlayerUtil.initializePlayer();
        }
    }

    private void displayAttachmentContent() {
        if (content.getRawAttachment() == null) {
            updateContent();
            return;
        }
        TextView description = (TextView) findViewById(R.id.attachment_description);
        final Attachment attachment = content.getRawAttachment();
        if (attachment.getDescription() != null && !attachment.getDescription().isEmpty()) {
            description.setText(attachment.getDescription());
            description.setTypeface(TestpressSdk.getRubikRegularFont(this));
            description.setVisibility(View.VISIBLE);
        } else {
            description.setVisibility(View.GONE);
        }
        Button downloadButton = (Button) findViewById(R.id.download_attachment);
        ViewUtils.setLeftDrawable(this, downloadButton, R.drawable.ic_file_download_18dp);
        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse(attachment.getAttachmentUrl())));
            }
        });
        attachmentContentLayout.setVisibility(View.VISIBLE);
        swipeRefresh.setRefreshing(false);
        createContentAttempt();
    }

    private void onExamContent() {
        if (content.getRawExam() == null) {
            updateContent();
            return;
        }
        // forceRefresh if already attempts is listed
        boolean forceRefresh = !courseAttemptsFromDB.isEmpty();
        courseAttemptsFromDB.clear();
        if (content.getAttemptsCount() > 0) {
            attemptsUrl = content.getAttemptsUrl();
            courseAttemptsFromNetwork.clear();
            loadAttempts(forceRefresh);
        } else {
            fetchLanguages(null);
        }
    }

    @SuppressLint("SetTextI18n")
    private void displayStartExamScreen(CourseAttempt pausedCourseAttempt) {
        final Exam exam = content.getRawExam();
        TextView numberOfQuestions = (TextView) findViewById(R.id.number_of_questions);
        TextView examDuration = (TextView) findViewById(R.id.exam_duration);
        TextView markPerQuestion = (TextView) findViewById(R.id.mark_per_question);
        TextView negativeMarks = (TextView) findViewById(R.id.negative_marks);
        TextView date = (TextView) findViewById(R.id.date);
        LinearLayout description = (LinearLayout) findViewById(R.id.description);
        LinearLayout dateLayout = (LinearLayout) findViewById(R.id.date_layout);
        TextView descriptionContent = (TextView) findViewById(R.id.descriptionContent);
        TextView questionsLabel = (TextView) findViewById(R.id.questions_label);
        TextView durationLabel = (TextView) findViewById(R.id.duration_label);
        TextView markLabel = (TextView) findViewById(R.id.mark_per_question_label);
        TextView negativeMarkLabel = (TextView) findViewById(R.id.negative_marks_label);
        TextView dateLabel = (TextView) findViewById(R.id.date_label);
        TextView languageLabel = (TextView) findViewById(in.testpress.exam.R.id.language_label);
        ViewUtils.setTypeface(new TextView[] {numberOfQuestions, examDuration, markPerQuestion,
                negativeMarks, date }, TestpressSdk.getRubikMediumFont(this));
        ViewUtils.setTypeface(
                new TextView[] {
                        descriptionContent, questionsLabel, languageLabel, durationLabel, markLabel,
                        negativeMarkLabel, dateLabel
                },
                TestpressSdk.getRubikRegularFont(this)
        );
        numberOfQuestions.setText(exam.getNumberOfQuestions().toString());
        if (pausedCourseAttempt == null) {
            examDuration.setText(exam.getDuration());
        } else {
            durationLabel.setText(getString(R.string.testpress_time_remaining));
            examDuration.setText(pausedCourseAttempt.getAssessment().getRemainingTime());
        }
        markPerQuestion.setText(exam.getMarkPerQuestion());
        negativeMarks.setText(exam.getNegativeMarks());
        if (exam.getFormattedStartDate().equals("forever")) {
            dateLayout.setVisibility(View.GONE);
        } else {
            date.setText(exam.getFormattedStartDate() + " -\n" + exam.getFormattedEndDate());
            dateLayout.setVisibility(View.VISIBLE);
        }
        if ((exam.getDescription() != null) && !exam.getDescription().trim().isEmpty()) {
            description.setVisibility(View.VISIBLE);
            descriptionContent.setText(exam.getDescription());
        }
        updateStartButton(exam, pausedCourseAttempt, true);
        attemptList.setVisibility(View.GONE);
        examDetailsLayout.setVisibility(View.VISIBLE);
        examContentLayout.setVisibility(View.VISIBLE);
        swipeRefresh.setRefreshing(false);
    }

    private boolean canAttemptExam(Exam exam) {
        if (exam.getAttemptsCount() == 0 ||
                ((exam.getAllowRetake()) &&
                        ((exam.getAttemptsCount() + exam.getPausedAttemptsCount()) <= exam.getMaxRetakes() ||
                                exam.getMaxRetakes() < 0))) {

            if (content.getIsLocked() || !content.getHasStarted() || exam.isEnded()) {
                if (courseAttemptsFromDB.isEmpty()) {
                    TextView webOnlyLabel = (TextView) findViewById(R.id.web_only_label);
                    if (!content.getHasStarted()) {
                        webOnlyLabel.setText(String.format(
                                getString(R.string.testpress_can_start_exam_only_after),
                                FormatDate.formatDateTime(exam.getStartDate())
                        ));
                    } else if (exam.isEnded()) {
                        webOnlyLabel.setText(R.string.testpress_exam_ended);
                    } else {
                        webOnlyLabel.setText(R.string.testpress_score_good_in_previous_exam);
                    }
                    webOnlyLabel.setTypeface(TestpressSdk.getRubikRegularFont(this));
                    webOnlyLabel.setVisibility(View.VISIBLE);
                }
                return false;
            } else {
                return !isWebOnlyExam(exam);
            }
        } else {
            return false;
        }
    }

    private boolean isWebOnlyExam(Exam exam) {
        if (exam.getDeviceAccessControl() != null &&
                exam.getDeviceAccessControl().equals("web")) {
            TextView webOnlyLabel;
            if (courseAttemptsFromDB.isEmpty()) {
                webOnlyLabel = (TextView) findViewById(R.id.web_only_label);
            } else {
                webOnlyLabel = (TextView) findViewById(R.id.attempt_web_only_label);
            }
            webOnlyLabel.setTypeface(TestpressSdk.getRubikRegularFont(this));
            webOnlyLabel.setVisibility(View.VISIBLE);
            return true;
        }
        return false;
    }

    private void loadAttempts(final boolean forceRefresh) {
        courseAttemptsFromDB = getCourseAttemptsFromDB();
        if (courseAttemptsFromDB.size() > 0 && !forceRefresh) {
            onCourseAttemptsLoaded();
        } else {
            showLoadingProgress();
        }
        attemptsApiRequest = examApiClient.getContentAttempts(attemptsUrl)
                .enqueue(new TestpressCallback<TestpressApiResponse<CourseAttempt>>() {
                    @Override
                    public void onSuccess(TestpressApiResponse<CourseAttempt> response) {
                        courseAttemptsFromNetwork.addAll(response.getResults());
                        if (response.getNext() != null) {
                            attemptsUrl = response.getNext();
                            loadAttempts(forceRefresh);
                        } else {
                            clearContentAttemptsInDB();
                            saveCourseAttemptInDB(courseAttemptsFromNetwork);
                            courseAttemptsFromDB = getCourseAttemptsFromDB();
                            onCourseAttemptsLoaded();
                        }
                    }

                    @Override
                    public void onException(TestpressException exception) {
                        handleError(exception, false);
                    }
                });
    }

    void fetchLanguages(final CourseAttempt pausedCourseAttempt) {
        showLoadingProgress();
        languagesApiRequest = examApiClient.getLanguages(content.getRawExam().getSlug())
                .enqueue(new TestpressCallback<TestpressApiResponse<Language>>() {
                    @Override
                    public void onSuccess(TestpressApiResponse<Language> apiResponse) {
                        Exam exam = content.getRawExam();
                        List<Language> languages = exam.getRawLanguages();
                        languages.addAll(apiResponse.getResults());
                        Map<String, Language> uniqueLanguages = new HashMap<>();
                        for (Language language : languages) {
                            uniqueLanguages.put(language.getCode(), language);
                        }
                        content.getRawExam().setLanguages(new ArrayList<>(uniqueLanguages.values()));
                        if (apiResponse.hasMore()) {
                            fetchLanguages(pausedCourseAttempt);
                        } else {
                            exam.saveLanguages(getBaseContext());
                            displayStartExamScreen(pausedCourseAttempt);
                        }
                    }

                    @Override
                    public void onException(TestpressException exception) {
                        handleError(exception, false);
                    }
                });
    }

    private void saveCourseAttemptInDB(List<CourseAttempt> courseAttemptList) {
        for(CourseAttempt courseAttempt : courseAttemptList) {
            Attempt attempt = courseAttempt.getRawAssessment();
            attemptDao.insertOrReplace(attempt);
            courseAttempt.setAttemptId(attempt.getId());
            courseAttempt.setCourseContentId(content.getId());
            courseAttemptDao.insertOrReplace(courseAttempt);
        }
    }

    private void onCourseAttemptsLoaded() {
        if (courseAttemptsFromDB.size() == 1 &&
                courseAttemptsFromDB.get(0).getAssessment().getState().equals(STATE_PAUSED)) {
            // Only one paused attempt
            fetchLanguages(courseAttemptsFromDB.get(0));
        } else {
            displayAttemptsList();
        }
    }

    private List<CourseAttempt> getCourseAttemptsFromDB() {
        return courseAttemptDao.queryBuilder()
                .where(CourseAttemptDao.Properties.CourseContentId.eq(content.getId())).list();
    }

    private void displayAttemptsList() {
        courseAttemptsFromDB = getCourseAttemptsFromDB();
        final Exam exam = content.getRawExam();
        final List<CourseAttempt> pausedAttempts = new ArrayList<>();
        if (exam.getPausedAttemptsCount() > 0) {
            for (CourseAttempt attempt : courseAttemptsFromDB) {
                if (attempt.getAssessment().getState().equals(STATE_PAUSED)) {
                    pausedAttempts.add(attempt);
                }
            }
        }
        updateStartButton(exam, pausedAttempts.isEmpty() ? null : pausedAttempts.get(0), false);
        attemptList.setNestedScrollingEnabled(false);
        attemptList.setHasFixedSize(true);
        attemptList.setLayoutManager(new LinearLayoutManager(this));
        ArrayList<CourseAttempt> attempts = new ArrayList<>(courseAttemptsFromDB);
        attemptList.setAdapter(new ContentAttemptListAdapter(this, content, attempts));
        attemptList.setVisibility(View.VISIBLE);
        examDetailsLayout.setVisibility(View.GONE);
        examContentLayout.setVisibility(View.VISIBLE);
        swipeRefresh.setRefreshing(false);
    }

    private void updateStartButton(final Exam exam, final CourseAttempt pausedCourseAttempt,
                                   final boolean discardExamDetails) {

        if (pausedCourseAttempt == null && canAttemptExam(exam)) {
            if (courseAttemptsFromDB.isEmpty()) {
                startButton.setText(R.string.testpress_start);
            } else {
                startButton.setText(R.string.testpress_retake);
            }
            if (discardExamDetails) {
                MultiLanguagesUtil.supportMultiLanguage(this, exam, startButton,
                        new MultiLanguagesUtil.LanguageSelectionListener() {
                            @Override
                            public void onLanguageSelected() {
                                startCourseExam(true, false);
                            }});
            } else {
                startButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        RetakeExamUtil.showRetakeOptions(ContentActivity.this,
                                new RetakeExamUtil.SelectionListener() {
                                    @Override
                                    public void onOptionSelected(boolean isPartial) {
                                        startCourseExam(false, isPartial);
                                    }
                        });
                    }
                });
            }
            startButton.setVisibility(View.VISIBLE);
        } else if (pausedCourseAttempt != null && !isWebOnlyExam(exam)) {
            startButton.setText(R.string.testpress_resume);
            if (discardExamDetails) {
                MultiLanguagesUtil.supportMultiLanguage(this, exam, startButton,
                        new MultiLanguagesUtil.LanguageSelectionListener() {
                            @Override
                            public void onLanguageSelected() {
                                resumeCourseExam(true, pausedCourseAttempt);
                            }});
            } else {
                startButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        resumeCourseExam(false, pausedCourseAttempt);
                    }
                });
            }
            startButton.setVisibility(View.VISIBLE);
        } else {
            startButton.setVisibility(View.GONE);
        }
    }

    private void createContentAttempt() {
        if (content.getRawVideo() != null && !isEmbeddableVideo) {
            showLoadingProgress();
        }
        createAttemptApiRequest = courseApiClient.createContentAttempt(content.getId())
                .enqueue(new TestpressCallback<CourseAttempt>() {
                    @Override
                    public void onSuccess(CourseAttempt courseAttempt) {
                        if (content.getAttemptsCount() == 0) {
                            SharedPreferences prefs = getSharedPreferences(
                                    TESTPRESS_CONTENT_SHARED_PREFS, Context.MODE_PRIVATE);
                            prefs.edit().putBoolean(FORCE_REFRESH, true).apply();
                        }
                        if (content.getRawVideo() != null && !isEmbeddableVideo) {
                            swipeRefresh.setRefreshing(false);
                            videoAttempt = courseAttempt.getRawVideoAttempt();
                            initExoPlayer(content.getRawVideo().getUrl());
                        }
                    }

                    @Override
                    public void onException(TestpressException exception) {
                        if (content.getRawVideo() != null && !isEmbeddableVideo) {
                            handleError(exception, false);
                        } else if (!exception.isNetworkError()) {
                            exception.printStackTrace();
                        }
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == TEST_TAKEN_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                if (getCallingActivity() == null) {
                    SharedPreferences prefs =
                            getSharedPreferences(TESTPRESS_CONTENT_SHARED_PREFS, Context.MODE_PRIVATE);

                    prefs.edit().putBoolean(FORCE_REFRESH, true).apply();
                }
                if (content.getRawExam() != null) {
                    clearContentAttemptsInDB();
                }
                updateContent();
            }
        }
    }

    private void clearContentAttemptsInDB() {
        courseAttemptDao.queryBuilder()
                .where(CourseAttemptDao.Properties.CourseContentId.eq(content.getId()))
                .buildDelete()
                .executeDeleteWithoutDetachingEntities();
    }

    private void updateContent() {
        showLoadingProgress();
        hideContents();
        String contentUrl = CONTENTS_PATH_V2_4 + contentId;
        updateContentApiRequest = courseApiClient.getContent(contentUrl)
                .enqueue(new TestpressCallback<Content>() {
                    @Override
                    public void onSuccess(Content content) {
                        switch (content.getContentType()) {
                            case VIDEO_TYPE:
                                Video video = content.getRawVideo();
                                videoDao.insertOrReplace(video);
                                content.setVideoId(video.getId());
                                break;
                            case EXAM_TYPE:
                                Exam exam = content.getRawExam();
                                exam.saveLanguages(getBaseContext());
                                examDao.insertOrReplace(exam);
                                content.setExamId(exam.getId());
                                break;
                            case ATTACHMENT_TYPE:
                                Attachment attachment = content.getRawAttachment();
                                attachmentDao.insertOrReplace(attachment);
                                content.setAttachmentId(attachment.getId());
                                break;
                            case HTML_TYPE:
                                HtmlContent htmlContent = content.getRawHtmlContent();
                                htmlContentDao.insertOrReplace(htmlContent);
                                content.setHtmlId(htmlContent.getId());
                                break;
                        }
                        if (ContentActivity.this.content != null) {
                            content.setModified(ContentActivity.this.content.getModified());
                            content.setModifiedDate(ContentActivity.this.content.getModifiedDate());
                            content.setCourseId(ContentActivity.this.content.getCourseId());
                            content.setActive(ContentActivity.this.content.getActive());
                        }
                        contentDao.insertOrReplace(content);
                        ContentActivity.this.content = content;
                        if (chapterId != null) {
                            contents = getContentsFromDB();
                        }
                        checkContentType();
                    }

                    @Override
                    public void onException(TestpressException exception) {
                        handleError(exception, true);
                    }
                });
    }

    List<Content> getContentsFromDB() {
        return contentDao.queryBuilder()
                .where(
                        ContentDao.Properties.ChapterId.eq(chapterId),
                        ContentDao.Properties.Active.eq(true)
                )
                .orderAsc(ContentDao.Properties.Order)
                .listLazy();
    }

    private void startCourseExam(boolean discardExamDetails, boolean isPartial) {
        //noinspection ConstantConditions
        TestpressExam.startCourseExam(this, content, discardExamDetails, isPartial,
                TestpressSdk.getTestpressSession(this));
    }

    private void resumeCourseExam(boolean discardExamDetails, CourseAttempt pausedCourseAttempt) {
        //noinspection ConstantConditions
        TestpressExam.resumeCourseAttempt(this, content, pausedCourseAttempt, discardExamDetails,
                TestpressSdk.getTestpressSession(this));
    }

    void loadBookmarkFolders(String url) {
        setBookmarkProgress(true);
        bookmarkFoldersApiRequest = examApiClient.getBookmarkFolders(url)
                .enqueue(new TestpressCallback<ApiResponse<FolderListResponse>>() {
                    @Override
                    public void onSuccess(ApiResponse<FolderListResponse> apiResponse) {
                        bookmarkFolders.addAll(apiResponse.getResults().getFolders());
                        if (apiResponse.getNext() != null) {
                            loadBookmarkFolders(apiResponse.getNext());
                        } else {
                            addFoldersToSpinner(bookmarkFolders);
                            setBookmarkProgress(false);
                            bookmarkFolderSpinner.performClick();
                        }
                    }

                    @Override
                    public void onException(TestpressException exception) {
                        setBookmarkProgress(false);
                        handleException(exception);
                    }
                });
    }

    void bookmark(String folder) {
        setBookmarkProgress(true);
        bookmarkApiRequest = examApiClient.bookmark(content.getId(), folder, "chaptercontent", "courses")
                .enqueue(new TestpressCallback<Bookmark>() {
                    @Override
                    public void onSuccess(Bookmark bookmark) {
                        content.setBookmarkId(bookmark.getId());
                        contentDao.updateInTx(content);
                        bookmarkButtonText.setText(R.string.testpress_remove_bookmark);
                        bookmarkButtonImage.setImageResource(R.drawable.ic_remove_bookmark);
                        setBookmarkProgress(false);
                    }

                    @Override
                    public void onException(TestpressException exception) {
                        setBookmarkProgress(false);
                        handleException(exception);
                    }
                });
    }

    void deleteBookmark(Long bookmarkId) {
        setBookmarkProgress(true);
        deleteBookmarkApiRequest = examApiClient.deleteBookmark(bookmarkId)
                .enqueue(new TestpressCallback<Void>() {
                    @Override
                    public void onSuccess(Void data) {
                        content.setBookmarkId(null);
                        contentDao.updateInTx(content);
                        bookmarkFolderSpinner.setSelection(0);
                        bookmarkButtonText.setText(R.string.testpress_bookmark_this);
                        bookmarkButtonImage.setImageResource(R.drawable.ic_bookmark);
                        setBookmarkProgress(false);
                    }

                    @Override
                    public void onException(TestpressException exception) {
                        setBookmarkProgress(false);
                        handleException(exception);
                    }
                });
    }

    void setBookmarkProgress(boolean show) {
        if (show) {
            bookmarkButtonLayout.setVisibility(View.GONE);
            animationView.setVisibility(View.VISIBLE);
        } else {
            animationView.setVisibility(View.GONE);
            bookmarkButtonLayout.setVisibility(View.VISIBLE);
        }
    }

    void addFoldersToSpinner(List<BookmarkFolder> bookmarkFolders) {
        folderSpinnerAdapter.clear();
        folderSpinnerAdapter.addHeader("-- Select Folder --");
        for (BookmarkFolder folder: bookmarkFolders) {
            folderSpinnerAdapter.addItem(folder.getName(), folder.getName(), false, 0);
        }
        folderSpinnerAdapter.addItem(null, UNCATEGORIZED, false, 0);
        folderSpinnerAdapter.notifyDataSetChanged();
    }

    private void validateAdjacentNavigationButton() {
        if (contents == null) {
            // Discard navigation buttons if deep linked
            return;
        }
        // Set previous button
        if (position == 0) {
            previousButton.setVisibility(View.INVISIBLE);
        } else {
            previousButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final long previousContentId = contents.get(position - 1).getId();
                    startActivity(ContentActivity.createIntent(previousContentId, chapterId,
                            ContentActivity.this));

                    finish();
                }
            });
            previousButton.setVisibility(View.VISIBLE);
        }
        // Set next button
        if (position == (contents.size() - 1)) {
            nextButton.setText(R.string.testpress_menu);
            nextButton.setVisibility(View.VISIBLE);
            nextButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SharedPreferences prefs = getSharedPreferences(
                            TESTPRESS_CONTENT_SHARED_PREFS, Context.MODE_PRIVATE);
                    prefs.edit().putBoolean(GO_TO_MENU, true).apply();
                    finish();
                }
            });
        } else {
            final Content nextContent = contents.get(position + 1);
            if (nextContent.getIsLocked()) {
                nextButton.setVisibility(View.INVISIBLE);
            } else {
                nextButton.setText(R.string.testpress_next_content);
                nextButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(ContentActivity.createIntent(nextContent.getId(), chapterId,
                                ContentActivity.this));

                        finish();
                    }
                });
                nextButton.setVisibility(View.VISIBLE);
            }
        }
        buttonLayout.setVisibility(View.VISIBLE);
    }

    private void handleError(TestpressException exception, final boolean onUpdateContent) {
        if (exception.isUnauthenticated()) {
            setEmptyText(R.string.testpress_authentication_failed,
                    R.string.testpress_please_login,
                    R.drawable.ic_error_outline_black_18dp);

            retryButton.setVisibility(View.GONE);
        } else if (exception.isNetworkError()) {
            if (!swipeRefresh.isRefreshing()) {
                if(!toast.getView().isShown()) {
                    toast.show();
                }
                return;
            }
            setEmptyText(R.string.testpress_network_error,
                    R.string.testpress_no_internet_try_again,
                    R.drawable.ic_error_outline_black_18dp);

            retryButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    emptyContainer.setVisibility(View.GONE);
                    if (onUpdateContent) {
                        updateContent();
                    } else {
                        checkContentType();
                    }
                }
            });
        }  else if (exception.isPageNotFound()) {
            setEmptyText(R.string.testpress_content_not_available,
                    R.string.testpress_content_not_available_description,
                    R.drawable.ic_error_outline_black_18dp);

            retryButton.setVisibility(View.GONE);
        } else {
            setEmptyText(R.string.testpress_error_loading_contents,
                    R.string.testpress_some_thing_went_wrong_try_again,
                    R.drawable.ic_error_outline_black_18dp);

            retryButton.setVisibility(View.GONE);
        }
    }

    private class ImageHandler {
        @JavascriptInterface
        public void onClickImage(String url) {
            startActivity(ZoomableImageActivity.createIntent(url, ContentActivity.this));
        }
    }

    protected void setEmptyText(final int title, final int description, final int left) {
        emptyContainer.setVisibility(View.VISIBLE);
        emptyTitleView.setText(title);
        emptyTitleView.setCompoundDrawablesWithIntrinsicBounds(left, 0, 0, 0);
        emptyDescView.setText(description);
        swipeRefresh.setRefreshing(false);
        swipeRefresh.setVisibility(View.GONE);
        retryButton.setVisibility(View.VISIBLE);
    }

    private void hideContents() {
        examDetailsLayout.setVisibility(View.GONE);
        attemptList.setVisibility(View.GONE);
        attachmentContentLayout.setVisibility(View.GONE);
        webView.setVisibility(View.GONE);
        startButton.setVisibility(View.GONE);
    }

    private void showLoadingProgress() {
        examContentLayout.setVisibility(View.GONE);
        if (!swipeRefresh.isRefreshing()) {
            swipeRefresh.setVisibility(View.VISIBLE);
            swipeRefresh.setRefreshing(true);
        }
    }

    private void setContentTitle(CharSequence title) {
        titleView.setText(title);
        //noinspection ConstantConditions
        boolean bookmarksEnabled = TestpressSdk.getTestpressSession(this).getInstituteSettings()
                .isBookmarksEnabled();

        if (content.getRawExam() == null && bookmarksEnabled) {
            if (content.getBookmarkId() != null) {
                bookmarkButtonText.setText(R.string.testpress_remove_bookmark);
                bookmarkButtonImage.setImageResource(R.drawable.ic_remove_bookmark);
            } else {
                bookmarkButtonText.setText(R.string.testpress_bookmark_this);
                bookmarkButtonImage.setImageResource(R.drawable.ic_bookmark);
            }
            bookmarkLayout.setVisibility(View.VISIBLE);
        } else {
            bookmarkLayout.setVisibility(View.GONE);
        }
        titleLayout.setVisibility(View.VISIBLE);
    }

    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void onPause() {
        super.onPause();
        webView.onPause();
        toast.cancel();
        if (exoPlayerUtil != null) {
            exoPlayerUtil.onPause();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void onResume() {
        super.onResume();
        webView.onResume();
        if (exoPlayerUtil != null) {
            exoPlayerUtil.onResume();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (exoPlayerUtil != null) {
            exoPlayerUtil.onStart();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (exoPlayerUtil != null) {
            exoPlayerUtil.onStop();
        }
    }

    @Override
    public RetrofitCall[] getRetrofitCalls() {
        return new RetrofitCall[] {
                createAttemptApiRequest, updateContentApiRequest, profileDetailApiRequest,
                attemptsApiRequest, languagesApiRequest, bookmarkFoldersApiRequest,
                bookmarkApiRequest, deleteBookmarkApiRequest
        };
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            // Set result with home button pressed true if activity is called by startActivityForResult
            if (getCallingActivity() != null) {
                Intent intent = new Intent();
                intent.putExtra(ACTION_PRESSED_HOME, true);
                if (content != null) {
                    intent.putExtra(CHAPTER_SLUG, content.getChapterSlug());
                }
                setResult(RESULT_CANCELED, intent);
                finish();
            } else {
                super.onBackPressed();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    void handleException(TestpressException exception) {
        if(exception.isUnauthenticated()) {
            Snackbar.make(mContentView, R.string.testpress_authentication_failed,
                    Snackbar.LENGTH_SHORT).show();
        } else if (exception.getCause() instanceof IOException) {
            Snackbar.make(mContentView, R.string.testpress_no_internet_connection,
                    Snackbar.LENGTH_SHORT).show();
        } else if (exception.isClientError()) {
            Snackbar.make(mContentView, R.string.testpress_folder_name_not_allowed,
                    Snackbar.LENGTH_SHORT).show();
        } else {
            Snackbar.make(mContentView, R.string.testpress_network_error,
                    Snackbar.LENGTH_SHORT).show();
        }
    }

}