package `in`.testpress.util.webview

import android.graphics.Bitmap
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import `in`.testpress.core.TestpressException

class BaseWebViewClient(
    private val listener: WebViewEventListener
) : WebViewClient() {
    
    private val errorList = linkedMapOf<String, WebResourceResponse?>()
    
    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        if (listener.isViewActive()) {
            errorList.clear()
            listener.onLoadingStarted()
        }
    }
    
    override fun onPageFinished(view: WebView?, url: String?) {
        if (listener.isViewActive()) {
            listener.onLoadingFinished()
            if (errorList.isNotEmpty()) {
                checkWebViewHasError(view)
            }
        }
    }
    
    override fun onReceivedError(
        view: WebView?, 
        request: WebResourceRequest?, 
        error: WebResourceError?
    ) {
        if (!listener.isViewActive()) return
        
        if (request?.isForMainFrame == true) {
            listener.onError(
                TestpressException.unexpectedWebViewError(
                    Exception("WebView error ${error?.errorCode}")
                )
            )
        }
    }
    
    override fun onReceivedHttpError(
        view: WebView?,
        request: WebResourceRequest?,
        errorResponse: WebResourceResponse?
    ) {
        request?.url?.toString()?.let { url ->
            errorList[url] = errorResponse
        }
    }
    
    private fun checkWebViewHasError(view: WebView?) {
        if (!listener.isViewActive()) return
        
        val currentUrl = view?.url ?: return
        errorList[currentUrl]?.let { response ->
            val statusCode = response.statusCode
            val reasonPhrase = response.reasonPhrase ?: "Unknown Error"
            listener.onError(TestpressException.httpError(statusCode, reasonPhrase))
            errorList.remove(currentUrl)
        }
    }
}

