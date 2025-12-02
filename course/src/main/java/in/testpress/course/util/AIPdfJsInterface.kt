package `in`.testpress.course.util

import android.app.Activity
import android.os.Build
import android.webkit.JavascriptInterface
import android.webkit.WebView
import `in`.testpress.core.TestpressCallback
import `in`.testpress.core.TestpressException
import `in`.testpress.course.repository.BookmarkRepository
import `in`.testpress.course.repository.HighlightRepository
import `in`.testpress.course.network.NetworkBookmark
import `in`.testpress.course.network.NetworkHighlight
import `in`.testpress.util.BaseJavaScriptInterface
import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import android.util.Log

class AIPdfJsInterface(
    private val activity: Activity,
    private val webView: WebView,
    private val contentId: Long
) : BaseJavaScriptInterface(activity) {

    private val bookmarkRepository = BookmarkRepository(activity)
    private val highlightRepository = HighlightRepository(activity)
    private val gson = GsonBuilder()
        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        .create()

    @JavascriptInterface
    fun onBookmarkCreate(bookmarkJson: String) {
        try {
            if (bookmarkJson.isBlank()) {
                throw IllegalArgumentException("Bookmark JSON cannot be empty")
            }

            val bookmarkMap = gson.fromJson(bookmarkJson, Map::class.java) as? Map<*, *>
                ?: throw IllegalArgumentException("Failed to parse bookmark JSON as object")

            val requestBody = hashMapOf<String, Any>().apply {
                put("content_type", "chapter_content")
                put("object_id", contentId)
                put("bookmark_type", "annotate")
                put("category", "attachment")
                
                val pageNumber = (bookmarkMap["page_number"] as? Number)?.toInt()
                    ?: throw IllegalArgumentException("Page number must be provided and greater than 0")
                
                if (pageNumber <= 0) {
                    throw IllegalArgumentException("Page number must be greater than 0")
                }
                
                put("page_number", pageNumber)
                
                val previewText = bookmarkMap["preview_text"] as? String ?: ""
                put("preview_text", previewText)
            }

            bookmarkRepository.createBookmark(requestBody, object : TestpressCallback<NetworkBookmark>() {
                override fun onSuccess(response: NetworkBookmark) {
                    if (response.id == null) {
                        val errorJson = gson.toJson(mapOf("error" to "Bookmark created but ID is missing"))
                        evaluateJavascript("window.LearnLens?.onBookmarkCreateError?.($errorJson);")
                        return
                    }
                    
                    val bookmarkId = response.id!!.toLong()
                    val pageNumber = response.pageNumber ?: 0
                    val previewText = response.previewText ?: ""
                    
                    val result = mapOf(
                        "id" to bookmarkId,
                        "page_number" to pageNumber,
                        "preview_text" to previewText
                    )
                    val resultJson = gson.toJson(result)
                    
                    evaluateJavascript("window.LearnLens?.onBookmarkCreateSuccess?.($resultJson);")
                }

                override fun onException(exception: TestpressException?) {
                    val errorMessage = exception?.message ?: "Unknown error"
                    val errorJson = gson.toJson(mapOf("error" to errorMessage))
                    evaluateJavascript("window.LearnLens?.onBookmarkCreateError?.($errorJson);")
                }
            })
        } catch (e: Exception) {
            val errorJson = gson.toJson(mapOf("error" to (e.message ?: "Unknown error")))
            evaluateJavascript("window.LearnLens?.onBookmarkCreateError?.($errorJson);")
        }
    }

    @JavascriptInterface
    fun onBookmarkDelete(bookmarkId: String) {
        try {
            val id = parseBookmarkId(bookmarkId)
            
            if (id == null) {
                val errorJson = gson.toJson(mapOf("error" to "Invalid bookmark ID"))
                evaluateJavascript("window.LearnLens?.onBookmarkDeleteError?.($errorJson);")
                return
            }
            
            bookmarkRepository.deleteBookmark(id, object : TestpressCallback<Void>() {
                override fun onSuccess(response: Void?) {
                    evaluateJavascript("window.LearnLens?.onBookmarkDeleteSuccess?.('$bookmarkId');")
                }

                override fun onException(exception: TestpressException?) {
                    val errorJson = gson.toJson(mapOf("error" to (exception?.message ?: "Unknown error")))
                    evaluateJavascript("window.LearnLens?.onBookmarkDeleteError?.($errorJson);")
                }
            })
        } catch (e: Exception) {
            val errorJson = gson.toJson(mapOf("error" to (e.message ?: "Unknown error")))
            evaluateJavascript("window.LearnLens?.onBookmarkDeleteError?.($errorJson);")
        }
    }

    @JavascriptInterface
    fun onHighlightCreate(highlightJson: String) {
        try {
            if (highlightJson.isBlank()) {
                throw IllegalArgumentException("Highlight JSON cannot be empty")
            }

            val highlightMap = gson.fromJson(highlightJson, Map::class.java) as? Map<*, *>
                ?: throw IllegalArgumentException("Failed to parse highlight JSON as object")

            val requestBody = hashMapOf<String, Any>().apply {
                val pageNumber = (highlightMap["page_number"] as? Number)?.toInt()
                    ?: throw IllegalArgumentException("Page number must be provided and greater than 0")
                
                if (pageNumber <= 0) {
                    throw IllegalArgumentException("Page number must be greater than 0")
                }
                
                put("page_number", pageNumber)
                
                val position = highlightMap["position"]
                    ?: throw IllegalArgumentException("Position is required for highlight")
                put("position", position)
                
                put("selected_text", highlightMap["selected_text"] as? String ?: "")
                put("notes", highlightMap["notes"] as? String ?: "")
                put("color", highlightMap["color"] as? String ?: "#FFEB3B")
            }

            highlightRepository.createHighlight(contentId, requestBody, object : TestpressCallback<NetworkHighlight>() {
                override fun onSuccess(response: NetworkHighlight) {
                    if (response.id == null) {
                        val errorJson = gson.toJson(mapOf("error" to "Highlight created but ID is missing"))
                        evaluateJavascript("window.LearnLens?.onHighlightCreateError?.($errorJson);")
                        return
                    }
                    
                    val highlightId = response.id!!.toLong()
                    val result = mapOf(
                        "id" to highlightId,
                        "page_number" to (response.pageNumber ?: 0),
                        "selected_text" to (response.selectedText ?: ""),
                        "notes" to (response.notes ?: ""),
                        "color" to (response.color ?: "#FFEB3B"),
                        "position" to (response.position ?: emptyList<Double>())
                    )
                    val resultJson = gson.toJson(result)
                    
                    evaluateJavascript("""
                        (function() {
                            var highlight = $resultJson;
                            if (window.LearnLens && window.LearnLens.onHighlightCreateSuccess) {
                                window.LearnLens.onHighlightCreateSuccess(highlight);
                            }
                        })();
                    """.trimIndent())
                }

                override fun onException(exception: TestpressException?) {
                    val errorMessage = exception?.message ?: "Unknown error"
                    val errorJson = gson.toJson(mapOf("error" to errorMessage))
                    evaluateJavascript("window.LearnLens?.onHighlightCreateError?.($errorJson);")
                }
            })
        } catch (e: Exception) {
            val errorJson = gson.toJson(mapOf("error" to (e.message ?: "Unknown error")))
            evaluateJavascript("window.LearnLens?.onHighlightCreateError?.($errorJson);")
        }
    }

    @JavascriptInterface
    fun onHighlightDelete(highlightId: String) {
        try {
            val id = parseHighlightId(highlightId)
            
            if (id == null) {
                val errorJson = gson.toJson(mapOf("error" to "Invalid highlight ID"))
                evaluateJavascript("window.LearnLens?.onHighlightDeleteError?.($errorJson);")
                return
            }
            
            highlightRepository.deleteHighlight(contentId, id, object : TestpressCallback<Void>() {
                override fun onSuccess(response: Void?) {
                    evaluateJavascript("window.LearnLens?.onHighlightDeleteSuccess?.('$highlightId');")
                }

                override fun onException(exception: TestpressException?) {
                    val errorJson = gson.toJson(mapOf("error" to (exception?.message ?: "Unknown error")))
                    evaluateJavascript("window.LearnLens?.onHighlightDeleteError?.($errorJson);")
                }
            })
        } catch (e: Exception) {
            val errorJson = gson.toJson(mapOf("error" to (e.message ?: "Unknown error")))
            evaluateJavascript("window.LearnLens?.onHighlightDeleteError?.($errorJson);")
        }
    }

    private fun parseBookmarkId(bookmarkId: String): Long? {
        return bookmarkId.trim().toLongOrNull()
    }

    private fun parseHighlightId(highlightId: String): Long? {
        return highlightId.trim().toLongOrNull()
    }

    private fun evaluateJavascript(script: String) {
        activity.runOnUiThread {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                webView.evaluateJavascript(script, null)
            } else {
                webView.loadUrl("javascript:$script")
            }
        }
    }
}

