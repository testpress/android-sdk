package `in`.testpress.util.webview

import android.graphics.Bitmap
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView as AndroidWebView
import android.webkit.WebViewClient
import `in`.testpress.core.TestpressException

class BaseWebViewClient(
    private val listener: WebViewEventListener
) : WebViewClient() {
    
    private val errorList = linkedMapOf<String, WebResourceResponse?>()
    
    override fun onPageStarted(view: AndroidWebView?, url: String?, favicon: Bitmap?) {
        if (listener.isViewActive()) {
            errorList.clear()
            listener.onLoadingStarted()
        }
    }
    
    override fun onPageFinished(view: AndroidWebView?, url: String?) {
        if (!listener.isViewActive()) return
        
        listener.onLoadingFinished()
        
        if (errorList.isNotEmpty()) {
            checkForPageError(view)
        }
    }
    
    override fun onReceivedError(
        view: AndroidWebView?,
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
        view: AndroidWebView?,
        request: WebResourceRequest?,
        errorResponse: WebResourceResponse?
    ) {
        request?.url?.toString()?.let { url ->
            errorList[url] = errorResponse
        }
    }
    
    private fun checkForPageError(view: AndroidWebView?) {
        if (!listener.isViewActive()) return
        
        val currentUrl = view?.url ?: return
        errorList[currentUrl]?.let { response ->
            listener.onError(
                TestpressException.httpError(
                    response.statusCode,
                    response.reasonPhrase ?: "Unknown Error"
                )
            )
            errorList.remove(currentUrl)
        }
    }
}
