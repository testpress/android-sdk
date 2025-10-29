package `in`.testpress.course.util

import android.content.Context
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebSettings
import android.webkit.WebView as AndroidWebView
import androidx.core.view.children
import `in`.testpress.core.TestpressSdk
import `in`.testpress.util.UserAgentProvider
import `in`.testpress.util.webview.WebView

object CacheWebView {
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
            if (::appContext.isInitialized) return true
            
            appContext = context.applicationContext
            true
        } catch (e: Exception) {
            false
        }
    }
    
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
    
    fun acquire(
        contentId: Long, 
        url: String, 
        loadUrl: Boolean = true,
        createWebView: () -> WebView,
        configure: (WebView) -> Unit
    ): WebView {
        if (!::appContext.isInitialized) {
            throw IllegalStateException(
                "CacheWebView not initialized. ContentProvider should have auto-initialized it. " +
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
        } catch (e: Exception) {
        }
    }
    
    fun detach(webView: WebView?) {
        try {
            webView?.let {
                it.onPause()
                (it.parent as? ViewGroup)?.removeView(it)
            }
        } catch (e: Exception) {
        }
    }
    
    fun clearAll() {
        try {
            synchronized(cache) {
                cache.values.forEach { it.destroy() }
                cache.clear()
            }
        } catch (e: Exception) {
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
            }
        }
    }
}

