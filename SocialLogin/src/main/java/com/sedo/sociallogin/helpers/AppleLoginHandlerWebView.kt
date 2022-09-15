package com.sedo.sociallogin.helpers

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Bitmap
import android.graphics.Rect
import android.view.Window
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.fragment.app.Fragment
import com.sedo.sociallogin.data.enums.SocialTypeEnum
import com.sedo.sociallogin.utils.Constants.AppleConstants.AUTHURL
import com.sedo.sociallogin.utils.Constants.AppleConstants.RESPONSE_MODE
import com.sedo.sociallogin.utils.Constants.AppleConstants.RESPONSE_TYPE
import com.sedo.sociallogin.utils.URLBuilder
import com.sedo.sociallogin.utils.UrlUtils.getUrlValues
import java.util.*

class AppleLoginHandlerWebView private constructor(
    mActivity: ComponentActivity? = null,
    mFragment: Fragment? = null,
    clientId: String?,
    redirectUri: String?
) : ISocialLogin(mActivity, mFragment, clientId, redirectUri) {

    override fun initMethod() {
        val state: String = UUID.randomUUID().toString()
        appleAuthURLFull = URLBuilder.buildAppleAuthUrl(
            authUrl = AUTHURL,
            responseType = RESPONSE_TYPE,
            responseMode = RESPONSE_MODE,
            clientId = clientId,
            state = state,
            redirectUri = redirectUri
        )
    }

    override fun startMethod() {
        appleAuthURLFull?.let { openWebViewDialog(it) };
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun openWebViewDialog(url: String) {
        getContext()?.let {
            appleLoginDialog = Dialog(it)
            val webView = WebView(it)
            webView.settings.javaScriptCanOpenWindowsAutomatically = true
            webView.settings.javaScriptEnabled = true
            webView.settings.domStorageEnabled = true
            instance?.let {
                webView.webViewClient = AppleLoginWebView(it)
            }
            webView.loadUrl(url)
            appleLoginDialog?.setContentView(webView)
            appleLoginDialog?.show()
        }
    }

    private class AppleLoginWebView(val instance: AppleLoginHandlerWebView) : WebViewClient() {

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            if (instance.redirectUri?.let { url?.startsWith(it) } == true) {
                view?.stopLoading();
                return
            }
            super.onPageStarted(view, url, favicon)
        }

        override fun onPageFinished(view: WebView, url: String) {
            val displayRectangle = Rect()
            val window: Window? = instance.getWindow()
            window?.decorView?.getWindowVisibleDisplayFrame(displayRectangle)
            val layoutParams = view.layoutParams
            layoutParams.height = (displayRectangle.height() * 0.9f).toInt()
            view.layoutParams = layoutParams
            if (instance.redirectUri?.let { url.startsWith(it) } == true && !url.contains("error")) {
                instance.handleSuccess(url)
                return
            }
            super.onPageFinished(view, url)
        }

    }

    override fun handleSuccess(url: Any) {
        url as String
        appleLoginDialog?.dismiss()
        val response = getUrlValues(url)
        response["id_token"]?.let { idToken ->
            response["code"]?.let { code ->
                socialLoginCallBack?.onSuccess(
                    SocialTypeEnum.APPLE,
                    idToken,
                    code
                )
            }
        }
    }

    companion object {

        private var appleAuthURLFull: String? = null
        private var appleLoginDialog: Dialog? = null

        @Volatile
        private var instance: AppleLoginHandlerWebView? = null

        fun getInstance(
            activity: ComponentActivity? = null,
            fragment: Fragment? = null,
            clientId: String?,
            redirectUri: String?
        ): AppleLoginHandlerWebView =
            instance
                ?: AppleLoginHandlerWebView(
                    activity, fragment, clientId, redirectUri
                ).also { instance = it }

        fun recreateInstance(
            activity: ComponentActivity? = null,
            fragment: Fragment? = null,
            clientId: String?,
            redirectUri: String?
        ): AppleLoginHandlerWebView {
            instance = null
            return instance
                ?: AppleLoginHandlerWebView(
                    activity, fragment, clientId, redirectUri
                ).also { instance = it }
        }
    }

}