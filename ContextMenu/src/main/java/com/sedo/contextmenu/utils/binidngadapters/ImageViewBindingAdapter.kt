package com.sedo.contextmenu.utils.binidngadapters

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.BaseRequestOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.sedo.contextmenu.R
import com.sedo.contextmenu.utils.extensions.px

@BindingAdapter("ivSetSrcImageFromResources")
fun ImageView.setImageFromResources(@DrawableRes imageRes: Int) {
    setImageResource(imageRes)
}

@BindingAdapter("bind_tint_hex_color")
fun ImageView.setTintHex(tintColor: String?) {
    tintColor?.let { Color.parseColor(tintColor) }
        ?.let { setColorFilter(it, android.graphics.PorterDuff.Mode.SRC_IN) }
}

@BindingAdapter("bind_tint_color")
fun ImageView.setImageTint(tintColor: Int?) {
    tintColor?.let { ContextCompat.getColor(context, it) }
        ?.let { setColorFilter(it, android.graphics.PorterDuff.Mode.SRC_IN) }
}

@BindingAdapter("imageRec")
fun ImageView?.setImageFromRec(
    @DrawableRes imageRes: Int
) {
    this?.setImageResource(imageRes)
}


@BindingAdapter(
    value = [
        "ivImageUrl",
        "ivImagePlaceholder",
        "ivImageErrorPlaceholder",
        "ivImageProgressId",
        "ivImageIsCircle",
        "ivImageIsRoundedCorners",
        "ivImageRoundedRadius"
    ],
    requireAll = false
)
fun ImageView.loadImage(
    image: Any?,
    @DrawableRes imagePlaceholder: Int? = R.drawable.ic_default_image_place_holder,
    @DrawableRes imageErrorPlaceholder: Int? = R.drawable.ic_default_image_place_holder,
    @IdRes imageProgressId: Int? = null,
    imageIsCircle: Boolean = false,
    imageIsRoundedCorners: Boolean = false,
    roundingRadius: Int? = null
) {
    if (image is Int) {
        setImageFromResources(image)
        return
    }
    image as String
    if (image.isNullOrEmpty()) {
        setImageResource(imageErrorPlaceholder ?: R.drawable.ic_default_image_place_holder)
        return
    }

    val progressView: ProgressBar? = imageProgressId?.let { findViewById(it) }
    progressView?.visibility = View.VISIBLE

    Glide.with(context)
        .load(image)
        .listener(object : RequestListener<Drawable> {
            override fun onLoadFailed(
                e: GlideException?,
                model: Any?,
                target: Target<Drawable>?,
                isFirstResource: Boolean
            ): Boolean {
                progressView?.visibility = View.GONE
                return false
            }

            override fun onResourceReady(
                resource: Drawable?,
                model: Any?,
                target: Target<Drawable>?,
                dataSource: DataSource?,
                isFirstResource: Boolean
            ): Boolean {
                progressView?.visibility = View.GONE
                return false
            }
        })
        .placeholder(imagePlaceholder ?: R.drawable.ic_default_image_place_holder)
        .apply(setUpRequestOptions(imageIsCircle, imageIsRoundedCorners, roundingRadius ?: 10))
        .error(imageErrorPlaceholder ?: R.drawable.ic_default_image_place_holder)
        .into(this)
}

fun setUpRequestOptions(
    imageIsCircle: Boolean,
    imageIsRoundedCorners: Boolean,
    roundingRadius: Int
): BaseRequestOptions<*> =
    when {
        imageIsCircle -> RequestOptions.circleCropTransform()
        imageIsRoundedCorners -> RequestOptions().transform(
            CenterCrop(), RoundedCorners(roundingRadius.px())
        )
        else -> RequestOptions.noTransformation()
    }
