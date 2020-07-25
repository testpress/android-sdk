package `in`.testpress.course.helpers

import android.os.Handler
import com.google.android.exoplayer2.offline.Download
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

class VideoDownloadMonitor() : CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO

    var downloadProgressUpdateHandler = Handler()
    private val runnable: Runnable
    var callback: Callback? = null

    init {
        runnable = Runnable {
            run()
        }
    }

    fun start() {
        downloadProgressUpdateHandler.postDelayed(runnable, 1000)
    }

    fun stop() {
        downloadProgressUpdateHandler.removeCallbacks(runnable)
        downloadProgressUpdateHandler.removeCallbacksAndMessages(null)
    }

    fun deleteVideo(download: Download) {
        launch {
            callback?.onDelete(download)
        }
    }

    fun updateVideoProgress(download: Download) {
        launch {
            callback?.onUpdate(download)
        }
    }

    private fun run() {
        launch {
            withContext(Dispatchers.IO) {
                callback?.onCurrentDownloadsUpdate()
                downloadProgressUpdateHandler.removeCallbacks(runnable)
                downloadProgressUpdateHandler.removeCallbacksAndMessages(null)
            }
        }
    }

    fun isRunning(): Boolean {
        return downloadProgressUpdateHandler.hasCallbacks(runnable)
    }

    interface Callback {
        suspend fun onCurrentDownloadsUpdate()
        suspend fun onUpdate(download: Download)
        suspend fun onDelete(download: Download)
    }
}
