package `in`.testpress.course.repository

import `in`.testpress.core.TestpressCallback
import `in`.testpress.course.network.BookmarksListApiResponse
import `in`.testpress.course.network.CourseNetwork
import `in`.testpress.course.network.NetworkBookmark
import `in`.testpress.v2_4.models.ApiResponse
import android.content.Context
import java.util.HashMap

class BookmarkRepository(private val context: Context) {
    private val courseNetwork = CourseNetwork(context)

    fun getBookmarks(
        queryParams: HashMap<String, Any>,
        callback: TestpressCallback<ApiResponse<BookmarksListApiResponse>>
    ) {
        courseNetwork.getBookmarks(queryParams).enqueue(callback)
    }

    fun createBookmark(
        bookmark: HashMap<String, Any>,
        callback: TestpressCallback<NetworkBookmark>
    ) {
        courseNetwork.createBookmark(bookmark).enqueue(callback)
    }

    fun deleteBookmark(
        bookmarkId: Long,
        callback: TestpressCallback<Void>
    ) {
        courseNetwork.deleteBookmark(bookmarkId).enqueue(callback)
    }
}
