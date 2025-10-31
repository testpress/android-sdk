package `in`.testpress.util.webview

import android.view.View
import android.view.Window
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.widget.FrameLayout
import androidx.activity.OnBackPressedDispatcher
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class BaseWebChromeClientTest {

    private lateinit var chromeClient: BaseWebChromeClient
    private lateinit var fragment: Fragment
    private lateinit var activity: FragmentActivity
    
    @Mock
    private lateinit var customView: View
    
    @Mock
    private lateinit var customViewCallback: WebChromeClient.CustomViewCallback
    
    @Mock
    private lateinit var permissionRequest: PermissionRequest

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        
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
    }

    @Test
    fun onShowCustomViewShouldEnterFullscreen() {
        chromeClient.onShowCustomView(customView, customViewCallback)
        
        val decorView = activity.window.decorView as FrameLayout
        assertTrue(decorView.childCount > 0)
        
        verify(customViewCallback, never()).onCustomViewHidden()
    }

    @Test
    fun onShowCustomViewShouldRejectIfAlreadyInFullscreen() {
        chromeClient.onShowCustomView(customView, customViewCallback)
        
        val secondCallback = mock(WebChromeClient.CustomViewCallback::class.java)
        val secondView = mock(View::class.java)
        
        chromeClient.onShowCustomView(secondView, secondCallback)
        
        verify(secondCallback).onCustomViewHidden()
    }

    @Test
    fun onHideCustomViewShouldExitFullscreen() {
        chromeClient.onShowCustomView(customView, customViewCallback)
        
        chromeClient.onHideCustomView()
        
        verify(customViewCallback).onCustomViewHidden()
    }

    @Test
    fun onHideCustomViewShouldDoNothingIfNotInFullscreen() {
        chromeClient.onHideCustomView()
        
        verify(customViewCallback, never()).onCustomViewHidden()
    }

    @Test
    fun onPermissionRequestShouldGrantAudioAndVideoPermissions() {
        chromeClient.onPermissionRequest(permissionRequest)
        
        verify(permissionRequest).grant(argThat { permissions ->
            permissions.contains(PermissionRequest.RESOURCE_AUDIO_CAPTURE) &&
            permissions.contains(PermissionRequest.RESOURCE_VIDEO_CAPTURE)
        })
    }

    @Test
    fun onPermissionRequestShouldHandleNullRequest() {
        // Should not crash
        chromeClient.onPermissionRequest(null)
    }

    @Test
    fun cleanupShouldExitFullscreenIfActive() {
        chromeClient.onShowCustomView(customView, customViewCallback)
        
        chromeClient.cleanup()
        
        verify(customViewCallback).onCustomViewHidden()
    }

    @Test
    fun cleanupShouldDoNothingIfNotInFullscreen() {
        // Should not crash
        chromeClient.cleanup()
    }

    @Test
    fun backButtonShouldExitFullscreen() {
        chromeClient.onShowCustomView(customView, customViewCallback)
        
        activity.onBackPressedDispatcher.onBackPressed()
        
        verify(customViewCallback).onCustomViewHidden()
    }
}

