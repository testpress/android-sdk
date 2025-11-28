package `in`.testpress.course.network

/**
 * API v3 bookmarks response model.
 * 
 * This matches the structure returned by `/api/v3/bookmarks/` endpoint.
 * The API returns all these fields regardless of bookmark type, but for PDF bookmarks
 * (bookmark_type: "annotate"), only the [bookmarks] field is used.
 * 
 * Note: The older API v2.4 uses [in.testpress.v2_4.models.BookmarksListResponse] (Java)
 * which has a different structure. This Kotlin model is specifically for API v3.
 */
data class BookmarksListApiResponse(
    val bookmarks: List<NetworkBookmark> = emptyList(),
    // Fields below are returned by API v3 but not used for PDF bookmarks
    val chapterContents: List<Any> = emptyList(),
    val questions: List<Any> = emptyList(),
    val posts: List<Any> = emptyList(),
    val forumThreads: List<Any> = emptyList(),
    val userSelectedAnswers: List<Any> = emptyList(),
    val bookmarkTypesCount: Map<String, Int>? = null
)