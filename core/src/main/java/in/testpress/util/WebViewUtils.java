package in.testpress.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.List;

import in.testpress.ui.ZoomableImageActivity;

public class WebViewUtils {

    private WebView webView;
    private boolean hasError;

    public WebViewUtils(WebView webView) {
        this.webView = webView;
    }

    @SuppressLint({"SetJavaScriptEnabled", "AddJavascriptInterface"})
    public static void initWebView(WebView webView) {
        webView.setWebChromeClient(new WebChromeClient());
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setPluginState(WebSettings.PluginState.ON);
        webSettings.getJavaScriptCanOpenWindowsAutomatically();
        webSettings.setBuiltInZoomControls(false);
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setDatabaseEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setAppCacheEnabled(true);
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        webSettings.setRenderPriority(WebSettings.RenderPriority.HIGH);
    }

    @SuppressLint({"AddJavascriptInterface", "SetJavaScriptEnabled"})
    public void initWebView(String htmlContent, final Activity activity) {
        initWebView(webView);
        webView.addJavascriptInterface(new ImageHandler(activity), "ImageHandler");
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                WebViewUtils.this.onPageStarted();
                hasError = false;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (!hasError) {
                    String javascript = getJavascript(activity);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        webView.evaluateJavascript(javascript, null);
                    } else {
                        webView.loadUrl(javascript, null);
                    }
                    onLoadFinished();
                }
            }

            @SuppressWarnings("deprecation")
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return WebViewUtils.this.shouldOverrideUrlLoading(activity, url);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request,
                                        WebResourceError error) {

                super.onReceivedError(view, request, error);
                if (InternetConnectivityChecker.isConnected(activity)) {
                    return;
                }
                onNetworkError();
                hasError = true;
            }
        });
        loadHtml(htmlContent);
    }

    public void loadHtml(String htmlContent) {
        webView.loadDataWithBaseURL("file:///android_asset/", getHeader() + htmlContent,
                "text/html", "utf-8", null);
    }

    protected void onLoadFinished() {
        webView.setVisibility(View.VISIBLE);
    }

    protected void onPageStarted() {
    }

    protected boolean shouldOverrideUrlLoading(Activity activity, String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        activity.startActivity(intent);
        return true;
    }

    protected void onNetworkError() {
    }

    private void evaluateJavascript(String javascript) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            webView.evaluateJavascript(javascript, null);
        } else {
            webView.loadUrl(javascript, null);
        }
    }

    public void updateBookmarkButtonState(boolean bookmarked) {
        evaluateJavascript(getBookmarkButtonUpdater(bookmarked));
    }

    public void hideBookmarkButton() {
        evaluateJavascript("hideBookmarkButton();");
    }

    public void displayBookmarkButton() {
        evaluateJavascript("displayBookmarkButton();");
    }

    protected static String getTestEngineHeader() {
        return "<script src='TestpressTestEngine.js'></script>";
    }

    protected static String getBookmarkHandlerScript() {
        return "<script src='TestpressReview.js'></script>";
    }

    protected static String getRadioButtonInitializer(int selectedOption) {
        return "initRadioGroup(" + selectedOption + ");";
    }

    protected static String getCheckBoxInitializer(List<Integer> selectedOptions) {
        return "initCheckBoxGroup(" + selectedOptions + ");";
    }

    protected static String getBookmarkButtonUpdater(boolean bookmarked) {
        return "updateBookmarkButtonState(" + bookmarked + ");";
    }

    public String getJavascript(Context context) {
        return "javascript:" +
                CommonUtils.getStringFromAsset(context, "TestpressImageTagHandler.js");
    }

    public String getBaseHeader() {
        return "<!DOCTYPE html><meta name='viewport' content='width=device-width, initial-scale=1, user-scalable=no' />" +
                "<link rel='stylesheet' type='text/css' href='testpress_typebase.css' />" +
                "<style>img{display: inline; height: auto !important; width: auto !important; max-width: 100%;}</style>" +
                "<script type='text/x-mathjax-config'>" +
                "    MathJax.Hub.Config({" +
                "      messageStyle: 'none'," +
                "      tex2jax: {" +
                "        inlineMath: [['\\\\[','\\\\]'], ['\\\\(','\\\\)']]," +
                "        displayMath: [ ['$$','$$'] ]," +
                "        processEscapes: true" +
                "      }" +
                "    });" +
                "</script>" +
                "<script src='MathJax-2.7.1/MathJax.js?noContrib'></script>" +
                "<script type='text/x-mathjax-config'>" +
                "    MathJax.Ajax.config.path['MathJax'] = 'MathJax-2.7.1';" +
                "    MathJax.Ajax.config.path['Contrib'] = 'MathJax-2.7.1/contrib';" +
                "</script>" +
                "<script src='MathJax-2.7.1/config/TeX-MML-AM_CHTML-full.js'></script>" +
                "<script src='MathJax-2.7.1/extensions/TeX/mhchem3/mhchem.js'></script>";
    }

    public String getHeader() {
        return getBaseHeader();
    }

    public String getQuestionsHeader() {
        return getBaseHeader() +
                "<link rel='stylesheet' type='text/css' href='testpress_questions_typebase.css' />" +
                "<link rel='stylesheet' type='text/css' href='icomoon/style.css' />";
    }

    public static String getHeadingTags(String headingText) {
        return "\n" +
                "   <div class='review-heading'>" +
                        headingText +
                "   </div>";
    }

    public static String getCorrectAnswerIndexWithTags(int index) {
        return "\n" +
                "<div class='alphabetical-option-ring-general'" +
                "   style='-webkit-box-ordinal-group: 1; box-ordinal-group: 1;'>" +
                        ((char) (65 + index)) +
                "</div>";
    }

    public static String getOptionWithTags(String optionText, int index, int colorRes, Context context) {
        String html = "\n<div class='review-option-item wrapper'>";
        if (colorRes == android.R.color.white) {
            html += "<div class='alphabetical-option-ring-general'>";
        } else {
            html += "<div class='alphabetical-option-ring-attempted' style='background-color:" +
                    getColor(context, colorRes) + ";'>";
        }
        return html + ((char) (65 + index)) + "</div>" +
                "    <span>" + optionText + "</span>" +
                "</div>";
    }

    public static String getRadioButtonOptionWithTags(String optionText, int id) {
        return "" +
                "<tr style='border-bottom:1px solid #e6e6e6;'>" +
                "   <td id='" + id + "' onclick='onRadioOptionClick(this)'" +
                "       class='option-item table-without-border wrapper'>" +
                "           <div name='" + id + "' class='icon-radio-unchecked'></div>" +
                "           <span style='margin-left:10px; margin-top:-2px;'>" + optionText + "</span>" +
                "   </td>" +
                "</tr>";
    }

    public static String getCheckBoxOptionWithTags(String optionText, int id) {
        return "" +
                "<tr style='border-bottom:1px solid #e6e6e6;'>" +
                "   <td id='" + id + "' onclick='onCheckBoxOptionClick(this)'" +
                "       class='option-item table-without-border wrapper'>" +
                "           <div name='" + id + "' class='icon-checkbox-unchecked'></div>" +
                "           <span style='margin-left:10px; margin-top:-2px;'>" + optionText + "</span>" +
                "   </td>" +
                "</tr>";
    }

    public static String getShortAnswerHeadersWithTags() {
        return "" +
                "<tr>" +
                "   <th class='short_answer_option_item table-without-border'>Answers</th>" +
                "   <th class='short_answer_option_item table-without-border'>Marks</th>" +
                "</tr>";
    }

    public static String getShortAnswersWithTags(String shortAnswerText, String marksAllocated) {
        return "" +
                "<tr>" +
                "   <td class='short_answer_option_item table-without-border'>" + shortAnswerText + "</td>" +
                "   <td class='short_answer_option_item table-without-border'>" + marksAllocated + "%</td>" +
                "</tr>";
    }

    public static String appendImageTags(String imageUrl) {
        return "<img src=\"" + imageUrl + "\">";
    }

    public static String getColor(Context context, int colorRes) {
        return "#" + Integer.toHexString(ContextCompat.getColor(context, colorRes) & 0x00ffffff);
    }

    public static String getBookmarkButtonWithTags(boolean bookmarked) {
        String image = bookmarked ? "testpress_remove_bookmark.svg" : "testpress_bookmark.svg";
        String text = bookmarked ? "Remove Bookmark" : "Bookmark this";
        return "" +
                "<div class='bookmark-button' onclick='onClickBookmarkButton()'>" +
                "   <img class='bookmark-image' src='" + image + "' />" +
                "   <span class='bookmark-text'>" + text + "</span>" +
                "</div>";
    }

    private class ImageHandler {
        Activity activity;

        ImageHandler(Activity activity) {
            this.activity = activity;
        }

        @JavascriptInterface
        public void onClickImage(String url) {
            WebViewUtils.this.onClickImage(url, activity);
        }
    }

    protected void onClickImage(String url, Activity activity) {
        activity.startActivity(ZoomableImageActivity.createIntent(url, activity));
    }

}
