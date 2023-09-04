package `in`.testpress.course.util

import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import com.github.vkay94.dtpv.DoubleTapPlayerView

/**
 * This class implements a custom touch listener for handling touch and drag interactions
 * within an ExoPlayer's video view, considering gestures for scaling and translation.
 */

class VideoTouchDragHandler(
    private val playerView: DoubleTapPlayerView,
    private val pinchToZoomGesture: PinchToZoomGesture,
    private val scaleGestureDetector: ScaleGestureDetector
) : View.OnTouchListener {

    private var lastTouchX = 0f
    private var lastTouchY = 0f
    private var playerViewPosX = 0f
    private var playerViewPosY = 0f
    private var newPlayerPosX = 0f
    private var newPlayerPosY = 0f
    private var deltaX = 0f
    private var deltaY = 0f
    private var touchEventCalled = 0
    private val MINIMUM_TOUCH_EVENT_REQUIRED = 5

    override fun onTouch(p0: View, motionEvent: MotionEvent): Boolean {
        scaleGestureDetector.onTouchEvent(motionEvent)
        if (this.pinchToZoomGesture.isDragEnabled) {
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    updateLastTouchCoordinates(motionEvent)
                    return false
                }
                MotionEvent.ACTION_UP -> {
                    resetAllValues()
                    return false
                }
                MotionEvent.ACTION_MOVE -> drag(motionEvent)
            }
        }
        return false
    }

    private fun resetAllValues() {
        touchEventCalled = 0
        deltaX = 0f
        deltaY = 0f
        newPlayerPosX = 0f
        newPlayerPosY = 0f
    }

    private fun drag(motionEvent: MotionEvent): Boolean {
        touchEventCalled += 1
        // Only start dragging after a few move events to avoid accidental drags.
        if (touchEventCalled < MINIMUM_TOUCH_EVENT_REQUIRED) {
            return false
        }
        calculateCoordinatesChange(motionEvent)
        calculateNewPositions()
        applyBoundaryChecks()
        updatePlayerViewPosition()
        updateLastTouchCoordinates(motionEvent)
        return true
    }

    private fun calculateNewPositions() {
        // Calculate the new positions based on the changes.
        newPlayerPosX = playerViewPosX + deltaX
        newPlayerPosY = playerViewPosY + deltaY
    }

    private fun calculateCoordinatesChange(motionEvent: MotionEvent) {
        // Calculate the change in touch coordinates.
        deltaX = motionEvent.rawX - lastTouchX
        deltaY = motionEvent.rawY - lastTouchY
    }

    private fun applyBoundaryChecks() {
        // Calculate the maximum allowed translations based on the scaled view size.
        val maxPosX: Float =
            (playerView.videoSurfaceView?.width!! * this.pinchToZoomGesture.scaleFactor - playerView.width) / 2
        val maxPosY: Float =
            (playerView.videoSurfaceView?.height!! * this.pinchToZoomGesture.scaleFactor - playerView.height) / 2
        val minPosX = -maxPosX
        val minPosY = -maxPosY

        // Apply boundary checks to prevent over-translation.
        playerViewPosX = newPlayerPosX.coerceAtLeast(minPosX).coerceAtMost(maxPosX)
        playerViewPosY = newPlayerPosY.coerceAtLeast(minPosY).coerceAtMost(maxPosY)
    }

    private fun updatePlayerViewPosition() {
        playerView.videoSurfaceView?.let {
            it.translationX = playerViewPosX
            it.translationY = playerViewPosY
        }
    }

    private fun updateLastTouchCoordinates(motionEvent: MotionEvent) {
        lastTouchX = motionEvent.rawX
        lastTouchY = motionEvent.rawY
    }
}