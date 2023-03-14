package `in`.testpress.course.util

import android.content.Context
import `in`.testpress.course.network.CourseNetwork
import `in`.testpress.database.entities.RunningContentEntity
import `in`.testpress.v2_4.models.ApiResponse
import java.util.HashMap

class FakeAPIClient(context: Context, val nextPage: Boolean=false):CourseNetwork(context) {

    var failureMessage: String? = null

    override suspend fun getRunningContents(
        courseId: Long,
        arguments: HashMap<String, Any>
    ): ApiResponse<List<RunningContentEntity>> {
        if (failureMessage != null) {
            return throw java.lang.Exception(failureMessage)
        }
        val response = ApiResponse<List<RunningContentEntity>>()
        response.results = getRunningContentList()
        response.next = if (nextPage) "http://localhost:800" else null
        return response

    }

    private fun getRunningContentList():List<RunningContentEntity>{
        return listOf(
            RunningContentEntity(1),
            RunningContentEntity(2),
            RunningContentEntity(3)
        )
    }

}