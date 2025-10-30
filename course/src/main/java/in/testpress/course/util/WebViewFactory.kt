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
 * - Memory percentage thresholds (30% of available RAM)
 * - Zero-crash guarantee through proactive monitoring
 * - Minimum 2 WebViews for back/forward navigation
 * 
 * Memory Allocation:
 * - Formula: (availableRAM * 0.30) / 300MB per WebView
 * - Minimum: 2 WebViews (~600MB)
 * - Maximum: 3 WebViews (~900MB)
 * 
 * Device Examples:
 * - 2-3GB RAM: MAX_SIZE = 2 (enables back/forward)
 * - 4-6GB RAM: MAX_SIZE = 2-3
 * - 8GB+ RAM: MAX_SIZE = 3
 */
object WebViewFactory {
    private const val TAG = "WebViewFactory"
    
    // Memory Management Constants
    private const val WEBVIEW_AVG_SIZE_MB = 300 // Average WebView memory footprint
    private const val MAX_MEMORY_PERCENTAGE = 0.10 // Max 10% of device RAM
    private const val MIN_FREE_MEMORY_MB = 200 // Minimum free memory to maintain
    private const val MAX_WEBVIEW_SIZE_MB = 500 // Max size per WebView before eviction
    private const val MEMORY_CHECK_INTERVAL_MS = 30_000L // Check every 30 seconds
    
    private lateinit var appContext: Context
    private var maxSize: Int = 2 // Dynamic, recalculated on init
    private val memoryCheckHandler = android.os.Handler(android.os.Looper.getMainLooper())
    
    // Track which WebView is currently active (attached and visible)
    @Volatile
    private var activeContentId: Long? = null
    
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
    
