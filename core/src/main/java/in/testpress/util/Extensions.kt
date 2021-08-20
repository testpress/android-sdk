package `in`.testpress.util

import android.view.View
import android.view.animation.Animation
import android.view.animation.RotateAnimation

object Extensions {

    fun View.startRotation() {
        val rotate = RotateAnimation(0F, 360F, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
        rotate.duration = 1000
        rotate.repeatCount = Animation.INFINITE
        startAnimation(rotate)
    }
}