package `in`.testpress.course.util

import android.view.MotionEvent
import android.view.View

/**
 * This class implements a custom touch listener for handling touch and drag interactions
 * within an ExoPlayer's video view, considering gestures for scaling and translation.
 */

class VideoTouchDragHandler(private val exoPlayerUtil: ExoPlayerUtil) : View.OnTouchListener {

    private var lastTouchX = 0f
    private var lastTouchY = 0f
    private var posX = 0f
    private var posY = 0f
    private var touchEventCalled = 0
    private val playerView get() = exoPlayerUtil.playerView
    private val scaleGesture get() = exoPlayerUtil.scaleGesture
    private val MINIMUM_TOUCH_EVENT_REQUIRED = 5

    override fun onTouch(p0: View, motionEvent: MotionEvent): Boolean {
        if (exoPlayerUtil.fullscreen) {
            exoPlayerUtil.scaleGestureDetector.onTouchEvent(motionEvent)
            if (scaleGesture.isDragEnabled) {
                when (motionEvent.action) {
                    MotionEvent.ACTION_DOWN -> {
                        // Store the initial touch coordinates.
                        lastTouchX = motionEvent.x
                        lastTouchY = motionEvent.y
                        return false
                    }
                    MotionEvent.ACTION_MOVE -> {
                        touchEventCalled += 1
                        // Only start dragging after a few move events to avoid accidental drags.
                        if (touchEventCalled < MINIMUM_TOUCH_EVENT_REQUIRED) {
                            return false
                        }
                        // Calculate the change in touch coordinates.
                        val deltaX: Float = motionEvent.rawX - lastTouchX
                        val deltaY: Float = motionEvent.rawY - lastTouchY

                        // Calculate the new positions based on the changes.
                        val newPosX: Float = posX + deltaX
                        val newPosY: Float = posY + deltaY

                        // Calculate the maximum allowed translations based on the scaled view size.
                        val maxPosX: Float =
                            (playerView.videoSurfaceView?.width!! * scaleGesture.scaleFactor - playerView.width) / 2
                        val maxPosY: Float =
                            (playerView.videoSurfaceView?.height!! * scaleGesture.scaleFactor - playerView.height) / 2
                        val minPosX = -maxPosX
                        val minPosY = -maxPosY

                        // Apply boundary checks to prevent over-translation.
                        posX = newPosX.coerceAtLeast(minPosX).coerceAtMost(maxPosX)
                        posY = newPosY.coerceAtLeast(minPosY).coerceAtMost(maxPosY)

                        // Apply translations to the video surface view.
                        playerView.videoSurfaceView?.translationX = posX
                        playerView.videoSurfaceView?.translationY = posY

                        // Update the last touch coordinates.
                        lastTouchX = motionEvent.rawX
                        lastTouchY = motionEvent.rawY
                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        touchEventCalled = 0
                        return false
                    }
                }
            }
        }
        return false
    }
}