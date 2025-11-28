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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.util.HashMap

class BookmarkRepository(private val context: Context) {
    private val courseNetwork = CourseNetwork(context)
    private val database = TestpressDatabase.invoke(context.applicationContext)
    private val bookmarkDao = database.bookmarkDao()
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun getBookmarks(
        queryParams: HashMap<String, Any>,
        callback: TestpressCallback<ApiResponse<BookmarksListApiResponse>>
    ) {
        val contentId = (queryParams["object_id"] as? Number)?.toLong()
        val bookmarkType = queryParams["bookmark_type"] as? String ?: "annotate"
        
        if (contentId != null) {
            scope.launch {
                try {
                    val cachedBookmarks = bookmarkDao.getBookmarksByContent(contentId, bookmarkType)
                    if (cachedBookmarks.isNotEmpty()) {
                        val networkBookmarks = cachedBookmarks.map { it.toNetworkBookmark() }
                        val bookmarksResponse = BookmarksListApiResponse(bookmarks = networkBookmarks)
                        val response = ApiResponse<BookmarksListApiResponse>().apply {
                            setResults(bookmarksResponse)
                        }
                        withContext(Dispatchers.Main) {
                            callback.onSuccess(response)
                        }
                    }
                } catch (e: Exception) {
                }
            }
        }
        
        courseNetwork.getBookmarks(queryParams).enqueue(object : TestpressCallback<ApiResponse<BookmarksListApiResponse>>() {
            override fun onSuccess(response: ApiResponse<BookmarksListApiResponse>) {
                scope.launch {
                    try {
                        val bookmarks = response.results?.bookmarks ?: emptyList()
                        if (contentId != null) {
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
                        }
                    } catch (e: Exception) {
                    }
                }
                callback.onSuccess(response)
            }

            override fun onException(exception: TestpressException?) {
                callback.onException(exception)
            }
        })
    }

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
                    }
                }
                callback.onSuccess(response)
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
            override fun onSuccess(response: Void) {
                scope.launch {
                    try {
                        bookmarkDao.deleteById(bookmarkId)
                    } catch (e: Exception) {
                    }
                }
                callback.onSuccess(response)
            }

            override fun onException(exception: TestpressException?) {
                callback.onException(exception)
            }
        })
    }
    
    fun getCachedBookmarks(contentId: Long, bookmarkType: String = "annotate"): List<NetworkBookmark> {
        return try {
            val cached = runBlocking(Dispatchers.IO) {
                bookmarkDao.getBookmarksByContent(contentId, bookmarkType)
            }
            cached.map { it.toNetworkBookmark() }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    private fun BookmarkEntity.toNetworkBookmark(): NetworkBookmark {
        return NetworkBookmark(
            id = this.id,
            pageNumber = this.pageNumber,
            previewText = this.previewText
        )
    }
}
