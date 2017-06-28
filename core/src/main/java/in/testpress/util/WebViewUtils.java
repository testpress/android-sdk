package in.testpress.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.List;

import in.testpress.ui.ZoomableImageActivity;

public class WebViewUtils {

    private WebView webView;

    public WebViewUtils(WebView webView) {
        this.webView = webView;
    }

    @SuppressLint({"SetJavaScriptEnabled", "AddJavascriptInterface"})
    public static void initWebView(WebView webView) {
        webView.setWebChromeClient(new WebChromeClient());
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setBuiltInZoomControls(false);
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setDatabaseEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setAppCacheEnabled(true);
    }

    @SuppressLint({"AddJavascriptInterface", "SetJavaScriptEnabled"})
    public void initWebView(String htmlContent, final Activity activity) {
        initWebView(webView);
        webView.addJavascriptInterface(new ImageHandler(activity), "ImageHandler");
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                String javascript = getJavascript(activity);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    webView.evaluateJavascript(javascript, null);
                } else {
                    webView.loadUrl(javascript, null);
                }
                onLoadFinished();
            }

            @SuppressWarnings("deprecation")
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                activity.startActivity(intent);
                return true;
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

    protected static String getTestEngineHeader() {
        return "<script src='TestpressTestEngine.js'></script>";
    }

    protected static String getRadioButtonInitializer(int selectedOption) {
        return "initRadioGroup(" + selectedOption + ");";
    }

    protected static String getCheckBoxInitializer(List<Integer> selectedOptions) {
        return "initCheckBoxGroup(" + selectedOptions + ");";
    }

    public String getJavascript(Context context) {
        return "javascript:" +
                CommonUtils.getStringFromAsset(context, "TestpressImageTagHandler.js") +
                CommonUtils.getStringFromAsset(context, "TestpressMathJaxRender.js");
    }

    public String getHeader() {
        return "<!DOCTYPE html><meta name='viewport' content='width=device-width, initial-scale=1, user-scalable=no' />" +
                "<link rel='stylesheet' type='text/css' href='testpress_questions_typebase.css' />" +
                "<style>img{display: inline; height: auto !important; width: auto !important; max-width: 100%;}</style>" +

                "<link rel='stylesheet' href='katex/katex.min.css' />" +
                "<script src='katex/katex.min.js'></script>" +
                "<script src='katex/contrib/auto-render.min.js'></script>";
    }

    public static String getHeadingTags(String headingText) {
        return "\n" +
                "   <div class='review-heading'>" +
                        headingText +
                "   </div>";
    }

    public static String getCorrectAnswerIndexWithTags(int index) {
        return "\n" +
                "<div class='alphabetical-option-ring-general'>" +
                        ((char) (65 + index)) +
                "</div>";
    }

    public static String getOptionWithTags(String optionText, int index, int colorRes, Context context) {
        String html = "\n<div class='review-option-item'>";
        if (colorRes == android.R.color.white) {
            html += "<div class='alphabetical-option-ring-general'>";
        } else {
            html += "<div class='alphabetical-option-ring-attempted' style='background-color:" +
                    getColor(context, colorRes) + ";'>";
        }
        return html + ((char) (65 + index)) + "</div>" +
                "    <div>" + optionText + "</div>" +
                "</div>";
    }

    public static String getRadioButtonOptionWithTags(String optionText, int id) {
        return "" +
                "<tr>" +
                "   <td id='" + id + "' onclick='onRadioOptionClick(this)' class='option-item table-without-border'>" +
                "       <div name='" + id + "' class='radio-button-unchecked'></div>" +
                "       <div>" + optionText + "</div>" +
                "   </td>" +
                "</tr>";
    }

    public static String getCheckBoxOptionWithTags(String optionText, int id) {
        return "" +
                "<tr>" +
                "   <td id='" + id + "' onclick='onCheckBoxOptionClick(this)' class='option-item table-without-border'>" +
                "       <div name='" + id + "' class='checkbox-unchecked'></div>" +
                "       <div>" + optionText + "</div>" +
                "   </td>" +
                "</tr>";
    }

    public static String getColor(Context context, int colorRes) {
        return "#" + Integer.toHexString(ContextCompat.getColor(context, colorRes) & 0x00ffffff);
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
