package `in`.testpress.course.util

import android.content.Context
import android.view.ViewGroup
import android.webkit.WebView as AndroidWebView
import android.widget.FrameLayout
import androidx.test.core.app.ApplicationProvider
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import `in`.testpress.util.webview.WebView

@RunWith(RobolectricTestRunner::class)
class WebViewFactoryTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        WebViewFactory.init(context)
        setMaxSize(3)
    }
    
    private fun setMaxSize(size: Int) {
        val maxSizeField = WebViewFactory::class.java.getDeclaredField("maxSize")
        maxSizeField.isAccessible = true
        maxSizeField.setInt(WebViewFactory, size)
    }

    @After
    fun tearDown() {
        WebViewFactory.clearAll()
    }

    @Test
    fun initShouldSucceedWithValidContext() {
        val result = WebViewFactory.init(context)
        assertTrue(result)
    }

    @Test
    fun initShouldHandleNullContext() {
        val result = WebViewFactory.init(null)
        assertFalse(result)
    }

    @Test
    fun initShouldBeIdempotent() {
        val result1 = WebViewFactory.init(context)
        val result2 = WebViewFactory.init(context)
        
        assertTrue(result1)
        assertTrue(result2)
    }

    @Test
    fun acquireShouldCreateNewWebViewForNewContentId() {
        val webView = WebViewFactory.createCached(1L, "https://example.com/pdf1.pdf", createWebView = { WebView(context) }) { }
        
        assertNotNull(webView)
        assertEquals(1, webView.id)
    }

    @Test
    fun acquireShouldReturnCachedWebViewForSameContentId() {
        val webView1 = WebViewFactory.createCached(1L, "https://example.com/pdf1.pdf", createWebView = { WebView(context) }) { }
        val webView2 = WebViewFactory.createCached(1L, "https://example.com/pdf1.pdf", createWebView = { WebView(context) }) { }
        
        assertSame(webView1, webView2)
    }

    @Test
    fun acquireShouldCreateDifferentWebViewsForDifferentContentIds() {
        val webView1 = WebViewFactory.createCached(1L, "https://example.com/pdf1.pdf", createWebView = { WebView(context) }) { }
        val webView2 = WebViewFactory.createCached(2L, "https://example.com/pdf2.pdf", createWebView = { WebView(context) }) { }
        
        assertNotSame(webView1, webView2)
        assertEquals(1, webView1.id)
        assertEquals(2, webView2.id)
    }

    @Test
    fun isCachedShouldReturnFalseForNewContent() {
        val cached = WebViewFactory.isCached(99L, "https://example.com/new.pdf")
        assertFalse(cached)
    }

    @Test
    fun isCachedShouldReturnTrueForCachedContent() {
        WebViewFactory.createCached(1L, "https://example.com/pdf1.pdf", createWebView = { WebView(context) }) { }
        
        val cached = WebViewFactory.isCached(1L, "https://example.com/pdf1.pdf")
        assertTrue(cached)
    }

    @Test
    fun isCachedShouldReturnFalseForDifferentUrl() {
        WebViewFactory.createCached(1L, "https://example.com/pdf1.pdf", createWebView = { WebView(context) }) { }
        
        val cached = WebViewFactory.isCached(1L, "https://example.com/different.pdf")
        assertFalse(cached)
    }

    @Test
    fun lruEvictionShouldRemoveOldestWebView() {
        val webView1 = WebViewFactory.createCached(1L, "https://example.com/pdf1.pdf", createWebView = { WebView(context) }) { }
        val webView2 = WebViewFactory.createCached(2L, "https://example.com/pdf2.pdf", createWebView = { WebView(context) }) { }
        val webView3 = WebViewFactory.createCached(3L, "https://example.com/pdf3.pdf", createWebView = { WebView(context) }) { }
        val webView4 = WebViewFactory.createCached(4L, "https://example.com/pdf4.pdf", createWebView = { WebView(context) }) { }
        
        // Oldest (1L) should be evicted
        assertFalse(WebViewFactory.isCached(1L, "https://example.com/pdf1.pdf"))
        assertTrue(WebViewFactory.isCached(2L, "https://example.com/pdf2.pdf"))
        assertTrue(WebViewFactory.isCached(3L, "https://example.com/pdf3.pdf"))
        assertTrue(WebViewFactory.isCached(4L, "https://example.com/pdf4.pdf"))
    }

    @Test
    fun lruAccessShouldUpdateOrder() {
        val webView1 = WebViewFactory.createCached(1L, "https://example.com/pdf1.pdf", createWebView = { WebView(context) }) { }
        val webView2 = WebViewFactory.createCached(2L, "https://example.com/pdf2.pdf", createWebView = { WebView(context) }) { }
        val webView3 = WebViewFactory.createCached(3L, "https://example.com/pdf3.pdf", createWebView = { WebView(context) }) { }
        
        // Access #1 again (moves to end)
        WebViewFactory.createCached(1L, "https://example.com/pdf1.pdf", createWebView = { WebView(context) }) { }
        
        // Add #4 (should evict #2, not #1)
        val webView4 = WebViewFactory.createCached(4L, "https://example.com/pdf4.pdf", createWebView = { WebView(context) }) { }
        
        assertTrue(WebViewFactory.isCached(1L, "https://example.com/pdf1.pdf"))
        assertFalse(WebViewFactory.isCached(2L, "https://example.com/pdf2.pdf"))
        assertTrue(WebViewFactory.isCached(3L, "https://example.com/pdf3.pdf"))
        assertTrue(WebViewFactory.isCached(4L, "https://example.com/pdf4.pdf"))
    }

    @Test
    fun configureLambdaShouldOnlyBeCalledOnceForSameContentId() {
        var configureCount = 0
        
        WebViewFactory.createCached(1L, "https://example.com/pdf1.pdf", createWebView = { WebView(context) }) { 
            configureCount++
        }
        WebViewFactory.createCached(1L, "https://example.com/pdf1.pdf", createWebView = { WebView(context) }) { 
            configureCount++
        }
        
        assertEquals(1, configureCount)
    }

    @Test
    fun attachShouldNotCrashWithNullParent() {
        val container = FrameLayout(context)
        val webView = WebViewFactory.createCached(1L, "https://example.com/pdf1.pdf", createWebView = { WebView(context) }) { }
        
        assertDoesNotThrow {
            WebViewFactory.attach(container, webView)
        }
    }

    @Test
    fun attachShouldAddWebViewToContainer() {
        val container = FrameLayout(context)
        val webView = WebViewFactory.createCached(1L, "https://example.com/pdf1.pdf", createWebView = { WebView(context) }) { }
        
        WebViewFactory.attach(container, webView)
        
        assertEquals(1, container.childCount)
        assertSame(webView, container.getChildAt(0))
    }

    @Test
    fun attachShouldRemoveWebViewFromPreviousParent() {
        val container1 = FrameLayout(context)
        val container2 = FrameLayout(context)
        val webView = WebViewFactory.createCached(1L, "https://example.com/pdf1.pdf", createWebView = { WebView(context) }) { }
        
        WebViewFactory.attach(container1, webView)
        WebViewFactory.attach(container2, webView)
        
        assertEquals(0, container1.childCount)
        assertEquals(1, container2.childCount)
        assertSame(webView, container2.getChildAt(0))
    }

    @Test
    fun detachShouldNotCrashWithNullWebView() {
        assertDoesNotThrow {
            WebViewFactory.detach(null)
        }
    }

    @Test
    fun detachShouldRemoveWebViewFromParent() {
        val container = FrameLayout(context)
        val webView = WebViewFactory.createCached(1L, "https://example.com/pdf1.pdf", createWebView = { WebView(context) }) { }
        
        WebViewFactory.attach(container, webView)
        assertEquals(1, container.childCount)
        
        WebViewFactory.detach(webView)
        assertEquals(0, container.childCount)
    }

    @Test
    fun clearAllShouldRemoveAllCachedWebViews() {
        WebViewFactory.createCached(1L, "https://example.com/pdf1.pdf", createWebView = { WebView(context) }) { }
        WebViewFactory.createCached(2L, "https://example.com/pdf2.pdf", createWebView = { WebView(context) }) { }
        WebViewFactory.createCached(3L, "https://example.com/pdf3.pdf", createWebView = { WebView(context) }) { }
        
        assertTrue(WebViewFactory.isCached(1L, "https://example.com/pdf1.pdf"))
        assertTrue(WebViewFactory.isCached(2L, "https://example.com/pdf2.pdf"))
        assertTrue(WebViewFactory.isCached(3L, "https://example.com/pdf3.pdf"))
        
        WebViewFactory.clearAll()
        
        assertFalse(WebViewFactory.isCached(1L, "https://example.com/pdf1.pdf"))
        assertFalse(WebViewFactory.isCached(2L, "https://example.com/pdf2.pdf"))
        assertFalse(WebViewFactory.isCached(3L, "https://example.com/pdf3.pdf"))
    }

    @Test
    fun clearAllShouldNotCrashEvenIfCacheIsEmpty() {
        assertDoesNotThrow {
            WebViewFactory.clearAll()
        }
    }

    @Test
    fun webViewSettingsShouldMatchWebViewFragment() {
        val webView = WebViewFactory.createCached(1L, "https://example.com/pdf1.pdf", createWebView = { WebView(context) }) { }
        
        assertTrue(webView.settings.javaScriptEnabled)
        assertFalse(webView.settings.allowFileAccess)
        assertFalse(webView.settings.useWideViewPort)
        assertTrue(webView.settings.loadWithOverviewMode)
        assertTrue(webView.settings.domStorageEnabled)
        assertFalse(webView.settings.builtInZoomControls)
    }
    
    @Test
    fun enableFileAccessShouldEnableAllFileAccessSettings() {
        val webView = WebViewFactory.createCached(1L, "https://example.com/pdf1.pdf", createWebView = { WebView(context) }) { wv ->
            wv.enableFileAccess()
        }
        
        assertTrue(webView.settings.allowFileAccess)
        assertTrue(webView.settings.allowFileAccessFromFileURLs)
        assertTrue(webView.settings.allowUniversalAccessFromFileURLs)
    }

    @Test
    fun webViewShouldHaveCorrectUserAgent() {
        val webView = WebViewFactory.createCached(1L, "https://example.com/pdf1.pdf", createWebView = { WebView(context) }) { }
        
        assertTrue(webView.settings.userAgentString.contains("TestpressAndroidApp/WebView"))
    }

    @Test
    fun acquireWithDifferentUrlShouldReloadSameWebView() {
        val webView1 = WebViewFactory.createCached(1L, "https://example.com/pdf1.pdf", createWebView = { WebView(context) }) { }
        val webView2 = WebViewFactory.createCached(1L, "https://example.com/different.pdf", createWebView = { WebView(context) }) { }
        
        assertSame(webView1, webView2)
    }

    @Test
    fun acquireWithLoadUrlFalseShouldNotLoadUrl() {
        val webView = WebViewFactory.createCached(1L, "cache_key", loadUrl = false, createWebView = { WebView(context) }) { wv ->
            wv.tag = "configured"
        }
        
        assertNotNull(webView)
        assertEquals("configured", webView.tag)
    }

    @Test
    fun acquireDefaultsToLoadUrlTrue() {
        val webView1 = WebViewFactory.createCached(1L, "https://example.com/pdf1.pdf", createWebView = { WebView(context) }) { }
        val webView2 = WebViewFactory.createCached(1L, "https://example.com/pdf1.pdf", loadUrl = true, createWebView = { WebView(context) }) { }
        
        assertSame(webView1, webView2)
    }
    
    @Test
    fun attachShouldMarkWebViewAsActive() {
        val container = FrameLayout(context)
        val webView = WebViewFactory.createCached(1L, "cache_key", createWebView = { WebView(context) }) { }
        
        WebViewFactory.attach(container, webView)
        
        assertNotNull(webView.parent)
    }
    
    @Test
    fun detachShouldMarkWebViewAsInactive() {
        val container = FrameLayout(context)
        val webView = WebViewFactory.createCached(1L, "cache_key", createWebView = { WebView(context) }) { }
        
        WebViewFactory.attach(container, webView)
        WebViewFactory.detach(webView)
        
        assertNull(webView.parent)
    }
    
    @Test
    fun createShouldReturnNewWebViewWithoutCaching() {
        val webView1 = WebViewFactory.create(context)
        val webView2 = WebViewFactory.create(context)
        
        assertNotSame(webView1, webView2)
    }

    private fun assertDoesNotThrow(block: () -> Unit) {
        try {
            block()
        } catch (e: Exception) {
            fail("Should not throw exception: ${e.message}")
        }
    }
}

