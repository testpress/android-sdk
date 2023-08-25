package `in`.testpress.course.util

import `in`.testpress.course.R
import android.os.*
import android.view.ScaleGestureDetector
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener
import android.view.View
import android.view.animation.*
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.view.isVisible
import com.github.vkay94.dtpv.DoubleTapPlayerView
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import java.text.DecimalFormat


class PinchToZoomGesture(
    exoPlayerMainFrame: FrameLayout,
    private val vibrator: Vibrator
) : SimpleOnScaleGestureListener() {

    var scaleFactor = 1.0f
    var isDragEnabled = false
    private val zoomModeText: TextView = exoPlayerMainFrame.findViewById(R.id.pinch_to_zoom_mode_text)
    private val zoomMesurmentText: TextView = exoPlayerMainFrame.findViewById(R.id.zoomed_mesurment_text)
    private val playerView: DoubleTapPlayerView = exoPlayerMainFrame.findViewById(R.id.exo_player_view)

    override fun onScale(detector: ScaleGestureDetector): Boolean {
        playerView.hideController()
        hideAllTextViews()
        scaleFactor *= detector.scaleFactor
        scaleFactor = 1.0f.coerceAtLeast(scaleFactor.coerceAtMost(6.0f))
        playerView.videoSurfaceView?.scaleX = scaleFactor
        playerView.videoSurfaceView?.scaleY = scaleFactor
        if (scaleFactor > 1.2) {
            val decimalFormat = DecimalFormat("0.0x")
            val formattedValue = decimalFormat.format(scaleFactor.toDouble())
            updateZoomMesurmentTextView(formattedValue)
        }
        return true
    }

    private fun hideAllTextViews() {
        zoomModeText.isVisible = false
        zoomMesurmentText.isVisible = false
    }

    override fun onScaleEnd(detector: ScaleGestureDetector?) {
        if (scaleFactor > 1 && scaleFactor < 1.2) {
            vibrator.vibrate(50)
            updateZoomModeTextView("Zoomed to fit")
            resetSurfaceView()
            resetScaleFactor()
            isDragEnabled = false
            playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
        } else if (scaleFactor > 1.2) {
            isDragEnabled = true
            playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
        } else {
            if (playerView.resizeMode != AspectRatioFrameLayout.RESIZE_MODE_FIT) {
                vibrator.vibrate(50)
                updateZoomModeTextView("Original")
                resetSurfaceView()
                resetScaleFactor()
                isDragEnabled = false
                playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
            }
        }
        zoomMesurmentText.isVisible = false
    }

    private fun updateZoomModeTextView(text: String) {
        zoomModeText.visibility = View.VISIBLE
        zoomModeText.text = text
        zoomModeText.hideTextView()
    }

    private fun updateZoomMesurmentTextView(text: String) {
        zoomMesurmentText.visibility = View.VISIBLE
        zoomMesurmentText.text = text
    }

    private fun resetSurfaceView() {
        playerView.videoSurfaceView?.scaleX = 1.0f
        playerView.videoSurfaceView?.scaleY = 1.0f
        playerView.videoSurfaceView?.x = 0f
        playerView.videoSurfaceView?.y = 0f
    }

    private fun resetScaleFactor() {
        scaleFactor = 1.0f
    }

    private fun TextView.hideTextView() {
        val animationSet = AnimationSet(false)
        val fadeOutAnimation = AlphaAnimation(1.0f, 0.0f)
        fadeOutAnimation.duration = 500
        fadeOutAnimation.startOffset = 500
        val interpolator: Interpolator = LinearInterpolator()
        fadeOutAnimation.interpolator = interpolator
        animationSet.addAnimation(fadeOutAnimation)
        this.startAnimation(animationSet)
        animationSet.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {}
            override fun onAnimationEnd(animation: Animation?) {
                this@hideTextView.visibility = View.GONE
            }
            override fun onAnimationRepeat(animation: Animation?) {}
        })
    }

}