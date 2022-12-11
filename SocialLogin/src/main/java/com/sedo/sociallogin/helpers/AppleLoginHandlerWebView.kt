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
import com.sedo.sociallogin.utils.Constants.AppleConstants.AUTHURL
import com.sedo.sociallogin.utils.Constants.AppleConstants.RESPONSE_MODE
import com.sedo.sociallogin.utils.Constants.AppleConstants.RESPONSE_TYPE
import com.sedo.sociallogin.utils.URLBuilder
import com.sedo.sociallogin.utils.UrlUtils.getUrlValues
import java.util.*


class AppleLoginHandlerWebView private constructor(
    mActivity: ComponentActivity? = null,
    mFragment: Fragment? = null,
    clientId: String? = null,
    redirectUri: String?,
    fullUrl: String? = null
) : ISocialLogin(mActivity, mFragment, clientId, redirectUri, fullUrl) {
    var webView: WebView? = null
    override fun initMethod() {
        appleAuthURLFull = if (fullUrl.isNullOrEmpty()) {
            val state: String = UUID.randomUUID().toString()
            URLBuilder.buildAppleAuthUrl(
                authUrl = AUTHURL,
                responseType = RESPONSE_TYPE,
                responseMode = RESPONSE_MODE,
                clientId = clientId,
                state = state,
                redirectUri = redirectUri,
            )
        } else {
            fullUrl
        }
    }

    override fun startMethod() {
        appleAuthURLFull?.let { openWebViewDialog(it) };
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun openWebViewDialog(url: String) {
        getContext()?.let {
            appleLoginDialog = Dialog(it, R.style.FullScreenTransparentDialog)
            appleLoginDialog?.setContentView(R.layout.apple_dialog)
            webView = appleLoginDialog?.findViewById(R.id.webview)
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
            webSettings?.allowUniversalAccessFromFileURLs = true;
            webSettings?.allowFileAccessFromFileURLs = true;
            instance?.let {
                webView?.webViewClient = AppleLoginWebView(it)
                webView?.webChromeClient = AppleLoginWebChromeClient(it)
            }
            webView?.loadUrl(url)
            appleLoginDialog?.show()
        }
    }

    private class AppleLoginWebChromeClient(val instance: AppleLoginHandlerWebView) :
        WebChromeClient() {
        // popup webview!
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
            newWebView?.settings?.allowFileAccessFromFileURLs = true
            newWebView?.settings?.allowUniversalAccessFromFileURLs = true
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

    private class AppleLoginWebView(val instance: AppleLoginHandlerWebView) : WebViewClient() {

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

    override fun handleSuccess(url: Any) {
        url as String
        appleLoginDialog?.dismiss()
        val response = getUrlValues(url)
        val code = response["code"]
        response["id_token"].let { idToken ->
            code.let { code ->
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
            clientId: String? = null,
            redirectUri: String?,
            fullUrl: String? = null
        ): AppleLoginHandlerWebView =
            instance
                ?: AppleLoginHandlerWebView(
                    activity, fragment, clientId, redirectUri, fullUrl
                ).also { instance = it }

        fun recreateInstance(
            activity: ComponentActivity? = null,
            fragment: Fragment? = null,
            clientId: String? = null,
            redirectUri: String?,
            fullUrl: String? = null
        ): AppleLoginHandlerWebView {
            instance = null
            return instance
                ?: AppleLoginHandlerWebView(
                    activity, fragment, clientId, redirectUri, fullUrl
                ).also { instance = it }
        }
    }

}