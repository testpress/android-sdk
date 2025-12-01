package `in`.testpress.course.repository

import `in`.testpress.core.TestpressCallback
import `in`.testpress.core.TestpressException
import `in`.testpress.course.network.BookmarksListApiResponse
import `in`.testpress.course.network.CourseNetwork
import `in`.testpress.course.network.NetworkBookmark
import `in`.testpress.database.TestpressDatabase
import `in`.testpress.database.entities.BookmarkEntity
import `in`.testpress.v2_4.models.ApiResponse
import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.HashMap

class BookmarkRepository(private val context: Context) {
    private val courseNetwork = CourseNetwork(context)
    private val database = TestpressDatabase.invoke(context.applicationContext)
    private val bookmarkDao = database.bookmarkDao()
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun createBookmark(
        bookmark: HashMap<String, Any>,
        callback: TestpressCallback<NetworkBookmark>
    ) {
        courseNetwork.createBookmark(bookmark).enqueue(object : TestpressCallback<NetworkBookmark>() {
            override fun onSuccess(response: NetworkBookmark) {
                scope.launch {
                    try {
                        response.id?.let { id ->
                            val contentId = (bookmark["object_id"] as? Number)?.toLong()
                            val bookmarkType = bookmark["bookmark_type"] as? String ?: "annotate"
                            if (contentId != null) {
                                val entity = BookmarkEntity(
                                    id = id,
                                    contentId = contentId,
                                    bookmarkType = bookmarkType,
                                    pageNumber = response.pageNumber,
                                    previewText = response.previewText
                                )
                                bookmarkDao.insert(entity)
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(
                            "BookmarkRepository",
                            "Failed to cache created bookmark in database",
                            e
                        )
                    }
                    withContext(Dispatchers.Main) {
                        callback.onSuccess(response)
                    }
                }
            }

            override fun onException(exception: TestpressException?) {
                callback.onException(exception)
            }
        })
    }

    fun deleteBookmark(
        bookmarkId: Long,
        callback: TestpressCallback<Void>
    ) {
        courseNetwork.deleteBookmark(bookmarkId).enqueue(object : TestpressCallback<Void>() {
            override fun onSuccess(response: Void?) {
                scope.launch {
                    try {
                        bookmarkDao.deleteById(bookmarkId)
                    } catch (e: Exception) {
                        Log.e(
                            "BookmarkRepository",
                            "Failed to delete bookmark with id=$bookmarkId from database",
                            e
                        )
                    }
                    withContext(Dispatchers.Main) {
                        callback.onSuccess(response)
                    }
                }
            }

            override fun onException(exception: TestpressException?) {
                callback.onException(exception)
            }
        })
    }
    
    suspend fun getCachedBookmarks(
        contentId: Long,
        bookmarkType: String = "annotate"
    ): List<NetworkBookmark> {
        return withContext(Dispatchers.IO) {
            try {
                bookmarkDao
                    .getBookmarksByContent(contentId, bookmarkType)
                    .map { it.toNetworkBookmark() }
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
    
    fun fetchBookmarks(
        contentId: Long,
        onSuccess: (List<NetworkBookmark>) -> Unit,
        onException: ((TestpressException?) -> Unit)? = null
    ) {
        val bookmarkType = "annotate"
        val queryParams = createBookmarkQueryParams(contentId)
        
        scope.launch {
            try {
                val cachedBookmarks = bookmarkDao.getBookmarksByContent(contentId, bookmarkType)
                if (cachedBookmarks.isNotEmpty()) {
                    val networkBookmarks = cachedBookmarks.map { it.toNetworkBookmark() }
                    withContext(Dispatchers.Main) {
                        onSuccess(networkBookmarks)
                    }
                }
            } catch (e: Exception) {
                Log.e(
                    "BookmarkRepository",
                    "Failed to load cached bookmarks for contentId=$contentId, type=$bookmarkType",
                    e
                )
            }
        }
        
        courseNetwork.getBookmarks(queryParams).enqueue(object : TestpressCallback<ApiResponse<BookmarksListApiResponse>>() {
            override fun onSuccess(response: ApiResponse<BookmarksListApiResponse>) {
                scope.launch {
                    try {
                        val bookmarks = response.results?.bookmarks ?: emptyList()
                        bookmarkDao.deleteByContent(contentId, bookmarkType)
                        val entities = bookmarks.mapNotNull { bookmark ->
                            bookmark.id?.let { id ->
                                BookmarkEntity(
                                    id = id,
                                    contentId = contentId,
                                    bookmarkType = bookmarkType,
                                    pageNumber = bookmark.pageNumber,
                                    previewText = bookmark.previewText
                                )
                            }
                        }
                        if (entities.isNotEmpty()) {
                            bookmarkDao.insertAll(entities)
                        }
                    } catch (e: Exception) {
                        Log.e(
                            "BookmarkRepository",
                            "Failed to update local cache after network bookmarks fetch for contentId=$contentId, type=$bookmarkType",
                            e
                        )
                    }
                }
                onSuccess(response.results?.bookmarks ?: emptyList())
            }

            override fun onException(exception: TestpressException?) {
                onException?.invoke(exception)
            }
        })
    }
    
    private fun createBookmarkQueryParams(contentId: Long) = hashMapOf<String, Any>(
        "content_type" to "chapter_content",
        "object_id" to contentId,
        "bookmark_type" to "annotate"
    )
    
    private fun BookmarkEntity.toNetworkBookmark(): NetworkBookmark {
        return NetworkBookmark(
            id = this.id,
            pageNumber = this.pageNumber,
            previewText = this.previewText
        )
    }
}
