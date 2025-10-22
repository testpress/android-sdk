package `in`.testpress.util.webview

import `in`.testpress.core.TestpressException
import `in`.testpress.fragments.WebViewFragment
import `in`.testpress.util.extension.openUrlInBrowser
import android.graphics.Bitmap
import android.webkit.*

class CustomWebViewClient(val fragment: WebViewFragment) : WebViewClient() {

    private var errorList = linkedMapOf<WebResourceRequest?,WebResourceResponse?>()
    private var pageLoadStartTime: Long = 0

    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        val url = request?.url.toString()
        return when {
            isPDFUrl(url) -> {
                fragment.openUrlInBrowser(url)
                true
            }
            shouldLoadInWebView(url) -> false
            else -> {
                fragment.openUrlInBrowser(url)
                true
            }
        }
    }

    private fun isPDFUrl(url: String?) = url?.contains(".pdf") ?: false

    private fun shouldLoadInWebView(url: String?):Boolean {
        return if (fragment.isInstituteUrl(url)){
            true
        } else {
            fragment.allowNonInstituteUrlInWebView
        }
    }

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        pageLoadStartTime = System.currentTimeMillis()
        android.util.Log.d("AI_TIMING", "🟦 STEP 14: WebView.onPageStarted() - URL loading started")
        android.util.Log.d("AI_TIMING", "   Loading URL: $url")
        if (fragment.showLoadingBetweenPages) fragment.showLoading()
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        val totalLoadTime = System.currentTimeMillis() - pageLoadStartTime
        android.util.Log.d("AI_TIMING", "✅ STEP 14 DONE: WebView.onPageFinished() - Page loaded!")
        android.util.Log.d("AI_TIMING", "   Loaded URL: $url")
        android.util.Log.d("AI_TIMING", "   ⏱️ TOTAL PAGE LOAD TIME: ${totalLoadTime}ms")
        android.util.Log.d("AI_TIMING", "")
        android.util.Log.d("AI_TIMING", "========================================")
        android.util.Log.d("AI_TIMING", "🎉 AI WEBVIEW FULLY LOADED AND VISIBLE!")
        android.util.Log.d("AI_TIMING", "⏱️ Total time from button click: ${totalLoadTime}ms")
        android.util.Log.d("AI_TIMING", "========================================")
        
        fragment.hideLoading()
        fragment.hideEmptyViewShowWebView()
        checkWebViewHasError()
    }

    override fun onReceivedError(
        view: WebView?,
        request: WebResourceRequest?,
        error: WebResourceError?
    ) {
        val requestUrl = request?.url.toString()
        val currentWebViewUrl = fragment.webView.url.toString()
        if (requestUrl == currentWebViewUrl) {
            fragment.showErrorView(TestpressException.unexpectedWebViewError(Exception("WebView error ${error?.errorCode}")))
        }
    }

    override fun onReceivedHttpError(
        view: WebView?,
        request: WebResourceRequest?,
        errorResponse: WebResourceResponse?
    ) {
        errorList[request] = errorResponse
    }

    private fun checkWebViewHasError() {
        // We are not showing error for other URLs like static and image URLs.
        // Because WebView can load multiple URLs simultaneously like browser.
        errorList.forEach { error ->
            val requestUrl = error.key?.url.toString()
            val currentWebViewUrl = fragment.webView.url.toString()
            if (requestUrl == currentWebViewUrl) {
                val statusCode = error.value?.statusCode ?: -1
                val reasonPhrase = error.value?.reasonPhrase ?: "Unknown Error"
                val httpError = TestpressException.httpError(statusCode, reasonPhrase)
                fragment.showErrorView(httpError)
                errorList.clear()
            }
        }
    }
}