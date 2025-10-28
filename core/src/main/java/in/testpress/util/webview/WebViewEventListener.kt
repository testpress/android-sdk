package `in`.testpress.util.webview

import `in`.testpress.core.TestpressException

interface WebViewEventListener {
    fun onLoadingStarted()
    fun onLoadingFinished()
    fun onError(exception: TestpressException)
    fun isViewActive(): Boolean
}

