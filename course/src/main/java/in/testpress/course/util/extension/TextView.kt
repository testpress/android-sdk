package `in`.testpress.course.util.extension

import `in`.testpress.course.util.AnimationType
import `in`.testpress.course.util.getAnimation
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.widget.TextView

/**
 * This function animates the hiding of a TextView by applying a combination of fade-in, stay visible,
 * and fade-out animations. After the animations are complete, the TextView's visibility is set to GONE.
 */

fun TextView.animateHideTextView() {

    val fadeInAnimation = getAnimation(AnimationType.FADE_IN, 250, 0)
    val stayAnimation = getAnimation(AnimationType.STAY, 500, fadeInAnimation.duration)
    val fadeOutAnimation = getAnimation(AnimationType.FADE_OUT, 250, fadeInAnimation.duration + stayAnimation.duration)

    val animationSet = AnimationSet(false).apply {
        this.addAnimation(fadeInAnimation)
        this.addAnimation(stayAnimation)
        this.addAnimation(fadeOutAnimation)
    }

    animationSet.setAnimationListener(object : Animation.AnimationListener {
        override fun onAnimationStart(animation: Animation?) {}
        override fun onAnimationEnd(animation: Animation?) {
            this@animateHideTextView.visibility = View.GONE
        }

        override fun onAnimationRepeat(animation: Animation?) {}
    })

    this.startAnimation(animationSet)
}