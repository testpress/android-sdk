package in.testpress.course.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.MenuItem;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import junit.framework.Assert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import in.testpress.core.TestpressCallback;
import in.testpress.core.TestpressException;
import in.testpress.core.TestpressSdk;
import in.testpress.course.R;
import in.testpress.course.models.HtmlContent;
import in.testpress.course.network.TestpressCourseApiClient;
import in.testpress.exam.TestpressExam;
import in.testpress.exam.models.CourseAttempt;
import in.testpress.exam.models.CourseContent;
import in.testpress.models.greendao.Exam;
import in.testpress.exam.network.TestpressExamApiClient;
import in.testpress.exam.util.MultiLanguagesUtil;
import in.testpress.models.TestpressApiResponse;
import in.testpress.models.greendao.Attachment;
import in.testpress.models.greendao.Content;
import in.testpress.models.greendao.Video;
import in.testpress.ui.BaseToolBarActivity;
import in.testpress.ui.ZoomableImageActivity;
import in.testpress.util.FormatDate;
import in.testpress.util.ViewUtils;

import static in.testpress.core.TestpressSdk.ACTION_PRESSED_HOME;
import static in.testpress.course.TestpressCourse.CHAPTER_URL;
import static in.testpress.exam.network.TestpressExamApiClient.STATE_PAUSED;
import static in.testpress.exam.ui.CarouselFragment.TEST_TAKEN_REQUEST_CODE;

public class ContentActivity extends BaseToolBarActivity {

    public static final String ACTIONBAR_TITLE = "title";
    public static final String TESTPRESS_CONTENT_SHARED_PREFS = "testpressContentSharedPreferences";
    public static final String FORCE_REFRESH = "forceRefreshContentList";
    public static final String GO_TO_MENU = "gotoMenu";
    public static final String CONTENT_ID = "contentId";
    public static final String CONTENTS = "contents";
    public static final String POSITION = "position";

    private WebView webView;
    private FrameLayout mCustomViewContainer;
    private LinearLayout mContentView;
    private View mCustomView;
    private SwipeRefreshLayout swipeRefresh;
    private LinearLayout examContentLayout;
    private LinearLayout examDetailsLayout;
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
    private boolean hasError = false;
    private ArrayList<Content> contents;
    private Content content;
    private String contentId;
    private String attemptsUrl;
    private ArrayList<CourseAttempt> courseAttempts = new ArrayList<>();
    private int position;
    private FullScreenChromeClient mWebChromeClient = null;
    private WebChromeClient.CustomViewCallback mCustomViewCallback;

    public static Intent createIntent(List<Content> contents,
                                      int position,
                                      AppCompatActivity activity) {

        Intent intent = new Intent(activity, ContentActivity.class);
        // TODO : Pass id instead of list from here
        //intent.putParcelableArrayListExtra(CONTENTS, new ArrayList<Content>(contents));
        intent.putExtra(POSITION, position);
        //noinspection ConstantConditions
        intent.putExtra(ACTIONBAR_TITLE, activity.getSupportActionBar().getTitle());
        return intent;
    }

    public static Intent createIntent(String contentId, Context context) {
        Intent intent = new Intent(context, ContentActivity.class);
        intent.putExtra(CONTENT_ID, contentId);
        return intent;
    }

