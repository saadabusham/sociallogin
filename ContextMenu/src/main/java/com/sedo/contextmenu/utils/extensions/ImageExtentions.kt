package com.sedo.contextmenu.utils.extensions

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Point
import android.graphics.Rect
import android.os.Build
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import android.util.DisplayMetrics
import android.view.View
import android.view.WindowManager
import com.sedo.contextmenu.utils.BlurFactor
import com.sedo.contextmenu.utils.BlurMaker.blur


fun Activity.blur(blur: Float? = null): Bitmap? {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1 || blur == null || (blur <= 0f || blur > 25f)) {
        return null
    }
    val bitmap: Bitmap? = this.takeScreenShot()
    val renderScript = RenderScript.create(this)

    // This will blur the bitmapOriginal with a radius of 16 and save it in bitmapOriginal
    val input = Allocation.createFromBitmap(
        renderScript,
        bitmap
    )
    // Use this constructor for best performance, because it uses USAGE_SHARED mode which reuses memory
    val output = Allocation.createTyped(renderScript, input.type)
    val script = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript))
    script.setRadius(blur)
    script.setInput(input)
    script.forEach(output)
    output.copyTo(bitmap)
    return bitmap
}

fun Activity.blur(blur: Float? = null, colorHex: String? = null, color: Int? = null): Bitmap? {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1 || blur == null || (blur <= 0f || blur > 25f)) {
        return null
    }
    val bitmap: Bitmap? = this.takeScreenShot()
    val widthHeight: IntArray = getScreenSize(this)
    return bitmap?.blur(
        this,
        BlurFactor(
            radius = blur.toInt(),
            colorHex = colorHex,
            color = color ?: Color.TRANSPARENT,
            width = widthHeight[WIDTH_INDEX],
            height = widthHeight[HEIGHT_INDEX]
        )
    )
}

fun Activity.takeScreenShot(): Bitmap? {
    val view: View = window.decorView
    view.isDrawingCacheEnabled = true
    view.buildDrawingCache()
    val bitmap: Bitmap = view.drawingCache
    val frame = Rect()
    window.decorView.getWindowVisibleDisplayFrame(frame)
    val statusBarHeight: Int = frame.top
    val widthHeight: IntArray = getScreenSize(this)
    val bitmapResult = Bitmap.createBitmap(
        bitmap,
        0,
        0,
        widthHeight[WIDTH_INDEX],
        widthHeight[HEIGHT_INDEX] - 0
    )
    view.destroyDrawingCache()
    return bitmapResult
}

fun getScreenSize(context: Context): IntArray {
    val widthHeight = IntArray(2)
    widthHeight[WIDTH_INDEX] = 0
    widthHeight[HEIGHT_INDEX] = 0
    val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    val display = windowManager.defaultDisplay
    val size = Point()
    display.getSize(size)
    widthHeight[WIDTH_INDEX] = size.x
    widthHeight[HEIGHT_INDEX] = size.y
    if (!isScreenSizeRetrieved(widthHeight)) {
        val metrics = DisplayMetrics()
        display.getMetrics(metrics)
        widthHeight[0] = metrics.widthPixels
        widthHeight[1] = metrics.heightPixels
    }
    if (!isScreenSizeRetrieved(widthHeight)) {
        widthHeight[0] = display.width // deprecated
        widthHeight[1] = display.height // deprecated
    }
    return widthHeight
}

private fun isScreenSizeRetrieved(widthHeight: IntArray): Boolean {
    return widthHeight[WIDTH_INDEX] != 0 && widthHeight[HEIGHT_INDEX] != 0
}

fun String.getColor(): Int? {
    try {
        var color = this
        if (!color.startsWith("#"))
            color = "#$color"
        Color.parseColor(color)
            .let {
                return it
            }
    } catch (e: Exception) {
        return null
    }
}

const val WIDTH_INDEX = 0
const val HEIGHT_INDEX = 1