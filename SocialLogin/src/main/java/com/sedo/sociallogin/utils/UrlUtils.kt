package com.sedo.sociallogin.utils

import java.io.UnsupportedEncodingException
import java.net.URLDecoder
import java.util.HashMap

object UrlUtils {

    @Throws(UnsupportedEncodingException::class)
    fun getUrlValues(url: String): Map<String, String?> {
        val i = url.indexOf("#")
        val paramsMap: MutableMap<String, String?> = HashMap()
        if (i > -1) {
            val searchURL = url.substring(url.indexOf("#") + 1)
            val params = searchURL.split("&").toTypedArray()
            for (param in params) {
                val temp = param.split("=").toTypedArray()
                paramsMap[temp[0]] = URLDecoder.decode(temp[1], "UTF-8")
            }
        }
        return paramsMap
    }
}