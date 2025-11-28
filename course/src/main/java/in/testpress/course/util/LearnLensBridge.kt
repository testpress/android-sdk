package `in`.testpress.course.util

import android.app.Activity
import android.os.Build
import android.webkit.JavascriptInterface
import android.webkit.WebView
import `in`.testpress.core.TestpressCallback
import `in`.testpress.core.TestpressException
import `in`.testpress.course.repository.BookmarkRepository
import `in`.testpress.course.network.NetworkBookmark
import `in`.testpress.util.BaseJavaScriptInterface
import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import android.util.Log

class LearnLensBridge(
    private val activity: Activity,
    private val webView: WebView,
    private val contentId: Long
) : BaseJavaScriptInterface(activity) {

    private val bookmarkRepository = BookmarkRepository(activity)
    private val gson = GsonBuilder()
        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        .create()

    @JavascriptInterface
    fun onBookmarkCreate(bookmarkJson: String) {
        try {
            if (bookmarkJson.isBlank()) {
                throw IllegalArgumentException("Bookmark JSON cannot be empty")
            }

            val trimmedJson = bookmarkJson.trim()
            val bookmarkMap: Map<*, *> = when {
                trimmedJson.matches(Regex("^-?\\d+$")) -> {
                    val pageNumber = trimmedJson.toIntOrNull()
                    if (pageNumber != null) {
                        mapOf("pageNumber" to pageNumber)
                    } else {
                        throw IllegalArgumentException("Invalid bookmark format: expected JSON object or page number")
                    }
                }
                trimmedJson.startsWith("{") && trimmedJson.endsWith("}") -> {
                    val parsed = gson.fromJson(bookmarkJson, Map::class.java) as? Map<*, *>
                    parsed ?: throw IllegalArgumentException("Failed to parse bookmark JSON as object")
                }
                else -> {
                    throw IllegalArgumentException("Bookmark JSON must be a valid JSON object or page number")
                }
            }

            val requestBody = hashMapOf<String, Any>().apply {
                put("content_type", "chapter_content")
                put("object_id", contentId)
                put("bookmark_type", "annotate")
                put("category", "attachment")
                
                bookmarkMap.forEach { (key, value) ->
                    if (value != null) {
                        val keyStr = key.toString()
                        when (keyStr) {
                            "page", "pageNumber" -> put("page_number", (value as? Number)?.toInt() ?: value)
                            "previewText", "preview_text" -> put("preview_text", value.toString())
                            "category" -> put("category", value)
                            else -> put(keyStr, value)
                        }
                    }
                }
                
                val pageNumber = get("page_number") as? Int
                if (pageNumber == null || pageNumber <= 0) {
                    throw IllegalArgumentException("Page number must be provided and greater than 0")
                }
                
                if (!containsKey("preview_text") || (get("preview_text") as? String).isNullOrBlank()) {
                    val pageNum = get("page_number") as? Int ?: pageNumber
                    put("preview_text", "Page $pageNum")
                }
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
                    
                    evaluateJavascript("""
                        (function() {
                            var bookmark = $resultJson;
                            if (window.LearnLens && window.LearnLens.onBookmarkCreateSuccess) {
                                window.LearnLens.onBookmarkCreateSuccess(bookmark);
                            }
                        })();
                    """.trimIndent())
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

    private fun parseBookmarkId(bookmarkId: String): Long? {
        return when {
            bookmarkId.contains("\"") || bookmarkId.trim().startsWith("{") -> {
                try {
                    val trimmed = bookmarkId.trim()
                    val jsonStr = if (trimmed.startsWith("{")) trimmed else bookmarkId
                    val bookmarkObj = gson.fromJson(jsonStr, Map::class.java) as? Map<*, *>
                    if (bookmarkObj != null) {
                        (bookmarkObj["id"] as? Number)?.toLong()
                            ?: (bookmarkObj["id"] as? String)?.toLongOrNull()
                            ?: bookmarkId.toLongOrNull()
                    } else {
                        bookmarkId.toLongOrNull()
                    }
                } catch (e: Exception) {
                    Log.e("LearnLensBridge", "Failed to parse bookmark ID from JSON: $bookmarkId", e)
                    bookmarkId.toLongOrNull()
                }
            }
            else -> bookmarkId.toLongOrNull()
        }
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