package com.sedo.sociallogin.utils

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
        urlBuilder.append("&state = $state")
        urlBuilder.append("&redirect_uri = $redirectUri")
        urlBuilder.append("&usePopup = true")
        return urlBuilder.toString()
    }
}