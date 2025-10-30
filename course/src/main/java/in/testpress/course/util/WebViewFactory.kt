package `in`.testpress.course.util

import android.content.Context
import android.util.Log
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebSettings
import android.webkit.WebView as AndroidWebView
import androidx.core.view.children
import `in`.testpress.core.TestpressSdk
import `in`.testpress.util.UserAgentProvider
import `in`.testpress.util.webview.WebView

object WebViewFactory {
    private const val TAG = "WebViewFactory"
    private const val MAX_SIZE = 3
    
    private lateinit var appContext: Context
    
    private val cache = object : LinkedHashMap<Long, CachedWebView>(MAX_SIZE, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<Long, CachedWebView>?): Boolean {
            val shouldRemove = size > MAX_SIZE
            if (shouldRemove && eldest != null) {
                eldest.value.destroy()
            }
            return shouldRemove
        }
    }
    
    fun init(context: Context?): Boolean {
        return try {
            if (context == null) return false
            if (::appContext.isInitialized) {
                Log.d(TAG, "Already initialized")
                return true
            }
            
            appContext = context.applicationContext
            Log.d(TAG, "✓ Initialized successfully")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize", e)
            false
        }
    }
    
    fun isCached(contentId: Long, cacheKey: String): Boolean {
        return try {
            if (!::appContext.isInitialized) return false
            synchronized(cache) {
                val cached = cache[contentId]
                cached != null && cached.cacheKey == cacheKey
            }
        } catch (e: Exception) {
            false
        }
    }
    
    fun create(context: Context): WebView {
        return WebView(context)
    }
    
    fun createCached(
        contentId: Long, 
        cacheKey: String, 
        loadUrl: Boolean = true,
        createWebView: () -> WebView,
        configure: (WebView) -> Unit
    ): WebView {
        if (!::appContext.isInitialized) {
            throw IllegalStateException(
                "WebViewFactory not initialized. ContentProvider should have auto-initialized it. " +
                "This is a configuration error - check AndroidManifest merging."
            )
        }
        
        synchronized(cache) {
            val cached = cache[contentId]
            
            if (cached != null && cached.cacheKey == cacheKey) {
                Log.d(TAG, "✓ Cache HIT: contentId=$contentId (reusing existing WebView)")
                return cached.webView
            }
            
            if (cached != null && cached.cacheKey != cacheKey) {
                Log.d(TAG, "⚠ Cache key changed: contentId=$contentId (old=${cached.cacheKey}, new=$cacheKey)")
                if (loadUrl) {
                    loadUrlWithAuth(cached.webView, cacheKey)
                }
                cached.cacheKey = cacheKey
                return cached.webView
            }
            
            Log.d(TAG, "✗ Cache MISS: contentId=$contentId (creating new WebView)")
            val webView = createWebView()
            configure(webView)
            
            if (loadUrl) {
                loadUrlWithAuth(webView, cacheKey)
            }
            
            cache[contentId] = CachedWebView(webView, cacheKey)
            Log.d(TAG, "✓ WebView cached: contentId=$contentId (cache size: ${cache.size})")
            return webView
        }
    }
    
    private fun loadUrlWithAuth(webView: WebView, url: String) {
        val session = TestpressSdk.getTestpressSession(appContext)
        val headers = mutableMapOf<String, String>()
        
        session?.token?.let { headers["Authorization"] = "JWT $it" }
        headers["User-Agent"] = UserAgentProvider.get(appContext)
        
        webView.loadUrl(url, headers)
    }
    
    fun attach(container: ViewGroup, webView: WebView) {
        try {
            (webView.parent as? ViewGroup)?.removeView(webView)
            container.children.filterIsInstance<WebView>().forEach {
                container.removeView(it)
            }
            container.addView(webView)
            webView.onResume()
            Log.d(TAG, "✓ WebView attached to container")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to attach WebView", e)
        }
    }
    
    fun detach(webView: WebView?) {
        try {
            webView?.let {
                it.onPause()
                (it.parent as? ViewGroup)?.removeView(it)
                Log.d(TAG, "✓ WebView detached from container")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to detach WebView", e)
        }
    }
    
    fun clearAll() {
        try {
            synchronized(cache) {
                val count = cache.size
                cache.values.forEach { it.destroy() }
                cache.clear()
                Log.d(TAG, "🗑 Cleared all cached WebViews ($count instances)")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear cache", e)
        }
    }
    
    private data class CachedWebView(val webView: WebView, var cacheKey: String) {
        fun destroy() {
            try {
                webView.loadUrl("about:blank")
                webView.stopLoading()
                webView.removeAllViews()
                webView.destroy()
            } catch (e: Exception) {
            }
        }
    }
}

