package `in`.testpress.course.ui

import `in`.testpress.core.TestpressSdk
import android.os.Bundle
import android.view.WindowManager
import us.zoom.sdk.MeetingActivity

class ZoomMeetActivity: MeetingActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val session = TestpressSdk.getTestpressSession(this)
        if (session != null && session.instituteSettings.isScreenshotDisabled) {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
            )
        }
    }
}