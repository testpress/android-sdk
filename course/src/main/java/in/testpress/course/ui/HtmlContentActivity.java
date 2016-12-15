package in.testpress.course.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.widget.SwipeRefreshLayout;
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
import android.widget.LinearLayout;
import android.widget.TextView;

import junit.framework.Assert;

import in.testpress.core.TestpressCallback;
import in.testpress.core.TestpressException;
import in.testpress.core.TestpressSdk;
import in.testpress.course.R;
import in.testpress.course.models.Content;
import in.testpress.course.models.HtmlContent;
import in.testpress.course.network.TestpressCourseApiClient;
import in.testpress.ui.BaseToolBarActivity;
import in.testpress.ui.ZoomableImageActivity;

public class HtmlContentActivity extends BaseToolBarActivity {

    public static final String CONTENT = "content";

    private WebView webView;
    private SwipeRefreshLayout swipeRefresh;
    private LinearLayout emptyContainer;
    private TextView emptyTitleView;
    private TextView emptyDescView;
    private TextView titleView;

    private boolean hasError = false;
    private Content content;

    public static Intent createIntent(Content content, Context context) {
        Intent intent = new Intent(context, HtmlContentActivity.class);
        intent.putExtra(CONTENT, content);
        return intent;
    }

    @SuppressWarnings({"ConstantConditions", "deprecation"})
    @SuppressLint({"SetJavaScriptEnabled", "AddJavascriptInterface"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.testpress_activity_html_content);
        webView = (WebView) findViewById(R.id.web_view);
        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        emptyContainer = (LinearLayout) findViewById(R.id.empty_container);
        emptyTitleView = (TextView) findViewById(R.id.empty_title);
        emptyDescView = (TextView) findViewById(R.id.empty_description);
        titleView = (TextView) findViewById(R.id.title);
        Button retryButton = (Button) findViewById(R.id.retry_button);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        content = getIntent().getParcelableExtra(CONTENT);
        Assert.assertNotNull("CONTENT must not be null.", content);
        if (content.getHtmlContentTitle() == null) {
            getSupportActionBar().setTitle(content.getName());
            setEmptyText(R.string.testpress_content_not_available,
                    R.string.testpress_content_not_available_description,
                    R.drawable.ic_content_paste_black_24dp);
            retryButton.setVisibility(View.GONE);
            return;
        }
        //noinspection ConstantConditions
        getSupportActionBar().setTitle(Html.fromHtml(content.getHtmlContentTitle()));
        titleView.setText(Html.fromHtml(content.getHtmlContentTitle()));
        titleView.setTypeface(TestpressSdk.getRubikMediumFont(this));
        retryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadUrl();
            }
        });
        webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        webView.setWebChromeClient(new WebChromeClient());
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setBuiltInZoomControls(false);
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        swipeRefresh.setColorSchemeResources(R.color.testpress_color_primary);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadUrl();
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
                emptyContainer.setVisibility(View.GONE);
                swipeRefresh.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                swipeRefresh.setRefreshing(false);
                super.onPageFinished(view, url);
                if(!hasError) {
                    String javascript = "javascript:var images = document.getElementsByTagName(\"img\");" +
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
                    swipeRefresh.setVisibility(View.VISIBLE);
                    emptyContainer.setVisibility(View.GONE);
                }
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request,
                                        WebResourceError error) {
                super.onReceivedError(view, request, error);
                setEmptyText(R.string.testpress_network_error, R.string.testpress_no_internet_try_again,
                        R.drawable.ic_error_outline_black_18dp);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
                return true;
            }

        });
        loadUrl();
    }

    protected void loadUrl() {
        hasError = false;
        swipeRefresh.post(new Runnable() {
            @Override
            public void run() {
                swipeRefresh.setRefreshing(true);
            }
        });
        new TestpressCourseApiClient(this).getHtmlContent(content.getHtmlContentUrl())
                .enqueue(new TestpressCallback<HtmlContent>() {
                    @Override
                    public void onSuccess(HtmlContent htmlContent) {
                        webView.loadDataWithBaseURL("file:///android_asset/", getHeader() +
                                htmlContent.getTextHtml(), "text/html", "UTF-8", null);
                    }

                    @Override
                    public void onException(TestpressException exception) {
                        setEmptyText(R.string.testpress_network_error,
                                R.string.testpress_no_internet_try_again,
                                R.drawable.ic_error_outline_black_18dp);
                    }
                });
    }

    String getHeader() {
        return "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1, user-scalable=no\" />" +
                "<link rel=\"stylesheet\" type=\"text/css\" href=\"typebase.css\" />" +
                "<style>img{display: inline;height: auto;max-width: 100%;}</style>";
    }

    class ImageHandler {
        @JavascriptInterface
        public void onClickImage(String url) {
            startActivity(ZoomableImageActivity.createIntent(url, HtmlContentActivity.this));
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
        return false;
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

}