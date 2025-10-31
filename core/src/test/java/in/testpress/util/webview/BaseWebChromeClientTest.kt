package `in`.testpress.util.webview

import android.view.View
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class BaseWebChromeClientTest {

    private lateinit var chromeClient: BaseWebChromeClient
    private lateinit var fragment: Fragment
    private lateinit var activity: FragmentActivity
    private lateinit var testView: View

    @Before
    fun setup() {
        activity = Robolectric.buildActivity(FragmentActivity::class.java)
            .create()
            .start()
            .resume()
            .get()
        
        fragment = Fragment()
        activity.supportFragmentManager.beginTransaction()
            .add(fragment, "test")
            .commitNow()
        
        chromeClient = BaseWebChromeClient(fragment)
        testView = View(activity)
    }

    @Test
    fun onShowCustomViewShouldAddViewToDecorView() {
        val callback = TestCustomViewCallback()
        val initialChildCount = (activity.window.decorView as FrameLayout).childCount
        
        chromeClient.onShowCustomView(testView, callback)
        
        val finalChildCount = (activity.window.decorView as FrameLayout).childCount
        assertTrue(finalChildCount > initialChildCount)
        assertFalse(callback.wasHidden)
    }

    @Test
    fun onShowCustomViewShouldRejectIfAlreadyInFullscreen() {
        val firstCallback = TestCustomViewCallback()
        chromeClient.onShowCustomView(testView, firstCallback)
        
        val secondCallback = TestCustomViewCallback()
        val secondView = View(activity)
        chromeClient.onShowCustomView(secondView, secondCallback)
        
        assertTrue(secondCallback.wasHidden)
    }

    @Test
    fun onHideCustomViewShouldRemoveView() {
        val callback = TestCustomViewCallback()
        val initialChildCount = (activity.window.decorView as FrameLayout).childCount
        chromeClient.onShowCustomView(testView, callback)
        
        chromeClient.onHideCustomView()
        
        val finalChildCount = (activity.window.decorView as FrameLayout).childCount
        assertEquals(initialChildCount, finalChildCount)
    }

    @Test
    fun onHideCustomViewShouldDoNothingIfNotInFullscreen() {
        // Should not crash
        chromeClient.onHideCustomView()
    }

    @Test
    fun onPermissionRequestShouldGrantAudioAndVideoPermissions() {
        val request = TestPermissionRequest()
        
        chromeClient.onPermissionRequest(request)
        
        assertTrue(request.wasGranted)
        assertTrue(request.grantedPermissions?.contains(PermissionRequest.RESOURCE_AUDIO_CAPTURE) == true)
        assertTrue(request.grantedPermissions?.contains(PermissionRequest.RESOURCE_VIDEO_CAPTURE) == true)
    }

    @Test
    fun onPermissionRequestShouldHandleNullRequest() {
        // Should not crash
        chromeClient.onPermissionRequest(null)
    }

    @Test
    fun cleanupShouldExitFullscreenIfActive() {
        val callback = TestCustomViewCallback()
        chromeClient.onShowCustomView(testView, callback)
        
        chromeClient.cleanup()
        
        assertTrue(callback.wasHidden)
    }

    @Test
    fun cleanupShouldDoNothingIfNotInFullscreen() {
        // Should not crash
        chromeClient.cleanup()
    }

    @Test
    fun backButtonShouldExitFullscreen() {
        val callback = TestCustomViewCallback()
        chromeClient.onShowCustomView(testView, callback)
        
        activity.onBackPressedDispatcher.onBackPressed()
        
        assertTrue(callback.wasHidden)
    }
    
    private class TestCustomViewCallback : WebChromeClient.CustomViewCallback {
        var wasHidden = false
        
        override fun onCustomViewHidden() {
            wasHidden = true
        }
    }
    
    private class TestPermissionRequest : PermissionRequest() {
        var wasGranted = false
        var grantedPermissions: Array<out String>? = null
        
        override fun getOrigin() = null
        override fun getResources() = emptyArray<String>()
        
        override fun grant(resources: Array<out String>?) {
            wasGranted = true
            grantedPermissions = resources
        }
        
        override fun deny() {}
    }
}

