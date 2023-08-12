package `in`.testpress.util.webview

import `in`.testpress.core.TestpressException
import `in`.testpress.fragments.WebViewFragment
import `in`.testpress.util.extension.openUrlInBrowser
import android.graphics.Bitmap
import android.webkit.*

class CustomWebViewClient(val fragment: WebViewFragment) : WebViewClient() {

    private var errorList = linkedMapOf<WebResourceRequest?,WebResourceResponse?>()

    override fun shouldOverrideUrlLoading(
        view: WebView?,
        request: WebResourceRequest?
    ): Boolean {
        return if (shouldLoadInWebView(request?.url.toString())) {
            false
        } else {
            fragment.openUrlInBrowser(request?.url.toString())
            true
        }
    }

    private fun shouldLoadInWebView(url: String?):Boolean {
        val isInstituteUrl = fragment.instituteSettings.isInstituteUrl(url)
        return if (isInstituteUrl){
            true
        } else {
            fragment.webViewFragmentSettings.allowNonInstituteUrlInWebView
        }
    }

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        if (fragment.webViewFragmentSettings.showLoadingBetweenPages) fragment.showLoading()
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        fragment.hideLoading()
        fragment.hideEmptyViewShowWebView()
        checkWebViewHasError()
    }

    override fun onReceivedError(
        view: WebView?,
        request: WebResourceRequest?,
        error: WebResourceError?
    ) {
        fragment.showErrorView(TestpressException.unexpectedError(Exception("WebView error")))
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