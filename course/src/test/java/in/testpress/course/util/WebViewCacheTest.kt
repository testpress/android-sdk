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
class WebViewCacheTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        WebViewCache.init(context)
    }

    @After
    fun tearDown() {
        WebViewCache.clearAll()
    }

    @Test
    fun initShouldSucceedWithValidContext() {
        val result = WebViewCache.init(context)
        assertTrue(result)
    }

    @Test
    fun initShouldHandleNullContext() {
        val result = WebViewCache.init(null)
        assertFalse(result)
    }

    @Test
    fun initShouldBeIdempotent() {
        val result1 = WebViewCache.init(context)
        val result2 = WebViewCache.init(context)
        
        assertTrue(result1)
        assertTrue(result2)
    }

    @Test
    fun acquireShouldCreateNewWebViewForNewContentId() {
        val webView = WebViewCache.acquire(1L, "https://example.com/pdf1.pdf") { }
        
        assertNotNull(webView)
        assertEquals(1, webView.id)
    }

    @Test
    fun acquireShouldReturnCachedWebViewForSameContentId() {
        val webView1 = WebViewCache.acquire(1L, "https://example.com/pdf1.pdf") { }
        val webView2 = WebViewCache.acquire(1L, "https://example.com/pdf1.pdf") { }
        
        assertSame(webView1, webView2)
    }

    @Test
    fun acquireShouldCreateDifferentWebViewsForDifferentContentIds() {
        val webView1 = WebViewCache.acquire(1L, "https://example.com/pdf1.pdf") { }
        val webView2 = WebViewCache.acquire(2L, "https://example.com/pdf2.pdf") { }
        
        assertNotSame(webView1, webView2)
        assertEquals(1, webView1.id)
        assertEquals(2, webView2.id)
    }

    @Test
    fun isCachedShouldReturnFalseForNewContent() {
        val cached = WebViewCache.isCached(99L, "https://example.com/new.pdf")
        assertFalse(cached)
    }

    @Test
    fun isCachedShouldReturnTrueForCachedContent() {
        WebViewCache.acquire(1L, "https://example.com/pdf1.pdf") { }
        
        val cached = WebViewCache.isCached(1L, "https://example.com/pdf1.pdf")
        assertTrue(cached)
    }

    @Test
    fun isCachedShouldReturnFalseForDifferentUrl() {
        WebViewCache.acquire(1L, "https://example.com/pdf1.pdf") { }
        
        val cached = WebViewCache.isCached(1L, "https://example.com/different.pdf")
        assertFalse(cached)
    }

    @Test
    fun lruEvictionShouldRemoveOldestWebView() {
        val webView1 = WebViewCache.acquire(1L, "https://example.com/pdf1.pdf") { }
        val webView2 = WebViewCache.acquire(2L, "https://example.com/pdf2.pdf") { }
        val webView3 = WebViewCache.acquire(3L, "https://example.com/pdf3.pdf") { }
        val webView4 = WebViewCache.acquire(4L, "https://example.com/pdf4.pdf") { }
        
        // Oldest (1L) should be evicted
        assertFalse(WebViewCache.isCached(1L, "https://example.com/pdf1.pdf"))
        assertTrue(WebViewCache.isCached(2L, "https://example.com/pdf2.pdf"))
        assertTrue(WebViewCache.isCached(3L, "https://example.com/pdf3.pdf"))
        assertTrue(WebViewCache.isCached(4L, "https://example.com/pdf4.pdf"))
    }

    @Test
    fun lruAccessShouldUpdateOrder() {
        val webView1 = WebViewCache.acquire(1L, "https://example.com/pdf1.pdf") { }
        val webView2 = WebViewCache.acquire(2L, "https://example.com/pdf2.pdf") { }
        val webView3 = WebViewCache.acquire(3L, "https://example.com/pdf3.pdf") { }
        
        // Access #1 again (moves to end)
        WebViewCache.acquire(1L, "https://example.com/pdf1.pdf") { }
        
        // Add #4 (should evict #2, not #1)
        val webView4 = WebViewCache.acquire(4L, "https://example.com/pdf4.pdf") { }
        
        assertTrue(WebViewCache.isCached(1L, "https://example.com/pdf1.pdf"))
        assertFalse(WebViewCache.isCached(2L, "https://example.com/pdf2.pdf"))
        assertTrue(WebViewCache.isCached(3L, "https://example.com/pdf3.pdf"))
        assertTrue(WebViewCache.isCached(4L, "https://example.com/pdf4.pdf"))
    }

    @Test
    fun configureLambdaShouldOnlyBeCalledOnceForSameContentId() {
        var configureCount = 0
        
        WebViewCache.acquire(1L, "https://example.com/pdf1.pdf") { 
            configureCount++
        }
        WebViewCache.acquire(1L, "https://example.com/pdf1.pdf") { 
            configureCount++
        }
        
        assertEquals(1, configureCount)
    }

    @Test
    fun attachShouldNotCrashWithNullParent() {
        val container = FrameLayout(context)
        val webView = WebViewCache.acquire(1L, "https://example.com/pdf1.pdf") { }
        
        assertDoesNotThrow {
            WebViewCache.attach(container, webView)
        }
    }

    @Test
    fun attachShouldAddWebViewToContainer() {
        val container = FrameLayout(context)
        val webView = WebViewCache.acquire(1L, "https://example.com/pdf1.pdf") { }
        
        WebViewCache.attach(container, webView)
        
        assertEquals(1, container.childCount)
        assertSame(webView, container.getChildAt(0))
    }

    @Test
    fun attachShouldRemoveWebViewFromPreviousParent() {
        val container1 = FrameLayout(context)
        val container2 = FrameLayout(context)
        val webView = WebViewCache.acquire(1L, "https://example.com/pdf1.pdf") { }
        
        WebViewCache.attach(container1, webView)
        WebViewCache.attach(container2, webView)
        
        assertEquals(0, container1.childCount)
        assertEquals(1, container2.childCount)
        assertSame(webView, container2.getChildAt(0))
    }

    @Test
    fun detachShouldNotCrashWithNullWebView() {
        assertDoesNotThrow {
            WebViewCache.detach(null)
        }
    }

    @Test
    fun detachShouldRemoveWebViewFromParent() {
        val container = FrameLayout(context)
        val webView = WebViewCache.acquire(1L, "https://example.com/pdf1.pdf") { }
        
        WebViewCache.attach(container, webView)
        assertEquals(1, container.childCount)
        
        WebViewCache.detach(webView)
        assertEquals(0, container.childCount)
    }

    @Test
    fun clearAllShouldRemoveAllCachedWebViews() {
        WebViewCache.acquire(1L, "https://example.com/pdf1.pdf") { }
        WebViewCache.acquire(2L, "https://example.com/pdf2.pdf") { }
        WebViewCache.acquire(3L, "https://example.com/pdf3.pdf") { }
        
        assertTrue(WebViewCache.isCached(1L, "https://example.com/pdf1.pdf"))
        assertTrue(WebViewCache.isCached(2L, "https://example.com/pdf2.pdf"))
        assertTrue(WebViewCache.isCached(3L, "https://example.com/pdf3.pdf"))
        
        WebViewCache.clearAll()
        
        assertFalse(WebViewCache.isCached(1L, "https://example.com/pdf1.pdf"))
        assertFalse(WebViewCache.isCached(2L, "https://example.com/pdf2.pdf"))
        assertFalse(WebViewCache.isCached(3L, "https://example.com/pdf3.pdf"))
    }

    @Test
    fun clearAllShouldNotCrashEvenIfCacheIsEmpty() {
        assertDoesNotThrow {
            WebViewCache.clearAll()
        }
    }

    @Test
    fun webViewSettingsShouldMatchWebViewFragment() {
        val webView = WebViewCache.acquire(1L, "https://example.com/pdf1.pdf") { }
        
        assertTrue(webView.settings.javaScriptEnabled)
        assertTrue(webView.settings.allowFileAccess)
        assertFalse(webView.settings.useWideViewPort)
        assertTrue(webView.settings.loadWithOverviewMode)
        assertTrue(webView.settings.domStorageEnabled)
        assertFalse(webView.settings.builtInZoomControls)
    }

    @Test
    fun webViewShouldHaveCorrectUserAgent() {
        val webView = WebViewCache.acquire(1L, "https://example.com/pdf1.pdf") { }
        
        assertTrue(webView.settings.userAgentString.contains("TestpressAndroidApp/WebView"))
    }

    @Test
    fun acquireWithDifferentUrlShouldReloadSameWebView() {
        val webView1 = WebViewCache.acquire(1L, "https://example.com/pdf1.pdf") { }
        val webView2 = WebViewCache.acquire(1L, "https://example.com/different.pdf") { }
        
        assertSame(webView1, webView2)
    }

    @Test
    fun acquireWithLoadUrlFalseShouldNotLoadUrl() {
        val webView = WebViewCache.acquire(1L, "cache_key", loadUrl = false) { wv ->
            wv.tag = "configured"
        }
        
        assertNotNull(webView)
        assertEquals("configured", webView.tag)
    }

    @Test
    fun acquireDefaultsToLoadUrlTrue() {
        val webView1 = WebViewCache.acquire(1L, "https://example.com/pdf1.pdf") { }
        val webView2 = WebViewCache.acquire(1L, "https://example.com/pdf1.pdf", loadUrl = true) { }
        
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

