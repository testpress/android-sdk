package `in`.testpress.course.util

import `in`.testpress.course.network.NetworkBookmark
import `in`.testpress.util.webview.WebView
import android.content.Context
import android.os.Build
import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder

class AIPdfJsInjector(private val context: Context, private val webView: WebView) {
    
    private val gson = GsonBuilder()
        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        .create()
    
    fun injectBookmarks(bookmarks: List<NetworkBookmark>) {
        val bookmarksJson = convertBookmarksToJson(bookmarks)
        val script = buildInjectionScript(bookmarksJson)
        executeScript(script)
    }
    
    private fun convertBookmarksToJson(bookmarks: List<NetworkBookmark>): String {
        val bookmarksForLearnLens = bookmarks.mapNotNull { bookmark ->
            bookmark.id?.let {
                mapOf(
                    "id" to it,
                    "page_number" to (bookmark.pageNumber ?: 0),
                    "preview_text" to (bookmark.previewText ?: "")
                )
            }
        }
        return gson.toJson(bookmarksForLearnLens)
    }
    
    
    fun checkReadiness(
        onReady: () -> Unit,
        onNotReady: () -> Unit,
        attempts: Int = 0,
        maxAttempts: Int = 30
    ) {
        if (attempts >= maxAttempts) {
            onNotReady()
            return
        }
        
        val checkScript = buildCheckScript()
        executeCheck(checkScript, onReady, onNotReady, attempts, maxAttempts)
    }
    
    private fun buildInjectionScript(bookmarksJson: String): String {
        return """
            (function() {
                var bookmarks = $bookmarksJson;
                if (!bookmarks || !Array.isArray(bookmarks) || bookmarks.length === 0) {
                    return;
                }
                
                // Wait for setAnnotationsForLearnLens to be available, then call it
                var attempts = 0;
                var maxAttempts = 50; // 5 seconds
                var interval = setInterval(function() {
                    attempts++;
                    if (typeof window.setAnnotationsForLearnLens === 'function') {
                        clearInterval(interval);
                        window.setAnnotationsForLearnLens(bookmarks);
                    } else if (attempts >= maxAttempts) {
                        clearInterval(interval);
                    }
                }, 100);
            })();
        """.trimIndent()
    }
    
    private fun buildCheckScript(): String {
        return """
            (function() {
                if (window.LearnLens && document.getElementById('learnlens-pdf-chat') && document.getElementById('learnlens-pdf-chat').children.length > 0) {
                    return true;
                }
                return false;
            })();
        """.trimIndent()
    }
    
    private fun executeScript(script: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            webView.evaluateJavascript(script, null)
        } else {
            webView.loadUrl("javascript:$script")
        }
    }
    
    private fun executeCheck(
        script: String,
        onReady: () -> Unit,
        onNotReady: () -> Unit,
        attempts: Int,
        maxAttempts: Int
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            webView.evaluateJavascript(script) { result ->
                if (result == "true") {
                    onReady()
                } else {
                    webView.postDelayed({
                        checkReadiness(onReady, onNotReady, attempts + 1, maxAttempts)
                    }, 100)
                }
            }
        } else {
            webView.postDelayed({ onNotReady() }, 500)
        }
    }
}

