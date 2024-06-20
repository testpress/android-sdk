package `in`.testpress.course.repository

import `in`.testpress.core.TestpressCallback
import `in`.testpress.core.TestpressException
import `in`.testpress.course.network.CourseNetwork
import `in`.testpress.course.network.NetworkContent
import android.content.Context

class OfflineExamRepository(val context: Context) {

    fun get(){

    }

    fun getAll() {

    }

    fun fetch(contentId: Long) {
        val courseClient = CourseNetwork(context)
        courseClient.getNetworkContentWithId(contentId).enqueue(
            object : TestpressCallback<NetworkContent>() {
                override fun onSuccess(result: NetworkContent?) {
                    println(result)
                }

                override fun onException(exception: TestpressException?) {
                }

            }
        )
    }

}