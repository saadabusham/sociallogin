package com.sedo.contextmenu.utils

import android.view.View
import android.view.animation.AlphaAnimation

object BlurryHelper {

    fun hasZero(vararg args: Int): Boolean {
        for (num in args) {
            if (num == 0) {
                return true
            }
        }
        return false
    }

    fun animate(v: View, duration: Int) {
        val alpha = AlphaAnimation(0f, 1f)
        alpha.duration = duration.toLong()
        v.startAnimation(alpha)
    }
}