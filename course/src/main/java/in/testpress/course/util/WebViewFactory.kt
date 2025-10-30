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
import java.io.File

object WebViewFactory {
    private const val TAG = "WebViewFactory"
    private const val MAX_SIZE = 2
    private const val DISK_CACHE_DIR = "webview_cache"
    
    private lateinit var appContext: Context
    
    private val cache = object : LinkedHashMap<Long, CachedWebView>(MAX_SIZE, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<Long, CachedWebView>?): Boolean {
            val shouldRemove = size > MAX_SIZE
            if (shouldRemove && eldest != null) {
                Log.d(TAG, "♻️ LRU Evicting: contentId=${eldest.key} (saving to disk)")
                saveToDisk(eldest.key, eldest.value.webView)
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
                Log.d(TAG, "✓ RAM Cache HIT: contentId=$contentId (instant)")
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
            
            val diskCached = loadFromDisk(contentId, cacheKey)
            if (diskCached != null) {
                configure(diskCached)
                cache[contentId] = CachedWebView(diskCached, cacheKey)
                Log.d(TAG, "✓ Disk Cache HIT: contentId=$contentId (fast ~200ms)")
                return diskCached
            }
            
            Log.d(TAG, "✗ Cache MISS: contentId=$contentId (creating new WebView)")
            val webView = createWebView()
            configure(webView)
            
            if (loadUrl) {
                loadUrlWithAuth(webView, cacheKey)
            }
            
            cache[contentId] = CachedWebView(webView, cacheKey)
            Log.d(TAG, "✓ WebView cached in RAM: contentId=$contentId (cache size: ${cache.size})")
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
            webView?.let { wv ->
                wv.onPause()
                (wv.parent as? ViewGroup)?.removeView(wv)
                
                synchronized(cache) {
                    cache.entries.find { it.value.webView == wv }?.let { entry ->
                        saveToDisk(entry.key, wv)
                    }
                }
                
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
                Log.d(TAG, "🗑 Cleared all RAM cached WebViews ($count instances)")
            }
            
            val diskCacheDir = File(appContext.filesDir, DISK_CACHE_DIR)
            val diskFileCount = diskCacheDir.listFiles()?.size ?: 0
            diskCacheDir.deleteRecursively()
            Log.d(TAG, "🗑 Cleared disk cache ($diskFileCount files)")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear cache", e)
        }
    }
    
    private fun saveToDisk(contentId: Long, webView: WebView) {
        try {
            val diskCacheDir = File(appContext.filesDir, DISK_CACHE_DIR)
            diskCacheDir.mkdirs()
            
            val cacheFile = File(diskCacheDir, "webview_$contentId.mht")
            
            webView.saveWebArchive(cacheFile.absolutePath) { filename ->
                if (filename != null) {
                    val fileSize = cacheFile.length() / 1024
                    Log.d(TAG, "💾 Saved to disk: contentId=$contentId (${fileSize}KB)")
                } else {
                    Log.w(TAG, "⚠ Failed to save to disk: contentId=$contentId")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error saving to disk: contentId=$contentId", e)
        }
    }
    
    private fun loadFromDisk(contentId: Long, cacheKey: String): WebView? {
        return try {
            val diskCacheDir = File(appContext.filesDir, DISK_CACHE_DIR)
            val cacheFile = File(diskCacheDir, "webview_$contentId.mht")
            
            if (!cacheFile.exists()) {
                Log.d(TAG, "💾 Disk cache MISS: contentId=$contentId")
                return null
            }
            
            val ageHours = (System.currentTimeMillis() - cacheFile.lastModified()) / (1000 * 60 * 60)
            if (ageHours > 24) {
                Log.d(TAG, "💾 Disk cache EXPIRED: contentId=$contentId (age: ${ageHours}h)")
                cacheFile.delete()
                return null
            }
            
            val webView = create(appContext)
            webView.loadUrl("file://${cacheFile.absolutePath}")
            
            val fileSize = cacheFile.length() / 1024
            Log.d(TAG, "💾 Loaded from disk: contentId=$contentId (${fileSize}KB, age: ${ageHours}h)")
            
            webView
        } catch (e: Exception) {
            Log.e(TAG, "Error loading from disk: contentId=$contentId", e)
            null
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

