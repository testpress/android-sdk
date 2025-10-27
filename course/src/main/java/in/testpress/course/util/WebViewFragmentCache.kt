package `in`.testpress.course.util

import `in`.testpress.fragments.WebViewFragment
import android.os.Bundle
import android.util.Log

/**
 * Simple cache for WebViewFragment instances.
 * Keeps up to 3 fragments in memory for instant reuse.
 */
object WebViewFragmentCache {
    private const val TAG = "WebViewFragmentCache"
    private const val MAX_SIZE = 3
    
    private val cache = object : LinkedHashMap<Long, WebViewFragment>(MAX_SIZE, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<Long, WebViewFragment>?): Boolean {
            return size > MAX_SIZE
        }
    }
    
    /**
     * Get cached fragment or create new one.
     */
    fun getOrCreate(contentId: Long, url: String): WebViewFragment {
        // Check cache first
        var fragment = cache[contentId]
        
        if (fragment != null) {
            Log.d(TAG, "Reusing cached WebViewFragment for contentId: $contentId")
            return fragment
        }
        
        // Create new fragment
        Log.d(TAG, "Creating new WebViewFragment for contentId: $contentId")
        fragment = WebViewFragment().apply {
            arguments = Bundle().apply {
                putString(WebViewFragment.URL_TO_OPEN, url)
                putBoolean(WebViewFragment.IS_AUTHENTICATION_REQUIRED, true)
            }
        }
        
        // Add to cache
        cache[contentId] = fragment
        return fragment
    }
    
    /**
     * Clear all cached fragments (call on logout).
     */
    fun clearAll() {
        cache.clear()
        Log.d(TAG, "Cleared all cached fragments")
    }
}

