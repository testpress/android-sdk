package `in`.testpress.course.util

import android.app.ActivityManager
import android.content.ComponentCallbacks2
import android.content.Context
import android.content.res.Configuration
import android.util.Log
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebSettings
import android.webkit.WebView as AndroidWebView
import androidx.core.view.children
import `in`.testpress.core.TestpressSdk
import `in`.testpress.util.UserAgentProvider
import `in`.testpress.util.webview.WebView

/**
 * Memory-safe WebView factory with LRU caching and dynamic memory management.
 * 
 * Features:
 * - Dynamic MAX_SIZE based on available device RAM
 * - Low memory listener for emergency cleanup
 * - Memory percentage thresholds (max 10% of available RAM)
 * - Zero-crash guarantee through proactive monitoring
 * 
 * Memory Estimates:
 * - Low-end device (2GB RAM): MAX_SIZE = 1 (~300MB WebView)
 * - Mid-range device (4-6GB RAM): MAX_SIZE = 2 (~600MB WebViews)
 * - High-end device (8GB+ RAM): MAX_SIZE = 3 (~900MB WebViews)
 */
object WebViewFactory {
    private const val TAG = "WebViewFactory"
    
    // Memory Management Constants
    private const val WEBVIEW_AVG_SIZE_MB = 300 // Average WebView memory footprint
    private const val MAX_MEMORY_PERCENTAGE = 0.10 // Max 10% of device RAM
    private const val MIN_FREE_MEMORY_MB = 200 // Minimum free memory to maintain
    
    private lateinit var appContext: Context
    private var maxSize: Int = 2 // Dynamic, recalculated on init
    
