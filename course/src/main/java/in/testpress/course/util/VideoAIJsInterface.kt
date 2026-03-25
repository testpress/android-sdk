package `in`.testpress.course.util

import android.app.Activity
import android.os.Build
import android.webkit.JavascriptInterface
import `in`.testpress.util.BaseJavaScriptInterface

class VideoAIJsInterface(
    private val activity: Activity,
    private val host: Host
) : BaseJavaScriptInterface(activity) {

    interface Host {
        fun onSeek(seconds: Double)
        fun onRequestClose()
    }

    @JavascriptInterface
    fun onSeek(seconds: String) {
        val t = seconds.trim().toDoubleOrNull() ?: return
        if (t < 0) return

        runOnUiThread {
            host.onSeek(t)
        }
    }

    @JavascriptInterface
    fun requestClose() {
        runOnUiThread {
            host.onRequestClose()
        }
    }

    private fun runOnUiThread(block: () -> Unit) {
        activity.runOnUiThread {
            if (!activity.isFinishing && (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1 || !activity.isDestroyed)) {
                block()
            }
        }
    }
}

