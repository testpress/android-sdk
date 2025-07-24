package `in`.testpress.course.ui

import `in`.testpress.core.TestpressSdk
import android.view.View
import android.view.WindowManager
import us.zoom.sdk.NewMeetingActivity

class ZoomMeetActivity: NewMeetingActivity() {
    val session = TestpressSdk.getTestpressSession(this)

    override fun setContentView(layoutResID: Int) {
        disableScreenRecording()
        super.setContentView(layoutResID)
    }

    override fun setContentView(view: View?) {
        disableScreenRecording()
        super.setContentView(view)
    }

    private fun disableScreenRecording() {
        if (session != null && session.instituteSettings.isScreenshotDisabled) {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
            )
        }
    }
}