package `in`.testpress.course.util

import android.content.Context
import android.util.Log
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebSettings
import android.webkit.WebView
import `in`.testpress.core.TestpressSdk
import `in`.testpress.util.UserAgentProvider
import `in`.testpress.util.webview.CustomWebChromeClient
import `in`.testpress.util.webview.CustomWebViewClient
import `in`.testpress.fragments.WebViewFragment

/**
 * Simple app-scoped cache for WebView objects (max 3).
 * Reuses WebViewFragment's setup logic for consistency.
 */
object PdfWebViewCache {
    private const val TAG = "PdfWebViewCache"
    private const val MAX_SIZE = 3
    
    private lateinit var appContext: Context
    
    // LRU cache
    private val cache = object : LinkedHashMap<Long, CachedWebView>(MAX_SIZE, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<Long, CachedWebView>?): Boolean {
            val shouldRemove = size > MAX_SIZE
            if (shouldRemove && eldest != null) {
                Log.d(TAG, "Evicting WebView for contentId: ${eldest.key}")
                eldest.value.destroy()
            }
            return shouldRemove
        }
    }
    
    /**
     * Initialize with application context.
     */
    fun init(context: Context) {
        appContext = context.applicationContext
        Log.d(TAG, "PdfWebViewCache initialized")
    }
    
    /**
     * Get or create a WebView for the given contentId and URL.
     * Uses same settings as WebViewFragment.
     */
    fun acquire(contentId: Long, url: String): WebView {
        synchronized(cache) {
            var cached = cache[contentId]
            
            // Return cached if URL matches
            if (cached != null && cached.url == url) {
                Log.d(TAG, "Reusing cached WebView for contentId: $contentId")
                return cached.webView
            }
            
            // Create new WebView with WebViewFragment's setup
            Log.d(TAG, "Creating new WebView for contentId: $contentId")
            val webView = WebView(appContext).apply {
                id = contentId.toInt()
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                
                // Copy WebViewFragment's settings
                settings.apply {
                    javaScriptEnabled = true
                    allowFileAccess = true
                    useWideViewPort = false
                    loadWithOverviewMode = true
                    domStorageEnabled = true
                    builtInZoomControls = false
                    cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
                    setSupportZoom(false)
                }
                
                // Simple clients (can't use CustomWebViewClient without fragment)
                webViewClient = android.webkit.WebViewClient()
                webChromeClient = android.webkit.WebChromeClient()
                
                // Set user agent like WebViewFragment
                settings.userAgentString += " TestpressAndroidApp/WebView"
                
                // Clear cache like WebViewFragment
                clearCache(true)
                clearHistory()
                
                // Enable cookies
                CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)
            }
            
            // Load URL with auth headers
            val session = TestpressSdk.getTestpressSession(appContext)
            val headers = mutableMapOf<String, String>()
            session?.token?.let { headers["Authorization"] = "JWT $it" }
            headers["User-Agent"] = UserAgentProvider.get(appContext)
            
            webView.loadUrl(url, headers)
            
            // Cache it
            cache[contentId] = CachedWebView(webView, url)
            return webView
        }
    }
    
    /**
     * Attach WebView to container.
     */
    fun attach(container: ViewGroup, webView: WebView) {
        (webView.parent as? ViewGroup)?.removeView(webView)
        container.removeAllViews()
        container.addView(webView)
        Log.d(TAG, "WebView attached")
    }
    
    /**
     * Detach WebView from container.
     */
    fun detach(webView: WebView?) {
        if (webView == null) return
        (webView.parent as? ViewGroup)?.removeView(webView)
        Log.d(TAG, "WebView detached")
    }
    
    /**
     * Clear all cached WebViews.
     */
    fun clearAll() {
        synchronized(cache) {
            cache.values.forEach { it.destroy() }
            cache.clear()
            Log.d(TAG, "Cleared all cached WebViews")
        }
    }
    
    private data class CachedWebView(val webView: WebView, val url: String) {
        fun destroy() {
            try {
                webView.loadUrl("about:blank")
                webView.stopLoading()
                webView.removeAllViews()
                webView.destroy()
            } catch (e: Exception) {
                Log.e(TAG, "Error destroying WebView", e)
            }
        }
    }
}