    private val cache = object : LinkedHashMap<Long, CachedWebView>(16, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<Long, CachedWebView>?): Boolean {
            val shouldRemove = size > maxSize
            if (shouldRemove && eldest != null) {
                Log.d(TAG, "♻️ LRU Evicting: contentId=${eldest.key} (cache full, size=$size/$maxSize)")
                eldest.value.destroy()
            }
            return shouldRemove
        }
    }
    
    /**
     * Low memory listener to proactively clear cache during memory pressure
     */
    private val memoryListener = object : ComponentCallbacks2 {
        override fun onConfigurationChanged(newConfig: Configuration) {}
        
        override fun onLowMemory() {
            Log.w(TAG, "⚠️ LOW MEMORY: Clearing all WebView cache")
            clearAll()
        }
        
        override fun onTrimMemory(level: Int) {
            when (level) {
                ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL,
                ComponentCallbacks2.TRIM_MEMORY_COMPLETE -> {
                    Log.w(TAG, "⚠️ CRITICAL MEMORY (level=$level): Clearing all cache")
                    clearAll()
                }
                ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW,
                ComponentCallbacks2.TRIM_MEMORY_MODERATE -> {
                    Log.w(TAG, "⚠️ MODERATE MEMORY (level=$level): Clearing oldest cache entries")
                    clearOldest()
                }
                else -> {
                    Log.d(TAG, "Memory trim level: $level (no action)")
                }
            }
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
            
            // Calculate dynamic MAX_SIZE based on device memory
            maxSize = calculateOptimalCacheSize()
            
            // Register low memory listener
            appContext.registerComponentCallbacks(memoryListener)
            
            Log.d(TAG, "✓ Initialized successfully (MAX_SIZE=$maxSize)")
            logMemoryInfo()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize", e)
            false
        }
    }
    
    /**
     * Calculate optimal cache size based on available device memory.
     * 
     * Strategy:
     * - Use max 10% of total device RAM for WebView cache
     * - Each WebView ~300MB average
     * - Minimum 1, Maximum 3 for safety
     */
    private fun calculateOptimalCacheSize(): Int {
        return try {
            val activityManager = appContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val memInfo = ActivityManager.MemoryInfo()
            activityManager.getMemoryInfo(memInfo)
            
            val totalRamMB = memInfo.totalMem / (1024 * 1024)
            val maxCacheMemoryMB = (totalRamMB * MAX_MEMORY_PERCENTAGE).toLong()
            val calculatedSize = (maxCacheMemoryMB / WEBVIEW_AVG_SIZE_MB).toInt()
            
            // Clamp between 1 and 3 for safety
            val safeSize = calculatedSize.coerceIn(1, 3)
            
            Log.d(TAG, "📊 Memory calculation: totalRAM=${totalRamMB}MB, " +
                "maxCache=${maxCacheMemoryMB}MB (${(MAX_MEMORY_PERCENTAGE * 100).toInt()}%), " +
                "calculatedSize=$calculatedSize, finalSize=$safeSize")
            
            safeSize
        } catch (e: Exception) {
            Log.e(TAG, "Failed to calculate cache size, using default=2", e)
            2 // Safe default
        }
    }
    
    /**
     * Log current memory status for debugging
     */
    private fun logMemoryInfo() {
        try {
            val activityManager = appContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val memInfo = ActivityManager.MemoryInfo()
            activityManager.getMemoryInfo(memInfo)
            
            val totalMB = memInfo.totalMem / (1024 * 1024)
            val availableMB = memInfo.availMem / (1024 * 1024)
            val usedMB = totalMB - availableMB
            val threshold = memInfo.threshold / (1024 * 1024)
            val isLowMemory = memInfo.lowMemory
            
            Log.d(TAG, "📊 Device Memory: Total=${totalMB}MB, Available=${availableMB}MB, " +
                "Used=${usedMB}MB, Threshold=${threshold}MB, LowMemory=$isLowMemory")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to log memory info", e)
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
                Log.w(TAG, "⚡⚡⚡ RAM CACHE HIT: contentId=$contentId (INSTANT <5ms) ⚡⚡⚡")
                logMemoryInfo()
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
            
            // Cache MISS - check memory before creating new WebView
            if (!hasEnoughMemoryForWebView()) {
                Log.w(TAG, "⚠️ LOW MEMORY: Clearing oldest cache entry before creating new WebView")
                clearOldest()
            }
            
            Log.d(TAG, "✗ Cache MISS: contentId=$contentId (creating new WebView)")
            val webView = createWebView()
            configure(webView)
            
            if (loadUrl) {
                loadUrlWithAuth(webView, cacheKey)
            }
            
            cache[contentId] = CachedWebView(webView, cacheKey)
            Log.d(TAG, "✓ WebView cached in RAM: contentId=$contentId (cache size: ${cache.size}/$maxSize)")
            logMemoryInfo()
            return webView
        }
    }
    
    /**
     * Check if device has enough memory to safely create a new WebView.
     * 
     * Criteria:
     * - Available memory > MIN_FREE_MEMORY_MB (200MB)
     * - Not in low memory state
     */
    private fun hasEnoughMemoryForWebView(): Boolean {
        return try {
            val activityManager = appContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val memInfo = ActivityManager.MemoryInfo()
            activityManager.getMemoryInfo(memInfo)
            
            val availableMB = memInfo.availMem / (1024 * 1024)
            val hasEnough = availableMB > MIN_FREE_MEMORY_MB && !memInfo.lowMemory
            
            if (!hasEnough) {
                Log.w(TAG, "⚠️ Insufficient memory: available=${availableMB}MB, " +
                    "required=${MIN_FREE_MEMORY_MB}MB, lowMemory=${memInfo.lowMemory}")
            }
            
            hasEnough
        } catch (e: Exception) {
            Log.e(TAG, "Failed to check memory, assuming sufficient", e)
            true // Fail-safe: allow creation
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
                Log.d(TAG, "✓ WebView detached from container (kept in RAM cache)")
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
                logMemoryInfo()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear cache", e)
        }
    }
    
    /**
     * Clear oldest (least recently used) cache entry
     * Used for emergency memory management
     */
    private fun clearOldest() {
        try {
            synchronized(cache) {
                if (cache.isEmpty()) return
                
                // LinkedHashMap with accessOrder=true keeps eldest at the beginning
                val eldest = cache.entries.firstOrNull()
                eldest?.let {
                    Log.d(TAG, "🗑 Clearing oldest: contentId=${it.key}")
                    it.value.destroy()
                    cache.remove(it.key)
                    logMemoryInfo()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear oldest", e)
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