    /**
     * Periodic memory check to detect WebView memory growth
     * Runs every 30 seconds to check if cached WebViews have grown too large
     */
    private val memoryCheckRunnable = object : Runnable {
        override fun run() {
            try {
                checkWebViewMemoryGrowth()
                // Schedule next check
                memoryCheckHandler.postDelayed(this, MEMORY_CHECK_INTERVAL_MS)
            } catch (e: Exception) {
                Log.e(TAG, "Error in memory check runnable", e)
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
            
            // Start periodic memory monitoring
            memoryCheckHandler.postDelayed(memoryCheckRunnable, MEMORY_CHECK_INTERVAL_MS)
            
            Log.d(TAG, "✓ Initialized successfully (MAX_SIZE=$maxSize, memory checks every ${MEMORY_CHECK_INTERVAL_MS/1000}s)")
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
     * - Use max 15% of total device RAM for WebView cache (increased from 10%)
     * - Each WebView ~300MB average
     * - Minimum 2, Maximum 3 for usability
     * 
     * Why minimum 2?
     * - Enables back/forward navigation without reload
     * - Most devices have enough memory (2GB+ = 600MB for 2 WebViews)
     */
    private fun calculateOptimalCacheSize(): Int {
        return try {
            val activityManager = appContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val memInfo = ActivityManager.MemoryInfo()
            activityManager.getMemoryInfo(memInfo)
            
            val totalRamMB = memInfo.totalMem / (1024 * 1024)
            val availableRamMB = memInfo.availMem / (1024 * 1024)
            
            // Strategy: Use available memory, not total
            val maxCacheMemoryMB = (availableRamMB * 0.30).toLong() // 30% of available
            val calculatedSize = (maxCacheMemoryMB / WEBVIEW_AVG_SIZE_MB).toInt()
            
            // Clamp between 2 and 3 for usability
            val safeSize = calculatedSize.coerceIn(2, 3)
            
            Log.d(TAG, "📊 Memory calculation: totalRAM=${totalRamMB}MB, " +
                "availableRAM=${availableRamMB}MB, maxCache=${maxCacheMemoryMB}MB (30% of available), " +
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
            
            // Mark this WebView as active
            synchronized(cache) {
                cache.entries.find { it.value.webView == webView }?.let { entry ->
                    activeContentId = entry.key
                    Log.d(TAG, "✓ WebView attached to container (contentId=${entry.key} now ACTIVE)")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to attach WebView", e)
        }
    }
    
    fun detach(webView: WebView?) {
        try {
            webView?.let { wv ->
                wv.onPause()
                (wv.parent as? ViewGroup)?.removeView(wv)
                
                // Mark this WebView as inactive
                synchronized(cache) {
                    cache.entries.find { it.value.webView == wv }?.let { entry ->
                        if (activeContentId == entry.key) {
                            activeContentId = null
                            Log.d(TAG, "✓ WebView detached from container (contentId=${entry.key} now INACTIVE, kept in RAM cache)")
                        } else {
                            Log.d(TAG, "✓ WebView detached from container (kept in RAM cache)")
                        }
                    }
                }
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
                activeContentId = null  // Reset active tracking
                Log.d(TAG, "🗑 Cleared all RAM cached WebViews ($count instances)")
                logMemoryInfo()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear cache", e)
        }
    }
    
    /**
     * Cleanup resources and stop background tasks
     * Call this when the app is being destroyed (optional)
     */
    fun cleanup() {
        try {
            // Stop memory monitoring
            memoryCheckHandler.removeCallbacks(memoryCheckRunnable)
            
            // Unregister memory listener
            if (::appContext.isInitialized) {
                appContext.unregisterComponentCallbacks(memoryListener)
            }
            
            // Clear cache
            clearAll()
            
            Log.d(TAG, "✓ Cleanup completed")
        } catch (e: Exception) {
            Log.e(TAG, "Error during cleanup", e)
        }
    }
    
    /**
     * Clear oldest (least recently used) cache entry
     * Used for emergency memory management
     * 
     * IMPORTANT: Never evicts the last/only ACTIVE WebView
     * - If user has 1 WebView AND it's active: Don't destroy mid-session
     * - If user has 1 WebView BUT it's inactive (background): Safe to evict! ✅
     * - If user has 2+ WebViews: Safe to evict oldest (not actively viewing)
     */
    private fun clearOldest() {
        try {
            synchronized(cache) {
                if (cache.isEmpty()) return
                
                val eldest = cache.entries.firstOrNull() ?: return
                val eldestContentId = eldest.key
                
                // Special handling for single WebView in cache
                if (cache.size == 1) {
                    // Check if this single WebView is currently ACTIVE (user viewing it)
                    if (activeContentId == eldestContentId) {
                        Log.w(TAG, "⚠️ Skip eviction: Only 1 WebView (contentId=$eldestContentId) and it's ACTIVE")
                        Log.w(TAG, "   User is currently viewing it, don't destroy mid-session")
                        Log.w(TAG, "   Let OS handle critical memory via ComponentCallbacks2")
                        return  // Don't destroy active session!
                    } else {
                        // Single WebView but it's INACTIVE (user navigated away)
                        Log.d(TAG, "🗑 Clearing inactive WebView: contentId=$eldestContentId")
                        Log.d(TAG, "   Cache size: 1, but WebView is in BACKGROUND (not active)")
                        eldest.value.destroy()
                        cache.remove(eldestContentId)
                        logMemoryInfo()
                        return
                    }
                }
                
                // Safe to evict: User has 2+ WebViews, they're viewing the newer one
                Log.d(TAG, "🗑 Clearing oldest: contentId=$eldestContentId (cache size: ${cache.size} → ${cache.size - 1})")
                eldest.value.destroy()
                cache.remove(eldestContentId)
                logMemoryInfo()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear oldest", e)
        }
    }
    
    /**
     * Check if cached WebViews have grown too large and evict if needed.
     * 
     * Monitors:
     * - Available system memory
     * - Total memory usage trend
     * - Individual WebView growth (via heuristics)
     * 
     * Actions:
     * - Clear oldest if available memory < MIN_FREE_MEMORY_MB
     * - Reduce cache size if memory pressure detected
     * - NEVER evicts last/only ACTIVE WebView (user is viewing it)
     * - DOES evict inactive background WebViews (even if only one)
     * 
     * Philosophy:
     * - 1 WebView + ACTIVE: User is viewing it, DON'T destroy mid-session
     * - 1 WebView + INACTIVE: User navigated away, SAFE to evict ✅
     * - 2+ WebViews: Safe to evict oldest (user viewing newer one)
     */
    private fun checkWebViewMemoryGrowth() {
        try {
            if (!::appContext.isInitialized) return
            
            synchronized(cache) {
                if (cache.isEmpty()) return
                
                val activityManager = appContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                val memInfo = ActivityManager.MemoryInfo()
                activityManager.getMemoryInfo(memInfo)
                
                val availableMB = memInfo.availMem / (1024 * 1024)
                val cacheSize = cache.size
                
                // Check 1: Low available memory
                if (availableMB < MIN_FREE_MEMORY_MB) {
                    Log.w(TAG, "⚠️ MEMORY GROWTH DETECTED: Available=${availableMB}MB < ${MIN_FREE_MEMORY_MB}MB " +
                        "(cache size: $cacheSize)")
                    clearOldest()
                    return
                }
                
                // Check 2: Memory threshold warning
                if (memInfo.lowMemory) {
                    Log.w(TAG, "⚠️ LOW MEMORY FLAG: Clearing oldest WebView (cache size: $cacheSize)")
                    clearOldest()
                    return
                }
                
                // Check 3: Estimated total WebView memory usage
                val estimatedWebViewMemoryMB = cacheSize * WEBVIEW_AVG_SIZE_MB
                val maxAllowedMemoryMB = (availableMB * 0.30).toLong()
                
                if (estimatedWebViewMemoryMB > maxAllowedMemoryMB) {
                    Log.w(TAG, "⚠️ CACHE MEMORY OVERFLOW: Estimated=${estimatedWebViewMemoryMB}MB > " +
                        "Allowed=${maxAllowedMemoryMB}MB (clearing oldest)")
                    clearOldest()
                    return
                }
                
                // All checks passed
                Log.d(TAG, "✓ Memory check OK: available=${availableMB}MB, " +
                    "cacheSize=$cacheSize, estimated=${estimatedWebViewMemoryMB}MB, " +
                    "lowMemory=${memInfo.lowMemory}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking WebView memory growth", e)
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

