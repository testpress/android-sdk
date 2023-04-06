package `in`.testpress.course.repository

import `in`.testpress.course.network.CourseNetwork
import `in`.testpress.course.pagination.CourseContentsRemoteMediator
import `in`.testpress.database.TestpressDatabase
import android.content.Context
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig

class CourseContentsRepository(val context: Context, val courseId: Long = -1,val type: Int) {

    val courseNetwork = CourseNetwork(context)
    val database = TestpressDatabase.invoke(context)

    @OptIn(ExperimentalPagingApi::class)
    fun courseContentList() = Pager(
        config = PagingConfig(pageSize = 15),
        remoteMediator = CourseContentsRemoteMediator(courseNetwork,database,courseId,type)
    ) {
        database.contentLiteDao().getCourseContents(courseId, type)
    }.flow
}