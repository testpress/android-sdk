package `in`.testpress.course.ui

import `in`.testpress.core.TestpressSdk
import android.view.View
import android.view.WindowManager
import us.zoom.sdk.MeetingActivity

class ZoomMeetActivity: MeetingActivity() {
    val session = TestpressSdk.getTestpressSession(this)

    override fun setContentView(layoutResID: Int) {
        if (session?.instituteSettings?.isScreenshotDisabled == true) {
            disableScreenRecording()
        }
        super.setContentView(layoutResID)
    }

    override fun setContentView(view: View?) {
        if (session?.instituteSettings?.isScreenshotDisabled == true) {
            disableScreenRecording()
        }
        super.setContentView(view)
    }

    private fun disableScreenRecording() {
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
    }
}