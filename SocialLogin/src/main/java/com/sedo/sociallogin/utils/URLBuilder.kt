package com.sedo.sociallogin.utils

import android.net.Uri

object URLBuilder {

    fun buildAppleAuthUrl(
        authUrl: String?,
        responseType: String?,
        responseMode: String?,
        clientId: String?,
        state: String?,
        redirectUri: String?,
        scope: String? = null
    ): String {
        val urlBuilder = StringBuilder()
        urlBuilder.append(authUrl)
        urlBuilder.append("?")
        urlBuilder.append("response_type=${responseType}")
        urlBuilder.append("&v=1.1.6")
        urlBuilder.append("&response_mode=${responseMode}")
        urlBuilder.append("&client_id=$clientId")
        scope?.let {
            urlBuilder.append("&scope=$scope")
        }
        urlBuilder.append("&state=$state")
        urlBuilder.append("&redirect_uri=$redirectUri")
        urlBuilder.append("&usePopup=true")
        return urlBuilder.toString()
    }

    fun buildStravaAuthUrl(
        authUrl: String?,
        responseType: String?,
        approvalType: String?,
        clientId: String?,
        redirectUri: String?,
        scope: String? = null
    ): Uri? {
        return Uri.parse(authUrl)
            .buildUpon()
            .appendQueryParameter("client_id", clientId)
            .appendQueryParameter("redirect_uri", redirectUri)
            .appendQueryParameter("response_type", responseType)
            .appendQueryParameter("approval_prompt", approvalType)
            .appendQueryParameter("scope", scope)
            .build()
    }
}