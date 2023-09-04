package `in`.testpress.course.util

import `in`.testpress.course.R
import `in`.testpress.course.util.extension.animateHideTextView
import android.app.Activity
import android.content.Context
import android.os.*
import android.view.ScaleGestureDetector
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import com.github.vkay94.dtpv.DoubleTapPlayerView
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import java.text.DecimalFormat

class PinchToZoomGesture(
    activity: Activity,
    exoPlayerMainFrame: FrameLayout
) : SimpleOnScaleGestureListener() {

    private val MAX_SCALE = 6.0f
    private val MIN_SCALE = 1.0f
    private val ZOOM_SCALE_THRESHOLD = 1.2f
    var scaleFactor = 1.0f
    var isDragEnabled = false
    private val zoomModeText: TextView = exoPlayerMainFrame.findViewById(R.id.zoom_mode_text)
    private val zoomSizeText: TextView = exoPlayerMainFrame.findViewById(R.id.zoom_size_text)
    private val playerView: DoubleTapPlayerView = exoPlayerMainFrame.findViewById(R.id.exo_player_view)
    private val vibrator = activity.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    private var currentMode = ZoomMode.ORIGINAL

    override fun onScale(detector: ScaleGestureDetector): Boolean {
        playerView.hideController()
        calculateScaleFactor(detector.scaleFactor)
        setPlayerViewScale()
        displayZoomSize()
        return true
    }

    private fun calculateScaleFactor(newScaleFactor: Float) {
        scaleFactor *= newScaleFactor
        scaleFactor = MIN_SCALE.coerceAtLeast(scaleFactor.coerceAtMost(MAX_SCALE))
    }

    private fun setPlayerViewScale() {
        playerView.videoSurfaceView?.let {
            it.scaleX = scaleFactor
            it.scaleY = scaleFactor
        }
    }

    private fun displayZoomSize() {
        if (scaleFactor > ZOOM_SCALE_THRESHOLD) {
            zoomModeText.clearAnimation()
            zoomSizeText.visibility = View.VISIBLE
            zoomSizeText.text = DecimalFormat("0.0x").format(scaleFactor.toDouble())
        }
    }

    override fun onScaleEnd(detector: ScaleGestureDetector) {
        when {
            scaleFactor > ZOOM_SCALE_THRESHOLD -> setZoomMode()
            scaleFactor > MIN_SCALE && scaleFactor < ZOOM_SCALE_THRESHOLD -> setZoomToFitMode()
            else -> setOriginalMode()
        }
        zoomSizeText.visibility = View.GONE
    }

    private fun setZoomMode() {
        isDragEnabled = true
        currentMode = ZoomMode.ZOOM
        playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
    }

    private fun setZoomToFitMode() {
        if (currentMode != ZoomMode.ZOOMED_TO_FIT) {
            currentMode = ZoomMode.ZOOMED_TO_FIT
            updateZoomModeTextView()
        }
        resetPinchToZoomGesture(ZoomMode.ZOOMED_TO_FIT)
        vibrator.vibrate(50)
        isDragEnabled = false
    }

    private fun setOriginalMode() {
        if (currentMode != ZoomMode.ORIGINAL) {
            currentMode = ZoomMode.ORIGINAL
            updateZoomModeTextView()
        }
        resetPinchToZoomGesture(ZoomMode.ORIGINAL)
        vibrator.vibrate(50)
        isDragEnabled = false
    }

    private fun updateZoomModeTextView() {
        zoomModeText.visibility = View.VISIBLE
        zoomModeText.text = currentMode.mode
        zoomModeText.animateHideTextView()
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

    enum class ZoomMode(val mode: String) {
        ORIGINAL("Original"),
        ZOOMED_TO_FIT("Zoomed to fit"),
        ZOOM("Zoom")
    }

}