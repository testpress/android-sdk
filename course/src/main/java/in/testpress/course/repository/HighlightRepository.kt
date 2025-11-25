package `in`.testpress.course.repository

import `in`.testpress.core.TestpressCallback
import `in`.testpress.core.TestpressException
import `in`.testpress.course.network.CourseNetwork
import `in`.testpress.course.network.NetworkHighlight
import `in`.testpress.network.RetrofitCall
import `in`.testpress.v2_4.models.ApiResponse
import android.content.Context
import java.util.HashMap

class HighlightRepository(private val context: Context) {
    private val courseNetwork = CourseNetwork(context)

    fun getHighlights(
        contentId: Long,
        callback: TestpressCallback<ApiResponse<List<NetworkHighlight>>>
    ) {
        courseNetwork.getHighlights(contentId).enqueue(callback)
    }

    fun createHighlight(
        contentId: Long,
        highlight: HashMap<String, Any>,
        callback: TestpressCallback<NetworkHighlight>
    ) {
        courseNetwork.createHighlight(contentId, highlight).enqueue(callback)
    }

    fun deleteHighlight(
        contentId: Long,
        highlightId: Long,
        callback: TestpressCallback<Void>
    ) {
        courseNetwork.deleteHighlight(contentId, highlightId).enqueue(callback)
    }
}

