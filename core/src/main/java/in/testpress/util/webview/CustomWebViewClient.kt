package `in`.testpress.util.webview

import `in`.testpress.core.TestpressException
import `in`.testpress.fragments.WebViewFragment
import `in`.testpress.util.extension.openUrlInBrowser
import android.graphics.Bitmap
import android.webkit.*

class CustomWebViewClient(val fragment: WebViewFragment) : WebViewClient() {

    override fun shouldOverrideUrlLoading(
        view: WebView?,
        request: WebResourceRequest?
    ): Boolean {
        return if (fragment.instituteSettings.isInstituteUrl(request?.url.toString())) {
            false
        } else {
            handleNonInstituteUrl(request?.url.toString())
        }
    }

    private fun handleNonInstituteUrl(url: String?): Boolean {
        return if (fragment.webViewFragmentSettings.allowNonInstituteUrlInWebView) {
            false
        } else {
            fragment.openUrlInBrowser(url ?: "")
            true
        }
    }

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
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
        fragment.showErrorView(
            TestpressException.httpError(
                errorResponse?.statusCode!!,
                errorResponse.reasonPhrase
            )
        )
    }
}