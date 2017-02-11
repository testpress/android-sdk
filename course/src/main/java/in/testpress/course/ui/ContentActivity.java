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
import android.widget.RelativeLayout;
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
import in.testpress.course.models.Attachment;
import in.testpress.course.models.Content;
import in.testpress.course.models.HtmlContent;
import in.testpress.course.models.Video;
import in.testpress.course.network.TestpressCourseApiClient;
import in.testpress.exam.TestpressExam;
import in.testpress.exam.models.CourseAttempt;
import in.testpress.exam.models.CourseContent;
import in.testpress.exam.models.Exam;
import in.testpress.exam.network.TestpressExamApiClient;
import in.testpress.model.TestpressApiResponse;
import in.testpress.ui.BaseToolBarActivity;
import in.testpress.ui.ZoomableImageActivity;
import in.testpress.util.ViewUtils;

import static in.testpress.exam.network.TestpressExamApiClient.STATE_PAUSED;
import static in.testpress.exam.ui.CarouselFragment.TEST_TAKEN_REQUEST_CODE;

public class ContentActivity extends BaseToolBarActivity {

    public static final String ACTIONBAR_TITLE = "title";
    public static final String TESTPRESS_CONTENT_SHARED_PREFS = "testpressContentSharedPreferences";
    public static final String FORCE_REFRESH = "forceRefreshContentList";
    public static final String GO_TO_MENU = "gotoMenu";
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
    private Button previousButton;
    private Button nextButton;
    private boolean hasError = false;
    private ArrayList<Content> contents;
    private Content content;
    private String attemptsUrl;
    private ArrayList<CourseAttempt> courseAttempts = new ArrayList<>();
    private int position;
    private FullScreenChromeClient mWebChromeClient = null;
    private WebChromeClient.CustomViewCallback mCustomViewCallback;

