package `in`.testpress.util.webview

import `in`.testpress.core.TestpressException
import `in`.testpress.fragments.WebViewFragment
import `in`.testpress.util.extension.openUrlInBrowser
import android.graphics.Bitmap
import android.webkit.*

class CustomWebViewClient(val fragment: WebViewFragment) : WebViewClient() {

    private var currentLoadingUrl = ""

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
        currentLoadingUrl = view?.url.toString()
        if (fragment.webViewFragmentSettings.showLoadingBetweenPages) fragment.showLoading()
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        fragment.hideLoading()
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
        // Verify if the error is related to the current URL being loaded in the WebView
        // This is important to display errors only for the specific URL being loaded in the WebView.
        // Since a WebView might load multiple types of URLs simultaneously, such as static and image URLs.
        val requestUrl = request?.url.toString()
        if (currentLoadingUrl == requestUrl) {
            val statusCode = errorResponse?.statusCode ?: -1
            val reasonPhrase = errorResponse?.reasonPhrase ?: "Unknown Error"
            val httpError = TestpressException.httpError(statusCode, reasonPhrase)
            fragment.showErrorView(httpError)
        }
    }
}