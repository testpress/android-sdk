package `in`.testpress.util.webview

import android.view.View
import android.view.ViewGroup
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.widget.FrameLayout
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment

open class BaseWebChromeClient(private val fragment: Fragment) : WebChromeClient() {
    
    private var fullscreenView: View? = null
    private var fullscreenCallback: CustomViewCallback? = null
    private var fullscreenBackCallback: OnBackPressedCallback? = null
    
    override fun onShowCustomView(view: View, callback: CustomViewCallback) {
        if (fullscreenView != null) {
            callback.onCustomViewHidden()
            return
        }
        
        fullscreenView = view
        fullscreenCallback = callback
        
        (fragment.requireActivity().window.decorView as? FrameLayout)?.addView(
            view,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
        
        hideSystemUI()
        
        val backCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                fullscreenCallback?.onCustomViewHidden()
            }
        }
        fullscreenBackCallback = backCallback
        fragment.requireActivity().onBackPressedDispatcher.addCallback(
            fragment.viewLifecycleOwner,
            backCallback
        )
    }
    
    override fun onHideCustomView() {
        if (fullscreenView == null) return
        
        (fragment.requireActivity().window.decorView as? FrameLayout)?.removeView(fullscreenView)
        fullscreenView = null
        fullscreenCallback?.onCustomViewHidden()
        fullscreenCallback = null
        
        showSystemUI()
        
        fullscreenBackCallback?.remove()
        fullscreenBackCallback = null
    }
    
    override fun onPermissionRequest(request: PermissionRequest?) {
        val permissions = arrayOf(
            PermissionRequest.RESOURCE_AUDIO_CAPTURE,
            PermissionRequest.RESOURCE_VIDEO_CAPTURE
        )
        request?.grant(permissions)
    }
    
    fun cleanup() {
        if (fullscreenView != null) {
            fullscreenCallback?.onCustomViewHidden()
            (fragment.requireActivity().window.decorView as? FrameLayout)?.removeView(fullscreenView)
            fullscreenView = null
            fullscreenCallback = null
            fullscreenBackCallback?.remove()
            fullscreenBackCallback = null
            showSystemUI()
        }
    }
    
    private fun hideSystemUI() {
        fragment.requireActivity().window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_FULLSCREEN
    }
    
    private fun showSystemUI() {
        fragment.requireActivity().window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
    }
}

