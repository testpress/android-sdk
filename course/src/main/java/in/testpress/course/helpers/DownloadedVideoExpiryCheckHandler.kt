package `in`.testpress.course.helpers

import `in`.testpress.core.TestpressCallback
import `in`.testpress.core.TestpressException
import `in`.testpress.course.network.CourseNetwork
import `in`.testpress.course.network.NetworkDownloadedVideosAccessChecker
import android.content.Context
import android.util.Log

class DownloadedVideoExpiryCheckHandler(val context: Context) {
    private val courseNetwork = CourseNetwork(context)
    private val videoDownloadManager = VideoDownloadManager(context)

    fun check(
        urls: HashMap<String, List<String>>,
        handleSuccess: (NetworkDownloadedVideosAccessChecker) -> Unit
    ) {
        courseNetwork.checkDownloadedVideosExpiry(urls)
            .enqueue(object : TestpressCallback<NetworkDownloadedVideosAccessChecker>() {
                override fun onSuccess(result: NetworkDownloadedVideosAccessChecker?) {
                    if (result?.remove != null) {
                        removeVideos(result.remove)
                    }
                    videoDownloadManager.updateRefreshDate()
                    result?.let { handleSuccess(it) }
                }

                override fun onException(exception: TestpressException?) {
                    Log.d("OfflineVideoRepository", "RefreshDownloadVideos ${exception}")
                }
            })
    }

    private fun removeVideos(urls: List<String>) {
        for (url in urls) {
            val downloadTask = DownloadTask(url, context)
            downloadTask.delete()
        }
    }
}