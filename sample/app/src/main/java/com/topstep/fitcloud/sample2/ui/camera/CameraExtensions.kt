package com.topstep.fitcloud.sample2.ui.camera

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.widget.FrameLayout
import android.widget.ImageButton

/** Milliseconds used for UI animations */
const val ANIMATION_FAST_MILLIS = 50L
const val ANIMATION_SLOW_MILLIS = 100L

/**
 * Simulate a button click, including a small delay while it is being pressed to trigger the
 * appropriate animations.
 */
fun ImageButton.simulateClick(delay: Long = ANIMATION_FAST_MILLIS) {
    performClick()
    isPressed = true
    invalidate()
    postDelayed({
        invalidate()
        isPressed = false
    }, delay)
}

fun FrameLayout.displayFlashAnim() {
    this.postDelayed({
        this.foreground = ColorDrawable(Color.WHITE)
        this.postDelayed(
            { this.foreground = null }, ANIMATION_FAST_MILLIS
        )
    }, ANIMATION_SLOW_MILLIS)
}