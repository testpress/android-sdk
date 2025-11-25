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
import com.google.gson.Gson
import com.google.gson.GsonBuilder

class LearnLensBridge(
    private val activity: Activity,
    private val webView: WebView,
    private val contentId: Long
) : BaseJavaScriptInterface(activity) {

    private val highlightRepository = HighlightRepository(activity)
    private val bookmarkRepository = BookmarkRepository(activity)
    // Use snake_case naming to match API and LearnLens expectations
    private val gson = GsonBuilder()
        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        .create()
    
    @JavascriptInterface
    fun log(message: String) {
        android.util.Log.d("LearnLensJS", message)
    }

    @JavascriptInterface
    fun onHighlightCreate(highlightJson: String) {
        try {
            // Validate and parse JSON input
            if (highlightJson.isBlank()) {
                throw IllegalArgumentException("Highlight JSON cannot be empty")
            }

            // Check if input is a valid JSON object (not a primitive)
            val trimmedJson = highlightJson.trim()
            if (!trimmedJson.startsWith("{") || !trimmedJson.endsWith("}")) {
                android.util.Log.e("LearnLensBridge", "Invalid highlight JSON format: $highlightJson")
                throw IllegalArgumentException("Highlight JSON must be a valid JSON object")
            }

            val highlightMap = gson.fromJson(highlightJson, Map::class.java) as? Map<*, *>
                ?: throw IllegalArgumentException("Failed to parse highlight JSON as object")

            val requestBody = hashMapOf<String, Any>().apply {
                highlightMap.forEach { (key, value) ->
                    if (value != null) {
                        val keyStr = key.toString()
                        // Normalize field names for API
                        when (keyStr) {
                            "page" -> put("pageNumber", (value as? Number)?.toInt() ?: value)
                            "pageNumber" -> put("pageNumber", (value as? Number)?.toInt() ?: value)
                            "bbox" -> {
                                // Handle bbox structure - convert to position if needed
                                when (value) {
                                    is Map<*, *> -> {
                                        val segments = (value["segments"] as? List<*>)?.firstOrNull()
                                        if (segments is Map<*, *>) {
                                            val x1 = (segments["x1"] as? Number)?.toDouble() ?: 0.0
                                            val y1 = (segments["y1"] as? Number)?.toDouble() ?: 0.0
                                            val x2 = (segments["x2"] as? Number)?.toDouble() ?: 0.0
                                            val y2 = (segments["y2"] as? Number)?.toDouble() ?: 0.0
                                            put("position", listOf(x1, y1, x2, y2))
                                        } else {
                                            put("position", value)
                                        }
                                    }
                                    else -> put("position", value)
                                }
                            }
                            else -> put(keyStr, value)
                        }
                    }
                }
            }

            highlightRepository.createHighlight(contentId, requestBody, object : TestpressCallback<NetworkHighlight>() {
                override fun onSuccess(response: NetworkHighlight) {
                    val resultJson = gson.toJson(response)
                    evaluateJavascript("window.LearnLens?.onHighlightCreateSuccess?.($resultJson);")
                }

                override fun onException(exception: TestpressException?) {
                    android.util.Log.e("LearnLensBridge", "Failed to create highlight: ${exception?.message}", exception)
                    val errorJson = gson.toJson(mapOf("error" to (exception?.message ?: "Unknown error")))
                    evaluateJavascript("window.LearnLens?.onHighlightCreateError?.($errorJson);")
                }
            })
        } catch (e: Exception) {
            android.util.Log.e("LearnLensBridge", "Error creating highlight: ${e.message}", e)
            android.util.Log.e("LearnLensBridge", "Received highlightJson: $highlightJson")
            val errorJson = gson.toJson(mapOf("error" to (e.message ?: "Unknown error")))
            evaluateJavascript("window.LearnLens?.onHighlightCreateError?.($errorJson);")
        }
    }

    @JavascriptInterface
    fun onHighlightDelete(highlightId: String) {
        try {
            // Handle both string ID and numeric ID from LearnLens
            val id = when {
                highlightId.contains("\"") -> {
                    try {
                        val highlightObj = gson.fromJson(highlightId, Map::class.java) as Map<*, *>
                        (highlightObj["id"] as? Number)?.toLong() ?: highlightId.toLongOrNull()
                    } catch (e: Exception) {
                        highlightId.toLongOrNull()
                    }
                }
                else -> highlightId.toLongOrNull()
            }
            
            if (id == null) {
                android.util.Log.e("LearnLensBridge", "Invalid highlight ID: $highlightId")
                val errorJson = gson.toJson(mapOf("error" to "Invalid highlight ID"))
                evaluateJavascript("window.LearnLens?.onHighlightDeleteError?.($errorJson);")
                return
            }
            
            highlightRepository.deleteHighlight(contentId, id, object : TestpressCallback<Void>() {
                override fun onSuccess(response: Void?) {
                    evaluateJavascript("window.LearnLens?.onHighlightDeleteSuccess?.('$highlightId');")
                }

                override fun onException(exception: TestpressException?) {
                    android.util.Log.e("LearnLensBridge", "Failed to delete highlight $id: ${exception?.message}", exception)
                    val errorJson = gson.toJson(mapOf("error" to (exception?.message ?: "Unknown error")))
                    evaluateJavascript("window.LearnLens?.onHighlightDeleteError?.($errorJson);")
                }
            })
        } catch (e: Exception) {
            android.util.Log.e("LearnLensBridge", "Error deleting highlight: ${e.message}", e)
            val errorJson = gson.toJson(mapOf("error" to (e.message ?: "Unknown error")))
            evaluateJavascript("window.LearnLens?.onHighlightDeleteError?.($errorJson);")
        }
    }

    @JavascriptInterface
    fun onBookmarkCreate(bookmarkJson: String) {
        try {
            // Validate and parse JSON input
            if (bookmarkJson.isBlank()) {
                throw IllegalArgumentException("Bookmark JSON cannot be empty")
            }

            android.util.Log.d("LearnLensBridge", "Received bookmark JSON: $bookmarkJson")
            val trimmedJson = bookmarkJson.trim()
            val bookmarkMap: Map<*, *> = when {
                // Handle case where LearnLens passes just a number (page number)
                trimmedJson.matches(Regex("^-?\\d+$")) -> {
                    val pageNumber = trimmedJson.toIntOrNull()
                    if (pageNumber != null) {
                        android.util.Log.w("LearnLensBridge", "Received page number instead of bookmark object: $pageNumber. Creating minimal bookmark.")
                        mapOf("pageNumber" to pageNumber)
                    } else {
                        throw IllegalArgumentException("Invalid bookmark format: expected JSON object or page number")
                    }
                }
                // Check if input is a valid JSON object
                trimmedJson.startsWith("{") && trimmedJson.endsWith("}") -> {
                    val parsed = gson.fromJson(bookmarkJson, Map::class.java) as? Map<*, *>
                    android.util.Log.d("LearnLensBridge", "Parsed bookmark object - keys: ${parsed?.keys}, previewText: ${parsed?.get("previewText")}, preview_text: ${parsed?.get("preview_text")}")
                    parsed ?: throw IllegalArgumentException("Failed to parse bookmark JSON as object")
                }
                else -> {
                    android.util.Log.e("LearnLensBridge", "Invalid bookmark JSON format: $bookmarkJson")
                    throw IllegalArgumentException("Bookmark JSON must be a valid JSON object or page number")
                }
            }

            val requestBody = hashMapOf<String, Any>().apply {
                // Add required fields for API to match query params (using snake_case as per web code)
                put("content_type", "chapter_content")
                put("object_id", contentId)
                put("bookmark_type", "annotate")
                // Category must be 'attachment' for chapter content bookmarks (from web code reference)
                put("category", "attachment")
                
                // Add fields from LearnLens (page, pageNumber, previewText, etc.)
                // Convert to snake_case as required by API
                bookmarkMap.forEach { (key, value) ->
                    if (value != null) {
                        val keyStr = key.toString()
                        // Normalize and convert to snake_case for API
                        when (keyStr) {
                            "page", "pageNumber" -> put("page_number", (value as? Number)?.toInt() ?: value)
                            "previewText", "preview_text" -> put("preview_text", value.toString())
                            "category" -> {
                                // Allow LearnLens to override category if provided, but default to "attachment"
                                put("category", value)
                            }
                            else -> put(keyStr, value)
                        }
                    }
                }
                
                // Ensure page_number is present and valid
                val pageNumber = get("page_number") as? Int
                if (pageNumber == null || pageNumber <= 0) {
                    throw IllegalArgumentException("Page number must be provided and greater than 0")
                }
                
                // Ensure preview_text is present (API may require it)
                // If preview_text is empty or missing, use a default like "Page X"
                if (!containsKey("preview_text") || (get("preview_text") as? String).isNullOrBlank()) {
                    val pageNum = get("page_number") as? Int ?: pageNumber
                    put("preview_text", "Page $pageNum")
                    android.util.Log.d("LearnLensBridge", "No preview text provided, using default: Page $pageNum")
                }
            }

            bookmarkRepository.createBookmark(requestBody, object : TestpressCallback<NetworkBookmark>() {
                override fun onSuccess(response: NetworkBookmark) {
                    // Verify ID is present
                    if (response.id == null) {
                        android.util.Log.e("LearnLensBridge", "Bookmark created but ID is null in response")
                        val errorJson = gson.toJson(mapOf("error" to "Bookmark created but ID is missing"))
                        evaluateJavascript("window.LearnLens?.onBookmarkCreateError?.($errorJson);")
                        return
                    }
                    
                    // Serialize with snake_case to match API format
                    // LearnLens expects the bookmark object with id, page_number, and preview_text
                    // Ensure ID is an integer (not Double) for LearnLens to recognize it
                    val bookmarkId = response.id!!.toLong()
                    val pageNumber = response.pageNumber ?: 0
                    val previewText = response.previewText ?: ""
                    
                    // Escape preview_text for JSON
                    val escapedPreviewText = previewText
                        .replace("\\", "\\\\")
                        .replace("\"", "\\\"")
                        .replace("\n", "\\n")
                        .replace("\r", "\\r")
                    
                    // Manually construct JSON to ensure ID is an integer, not a Double
                    val resultJson = """{"id":$bookmarkId,"page_number":$pageNumber,"preview_text":"$escapedPreviewText"}"""
                    
                    android.util.Log.d("LearnLensBridge", "Bookmark created successfully: $resultJson")
                    android.util.Log.d("LearnLensBridge", "Bookmark ID: $bookmarkId (Long), page_number: $pageNumber, preview_text: $previewText")
                    
                    // Pass the bookmark object to LearnLens's success callback
                    // The HTML template sets up onBookmarkCreateSuccess handler
                    evaluateJavascript("""
                        (function() {
                            var bookmark = $resultJson;
                            console.log('LearnLensBridge: Sending bookmark to LearnLens', bookmark);
                            
                            // Call the success handler set up by the HTML template
                            if (window.LearnLens && window.LearnLens.onBookmarkCreateSuccess) {
                                window.LearnLens.onBookmarkCreateSuccess(bookmark);
                            } else {
                                console.warn('LearnLensBridge: onBookmarkCreateSuccess handler not found');
                            }
                        })();
                    """.trimIndent())
                }

                override fun onException(exception: TestpressException?) {
                    val errorMessage = exception?.message ?: "Unknown error"
                    android.util.Log.e("LearnLensBridge", "Failed to create bookmark: $errorMessage", exception)
                    
                    // Log request body for debugging
                    android.util.Log.e("LearnLensBridge", "Request body: ${gson.toJson(requestBody)}")
                    
                    // Try to get error details from response
                    val response = exception?.response
                    if (response != null && response.errorBody() != null) {
                        try {
                            val errorBody = response.errorBody()?.string()
                            android.util.Log.e("LearnLensBridge", "API error response: $errorBody")
                        } catch (e: Exception) {
                            android.util.Log.e("LearnLensBridge", "Failed to read error body", e)
                        }
                    }
                    
                    val errorJson = gson.toJson(mapOf("error" to errorMessage))
                    evaluateJavascript("window.LearnLens?.onBookmarkCreateError?.($errorJson);")
                }
            })
        } catch (e: Exception) {
            android.util.Log.e("LearnLensBridge", "Error creating bookmark: ${e.message}", e)
            android.util.Log.e("LearnLensBridge", "Received bookmarkJson: $bookmarkJson")
            val errorJson = gson.toJson(mapOf("error" to (e.message ?: "Unknown error")))
            evaluateJavascript("window.LearnLens?.onBookmarkCreateError?.($errorJson);")
        }
    }

    @JavascriptInterface
    fun onBookmarkDelete(bookmarkId: String) {
        try {
            android.util.Log.d("LearnLensBridge", "onBookmarkDelete called with: $bookmarkId")
            
            // Handle both string ID and numeric ID from LearnLens
            val id = when {
                bookmarkId.contains("\"") || bookmarkId.trim().startsWith("{") -> {
                    // If it's a JSON string, try to extract ID from object
                    try {
                        val trimmed = bookmarkId.trim()
                        val jsonStr = if (trimmed.startsWith("{")) trimmed else bookmarkId
                        val bookmarkObj = gson.fromJson(jsonStr, Map::class.java) as? Map<*, *>
                        if (bookmarkObj != null) {
                            // Try both "id" and snake_case "id" (should be same, but be safe)
                            val extractedId = (bookmarkObj["id"] as? Number)?.toLong()
                                ?: (bookmarkObj["id"] as? String)?.toLongOrNull()
                            if (extractedId != null) {
                                android.util.Log.d("LearnLensBridge", "Extracted ID from bookmark object: $extractedId")
                                extractedId
                            } else {
                                android.util.Log.w("LearnLensBridge", "Could not extract ID from bookmark object: $bookmarkObj")
                                bookmarkId.toLongOrNull()
                            }
                        } else {
                            bookmarkId.toLongOrNull()
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("LearnLensBridge", "Error parsing bookmark JSON: ${e.message}", e)
                        bookmarkId.toLongOrNull()
                    }
                }
                else -> {
                    // Try to parse as number directly
                    bookmarkId.toLongOrNull() ?: run {
                        android.util.Log.w("LearnLensBridge", "Could not parse bookmark ID as number: $bookmarkId")
                        null
                    }
                }
            }
            
            if (id == null) {
                android.util.Log.e("LearnLensBridge", "Invalid bookmark ID: $bookmarkId")
                val errorJson = gson.toJson(mapOf("error" to "Invalid bookmark ID"))
                evaluateJavascript("window.LearnLens?.onBookmarkDeleteError?.($errorJson);")
                return
            }
            
            android.util.Log.d("LearnLensBridge", "Deleting bookmark with ID: $id")
            
            bookmarkRepository.deleteBookmark(id, object : TestpressCallback<Void>() {
                override fun onSuccess(response: Void?) {
                    evaluateJavascript("window.LearnLens?.onBookmarkDeleteSuccess?.('$bookmarkId');")
                }

                override fun onException(exception: TestpressException?) {
                    android.util.Log.e("LearnLensBridge", "Failed to delete bookmark $id: ${exception?.message}", exception)
                    val errorJson = gson.toJson(mapOf("error" to (exception?.message ?: "Unknown error")))
                    evaluateJavascript("window.LearnLens?.onBookmarkDeleteError?.($errorJson);")
                }
            })
        } catch (e: Exception) {
            android.util.Log.e("LearnLensBridge", "Error deleting bookmark: ${e.message}", e)
            val errorJson = gson.toJson(mapOf("error" to (e.message ?: "Unknown error")))
            evaluateJavascript("window.LearnLens?.onBookmarkDeleteError?.($errorJson);")
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

