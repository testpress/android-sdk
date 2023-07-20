package `in`.testpress.util.webview

import `in`.testpress.core.TestpressException
import `in`.testpress.fragments.WebViewFragment
import `in`.testpress.util.extension.openUrlInBrowser
import android.graphics.Bitmap
import android.util.Log
import android.webkit.*

class CustomWebViewClient(val fragment: WebViewFragment) : WebViewClient() {

    override fun shouldOverrideUrlLoading(
        view: WebView?,
        request: WebResourceRequest?
    ): Boolean {
        Log.d("TAG", "shouldOverrideUrlLoading: ${request?.url}")
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