package `in`.testpress.util.webview

import android.content.Context
import android.util.AttributeSet
import android.webkit.CookieManager
import android.webkit.WebSettings
import android.webkit.WebView

open class CustomWebView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : WebView(context, attrs, defStyleAttr) {
    
    init {
        configureDefaultSettings()
    }
    
    private fun configureDefaultSettings() {
        settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            useWideViewPort = false
            loadWithOverviewMode = true
            builtInZoomControls = false
            cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
            setSupportZoom(false)
            userAgentString += " TestpressAndroidApp/WebView"
        }
        
        clearCache(true)
        clearHistory()
        CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)
        setWebContentsDebuggingEnabled(true)
    }
    
    fun enableFileAccess() {
        settings.apply {
            allowFileAccess = true
            allowFileAccessFromFileURLs = true
            allowUniversalAccessFromFileURLs = true
        }
    }
}

