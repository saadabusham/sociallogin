package com.sedo.contextmenu.utils

import android.graphics.Color

data class BlurFactor (
    var width :Int = 0,
    var height :Int = 0,
    var radius:Int = DEFAULT_RADIUS,
    var sampling :Int= DEFAULT_SAMPLING,
    var color :Int = Color.TRANSPARENT,
    var colorHex :String? = null
)
const val DEFAULT_RADIUS = 25
const val DEFAULT_SAMPLING = 1