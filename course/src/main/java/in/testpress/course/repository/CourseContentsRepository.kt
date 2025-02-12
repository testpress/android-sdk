package `in`.testpress.course.repository

import `in`.testpress.course.network.CourseNetwork
import `in`.testpress.course.pagination.CourseContentsRemoteMediator
import `in`.testpress.database.TestpressDatabase
import android.content.Context
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import `in`.testpress.database.entities.CourseContentType

class CourseContentsRepository(val context: Context, val courseId: Long = -1,val type: Int) {

    val courseNetwork = CourseNetwork(context)
    val database = TestpressDatabase.invoke(context)

    @OptIn(ExperimentalPagingApi::class)
    fun courseContentList() = Pager(
        config = PagingConfig(pageSize = 10, initialLoadSize = 20),
        remoteMediator = CourseContentsRemoteMediator(courseNetwork,database,courseId,type)
    ) {
        if (type == CourseContentType.RUNNING_CONTENT.ordinal){
            database.contentLiteDao().getRunningContents(courseId, type)
        } else {
            database.contentLiteDao().getUpcomingContents(courseId,type)
        }
    }.flow
}