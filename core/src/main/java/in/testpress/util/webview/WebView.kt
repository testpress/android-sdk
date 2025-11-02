package `in`.testpress.util.webview

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.webkit.CookieManager
import android.webkit.WebSettings
import android.webkit.WebView as AndroidWebView
import `in`.testpress.util.HtmlResourceProcessor
import `in`.testpress.util.LocalWebFileCache

open class WebView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AndroidWebView(context, attrs, defStyleAttr) {
    
    init {
        configureDefaultSettings()
        configureFocusAndTouch()
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
        
        if (0 != (context.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE)) {
            setWebContentsDebuggingEnabled(true)
        }
    }
    
    private fun configureFocusAndTouch() {
        // Input and focus handling (matches XML defaults for interactive views)
        isFocusable = true
        isFocusableInTouchMode = true
        isClickable = true
        isLongClickable = true
        isSaveEnabled = true
        
        // Accessibility (Android O+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            importantForAutofill = View.IMPORTANT_FOR_AUTOFILL_YES
        }
        importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
        
        // Request initial focus
        requestFocus()
    }
    
    fun enableFileAccess() {
        settings.apply {
            allowFileAccess = true
            allowFileAccessFromFileURLs = true
            allowUniversalAccessFromFileURLs = true
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }
    }
    
    fun loadTemplateAndCacheResources(
        templateName: String,
        replacements: Map<String, String>,
        baseUrl: String
    ) {
        val template = LocalWebFileCache.loadTemplate(context, templateName, replacements)
        val resources = HtmlResourceProcessor.extractExternalResources(template)
        
        if (resources.isNotEmpty()) {
            LocalWebFileCache.downloadMultipleInBackground(context, resources)
        }
        
        val localPaths = resources.associate { (url, fileName) ->
            url to LocalWebFileCache.getLocalPath(context, fileName, url)
        }
        
        val processedHtml = HtmlResourceProcessor.replaceUrls(template, localPaths)
        loadDataWithBaseURL(baseUrl, processedHtml, "text/html", "UTF-8", null)
    }
}
