package `in`.testpress.course.util

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.animation.doOnRepeat
import androidx.core.view.doOnLayout
import kotlin.random.Random

class WatermarkOverlay @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private var watermarkText: String = ""
    private val textPaint = Paint().apply {
        color = Color.BLACK
        textSize = 30f
        isAntiAlias = true
    }

    private var isDynamic = false
    private var dynamicX = 0f
    private var dynamicY = 0f
    private var animator: ValueAnimator? = null

    // Static positioning enum
    enum class StaticPosition {
        TOP_LEFT, TOP_RIGHT, CENTER, BOTTOM_LEFT, BOTTOM_RIGHT
    }

    private var staticPosition = StaticPosition.TOP_RIGHT

    fun setWatermark(text: String) {
        watermarkText = text
        invalidate()
    }

    fun setDynamicWatermark() {
        isDynamic = true
        doOnLayout {
            startHorizontalAnimation()
        }
    }

    fun setStaticWatermark(position: StaticPosition) {
        isDynamic = false
        staticPosition = position
        invalidate()
    }

    fun setStaticWatermark(position: String) {
        isDynamic = false
        staticPosition = when (position) {
            "top-left" -> StaticPosition.TOP_LEFT
            "top-right" -> StaticPosition.TOP_RIGHT
            "bottom-left" -> StaticPosition.BOTTOM_LEFT
            "bottom-right" -> StaticPosition.BOTTOM_RIGHT
            "middle" -> StaticPosition.CENTER
            else -> StaticPosition.TOP_RIGHT
        }
        invalidate()
    }

    private fun startHorizontalAnimation() {
        animator?.cancel()

        // Calculate the full width of the watermark text
        val textWidth = textPaint.measureText(watermarkText)

        // Randomize vertical position for every animation cycle
        dynamicY = Random.nextFloat() * (height - 50)

        animator = ValueAnimator.ofFloat(-textWidth, width.toFloat()).apply {
            duration = 30000 // Animation duration
            repeatMode = ValueAnimator.REVERSE
            repeatCount = ValueAnimator.INFINITE

            addUpdateListener {
                dynamicX = it.animatedValue as Float
                invalidate()
            }

            doOnRepeat {
                dynamicY = Random.nextFloat() * (height - 50)
            }

            start()
        }

        animator?.doOnRepeat { }
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (watermarkText.isEmpty()) return

        if (isDynamic) {
            // Draw watermark at dynamic motion position
            val x = dynamicX
            val y = dynamicY
            canvas.drawText(watermarkText, x, y, textPaint)
        } else {
            // Handle static positioning
            val (x, y) = calculateStaticPosition()
            canvas.drawText(watermarkText, x, y, textPaint)
        }
    }

    private fun calculateStaticPosition(): Pair<Float, Float> {
        return when (staticPosition) {
            StaticPosition.TOP_LEFT -> Pair(20f, 50f)
            StaticPosition.TOP_RIGHT -> Pair(width - textPaint.measureText(watermarkText) - 20f, 50f)
            StaticPosition.CENTER -> Pair((width - textPaint.measureText(watermarkText)) / 2, height / 2f)
            StaticPosition.BOTTOM_LEFT -> Pair(20f, height - 20f)
            StaticPosition.BOTTOM_RIGHT -> Pair(width - textPaint.measureText(watermarkText) - 20f, height - 20f)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        animator?.cancel()
    }
}