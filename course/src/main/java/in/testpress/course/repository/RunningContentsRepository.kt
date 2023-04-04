package `in`.testpress.course.repository

import `in`.testpress.course.network.CourseNetwork
import `in`.testpress.course.pagination.RunningContentRemoteMediator
import `in`.testpress.database.TestpressDatabase
import android.content.Context
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig

class RunningContentsRepository(val context: Context, val courseId: Long = -1,val type: Int) {

    val courseNetwork = CourseNetwork(context)
    val database = TestpressDatabase.invoke(context)

    @OptIn(ExperimentalPagingApi::class)
    fun runningContentList() = Pager(
        config = PagingConfig(pageSize = 15),
        remoteMediator = RunningContentRemoteMediator(courseNetwork,database,courseId,type)
    ) {
        database.contentLiteDao().getRunningContents(courseId)
    }.flow
}