    public static Intent createIntent(List<Content> contents,
                                      int position,
                                      AppCompatActivity activity) {

        Intent intent = new Intent(activity, ContentActivity.class);
        intent.putParcelableArrayListExtra(CONTENTS, new ArrayList<Content>(contents));
        intent.putExtra(POSITION, position);
        //noinspection ConstantConditions
        intent.putExtra(ACTIONBAR_TITLE, activity.getSupportActionBar().getTitle());
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
        buttonLayout = (LinearLayout) findViewById(R.id.button_layout);
        previousButton = (Button) findViewById(R.id.previous);
        nextButton = (Button) findViewById(R.id.next);
        retryButton = (Button) findViewById(R.id.retry_button);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        contents = getIntent().getParcelableArrayListExtra(CONTENTS);
        Assert.assertNotNull("CONTENTS must not be null.", contents);
        position = getIntent().getIntExtra(POSITION, -1);
        if (position == -1) {
            throw new IllegalArgumentException("POSITION must not be null.");
        }
        content = contents.get(position);
        String title = getIntent().getStringExtra(ACTIONBAR_TITLE);
        Assert.assertNotNull("ACTIONBAR_TITLE must not be null.", title);
        //noinspection ConstantConditions
        getSupportActionBar().setTitle(title);
        TextView pageNumber = (TextView) findViewById(R.id.page_number);
        pageNumber.setText(String.format("%d/%d", position + 1, contents.size()));
        ViewUtils.setTypeface(
                new TextView[] {titleView, previousButton, nextButton, startButton, pageNumber},
                TestpressSdk.getRubikMediumFont(this)
        );
        retryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkContentType();
            }
        });
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

                hasError = true;
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
                return true;
            }

        });
        checkContentType();
    }

    private void checkContentType() {
        hideContents();
        if (content.getHtmlContentTitle() != null) {
            loadContentHtml();
        } else if (content.getVideo() != null) {
            Video video = content.getVideo();
            titleView.setText(video.getTitle());
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
        swipeRefresh.post(new Runnable() {
            @Override
            public void run() {
                swipeRefresh.setRefreshing(true);
            }
        });
        titleView.setText(Html.fromHtml(content.getHtmlContentTitle()));
        new TestpressCourseApiClient(this).getHtmlContent(content.getHtmlContentUrl())
                .enqueue(new TestpressCallback<HtmlContent>() {
                    @Override
                    public void onSuccess(HtmlContent htmlContent) {
                        webView.loadDataWithBaseURL("file:///android_asset/", getHeader() +
                                htmlContent.getTextHtml(), "text/html", "UTF-8", null);
                    }

                    @Override
                    public void onException(TestpressException exception) {
                        if (exception.isUnauthenticated()) {
                            setEmptyText(R.string.testpress_authentication_failed,
                                    R.string.testpress_please_login,
                                    R.drawable.ic_error_outline_black_18dp);
                        } else if (exception.isNetworkError()) {
                            setEmptyText(R.string.testpress_network_error,
                                    R.string.testpress_no_internet_try_again,
                                    R.drawable.ic_error_outline_black_18dp);
                        } else {
                            setEmptyText(R.string.testpress_error_loading_contents,
                                    R.string.testpress_some_thing_went_wrong_try_again,
                                    R.drawable.ic_error_outline_black_18dp);
                        }
                    }
                });
    }

    private void displayAttachmentContent() {
        titleView.setText(content.getName());
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
        Exam exam = content.getExam();
        titleView.setText(content.getName());
        if (content.getAttemptsCount() > 1 ||
                (content.getAttemptsCount() == 1 && exam.getPausedAttemptsCount() == 0)) {
            courseAttempts.clear();
            attemptsUrl = content.getAttemptsUrl();
            loadAttempts();
        } else {
            displayStartExamScreen();
        }
    }

    @SuppressLint("SetTextI18n")
    private void displayStartExamScreen() {
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
        ViewUtils.setTypeface(new TextView[] {numberOfQuestions, examDuration, markPerQuestion,
                negativeMarks}, TestpressSdk.getRubikMediumFont(this));
        ViewUtils.setTypeface(new TextView[] {descriptionContent, questionsLabel,
                durationLabel, markLabel}, TestpressSdk.getRubikRegularFont(this));
        numberOfQuestions.setText(exam.getNumberOfQuestions().toString());
        examDuration.setText(exam.getDuration());
        markPerQuestion.setText(exam.getMarkPerQuestion());
        negativeMarks.setText(exam.getNegativeMarks());
        if ((exam.getDescription() != null) && !exam.getDescription().trim().isEmpty()) {
            description.setVisibility(View.VISIBLE);
            descriptionContent.setText(exam.getDescription());
        }
        if (exam.getPausedAttemptsCount() > 0) {
            startButton.setText(R.string.testpress_resume);
        } else {
            startButton.setText(R.string.testpress_start);
        }
        if (exam.getAttemptsCount() == 0 ||
                ((exam.getAllowRetake()) &&
                        (exam.getAttemptsCount() <= exam.getMaxRetakes() ||
                                exam.getMaxRetakes() < 0))) {
            startButton.setVisibility(View.VISIBLE);
        } else {
            startButton.setVisibility(View.GONE);
        }
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startCourseExam();
            }
        });
        validateAdjacentNavigationButton();
        examDetailsLayout.setVisibility(View.VISIBLE);
        examContentLayout.setVisibility(View.VISIBLE);
        swipeRefresh.setRefreshing(false);
    }

    private void loadAttempts() {
        if (!swipeRefresh.isRefreshing()) {
            swipeRefresh.setVisibility(View.VISIBLE);
            swipeRefresh.post(new Runnable() {
                @Override
                public void run() {
                    swipeRefresh.setRefreshing(true);
                }
            });
        }
        Map<String, Object> queryParams = new HashMap<>();
        new TestpressExamApiClient(this).getContentAttempts(attemptsUrl, queryParams)
                .enqueue(new TestpressCallback<TestpressApiResponse<CourseAttempt>>() {
                    @Override
                    public void onSuccess(TestpressApiResponse<CourseAttempt> response) {
                        courseAttempts.addAll(response.getResults());
                        if (response.getNext() != null) {
                            attemptsUrl = response.getNext();
                            loadAttempts();
                        } else {
                            displayAttemptsList();
                        }
                    }

                    @Override
                    public void onException(TestpressException exception) {
                        if (exception.isUnauthenticated()) {
                            setEmptyText(R.string.testpress_authentication_failed,
                                    R.string.testpress_please_login,
                                    R.drawable.ic_error_outline_black_18dp);
                        } else if (exception.isNetworkError()) {
                            setEmptyText(R.string.testpress_network_error,
                                    R.string.testpress_no_internet_try_again,
                                    R.drawable.ic_error_outline_black_18dp);
                        } else {
                            setEmptyText(R.string.testpress_error_loading_contents,
                                    R.string.testpress_some_thing_went_wrong_try_again,
                                    R.drawable.ic_error_outline_black_18dp);
                        }
                        retryButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                loadAttempts();
                            }
                        });
                    }
                });
    }

    private void displayAttemptsList() {
        final Exam exam = content.getExam();
        if ((exam.getAllowRetake()) &&
                        (exam.getAttemptsCount() <= exam.getMaxRetakes() ||
                                exam.getMaxRetakes() < 0)) {

            startButton.setVisibility(View.VISIBLE);
            final List<CourseAttempt> pausedAttempts = new ArrayList<>();
            if (exam.getPausedAttemptsCount() > 0) {
                for (CourseAttempt attempt : courseAttempts) {
                    if (attempt.getAssessment().getState().equals(STATE_PAUSED)) {
                        pausedAttempts.add(attempt);
                    }
                }
            }
            if (pausedAttempts.isEmpty()) {
                startButton.setText(R.string.testpress_retake);
                startButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startCourseExam();
                    }
                });
            } else {
                startButton.setText(R.string.testpress_resume);
                startButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //noinspection ConstantConditions
                        TestpressExam.resumeCourseAttempt(ContentActivity.this,
                                new CourseContent(content.getAttemptsUrl(), exam),
                                pausedAttempts.get(0), false,
                                TestpressSdk.getTestpressSession(ContentActivity.this));
                    }
                });
            }
        } else {
            startButton.setVisibility(View.GONE);
        }
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
        swipeRefresh.setVisibility(View.VISIBLE);
        swipeRefresh.post(new Runnable() {
            @Override
            public void run() {
                swipeRefresh.setRefreshing(true);
            }
        });
        hideContents();
        new TestpressCourseApiClient(this).getContent(content.getUrl())
                .enqueue(new TestpressCallback<Content>() {
                    @Override
                    public void onSuccess(Content content) {
                        contents.set(position, content);
                        ContentActivity.this.content = content;
                        checkContentType();
                    }

                    @Override
                    public void onException(TestpressException exception) {
                        if (exception.isUnauthenticated()) {
                            setEmptyText(R.string.testpress_authentication_failed,
                                    R.string.testpress_please_login,
                                    R.drawable.ic_error_outline_black_18dp);
                        } else if (exception.isNetworkError()) {
                            setEmptyText(R.string.testpress_network_error,
                                    R.string.testpress_no_internet_try_again,
                                    R.drawable.ic_error_outline_black_18dp);
                        } else {
                            setEmptyText(R.string.testpress_error_loading_contents,
                                    R.string.testpress_some_thing_went_wrong_try_again,
                                    R.drawable.ic_error_outline_black_18dp);
                        }
                        retryButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                updateContent();
                            }
                        });
                    }
                });
    }

    String getHeader() {
        return "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1, user-scalable=no\" />" +
                "<link rel=\"stylesheet\" type=\"text/css\" href=\"typebase.css\" />" +
                "<style>" +
                "   img{display: inline; height: auto !important; width: auto !important; max-width: 100%;}" +
                "</style>";
    }

    private void startCourseExam() {
        //noinspection ConstantConditions
        TestpressExam.startCourseExam(ContentActivity.this,
                new CourseContent(content.getAttemptsUrl(), content.getExam()),
                true, TestpressSdk.getTestpressSession(ContentActivity.this));
    }

    private void validateAdjacentNavigationButton() {
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

    class ImageHandler {
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
    }

    private void hideContents() {
        buttonLayout.setVisibility(View.GONE);
        examContentLayout.setVisibility(View.GONE);
        attachmentContentLayout.setVisibility(View.GONE);
        webView.setVisibility(View.GONE);
        startButton.setVisibility(View.GONE);
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
            super.onBackPressed();
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