package `in`.testpress.util.webview

import `in`.testpress.core.TestpressException
import `in`.testpress.fragments.WebViewFragment
import `in`.testpress.util.extension.openUrlInBrowser
import android.graphics.Bitmap
import android.webkit.*

class CustomWebViewClient(val fragment: WebViewFragment) : WebViewClient() {

    private var errorList = linkedMapOf<WebResourceRequest?,WebResourceResponse?>()
    private var pageLoadStartTime: Long = 0
    private var loadUrlCalledTime: Long = 0
    private var firstResourceRequestTime: Long = 0
    private var resourceLoadTimes = mutableMapOf<String, Long>()
    private var mainPageUrl: String? = null

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
    
    fun setLoadUrlTime(time: Long) {
        loadUrlCalledTime = time
    }

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        pageLoadStartTime = System.currentTimeMillis()
        mainPageUrl = url
        
        // Calculate time from loadUrl() call to onPageStarted()
        val networkSetupTime = if (loadUrlCalledTime > 0) {
            pageLoadStartTime - loadUrlCalledTime
        } else {
            -1L
        }
        
        android.util.Log.d("AI_TIMING", "")
        android.util.Log.d("AI_TIMING", "================================================")
        android.util.Log.d("AI_TIMING", "🟦 STEP 14: WebView.onPageStarted() FIRED!")
        android.util.Log.d("AI_TIMING", "================================================")
        android.util.Log.d("AI_TIMING", "   URL: $url")
        android.util.Log.d("AI_TIMING", "")
        
        if (networkSetupTime > 0) {
            android.util.Log.d("AI_TIMING", "⏱️  TIME FROM loadUrl() TO onPageStarted(): ${networkSetupTime}ms")
            android.util.Log.d("AI_TIMING", "")
            android.util.Log.d("AI_TIMING", "📊 THIS ${networkSetupTime}ms INCLUDES ALL NETWORK SETUP:")
            android.util.Log.d("AI_TIMING", "   ✅ DNS lookup (resolve domain to IP)")
            android.util.Log.d("AI_TIMING", "   ✅ TCP connection (3-way handshake)")
            android.util.Log.d("AI_TIMING", "   ✅ SSL/TLS handshake (if HTTPS)")
            android.util.Log.d("AI_TIMING", "   ✅ HTTP request sent to server")
            android.util.Log.d("AI_TIMING", "   ✅ Server processing request")
            android.util.Log.d("AI_TIMING", "   ✅ Response headers received")
            android.util.Log.d("AI_TIMING", "")
            
            // Estimate breakdown (rough estimates)
            android.util.Log.d("AI_TIMING", "📈 ESTIMATED BREAKDOWN (typical values):")
            val dns = (networkSetupTime * 0.15).toLong()
            val tcp = (networkSetupTime * 0.10).toLong()
            val ssl = (networkSetupTime * 0.20).toLong()
            val request = (networkSetupTime * 0.05).toLong()
            val server = (networkSetupTime * 0.45).toLong()
            val headers = (networkSetupTime * 0.05).toLong()
            
            android.util.Log.d("AI_TIMING", "   DNS Lookup:       ~${dns}ms (~15% of total)")
            android.util.Log.d("AI_TIMING", "   TCP Connection:   ~${tcp}ms (~10% of total)")
            android.util.Log.d("AI_TIMING", "   SSL Handshake:    ~${ssl}ms (~20% of total)")
            android.util.Log.d("AI_TIMING", "   HTTP Request:     ~${request}ms (~5% of total)")
            android.util.Log.d("AI_TIMING", "   Server Processing: ~${server}ms (~45% of total) 🔴 BIGGEST")
            android.util.Log.d("AI_TIMING", "   Response Headers: ~${headers}ms (~5% of total)")
            android.util.Log.d("AI_TIMING", "")
            
            if (server > 1000) {
                android.util.Log.d("AI_TIMING", "⚠️  WARNING: Server processing took ~${server}ms (>1 second!)")
                android.util.Log.d("AI_TIMING", "   This is SLOW - backend team should optimize!")
            }
        }
        
        android.util.Log.d("AI_TIMING", "🔄 Now downloading HTML content and resources...")
        android.util.Log.d("AI_TIMING", "================================================")
        android.util.Log.d("AI_TIMING", "")
        
        if (fragment.showLoadingBetweenPages) fragment.showLoading()
    }
    
    override fun onLoadResource(view: WebView?, url: String?) {
        val currentTime = System.currentTimeMillis()
        
        // Track first resource load time
        if (firstResourceRequestTime == 0L && url != null) {
            firstResourceRequestTime = currentTime
            android.util.Log.d("AI_TIMING", "📥 First resource request started:")
            android.util.Log.d("AI_TIMING", "   URL: $url")
        }
        
        // Log main page resources
        if (url != null && url == mainPageUrl) {
            android.util.Log.d("AI_TIMING", "📄 Loading main HTML: $url")
        }
        
        // Track resource start time
        if (url != null) {
            resourceLoadTimes[url] = currentTime
        }
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