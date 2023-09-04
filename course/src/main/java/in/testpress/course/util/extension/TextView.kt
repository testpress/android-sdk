package `in`.testpress.course.util.extension

import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.widget.TextView

/**
 * This function animates the hiding of a TextView by applying a combination of fade-in, stay visible,
 * and fade-out animations. After the animations are complete, the TextView's visibility is set to GONE.
 */

fun TextView.animateHideTextView() {
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
            this@animateHideTextView.visibility = View.GONE
        }

        override fun onAnimationRepeat(animation: Animation?) {}
    })

    this.startAnimation(animationSet)
}