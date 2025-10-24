package `in`.testpress.course.util

import android.util.Log
import `in`.testpress.course.fragments.AIChatPdfFragment
import java.util.LinkedHashMap

/**
 * Global cache for AIChatPdfFragment instances to enable instant loading
 * when switching to AI view for previously loaded PDFs.
 * 
 * Uses LRU (Least Recently Used) eviction strategy to limit memory usage.
 */
object AIChatFragmentCache {
    private const val TAG = "AIChatFragmentCache"
    private const val MAX_CACHE_SIZE = 5 // Cache up to 5 AI fragments
    
    // LRU Cache: LinkedHashMap with accessOrder=true
    private val cache = object : LinkedHashMap<Long, AIChatPdfFragment>(
        MAX_CACHE_SIZE + 1, // Initial capacity
        0.75f, // Load factor
        true // Access order (LRU)
    ) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<Long, AIChatPdfFragment>?): Boolean {
            val shouldRemove = size > MAX_CACHE_SIZE
            if (shouldRemove && eldest != null) {
                Log.d(TAG, "Evicting LRU fragment for contentId: ${eldest.key}")
            }
            return shouldRemove
        }
    }
    
    /**
     * Get cached fragment for a PDF content ID
     */
    fun get(contentId: Long): AIChatPdfFragment? {
        val fragment = cache[contentId]
        if (fragment != null) {
            Log.d(TAG, "✅ Cache HIT for contentId: $contentId")
        } else {
            Log.d(TAG, "❌ Cache MISS for contentId: $contentId")
        }
        return fragment
    }
    
    /**
     * Store fragment in cache
     */
    fun put(contentId: Long, fragment: AIChatPdfFragment) {
        cache[contentId] = fragment
        Log.d(TAG, "📦 Cached fragment for contentId: $contentId (cache size: ${cache.size})")
    }
    
    /**
     * Check if fragment exists in cache
     */
    fun contains(contentId: Long): Boolean {
        return cache.containsKey(contentId)
    }
    
    /**
     * Remove fragment from cache
     */
    fun remove(contentId: Long) {
        cache.remove(contentId)
        Log.d(TAG, "🗑️ Removed fragment for contentId: $contentId")
    }
    
    /**
     * Clear all cached fragments
     */
    fun clear() {
        cache.clear()
        Log.d(TAG, "🧹 Cleared all cached fragments")
    }
    
    /**
     * Get current cache size
     */
    fun size(): Int = cache.size
}

