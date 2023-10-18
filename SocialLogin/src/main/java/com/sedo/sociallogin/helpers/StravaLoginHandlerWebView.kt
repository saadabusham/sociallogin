package com.sedo.sociallogin.helpers

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Bitmap
import android.os.Message
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebView.WebViewTransport
import android.webkit.WebViewClient
import android.widget.LinearLayout
import androidx.activity.ComponentActivity
import androidx.fragment.app.Fragment
import com.sedo.sociallogin.*
import com.sedo.sociallogin.data.enums.SocialTypeEnum
import com.sedo.sociallogin.utils.Constants.StravaConstants.APPROVAL_TYPE
import com.sedo.sociallogin.utils.Constants.StravaConstants.AUTHURL
import com.sedo.sociallogin.utils.Constants.StravaConstants.SCOPE
import com.sedo.sociallogin.utils.Constants.StravaConstants.RESPONSE_TYPE
import com.sedo.sociallogin.utils.URLBuilder
import com.sedo.sociallogin.utils.UrlUtils.getUrlValues
import java.util.*


class StravaLoginHandlerWebView private constructor(
    mActivity: ComponentActivity? = null,
    mFragment: Fragment? = null,
    clientId: String? = null,
    redirectUri: String?,
    fullUrl: String? = null
) : ISocialLogin(mActivity, mFragment, clientId, redirectUri, fullUrl) {
    var webView: WebView? = null
    override fun initMethod() {
        authURLFull = if (fullUrl.isNullOrEmpty()) {
            URLBuilder.buildStravaAuthUrl(
                authUrl = AUTHURL,
                responseType = RESPONSE_TYPE,
                approvalType = APPROVAL_TYPE,
                clientId = clientId,
                redirectUri = redirectUri,
                scope = SCOPE
            ).toString()
        } else {
            fullUrl
        }
    }

    override fun startMethod() {
        authURLFull?.let { openWebViewDialog(it) };
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun openWebViewDialog(url: String) {
        getContext()?.let {
            loginDialog = Dialog(it, R.style.FullScreenTransparentDialog)
            loginDialog?.setContentView(R.layout.webview_dialog)
            webView = loginDialog?.findViewById(R.id.webview)
            webView?.settings?.javaScriptCanOpenWindowsAutomatically = true
            webView?.settings?.javaScriptEnabled = true
            webView?.settings?.domStorageEnabled = true
            val webSettings = webView?.settings
            webSettings?.javaScriptEnabled = true;
            webSettings?.domStorageEnabled = true;
            webSettings?.setSupportMultipleWindows(true);
            webSettings?.javaScriptCanOpenWindowsAutomatically = true;
            webSettings?.allowFileAccess = true;
            webSettings?.allowContentAccess = true;
            instance?.let {
                webView?.webViewClient = StravaLoginWebView(it)
                webView?.webChromeClient = StravaLoginWebChromeClient(it)
            }
            webView?.loadUrl(url)
            loginDialog?.show()
        }
    }

    private class StravaLoginWebChromeClient(val instance: StravaLoginHandlerWebView) :
        WebChromeClient() {
        @SuppressLint("SetJavaScriptEnabled")
        override fun onCreateWindow(
            view: WebView,
            isDialog: Boolean,
            isUserGesture: Boolean,
            resultMsg: Message
        ): Boolean {
            val newWebView = instance.getContext()?.let { WebView(it) }
            newWebView?.settings?.javaScriptEnabled = true
            newWebView?.settings?.javaScriptCanOpenWindowsAutomatically = true
            newWebView?.settings?.setSupportMultipleWindows(true)
            newWebView?.settings?.domStorageEnabled = true
            newWebView?.settings?.allowFileAccess = true
            newWebView?.settings?.allowContentAccess = true
            newWebView?.layoutParams =
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                ) //making sure the popup opens full screen
            view.addView(newWebView)
            val transport = resultMsg.obj as WebViewTransport
            transport.webView = newWebView
            resultMsg.sendToTarget()
            newWebView?.webViewClient = object : WebViewClient() {
                @Deprecated("Deprecated in Java")
                override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                    view.loadUrl(url)
                    return true
                }
            }
            newWebView?.webChromeClient = object : WebChromeClient() {
                override fun onCloseWindow(window: WebView) {
                    super.onCloseWindow(window)
                    if (newWebView != null) {
                        instance.webView?.removeView(newWebView)
                    }
                }
            }
            return true
        }

        override fun onCloseWindow(window: WebView?) {
            super.onCloseWindow(window)
        }
    }

    private class StravaLoginWebView(val instance: StravaLoginHandlerWebView) : WebViewClient() {

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            if (instance.redirectUri?.let { url?.startsWith(it) } == true) {
                view?.stopLoading();
                return
            }
            super.onPageStarted(view, url, favicon)
        }

        override fun onPageFinished(view: WebView, url: String) {
            if (instance.redirectUri?.let { url.startsWith(it) } == true && !url.contains("error")) {
                instance.handleSuccess(url)
                return
            }
            super.onPageFinished(view, url)
        }

    }

    override fun handleSuccess(data: Any) {
        data as String
        loginDialog?.dismiss()
        val response = getUrlValues(data)
        val code = response["code"]
        response["id_token"].let { idToken ->
            code.let { code ->
                socialLoginCallBack?.onSuccess(
                    SocialTypeEnum.STRAVA,
                    idToken,
                    code
                )
            }
        }
    }

    companion object {

        private var authURLFull: String? = null
        private var loginDialog: Dialog? = null

        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var instance: StravaLoginHandlerWebView? = null

        fun getInstance(
            activity: ComponentActivity? = null,
            fragment: Fragment? = null,
            clientId: String? = null,
            redirectUri: String?,
            fullUrl: String? = null
        ): StravaLoginHandlerWebView =
            instance
                ?: StravaLoginHandlerWebView(
                    activity, fragment, clientId, redirectUri, fullUrl
                ).also { instance = it }

        fun recreateInstance(
            activity: ComponentActivity? = null,
            fragment: Fragment? = null,
            clientId: String? = null,
            redirectUri: String?,
            fullUrl: String? = null
        ): StravaLoginHandlerWebView {
            instance = null
            return instance
                ?: StravaLoginHandlerWebView(
                    activity, fragment, clientId, redirectUri, fullUrl
                ).also { instance = it }
        }
    }

}