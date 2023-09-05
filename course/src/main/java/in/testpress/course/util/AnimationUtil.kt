package `in`.testpress.course.util

import android.view.animation.AlphaAnimation

enum class AnimationType(val fromAlpha: Float, val toAlpha: Float) {
    FADE_IN(0.0f, 1.0f),
    STAY(1.0f, 1.0f),
    FADE_OUT(1.0f, 0.0f)
}

fun getAnimation(type: AnimationType, duration: Long, startOffset: Long): AlphaAnimation {
    return AlphaAnimation(type.fromAlpha, type.toAlpha).apply {
        this.duration = duration
        this.startOffset = startOffset
    }
}