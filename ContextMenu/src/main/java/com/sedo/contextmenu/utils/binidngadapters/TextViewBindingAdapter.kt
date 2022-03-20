package com.sedo.contextmenu.utils.binidngadapters

import android.graphics.Color
import android.widget.TextView
import androidx.databinding.BindingAdapter

@BindingAdapter("bind_text_color")
fun TextView.setTextColor(color: Int?) {
    color?.let {
        setTextColor(context.resources.getColor(color))
    }
}

@BindingAdapter("bind_text_hex_color")
fun TextView.setTextColorHex(tintColor: String?) {
    try {
        var color: String?
        tintColor?.let {
            color = tintColor
            if (color?.startsWith("#") == false)
                color = "#$color"
            Color.parseColor(color).let { setTextColor(it) }
        }
    } catch (e: Exception) {

    }
}