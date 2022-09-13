package com.sedo.sociallogin.helpers

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Rect
import android.view.Window
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.Fragment
import com.sedo.sociallogin.`interface`.SocialLoginCallBack
import com.sedo.sociallogin.data.enums.SocialTypeEnum
import java.io.UnsupportedEncodingException
import java.net.URLDecoder
import java.util.*

class AppleLoginHandlerWebView private constructor(
    mActivity: ComponentActivity? = null,
    mFragment: Fragment? = null,
    clientId: String?,
    redirectUri: String?
) : ISocialLogin(mActivity, mFragment, clientId, redirectUri) {

    override fun showError(show: Boolean) {
        this.showError = show
    }

    override fun setCallBack(socialLoginCallBack: SocialLoginCallBack) {
        this.socialLoginCallBack = socialLoginCallBack
    }

    override fun setRegisterResult(resultLauncher: ActivityResultLauncher<Intent>) {
        this.resultLauncher = resultLauncher
    }

    //  Google Login issues
    override fun initMethod() {
        val state: String = UUID.randomUUID().toString()
        appleAuthURLFull =
            "$AUTHURL?response_type=${RESPONSE_TYPE}&v=1.1.6&response_mode=${RESPONSE_MODE}"+"&client_id=$clientId"/*&scope=$SCOPE*/ + "&state=$state&redirect_uri=$redirectUri&usePopup=true"
    }

    override fun startMethod() {
        appleAuthURLFull?.let { openWebViewDialog(it) };
    }

    override fun setResult(completedTask: Any) {
        handleResult(completedTask)
    }

    override fun handleResult(completedTask: Any) {
        try {

        } catch (e: Exception) {
            Toast.makeText(
                getContext(),
                "signInResult:failed code=" + e.message,
                Toast.LENGTH_SHORT
            ).show()
        }
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
            if (instance.redirectUri?.let { url.startsWith(it) } == true) {
                appleLoginDialog?.dismiss()
                val response = getUrlValues(url)
                response["id_token"]?.let { idToken ->
                    response["code"]?.let { code ->
                        instance.socialLoginCallBack?.onSuccess(
                            SocialTypeEnum.APPLE,
                            idToken,
                            code
                        )
                    }
                }
                return
            }
            super.onPageFinished(view, url)
        }

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

    companion object {
        const val AUTHURL = "https://appleid.apple.com/auth/authorize"
        const val SCOPE = "name%20email"
        const val RESPONSE_TYPE = "code%20id_token"
        const val RESPONSE_MODE = "fragment"

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