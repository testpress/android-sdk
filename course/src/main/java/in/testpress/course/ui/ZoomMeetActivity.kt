package `in`.testpress.course.ui

import `in`.testpress.R
import `in`.testpress.core.TestpressCallback
import `in`.testpress.core.TestpressException
import `in`.testpress.core.TestpressSdk
import `in`.testpress.core.TestpressUserDetails
import `in`.testpress.course.util.WatermarkOverlay
import `in`.testpress.models.ProfileDetails
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.WindowManager
import androidx.core.content.ContextCompat
import us.zoom.sdk.NewMeetingActivity

class ZoomMeetActivity: NewMeetingActivity() {
    val session = TestpressSdk.getTestpressSession(this)
    private var watermarkOverlay: WatermarkOverlay? = null
    private var globalLayoutListener: ViewTreeObserver.OnGlobalLayoutListener? = null

    override fun setContentView(layoutResID: Int) {
        disableScreenRecording()
        super.setContentView(layoutResID)
    }

    override fun setContentView(view: View?) {
        disableScreenRecording()
        super.setContentView(view)
    }

    override fun onResume() {
        super.onResume()
        addWatermarkOverlay()
    }

    override fun onDestroy() {
        super.onDestroy()
        TestpressUserDetails.getInstance().cancel()
        watermarkOverlay?.let { overlay ->
            globalLayoutListener?.let { listener ->
                overlay.viewTreeObserver.removeOnGlobalLayoutListener(listener)
            }
        }
    }

    private fun disableScreenRecording() {
        if (session != null && session.instituteSettings.isScreenshotDisabled) {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
            )
        }
    }

    private fun addWatermarkOverlay() {
        val settings = session?.instituteSettings ?: return
        val watermarkType = settings.videoWatermarkType

        if (watermarkType == null || watermarkType == WatermarkOverlay.TYPE_HIDDEN) {
            return
        }

        val profileDetails = TestpressUserDetails.getInstance().profileDetails
        if (profileDetails == null) {
            TestpressUserDetails.getInstance().load(this, object : TestpressCallback<ProfileDetails>() {
                override fun onSuccess(details: ProfileDetails) {
                    if (!isFinishing && !isDestroyed) {
                        displayWatermark(details, watermarkType, settings.videoWatermarkPosition)
                    }
                }

                override fun onException(exception: TestpressException) {
                    // Do nothing
                }
            })
        } else {
            displayWatermark(profileDetails, watermarkType, settings.videoWatermarkPosition)
        }
    }

    private fun displayWatermark(profileDetails: ProfileDetails, type: String, position: String?) {
        val decorView = window.decorView as? ViewGroup ?: return

        if (watermarkOverlay != null) {
            decorView.removeView(watermarkOverlay)
        }

        val primaryId = profileDetails.username ?: ""
        val secondaryId = profileDetails.email ?: profileDetails.phone ?: ""
        val watermarkText = if (secondaryId.isNotEmpty()) "$primaryId | $secondaryId" else primaryId

        val newWatermarkOverlay = WatermarkOverlay(this).apply {
            isClickable = false
            isFocusable = false
            setWatermark(watermarkText)
            val watermarkColor = ContextCompat.getColor(this@ZoomMeetActivity, R.color.testpress_video_watermark_color)
            setTextColor(watermarkColor)
            setTextSize(40f)
            if (type == WatermarkOverlay.TYPE_DYNAMIC) {
                setDynamicWatermark()
            } else {
                setStaticWatermark(position ?: WatermarkOverlay.POSITION_TOP_RIGHT)
            }
        }
        watermarkOverlay = newWatermarkOverlay

        decorView.addView(
            newWatermarkOverlay,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )

        newWatermarkOverlay.bringToFront()

        globalLayoutListener = ViewTreeObserver.OnGlobalLayoutListener {
            val parent = newWatermarkOverlay.parent as? ViewGroup
            if (parent != null && parent.indexOfChild(newWatermarkOverlay) < parent.childCount - 1) {
                newWatermarkOverlay.bringToFront()
            }
        }
        newWatermarkOverlay.viewTreeObserver.addOnGlobalLayoutListener(globalLayoutListener)
    }
}