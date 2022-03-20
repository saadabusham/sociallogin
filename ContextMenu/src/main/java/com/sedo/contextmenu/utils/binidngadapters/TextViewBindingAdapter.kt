package com.sedo.contextmenu.utils.binidngadapters

import android.widget.TextView
import androidx.databinding.BindingAdapter

@BindingAdapter("bind_text_color")
fun TextView.setTextColor(color: Int?) {
    color?.let {
        setTextColor(context.resources.getColor(color))
    }
}