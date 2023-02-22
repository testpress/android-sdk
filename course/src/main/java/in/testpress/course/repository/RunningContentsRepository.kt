package `in`.testpress.course.repository

import `in`.testpress.course.network.CourseNetwork
import `in`.testpress.course.pagination.RunningContentRemoteMediator
import `in`.testpress.database.TestpressDatabase
import `in`.testpress.database.entities.RunningContentEntity
import `in`.testpress.v2_4.models.ApiResponse
import android.content.Context
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig

class RunningContentsRepository(val context: Context, val courseId: Long = -1) {

    val courseNetwork = CourseNetwork(context)
    val db = TestpressDatabase.invoke(context)

    suspend fun fetchRunningContents(page: Int = 1): ApiResponse<List<RunningContentEntity>> {
        val queryParams = hashMapOf<String, Any>("page" to page)
        return courseNetwork.getRunningContents(courseId, queryParams)
    }

    @OptIn(ExperimentalPagingApi::class)
    fun runningContentList() = Pager(
        config = PagingConfig(pageSize = 15),
        remoteMediator = RunningContentRemoteMediator(this)
    ) {
        db.runningContentDao().getAll(courseId)
    }.flow
}