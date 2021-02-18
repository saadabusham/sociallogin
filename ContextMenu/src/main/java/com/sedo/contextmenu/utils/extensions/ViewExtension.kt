package com.sedo.contextmenu.utils

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import androidx.annotation.StringRes
import com.google.android.material.snackbar.Snackbar
import java.lang.System.currentTimeMillis

fun View.visible() {
    visibility = View.VISIBLE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}

fun View.gone() {
    visibility = View.GONE
}

fun View.disableView() {
    isEnabled = false
}

fun View.enableView() {
    isEnabled = true
}


fun View.disableViews() {
    val layout = this as ViewGroup
    for (i in 0 until layout.childCount) {
        val child = layout.getChildAt(i)
        child.isEnabled = false
        if (child is ViewGroup) {
            child.disableViews()
        }
    }

}

fun View.enableViews() {
    val layout = this as ViewGroup
    for (i in 0 until layout.childCount) {
        val child = layout.getChildAt(i)
        child.isEnabled = true
        if (child is ViewGroup) {
            child.enableViews()
        }
    }
}