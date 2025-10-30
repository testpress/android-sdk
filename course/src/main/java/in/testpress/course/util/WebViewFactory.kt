package `in`.testpress.course.util

import android.app.ActivityManager
import android.content.ComponentCallbacks2
import android.content.Context
import android.content.res.Configuration
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebSettings
import android.webkit.WebView as AndroidWebView
import androidx.core.view.children
import `in`.testpress.core.TestpressSdk
import `in`.testpress.util.UserAgentProvider
import `in`.testpress.util.webview.WebView

object WebViewFactory {
    
    private const val WEBVIEW_AVG_SIZE_MB = 300
    private const val MIN_FREE_MEMORY_MB = 200
    private const val MEMORY_CHECK_INTERVAL_MS = 30_000L
    private const val AVAILABLE_RAM_PERCENTAGE = 0.30
    
    private lateinit var appContext: Context
    private var maxSize: Int = 2
    private val memoryCheckHandler = android.os.Handler(android.os.Looper.getMainLooper())
    
    @Volatile
    private var activeContentId: Long? = null
    
    private val cache = object : LinkedHashMap<Long, CachedWebView>(16, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<Long, CachedWebView>?): Boolean {
            val shouldRemove = size > maxSize
            if (shouldRemove && eldest != null) {
                eldest.value.destroy()
            }
            return shouldRemove
        }
    }
    
    private val memoryListener = object : ComponentCallbacks2 {
        override fun onConfigurationChanged(newConfig: Configuration) {}
        override fun onLowMemory() = clearAll()
        override fun onTrimMemory(level: Int) {
            when (level) {
                ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL,
                ComponentCallbacks2.TRIM_MEMORY_COMPLETE -> clearAll()
                ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW,
                ComponentCallbacks2.TRIM_MEMORY_MODERATE -> clearOldest()
            }
        }
    }
    
    private val memoryCheckRunnable = object : Runnable {
        override fun run() {
            checkWebViewMemoryGrowth()
            memoryCheckHandler.postDelayed(this, MEMORY_CHECK_INTERVAL_MS)
        }
    }
    
    fun init(context: Context?): Boolean {
        if (context == null || ::appContext.isInitialized) return true
        
        appContext = context.applicationContext
        maxSize = calculateOptimalCacheSize()
        appContext.registerComponentCallbacks(memoryListener)
        memoryCheckHandler.postDelayed(memoryCheckRunnable, MEMORY_CHECK_INTERVAL_MS)
        return true
    }
    
    private fun calculateOptimalCacheSize(): Int {
        val memInfo = getMemoryInfo()
        val availableRamMB = memInfo.availMem / (1024 * 1024)
        val maxCacheMemoryMB = (availableRamMB * AVAILABLE_RAM_PERCENTAGE).toLong()
        val calculatedSize = (maxCacheMemoryMB / WEBVIEW_AVG_SIZE_MB).toInt()
        return calculatedSize.coerceIn(2, 3)
    }
    
    private fun getMemoryInfo(): ActivityManager.MemoryInfo {
        val activityManager = appContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        return ActivityManager.MemoryInfo().also { activityManager.getMemoryInfo(it) }
    }
    
    fun isCached(contentId: Long, cacheKey: String): Boolean {
        if (!::appContext.isInitialized) return false
        synchronized(cache) {
            val cached = cache[contentId]
            return cached != null && cached.cacheKey == cacheKey
        }
    }
    
    fun create(context: Context): WebView = WebView(context)
    
    fun createCached(
        contentId: Long,
        cacheKey: String,
        loadUrl: Boolean = true,
        createWebView: () -> WebView,
        configure: (WebView) -> Unit
    ): WebView {
        checkInitialized()
        
        synchronized(cache) {
            val existing = cache[contentId]
            
            if (existing != null && existing.cacheKey == cacheKey) {
                return existing.webView
            }
            
            if (existing != null && existing.cacheKey != cacheKey) {
                if (loadUrl) loadUrlWithAuth(existing.webView, cacheKey)
                existing.cacheKey = cacheKey
                return existing.webView
            }
            
            if (!hasEnoughMemory()) clearOldest()
            
            val webView = createWebView()
            configure(webView)
            if (loadUrl) loadUrlWithAuth(webView, cacheKey)
            
            cache[contentId] = CachedWebView(webView, cacheKey)
            return webView
        }
    }
    
    private fun checkInitialized() {
        if (!::appContext.isInitialized) {
            throw IllegalStateException("WebViewFactory not initialized")
        }
    }
    
    private fun hasEnoughMemory(): Boolean {
        val memInfo = getMemoryInfo()
        val availableMB = memInfo.availMem / (1024 * 1024)
        return availableMB > MIN_FREE_MEMORY_MB && !memInfo.lowMemory
    }
    
    private fun loadUrlWithAuth(webView: WebView, url: String) {
        val session = TestpressSdk.getTestpressSession(appContext)
        val headers = mutableMapOf<String, String>()
        session?.token?.let { headers["Authorization"] = "JWT $it" }
        headers["User-Agent"] = UserAgentProvider.get(appContext)
        webView.loadUrl(url, headers)
    }
    
    fun attach(container: ViewGroup, webView: WebView) {
        (webView.parent as? ViewGroup)?.removeView(webView)
        container.children.filterIsInstance<WebView>().forEach { container.removeView(it) }
        container.addView(webView)
        webView.onResume()
        markAsActive(webView)
    }
    
    private fun markAsActive(webView: WebView) {
        synchronized(cache) {
            cache.entries.find { it.value.webView == webView }?.let { entry ->
                activeContentId = entry.key
            }
        }
    }
    
    fun detach(webView: WebView?) {
        webView?.let { wv ->
            wv.onPause()
            (wv.parent as? ViewGroup)?.removeView(wv)
            markAsInactive(wv)
        }
    }
    
    private fun markAsInactive(webView: WebView) {
        synchronized(cache) {
            cache.entries.find { it.value.webView == webView }?.let { entry ->
                if (activeContentId == entry.key) {
                    activeContentId = null
                }
            }
        }
    }
    
    fun clearAll() {
        synchronized(cache) {
            cache.values.forEach { it.destroy() }
            cache.clear()
            activeContentId = null
        }
    }
    
    fun cleanup() {
        memoryCheckHandler.removeCallbacks(memoryCheckRunnable)
        if (::appContext.isInitialized) {
            appContext.unregisterComponentCallbacks(memoryListener)
        }
        clearAll()
    }
    
    private fun clearOldest() {
        synchronized(cache) {
            if (cache.isEmpty()) return
            
            val eldest = cache.entries.firstOrNull() ?: return
            val eldestContentId = eldest.key
            
            if (cache.size == 1) {
                if (isActive(eldestContentId)) return
                removeFromCache(eldest)
            } else {
                removeFromCache(eldest)
            }
        }
    }
    
    private fun isActive(contentId: Long): Boolean {
        return activeContentId == contentId
    }
    
    private fun removeFromCache(entry: MutableMap.MutableEntry<Long, CachedWebView>) {
        entry.value.destroy()
        cache.remove(entry.key)
    }
    
    private fun checkWebViewMemoryGrowth() {
        if (!::appContext.isInitialized) return
        
        synchronized(cache) {
            if (cache.isEmpty()) return
            
            val memInfo = getMemoryInfo()
            val availableMB = memInfo.availMem / (1024 * 1024)
            
            if (shouldEvictDueToLowMemory(availableMB)) {
                clearOldest()
                return
            }
            
            if (memInfo.lowMemory) {
                clearOldest()
                return
            }
            
            if (shouldEvictDueToOverflow(availableMB)) {
                clearOldest()
            }
        }
    }
    
    private fun shouldEvictDueToLowMemory(availableMB: Long): Boolean {
        return availableMB < MIN_FREE_MEMORY_MB
    }
    
    private fun shouldEvictDueToOverflow(availableMB: Long): Boolean {
        val estimatedUsageMB = cache.size * WEBVIEW_AVG_SIZE_MB
        val maxAllowedMB = (availableMB * AVAILABLE_RAM_PERCENTAGE).toLong()
        return estimatedUsageMB > maxAllowedMB
    }
    
    private data class CachedWebView(val webView: WebView, var cacheKey: String) {
        fun destroy() {
            webView.loadUrl("about:blank")
            webView.stopLoading()
            webView.removeAllViews()
            webView.destroy()
        }
    }
}
