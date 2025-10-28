package `in`.testpress.course.util

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class CacheWebViewInitializerTest {

    private lateinit var context: Context
    private lateinit var initializer: CacheWebViewInitializer

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        initializer = CacheWebViewInitializer()
    }

    @Test
    fun onCreateShouldReturnTrueOnSuccess() {
        // Attach context to provider
        initializer.attachInfo(context, null)
        
        val result = initializer.onCreate()
        
        assertTrue(result)
    }

    @Test
    fun onCreateShouldAlwaysReturnTrueEvenOnFailure() {
        // Even with null context, should return true to not block app
        val result = initializer.onCreate()
        
        assertTrue(result)
    }

    @Test
    fun onCreateShouldInitializeCacheWhenContextAvailable() {
        initializer.attachInfo(context, null)
        initializer.onCreate()
        
        // Verify cache is usable
        val webView = CacheWebView.acquire(1L, "https://example.com/test.pdf", createWebView = { WebView(context) }) { }
        assertNotNull(webView)
    }

    @Test
    fun queryShouldReturnNull() {
        assertNull(initializer.query(android.net.Uri.EMPTY, null, null, null, null))
    }

    @Test
    fun getTypeShouldReturnNull() {
        assertNull(initializer.getType(android.net.Uri.EMPTY))
    }

    @Test
    fun insertShouldReturnNull() {
        assertNull(initializer.insert(android.net.Uri.EMPTY, null))
    }

    @Test
    fun deleteShouldReturnZero() {
        assertEquals(0, initializer.delete(android.net.Uri.EMPTY, null, null))
    }

    @Test
    fun updateShouldReturnZero() {
        assertEquals(0, initializer.update(android.net.Uri.EMPTY, null, null, null))
    }
}

