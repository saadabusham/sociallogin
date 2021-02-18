package com.sedo.contextmenu.utils.binidngadapters


import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.databinding.BindingAdapter

@BindingAdapter("ivSetSrcImageFromResources")
fun ImageView.setImageFromResources(@DrawableRes imageRes: Int) {
    setImageResource(imageRes)
}

@BindingAdapter("imageRec")
fun ImageView?.setImageFromRec(
    @DrawableRes imageRes: Int
) {
    this?.setImageResource(imageRes)
}
