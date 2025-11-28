package `in`.testpress.course.util

import `in`.testpress.core.TestpressCallback
import `in`.testpress.core.TestpressException
import `in`.testpress.course.network.BookmarksListApiResponse
import `in`.testpress.course.network.NetworkBookmark
import `in`.testpress.course.repository.BookmarkRepository
import `in`.testpress.util.webview.WebView
import `in`.testpress.v2_4.models.ApiResponse
import android.content.Context
import android.os.Build
import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import java.util.HashMap

class LearnLensHelper(private val context: Context, private val webView: WebView) {
    
    private val bookmarkRepository = BookmarkRepository(context)
    private val gson = GsonBuilder()
        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        .create()
    
    fun fetchBookmarks(contentId: Long, callback: (List<NetworkBookmark>) -> Unit) {
        val queryParams = hashMapOf<String, Any>(
            "content_type" to "chapter_content",
            "object_id" to contentId,
            "bookmark_type" to "annotate"
        )
        
        bookmarkRepository.getBookmarks(queryParams, object : TestpressCallback<ApiResponse<BookmarksListApiResponse>>() {
            override fun onSuccess(response: ApiResponse<BookmarksListApiResponse>) {
                val bookmarks = response.results?.bookmarks ?: emptyList()
                callback(bookmarks)
            }
            
            override fun onException(exception: TestpressException?) {
                callback(emptyList())
            }
        })
    }
    
    fun injectBookmarks(bookmarks: List<NetworkBookmark>) {
        val bookmarksForLearnLens = bookmarks.mapNotNull { bookmark ->
            bookmark.id?.let {
                mapOf(
                    "id" to it,
                    "page_number" to (bookmark.pageNumber ?: 0),
                    "preview_text" to (bookmark.previewText ?: "")
                )
            }
        }
        
        val bookmarksJson = gson.toJson(bookmarksForLearnLens)
        val script = buildInjectionScript(bookmarksJson)
        executeScript(script)
    }
    
    fun convertBookmarksToJson(bookmarks: List<NetworkBookmark>): String {
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
    
    fun buildTemplateReplacements(
        pdfUrl: String,
        pdfId: String,
        authToken: String,
        pdfTitle: String,
        bookmarks: List<NetworkBookmark>
    ): Map<String, String> {
        val bookmarksJson = convertBookmarksToJson(bookmarks)
        return mapOf(
            "PDF_URL" to pdfUrl,
            "PDF_ID" to pdfId,
            "AUTH_TOKEN" to authToken,
            "PDF_TITLE" to pdfTitle,
            "INITIAL_BOOKMARKS_JSON" to bookmarksJson
        )
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
            (function injectAnnotations() {
                var bookmarks = $bookmarksJson;
                
                if (window.setAnnotationsForLearnLens) {
                    window.setAnnotationsForLearnLens(bookmarks);
                } else if (window.LearnLens && window.LearnLens.setAnnotations) {
                    try {
                        window.LearnLens.setAnnotations([], bookmarks);
                    } catch (e) {
                        console.error("LearnLens.setAnnotations failed:", e);
                    }
                } else {
                    var attempts = 0;
                    var maxAttempts = 20;
                    var interval = setInterval(function() {
                        attempts++;
                        if (window.setAnnotationsForLearnLens) {
                            clearInterval(interval);
                            window.setAnnotationsForLearnLens(bookmarks);
                        } else if (window.LearnLens && window.LearnLens.setAnnotations) {
                            clearInterval(interval);
                            try {
                                window.LearnLens.setAnnotations([], bookmarks);
                            } catch (e) {
                                console.error("LearnLens.setAnnotations failed:", e);
                            }
                        } else if (attempts >= maxAttempts) {
                            clearInterval(interval);
                        }
                    }, 500);
                }
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