    @SuppressWarnings({"ConstantConditions", "deprecation"})
    @SuppressLint({"SetJavaScriptEnabled", "AddJavascriptInterface", "DefaultLocale"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.testpress_activity_content_detail);
        webView = (WebView) findViewById(R.id.web_view);
        mCustomViewContainer = (FrameLayout) findViewById(R.id.container);
        mContentView = (LinearLayout) findViewById(R.id.main_content);
        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        examContentLayout = (LinearLayout) findViewById(R.id.exam_content_layout);
        examDetailsLayout = (LinearLayout) findViewById(R.id.exam_details_layout);
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
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        TextView pageNumber = (TextView) findViewById(R.id.page_number);
        ViewUtils.setTypeface(
                new TextView[] {titleView, previousButton, nextButton, startButton, pageNumber},
                TestpressSdk.getRubikMediumFont(this)
        );
        mWebChromeClient = new FullScreenChromeClient();
        webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        webView.setWebChromeClient(mWebChromeClient);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setPluginState(WebSettings.PluginState.ON);
        webSettings.getJavaScriptCanOpenWindowsAutomatically();
        webSettings.setBuiltInZoomControls(false);
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        swipeRefresh.setColorSchemeResources(R.color.testpress_color_primary);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateContent();
            }
        });
        webView.addJavascriptInterface(new ImageHandler(), "ImageHandler");
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                swipeRefresh.post(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefresh.setRefreshing(true);
                    }
                });
                hasError = false;
                emptyContainer.setVisibility(View.GONE);
                swipeRefresh.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                swipeRefresh.setRefreshing(false);
                super.onPageFinished(view, url);
                if(!hasError) {
                    String javascript = "" +
                            "javascript:" +
                            "var images = document.getElementsByTagName('img');" +
                            "for (i = 0; i < images.length; i++) {" +
                            "   images[i].onclick = (" +
                            "       function() {" +
                            "           var src = images[i].src;" +
                            "           return function() {" +
                            "               ImageHandler.onClickImage(src);" +
                            "           }" +
                            "       }" +
                            "   )();" +
                            "}";
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        webView.evaluateJavascript(javascript, null);
                    } else {
                        webView.loadUrl(javascript, null);
                    }
                    validateAdjacentNavigationButton();
                    webView.setVisibility(View.VISIBLE);
                    createContentAttempt();
                }
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request,
                                        WebResourceError error) {

                super.onReceivedError(view, request, error);
                setEmptyText(R.string.testpress_network_error,
                        R.string.testpress_no_internet_try_again,
                        R.drawable.ic_error_outline_black_18dp);

                retryButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        checkContentType();
                    }
                });
                hasError = true;
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
                return true;
            }

        });
        // TODO: contents = getIntent().getParcelableArrayListExtra(CONTENTS);
        if (contents == null) {
            contentId = getIntent().getStringExtra(CONTENT_ID);
            if (contentId == null) {
                Assert.assertNotNull("contentId must not be null.", contents);
            } else {
                updateContent();
            }
        } else {
            position = getIntent().getIntExtra(POSITION, -1);
            if (position == -1) {
                throw new IllegalArgumentException("POSITION must not be null.");
            }
            content = contents.get(position);
            String title = getIntent().getStringExtra(ACTIONBAR_TITLE);
            Assert.assertNotNull("ACTIONBAR_TITLE must not be null.", title);
            getSupportActionBar().setTitle(title);
            pageNumber.setText(String.format("%d/%d", position + 1, contents.size()));
            checkContentType();
        }
    }

    private void checkContentType() {
        hideContents();
        if (content.getHtmlContentTitle() != null) {
            loadContentHtml();
        } else if (content.getVideo() != null) {
            Video video = content.getVideo();
            setContentTitle(video.getTitle());
            webView.loadDataWithBaseURL("file:///android_asset/", getHeader() +
                    "<div class='videoWrapper'>" + video.getEmbedCode() + "</div>",
                    "text/html", "UTF-8", null);
        } else if (content.getExam() != null) {
            onExamContent();
        } else if (content.getAttachment() != null) {
            displayAttachmentContent();
        } else {
            setEmptyText(R.string.testpress_error_loading_contents,
                    R.string.testpress_some_thing_went_wrong_try_again,
                    R.drawable.ic_error_outline_black_18dp);
        }
    }

    private void loadContentHtml() {
        showLoadingProgress();
        //noinspection deprecation
        setContentTitle(Html.fromHtml(content.getHtmlContentTitle()));
        new TestpressCourseApiClient(this).getHtmlContent(content.getHtmlContentUrl())
                .enqueue(new TestpressCallback<HtmlContent>() {
                    @Override
                    public void onSuccess(HtmlContent htmlContent) {
                        webView.loadDataWithBaseURL("file:///android_asset/", getHeader() +
                                htmlContent.getTextHtml(), "text/html", "UTF-8", null);
                    }

                    @Override
                    public void onException(TestpressException exception) {
                        handleError(exception, false);
                    }
                });
    }

    private void displayAttachmentContent() {
        setContentTitle(content.getName());
        TextView description = (TextView) findViewById(R.id.attachment_description);
        final Attachment attachment = content.getAttachment();
        if (attachment.getDescription() != null) {
            description.setText(attachment.getDescription());
            description.setTypeface(TestpressSdk.getRubikRegularFont(this));
            description.setVisibility(View.VISIBLE);
        } else {
            description.setVisibility(View.GONE);
        }
        Button downloadButton = (Button) findViewById(R.id.download_attachment);
        ViewUtils.setLeftDrawable(this, downloadButton, R.drawable.ic_file_download_white_18dp);
        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse(attachment.getAttachmentUrl())));
            }
        });
        validateAdjacentNavigationButton();
        attachmentContentLayout.setVisibility(View.VISIBLE);
        swipeRefresh.setRefreshing(false);
        createContentAttempt();
    }

    private void onExamContent() {
        setContentTitle(content.getName());
        courseAttempts.clear();
        if (content.getAttemptsCount() > 0) {
            attemptsUrl = content.getAttemptsUrl();
            loadAttempts();
        } else {
            displayStartExamScreen(null);
        }
    }

    @SuppressLint("SetTextI18n")
    private void displayStartExamScreen(CourseAttempt pausedCourseAttempt) {
        final Exam exam = content.getExam();
        TextView numberOfQuestions = (TextView) findViewById(R.id.number_of_questions);
        TextView examDuration = (TextView) findViewById(R.id.exam_duration);
        TextView markPerQuestion = (TextView) findViewById(R.id.mark_per_question);
        TextView negativeMarks = (TextView) findViewById(R.id.negative_marks);
        LinearLayout description = (LinearLayout) findViewById(R.id.description);
        TextView descriptionContent = (TextView) findViewById(R.id.descriptionContent);
        TextView questionsLabel = (TextView) findViewById(R.id.questions_label);
        TextView durationLabel = (TextView) findViewById(R.id.duration_label);
        TextView markLabel = (TextView) findViewById(R.id.mark_per_question_label);
        TextView negativeMarkLabel = (TextView) findViewById(R.id.negative_marks_label);
        TextView languageLabel = (TextView) findViewById(in.testpress.exam.R.id.language_label);
        ViewUtils.setTypeface(new TextView[] {numberOfQuestions, examDuration, markPerQuestion,
                negativeMarks}, TestpressSdk.getRubikMediumFont(this));
        ViewUtils.setTypeface(new TextView[] {descriptionContent, questionsLabel, languageLabel,
                durationLabel, markLabel, negativeMarkLabel}, TestpressSdk.getRubikRegularFont(this));
        numberOfQuestions.setText(exam.getNumberOfQuestions().toString());
        if (pausedCourseAttempt == null) {
            examDuration.setText(exam.getDuration());
        } else {
            durationLabel.setText(getString(R.string.testpress_time_remaining));
            examDuration.setText(pausedCourseAttempt.getAssessment().getRemainingTime());
        }
        markPerQuestion.setText(exam.getMarkPerQuestion());
        negativeMarks.setText(exam.getNegativeMarks());
        if ((exam.getDescription() != null) && !exam.getDescription().trim().isEmpty()) {
            description.setVisibility(View.VISIBLE);
            descriptionContent.setText(exam.getDescription());
        }
        updateStartButton(exam, pausedCourseAttempt, true);
        validateAdjacentNavigationButton();
        examDetailsLayout.setVisibility(View.VISIBLE);
        examContentLayout.setVisibility(View.VISIBLE);
        swipeRefresh.setRefreshing(false);
    }

    private boolean canAttemptExam(Exam exam) {
        if (exam.getAttemptsCount() == 0 ||
                ((exam.getAllowRetake()) &&
                        ((exam.getAttemptsCount() + exam.getPausedAttemptsCount()) <= exam.getMaxRetakes() ||
                                exam.getMaxRetakes() < 0))) {

            if (content.getIsLocked() || !content.getHasStarted()) {
                if (courseAttempts.isEmpty()) {
                    TextView webOnlyLabel = (TextView) findViewById(R.id.web_only_label);
                    if (!content.getHasStarted()) {
                        webOnlyLabel.setText(String.format(
                                getString(R.string.testpress_can_start_exam_only_after),
                                FormatDate.formatDateTime(exam.getStartDate().toString())
                        ));
                    } else {
                        webOnlyLabel.setText(R.string.testpress_score_good_in_previous_exam);
                    }
                    webOnlyLabel.setTypeface(TestpressSdk.getRubikRegularFont(this));
                    webOnlyLabel.setVisibility(View.VISIBLE);
                }
                return false;
            } else {
                return !isWebOnlyExam(exam) && !exam.isEnded();
            }
        } else {
            return false;
        }
    }

    private boolean isWebOnlyExam(Exam exam) {
        if (exam.getDeviceAccessControl() != null &&
                exam.getDeviceAccessControl().equals("web")) {
            TextView webOnlyLabel;
            if (courseAttempts.isEmpty()) {
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

    private void loadAttempts() {
        showLoadingProgress();
        Map<String, Object> queryParams = new HashMap<>();
        new TestpressExamApiClient(this).getContentAttempts(attemptsUrl, queryParams)
                .enqueue(new TestpressCallback<TestpressApiResponse<CourseAttempt>>() {
                    @Override
                    public void onSuccess(TestpressApiResponse<CourseAttempt> response) {
                        courseAttempts.addAll(response.getResults());
                        if (response.getNext() != null) {
                            attemptsUrl = response.getNext();
                            loadAttempts();
                        } else if (courseAttempts.size() == 1 &&
                                courseAttempts.get(0).getAssessment().getState().equals(STATE_PAUSED)) {
                            // Only one paused attempt
                            displayStartExamScreen(courseAttempts.get(0));
                        } else {
                            displayAttemptsList();
                        }
                    }

                    @Override
                    public void onException(TestpressException exception) {
                        handleError(exception, false);
                    }
                });
    }

    private void displayAttemptsList() {
        final Exam exam = content.getExam();
        final List<CourseAttempt> pausedAttempts = new ArrayList<>();
        if (exam.getPausedAttemptsCount() > 0) {
            for (CourseAttempt attempt : courseAttempts) {
                if (attempt.getAssessment().getState().equals(STATE_PAUSED)) {
                    pausedAttempts.add(attempt);
                }
            }
        }
        updateStartButton(exam, pausedAttempts.isEmpty() ? null : pausedAttempts.get(0), false);
        RecyclerView attemptList = (RecyclerView) findViewById(R.id.attempt_list);
        attemptList.setNestedScrollingEnabled(false);
        attemptList.setHasFixedSize(true);
        attemptList.setLayoutManager(new LinearLayoutManager(this));
        attemptList.setAdapter(new ContentAttemptListAdapter(this, content, courseAttempts));
        validateAdjacentNavigationButton();
        examDetailsLayout.setVisibility(View.GONE);
        examContentLayout.setVisibility(View.VISIBLE);
        swipeRefresh.setRefreshing(false);
    }

    private void updateStartButton(final Exam exam, final CourseAttempt pausedCourseAttempt,
                                   final boolean discardExamDetails) {

        if (pausedCourseAttempt == null && canAttemptExam(exam)) {
            if (courseAttempts.isEmpty()) {
                startButton.setText(R.string.testpress_start);
            } else {
                startButton.setText(R.string.testpress_retake);
            }
            if (discardExamDetails) {
                MultiLanguagesUtil.supportMultiLanguage(this, exam, startButton,
                        new MultiLanguagesUtil.LanguageSelectionListener() {
                            @Override
                            public void onLanguageSelected() {
                                startCourseExam(true);
                            }});
            } else {
                startButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startCourseExam(false);
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
        new TestpressExamApiClient(this)
                .createContentAttempt(content.getAttemptsUrl())
                .enqueue(new TestpressCallback<CourseAttempt>() {
                    @Override
                    public void onSuccess(CourseAttempt courseAttempt) {
                        if (content.getAttemptsCount() == 0) {
                            SharedPreferences prefs = getSharedPreferences(
                                    TESTPRESS_CONTENT_SHARED_PREFS, Context.MODE_PRIVATE);
                            prefs.edit().putBoolean(FORCE_REFRESH, true).apply();
                        }
                    }

                    @Override
                    public void onException(TestpressException exception) {
                        if (!exception.isNetworkError()) {
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
                SharedPreferences prefs =
                        getSharedPreferences(TESTPRESS_CONTENT_SHARED_PREFS, Context.MODE_PRIVATE);
                prefs.edit().putBoolean(FORCE_REFRESH, true).apply();
                updateContent();
            }
        }
    }

    private void updateContent() {
        showLoadingProgress();
        hideContents();
        String contentUrl;
        if (content != null) {
            contentUrl = content.getUrl();
        } else {
            contentUrl = TestpressCourseApiClient.CONTENTS_PATH + contentId;
        }
        new TestpressCourseApiClient(this).getContent(contentUrl)
                .enqueue(new TestpressCallback<Content>() {
                    @Override
                    public void onSuccess(Content content) {
                        if (contents != null) {
                            contents.set(position, content);
                        }
                        ContentActivity.this.content = content;
                        checkContentType();
                    }

                    @Override
                    public void onException(TestpressException exception) {
                        handleError(exception, true);
                    }
                });
    }

    String getHeader() {
        return "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1, user-scalable=no\" />" +
                "<link rel=\"stylesheet\" type=\"text/css\" href=\"testpress_typebase.css\" />" +
                "<style>" +
                "   img{display: inline; height: auto !important; width: auto !important; max-width: 100%;}" +
                "</style>";
    }

    private void startCourseExam(boolean discardExamDetails) {
        //noinspection ConstantConditions
        TestpressExam.startCourseExam(ContentActivity.this,
                new CourseContent(content.getAttemptsUrl(), content.getExam()), discardExamDetails,
                TestpressSdk.getTestpressSession(ContentActivity.this));
    }

    private void resumeCourseExam(boolean discardExamDetails, CourseAttempt pausedCourseAttempt) {
        //noinspection ConstantConditions
        TestpressExam.resumeCourseAttempt(ContentActivity.this,
                new CourseContent(content.getAttemptsUrl(), content.getExam()),
                pausedCourseAttempt,
                discardExamDetails,
                TestpressSdk.getTestpressSession(ContentActivity.this));
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
            final int previousPosition = position - 1;
            previousButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(ContentActivity.createIntent(contents, previousPosition,
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
            final int nextPosition = position + 1;
            if (contents.get(nextPosition).getIsLocked()) {
                nextButton.setVisibility(View.INVISIBLE);
            } else {
                nextButton.setText(R.string.testpress_next_content);
                nextButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        startActivity(ContentActivity.createIntent(contents, nextPosition,
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
            setEmptyText(R.string.testpress_network_error,
                    R.string.testpress_no_internet_try_again,
                    R.drawable.ic_error_outline_black_18dp);

            retryButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onUpdateContent) {
                        updateContent();
                    } else {
                        checkContentType();
                    }
                }
            });
        }  else if (exception.getResponse().code() == 404) {
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
        hasError = true;
        swipeRefresh.setRefreshing(false);
        swipeRefresh.setVisibility(View.GONE);
        retryButton.setVisibility(View.VISIBLE);
    }

    private void hideContents() {
        buttonLayout.setVisibility(View.GONE);
        attachmentContentLayout.setVisibility(View.GONE);
        webView.setVisibility(View.GONE);
        startButton.setVisibility(View.GONE);
    }

    private void showLoadingProgress() {
        examContentLayout.setVisibility(View.GONE);
        if (!swipeRefresh.isRefreshing()) {
            swipeRefresh.setVisibility(View.VISIBLE);
            swipeRefresh.post(new Runnable() {
                @Override
                public void run() {
                    swipeRefresh.setRefreshing(true);
                }
            });
        }
    }

    private void setContentTitle(CharSequence title) {
        titleView.setText(title);
        titleLayout.setVisibility(View.VISIBLE);
    }

    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void onPause() {
        super.onPause();
        webView.onPause();
    }

    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void onResume() {
        super.onResume();
        webView.onResume();
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            // Set result with home button pressed true if activity is called by startActivityForResult
            if (getCallingActivity() != null) {
                Intent intent = new Intent();
                intent.putExtra(ACTION_PRESSED_HOME, true);
                if (content != null) {
                    intent.putExtra(CHAPTER_URL, content.getChapterUrl());
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

    private class FullScreenChromeClient extends WebChromeClient {
        FrameLayout.LayoutParams LayoutParameters = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);

        @Override
        public void onShowCustomView(View view, CustomViewCallback callback) {
            if (mCustomView != null) {
                callback.onCustomViewHidden();
                return;
            }
            mContentView.setVisibility(View.INVISIBLE);
            view.setLayoutParams(LayoutParameters);
            mCustomView = view;
            mCustomViewCallback = callback;
            mCustomViewContainer.setVisibility(View.VISIBLE);
            mCustomViewContainer.addView(view);
        }

        @Override
        public void onHideCustomView() {
            if (mCustomView != null) {
                mCustomView.setVisibility(View.GONE);
                mCustomViewContainer.removeView(mCustomView);
                mCustomView = null;
                mCustomViewContainer.setVisibility(View.GONE);
                mCustomViewCallback.onCustomViewHidden();
                mContentView.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (mCustomView != null) {
            mWebChromeClient.onHideCustomView();
        } else {
            super.onBackPressed();
        }
    }

}