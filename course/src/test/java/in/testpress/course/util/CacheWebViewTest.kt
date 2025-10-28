package `in`.testpress.course.util

import android.content.Context
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.FrameLayout
import androidx.test.core.app.ApplicationProvider
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class CacheWebViewTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        CacheWebView.init(context)
    }

    @After
    fun tearDown() {
        CacheWebView.clearAll()
    }

    @Test
    fun initShouldSucceedWithValidContext() {
        val result = CacheWebView.init(context)
        assertTrue(result)
    }

    @Test
    fun initShouldHandleNullContext() {
        val result = CacheWebView.init(null)
        assertFalse(result)
    }

    @Test
    fun initShouldBeIdempotent() {
        val result1 = CacheWebView.init(context)
        val result2 = CacheWebView.init(context)
        
        assertTrue(result1)
        assertTrue(result2)
    }

    @Test
    fun acquireShouldCreateNewWebViewForNewContentId() {
        val webView = CacheWebView.acquire(1L, "https://example.com/pdf1.pdf", createWebView = { WebView(context) }) { }
        
        assertNotNull(webView)
        assertEquals(1, webView.id)
    }

    @Test
    fun acquireShouldReturnCachedWebViewForSameContentId() {
        val webView1 = CacheWebView.acquire(1L, "https://example.com/pdf1.pdf", createWebView = { WebView(context) }) { }
        val webView2 = CacheWebView.acquire(1L, "https://example.com/pdf1.pdf", createWebView = { WebView(context) }) { }
        
        assertSame(webView1, webView2)
    }

    @Test
    fun acquireShouldCreateDifferentWebViewsForDifferentContentIds() {
        val webView1 = CacheWebView.acquire(1L, "https://example.com/pdf1.pdf", createWebView = { WebView(context) }) { }
        val webView2 = CacheWebView.acquire(2L, "https://example.com/pdf2.pdf", createWebView = { WebView(context) }) { }
        
        assertNotSame(webView1, webView2)
        assertEquals(1, webView1.id)
        assertEquals(2, webView2.id)
    }

    @Test
    fun isCachedShouldReturnFalseForNewContent() {
        val cached = CacheWebView.isCached(99L, "https://example.com/new.pdf")
        assertFalse(cached)
    }

    @Test
    fun isCachedShouldReturnTrueForCachedContent() {
        CacheWebView.acquire(1L, "https://example.com/pdf1.pdf", createWebView = { WebView(context) }) { }
        
        val cached = CacheWebView.isCached(1L, "https://example.com/pdf1.pdf")
        assertTrue(cached)
    }

    @Test
    fun isCachedShouldReturnFalseForDifferentUrl() {
        CacheWebView.acquire(1L, "https://example.com/pdf1.pdf", createWebView = { WebView(context) }) { }
        
        val cached = CacheWebView.isCached(1L, "https://example.com/different.pdf")
        assertFalse(cached)
    }

    @Test
    fun lruEvictionShouldRemoveOldestWebView() {
        val webView1 = CacheWebView.acquire(1L, "https://example.com/pdf1.pdf", createWebView = { WebView(context) }) { }
        val webView2 = CacheWebView.acquire(2L, "https://example.com/pdf2.pdf", createWebView = { WebView(context) }) { }
        val webView3 = CacheWebView.acquire(3L, "https://example.com/pdf3.pdf", createWebView = { WebView(context) }) { }
        val webView4 = CacheWebView.acquire(4L, "https://example.com/pdf4.pdf", createWebView = { WebView(context) }) { }
        
        // Oldest (1L) should be evicted
        assertFalse(CacheWebView.isCached(1L, "https://example.com/pdf1.pdf"))
        assertTrue(CacheWebView.isCached(2L, "https://example.com/pdf2.pdf"))
        assertTrue(CacheWebView.isCached(3L, "https://example.com/pdf3.pdf"))
        assertTrue(CacheWebView.isCached(4L, "https://example.com/pdf4.pdf"))
    }

    @Test
    fun lruAccessShouldUpdateOrder() {
        val webView1 = CacheWebView.acquire(1L, "https://example.com/pdf1.pdf", createWebView = { WebView(context) }) { }
        val webView2 = CacheWebView.acquire(2L, "https://example.com/pdf2.pdf", createWebView = { WebView(context) }) { }
        val webView3 = CacheWebView.acquire(3L, "https://example.com/pdf3.pdf", createWebView = { WebView(context) }) { }
        
        // Access #1 again (moves to end)
        CacheWebView.acquire(1L, "https://example.com/pdf1.pdf", createWebView = { WebView(context) }) { }
        
        // Add #4 (should evict #2, not #1)
        val webView4 = CacheWebView.acquire(4L, "https://example.com/pdf4.pdf", createWebView = { WebView(context) }) { }
        
        assertTrue(CacheWebView.isCached(1L, "https://example.com/pdf1.pdf"))
        assertFalse(CacheWebView.isCached(2L, "https://example.com/pdf2.pdf"))
        assertTrue(CacheWebView.isCached(3L, "https://example.com/pdf3.pdf"))
        assertTrue(CacheWebView.isCached(4L, "https://example.com/pdf4.pdf"))
    }

    @Test
    fun configureLambdaShouldOnlyBeCalledOnceForSameContentId() {
        var configureCount = 0
        
        CacheWebView.acquire(1L, "https://example.com/pdf1.pdf", createWebView = { WebView(context) }) { 
            configureCount++
        }
        CacheWebView.acquire(1L, "https://example.com/pdf1.pdf", createWebView = { WebView(context) }) { 
            configureCount++
        }
        
        assertEquals(1, configureCount)
    }

    @Test
    fun attachShouldNotCrashWithNullParent() {
        val container = FrameLayout(context)
        val webView = CacheWebView.acquire(1L, "https://example.com/pdf1.pdf", createWebView = { WebView(context) }) { }
        
        assertDoesNotThrow {
            CacheWebView.attach(container, webView)
        }
    }

    @Test
    fun attachShouldAddWebViewToContainer() {
        val container = FrameLayout(context)
        val webView = CacheWebView.acquire(1L, "https://example.com/pdf1.pdf", createWebView = { WebView(context) }) { }
        
        CacheWebView.attach(container, webView)
        
        assertEquals(1, container.childCount)
        assertSame(webView, container.getChildAt(0))
    }

    @Test
    fun attachShouldRemoveWebViewFromPreviousParent() {
        val container1 = FrameLayout(context)
        val container2 = FrameLayout(context)
        val webView = CacheWebView.acquire(1L, "https://example.com/pdf1.pdf", createWebView = { WebView(context) }) { }
        
        CacheWebView.attach(container1, webView)
        CacheWebView.attach(container2, webView)
        
        assertEquals(0, container1.childCount)
        assertEquals(1, container2.childCount)
        assertSame(webView, container2.getChildAt(0))
    }

    @Test
    fun detachShouldNotCrashWithNullWebView() {
        assertDoesNotThrow {
            CacheWebView.detach(null)
        }
    }

    @Test
    fun detachShouldRemoveWebViewFromParent() {
        val container = FrameLayout(context)
        val webView = CacheWebView.acquire(1L, "https://example.com/pdf1.pdf", createWebView = { WebView(context) }) { }
        
        CacheWebView.attach(container, webView)
        assertEquals(1, container.childCount)
        
        CacheWebView.detach(webView)
        assertEquals(0, container.childCount)
    }

    @Test
    fun clearAllShouldRemoveAllCachedWebViews() {
        CacheWebView.acquire(1L, "https://example.com/pdf1.pdf", createWebView = { WebView(context) }) { }
        CacheWebView.acquire(2L, "https://example.com/pdf2.pdf", createWebView = { WebView(context) }) { }
        CacheWebView.acquire(3L, "https://example.com/pdf3.pdf", createWebView = { WebView(context) }) { }
        
        assertTrue(CacheWebView.isCached(1L, "https://example.com/pdf1.pdf"))
        assertTrue(CacheWebView.isCached(2L, "https://example.com/pdf2.pdf"))
        assertTrue(CacheWebView.isCached(3L, "https://example.com/pdf3.pdf"))
        
        CacheWebView.clearAll()
        
        assertFalse(CacheWebView.isCached(1L, "https://example.com/pdf1.pdf"))
        assertFalse(CacheWebView.isCached(2L, "https://example.com/pdf2.pdf"))
        assertFalse(CacheWebView.isCached(3L, "https://example.com/pdf3.pdf"))
    }

    @Test
    fun clearAllShouldNotCrashEvenIfCacheIsEmpty() {
        assertDoesNotThrow {
            CacheWebView.clearAll()
        }
    }

    @Test
    fun webViewSettingsShouldMatchWebViewFragment() {
        val webView = CacheWebView.acquire(1L, "https://example.com/pdf1.pdf", createWebView = { WebView(context) }) { }
        
        assertTrue(webView.settings.javaScriptEnabled)
        assertTrue(webView.settings.allowFileAccess)
        assertFalse(webView.settings.useWideViewPort)
        assertTrue(webView.settings.loadWithOverviewMode)
        assertTrue(webView.settings.domStorageEnabled)
        assertFalse(webView.settings.builtInZoomControls)
    }

    @Test
    fun webViewShouldHaveCorrectUserAgent() {
        val webView = CacheWebView.acquire(1L, "https://example.com/pdf1.pdf", createWebView = { WebView(context) }) { }
        
        assertTrue(webView.settings.userAgentString.contains("TestpressAndroidApp/WebView"))
    }

    @Test
    fun acquireWithDifferentUrlShouldReloadSameWebView() {
        val webView1 = CacheWebView.acquire(1L, "https://example.com/pdf1.pdf", createWebView = { WebView(context) }) { }
        val webView2 = CacheWebView.acquire(1L, "https://example.com/different.pdf", createWebView = { WebView(context) }) { }
        
        assertSame(webView1, webView2)
    }

    @Test
    fun acquireWithLoadUrlFalseShouldNotLoadUrl() {
        val webView = CacheWebView.acquire(1L, "cache_key", loadUrl = false, createWebView = { WebView(context) }) { wv ->
            wv.tag = "configured"
        }
        
        assertNotNull(webView)
        assertEquals("configured", webView.tag)
    }

    @Test
    fun acquireDefaultsToLoadUrlTrue() {
        val webView1 = CacheWebView.acquire(1L, "https://example.com/pdf1.pdf", createWebView = { WebView(context) }) { }
        val webView2 = CacheWebView.acquire(1L, "https://example.com/pdf1.pdf", loadUrl = true, createWebView = { WebView(context) }) { }
        
        assertSame(webView1, webView2)
    }

    private fun assertDoesNotThrow(block: () -> Unit) {
        try {
            block()
        } catch (e: Exception) {
            fail("Should not throw exception: ${e.message}")
        }
    }
}

