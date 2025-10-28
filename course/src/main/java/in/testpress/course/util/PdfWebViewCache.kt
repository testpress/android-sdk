package `in`.testpress.course.util

import android.content.Context
import android.util.Log
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.core.view.children
import `in`.testpress.core.TestpressSdk
import `in`.testpress.util.UserAgentProvider

/**
 * App-scoped LRU cache for WebView instances used in AI PDF chat.
 * 
 * Maintains up to 3 WebView instances to enable instant switching between PDFs
 * without reloading. WebViews survive Activity destruction for improved UX.
 * 
 * Usage:
 * - Call init() from Application.onCreate()
 * - Call acquire() to get/create cached WebView
 * - Call attach()/detach() for fragment lifecycle management
 * - Call clearAll() on logout
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
     * Initialize cache with application context.
     * Safe to call multiple times (idempotent).
     * 
     * @return true if initialized successfully, false otherwise
     */
    fun init(context: Context?): Boolean {
        return try {
            if (context == null) return false
            if (::appContext.isInitialized) return true
            
            appContext = context.applicationContext
            true
        } catch (e: Exception) {
            // Init failed - cache won't work but app continues
            false
        }
    }
    
    /**
     * Check if WebView is cached for this contentId and URL.
     * Safe to call even if cache not initialized.
     * 
     * @return true if cached WebView exists and URL matches, false otherwise
     */
    fun isCached(contentId: Long, url: String): Boolean {
        return try {
            if (!::appContext.isInitialized) return false
            synchronized(cache) {
                val cached = cache[contentId]
                cached != null && cached.url == url
            }
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Acquire a WebView for the given contentId and URL.
     * 
     * Returns cached instance if available (instant, no reload).
     * Creates new instance if not cached (one-time setup cost).
     * 
     * @param contentId Unique identifier for the PDF content
     * @param url PDF URL to load (or cache key if loadUrl = false)
     * @param loadUrl Whether to call loadUrl() - set false if configure lambda loads HTML data
     * @param configure Configuration lambda called ONLY when creating new WebView
     * @return WebView instance ready for attachment
     * @throws IllegalStateException if cache not initialized (should never happen in production)
     */
    fun acquire(contentId: Long, url: String, loadUrl: Boolean = true, configure: (WebView) -> Unit): WebView {
        if (!::appContext.isInitialized) {
            throw IllegalStateException(
                "PdfWebViewCache not initialized. ContentProvider should have auto-initialized it. " +
                "This is a configuration error - check AndroidManifest merging."
            )
        }
        
        synchronized(cache) {
            val cached = cache[contentId]
            
            if (cached != null && cached.url == url) {
                return cached.webView
            }
            
            if (cached != null && cached.url != url) {
                if (loadUrl) {
                    loadUrlWithAuth(cached.webView, url)
                }
                cached.url = url
                return cached.webView
            }
            
            val webView = createWebView()
            configure(webView)
            
            if (loadUrl) {
                loadUrlWithAuth(webView, url)
            }
            
            cache[contentId] = CachedWebView(webView, url)
            return webView
        }
    }
    
    /**
     * Create a new WebView with settings matching WebViewFragment.
     */
    private fun createWebView(): WebView {
        return WebView(appContext).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            
            settings.apply {
                javaScriptEnabled = true
                allowFileAccess = true
                useWideViewPort = false
                loadWithOverviewMode = true
                domStorageEnabled = true
                builtInZoomControls = false
                cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
                setSupportZoom(false)
                userAgentString += " TestpressAndroidApp/WebView"
            }
            
            clearCache(true)
            clearHistory()
            
            CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)
        }
    }
    
    /**
     * Load URL with authentication headers matching WebViewFragment behavior.
     */
    private fun loadUrlWithAuth(webView: WebView, url: String) {
        val session = TestpressSdk.getTestpressSession(appContext)
        val headers = mutableMapOf<String, String>()
        
        session?.token?.let { headers["Authorization"] = "JWT $it" }
        headers["User-Agent"] = UserAgentProvider.get(appContext)
        
        webView.loadUrl(url, headers)
    }
    
    /**
     * Attach WebView to a container.
     * Safely removes from previous parent if needed.
     * Also resumes WebView for CPU/battery efficiency.
     * GUARANTEED not to crash - logs error and continues.
     */
    fun attach(container: ViewGroup, webView: WebView) {
        try {
            (webView.parent as? ViewGroup)?.removeView(webView)
            container.children.filterIsInstance<WebView>().forEach {
                container.removeView(it)
            }
            container.addView(webView)
            webView.onResume()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to attach WebView (non-fatal)", e)
        }
    }
    
    /**
     * Detach WebView from its container.
     * WebView remains in cache for later reuse.
     * Also pauses WebView for CPU/battery efficiency.
     * GUARANTEED not to crash - logs error and continues.
     */
    fun detach(webView: WebView?) {
        try {
            webView?.let {
                it.onPause()
                (it.parent as? ViewGroup)?.removeView(it)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to detach WebView (non-fatal)", e)
        }
    }
    
    /**
     * Clear all cached WebViews.
     * Call this on user logout or when clearing app data.
     * GUARANTEED not to crash - logs error and continues.
     */
    fun clearAll() {
        try {
            synchronized(cache) {
                cache.values.forEach { it.destroy() }
                cache.clear()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear cache (non-fatal)", e)
        }
    }
    
    private data class CachedWebView(val webView: WebView, var url: String) {
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

