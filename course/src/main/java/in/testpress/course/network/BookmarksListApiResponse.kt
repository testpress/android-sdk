package `in`.testpress.course.network

data class BookmarksListApiResponse(
    val bookmarks: List<NetworkBookmark> = emptyList(),
    val chapterContents: List<Any> = emptyList(),
    val questions: List<Any> = emptyList(),
    val posts: List<Any> = emptyList(),
    val forumThreads: List<Any> = emptyList(),
    val userSelectedAnswers: List<Any> = emptyList(),
    val bookmarkTypesCount: Map<String, Int>? = null
)

