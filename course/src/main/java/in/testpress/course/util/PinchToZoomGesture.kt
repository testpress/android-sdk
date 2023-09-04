package `in`.testpress.course.util

import `in`.testpress.course.R
import `in`.testpress.course.util.extension.animateHideTextView
import android.content.Context
import android.os.*
import android.util.Log
import android.view.ScaleGestureDetector
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener
import android.view.View
import android.widget.TextView
import com.github.vkay94.dtpv.DoubleTapPlayerView
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import java.text.DecimalFormat

class PinchToZoomGesture(
    exoPlayerUtil: ExoPlayerUtil,
) : SimpleOnScaleGestureListener() {

    var scaleFactor = 1.0f
    var isDragEnabled = false
    private val zoomModeText: TextView = exoPlayerUtil.exoPlayerMainFrame.findViewById(R.id.zoom_mode_text)
    private val zoomSizeText: TextView = exoPlayerUtil.exoPlayerMainFrame.findViewById(R.id.zoom_size_text)
    private val playerView: DoubleTapPlayerView = exoPlayerUtil.exoPlayerMainFrame.findViewById(R.id.exo_player_view)
    private val vibrator = exoPlayerUtil.activity.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    private var currentMode = ZoomMode.ORIGINAL

    override fun onScale(detector: ScaleGestureDetector): Boolean {
        playerView.hideController()
        Log.d("TAG", "onScale: ${detector.scaleFactor}")
        // Update the scaleFactor by multiplying it with the detected scale factor,
        // and ensure it stays within the range of 1.0f to 6.0f.
        scaleFactor *= detector.scaleFactor
        scaleFactor = 1.0f.coerceAtLeast(scaleFactor.coerceAtMost(6.0f))
        playerView.videoSurfaceView?.scaleX = scaleFactor
        playerView.videoSurfaceView?.scaleY = scaleFactor
        if (scaleFactor > 1.2) {
            updateZoomSizeTextView()
        }
        return true
    }

    override fun onScaleEnd(detector: ScaleGestureDetector) {
        if (scaleFactor > 1.2) {
            isDragEnabled = true
            currentMode = ZoomMode.ZOOM
            playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
        } else {
            if (scaleFactor > 1 && scaleFactor < 1.2) {
                setZoomToFitMode()
            } else {
                setOriginalMode()
            }
            vibrator.vibrate(50)
            isDragEnabled = false
        }
        zoomSizeText.visibility = View.GONE
    }

    private fun setOriginalMode() {
        if (currentMode != ZoomMode.ORIGINAL) {
            currentMode = ZoomMode.ORIGINAL
            updateZoomModeTextView()
        }
        resetPinchToZoomGesture(ZoomMode.ORIGINAL)
    }

    private fun setZoomToFitMode() {
        if (currentMode != ZoomMode.ZOOMED_TO_FIT) {
            currentMode = ZoomMode.ZOOMED_TO_FIT
            updateZoomModeTextView()
        }
        resetPinchToZoomGesture(ZoomMode.ZOOMED_TO_FIT)
    }

    private fun updateZoomModeTextView() {
        zoomModeText.visibility = View.VISIBLE
        zoomModeText.text = currentMode.mode
        zoomModeText.animateHideTextView()
    }

    private fun updateZoomSizeTextView() {
        zoomModeText.clearAnimation()
        zoomSizeText.visibility = View.VISIBLE
        zoomSizeText.text = DecimalFormat("0.0x").format(scaleFactor.toDouble())
    }

    fun resetPinchToZoomGesture(zoomMode: ZoomMode) {
        scaleFactor = 1.0f
        playerView.videoSurfaceView?.scaleX = 1.0f
        playerView.videoSurfaceView?.scaleY = 1.0f
        playerView.videoSurfaceView?.x = 0f
        playerView.videoSurfaceView?.y = 0f
        playerView.resizeMode = if (zoomMode == ZoomMode.ZOOMED_TO_FIT) {
            AspectRatioFrameLayout.RESIZE_MODE_ZOOM
        } else {
            AspectRatioFrameLayout.RESIZE_MODE_FIT
        }
    }

    enum class ZoomMode (val mode: String){
        ORIGINAL("Original"),
        ZOOMED_TO_FIT("Zoomed to fit"),
        ZOOM("Zoom")
    }

}