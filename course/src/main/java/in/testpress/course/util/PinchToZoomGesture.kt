package `in`.testpress.course.util

import `in`.testpress.course.R
import android.content.Context
import android.os.*
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener
import android.view.View
import android.view.View.OnTouchListener
import android.view.animation.*
import android.widget.TextView
import androidx.core.view.isVisible
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
        zoomModeText.hideTextView()
    }

    private fun updateZoomSizeTextView() {
        zoomModeText.clearAnimation()
        zoomSizeText.visibility = View.VISIBLE
        zoomSizeText.text = DecimalFormat("0.0x").format(scaleFactor.toDouble())
    }

    private fun TextView.hideTextView() {
        val animationSet = AnimationSet(false)

        // Fade in animation
        val fadeInAnimation = AlphaAnimation(0.0f, 1.0f)
        fadeInAnimation.duration = 250
        animationSet.addAnimation(AlphaAnimation(0.0f, 1.0f).apply {
            duration = 250
        })

        // Stay visible for 500ms
        val stayDuration = 500
        animationSet.addAnimation(AlphaAnimation(1.0f, 1.0f).apply {
            duration = stayDuration.toLong()
            startOffset = fadeInAnimation.duration
        })

        // Fade out animation
        val fadeOutAnimation = AlphaAnimation(1.0f, 0.0f)
        fadeOutAnimation.duration = 250
        fadeOutAnimation.startOffset = fadeInAnimation.duration + stayDuration
        animationSet.addAnimation(fadeOutAnimation)

        animationSet.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {}
            override fun onAnimationEnd(animation: Animation?) {
                this@hideTextView.visibility = View.GONE
            }

            override fun onAnimationRepeat(animation: Animation?) {}
        })

        this.startAnimation(animationSet)
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

class OnTouchDragListener(private val exoPlayerUtil: ExoPlayerUtil) : OnTouchListener {

    private var lastTouchX = 0f
    private var lastTouchY = 0f
    private var posX = 0f
    private var posY = 0f
    private var moveCalled = 0
    private val playerView get() = exoPlayerUtil.playerView
    private val scaleGesture get() = exoPlayerUtil.scaleGesture

    override fun onTouch(p0: View, motionEvent: MotionEvent): Boolean {
        if (exoPlayerUtil.fullscreen) {
            exoPlayerUtil.scaleGestureDetector.onTouchEvent(motionEvent)
            if (scaleGesture.isDragEnabled) {
                when (motionEvent.action) {
                    MotionEvent.ACTION_DOWN -> {
                        lastTouchX = motionEvent.x
                        lastTouchY = motionEvent.y
                        return false
                    }
                    MotionEvent.ACTION_MOVE -> {
                        moveCalled += 1
                        if (moveCalled < 5) {
                            return false
                        }
                        val deltaX: Float = motionEvent.rawX - lastTouchX
                        val deltaY: Float = motionEvent.rawY - lastTouchY

                        // Calculate the new positions
                        val newPosX: Float = posX + deltaX
                        val newPosY: Float = posY + deltaY

                        // Calculate the maximum allowed translations
                        val maxPosX: Float =
                            (playerView.videoSurfaceView?.width!! * scaleGesture.scaleFactor - playerView.width) / 2
                        val maxPosY: Float =
                            (playerView.videoSurfaceView?.height!! * scaleGesture.scaleFactor - playerView.height) / 2
                        val minPosX = -maxPosX
                        val minPosY = -maxPosY

                        // Apply boundary checks
                        posX = newPosX.coerceAtLeast(minPosX).coerceAtMost(maxPosX)
                        posY = newPosY.coerceAtLeast(minPosY).coerceAtMost(maxPosY)

                        // Apply translations
                        playerView.videoSurfaceView?.translationX = posX
                        playerView.videoSurfaceView?.translationY = posY
                        lastTouchX = motionEvent.rawX
                        lastTouchY = motionEvent.rawY
                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        moveCalled = 0
                        return false
                    }
                }
            }
        }
        return false
    }
}
