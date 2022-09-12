package com.sedo.sociallogin.helpers

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Rect
import android.util.Log
import android.view.Window
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.Fragment
import com.sedo.sociallogin.`interface`.SocialLoginCallBack
import com.sedo.sociallogin.data.enums.SocialTypeEnum
import com.willowtreeapps.signinwithapplebutton.SignInWithAppleConfiguration
import com.willowtreeapps.signinwithapplebutton.SignInWithAppleResult
import com.willowtreeapps.signinwithapplebutton.view.SignInWithAppleButton
import java.io.UnsupportedEncodingException
import java.net.URLDecoder
import java.util.*

class AppleLoginHandlerWebView private constructor(
    mActivity: ComponentActivity? = null,
    mFragment: Fragment? = null,
    clientId: String?,
    redirectUri: String?
) : ISocialLogin(mActivity, mFragment, clientId, redirectUri) {

    companion object {
        const val AUTHURL = "https://appleid.apple.com/auth/authorize"
        const val SCOPE = "name%20email"

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
            "$AUTHURL?response_type=code&v=1.1.6&response_mode=form_post&client_id=$clientId&scope=$SCOPE&state=$state&redirect_uri=$redirectUri&usePopup=true"
    }

    override fun startMethod() {
        appleAuthURLFull?.let { openWebViewDialog(it) };
//
//        val configuration = SignInWithAppleConfiguration(
//            clientId = clientId ?: "",
//            redirectUri = redirectUri ?: "",
//            scope = "email"
//        )
//
//        val signInWithAppleButton = getContext()?.let { SignInWithAppleButton(it) }
//        fragment?.childFragmentManager?.let {
//            signInWithAppleButton?.setUpSignInWithAppleOnClick(it, configuration) { result ->
//                when (result) {
//                    is SignInWithAppleResult.Success -> {
//                        // Handle success
//                        socialLoginCallBack?.onSuccess(
//                            SocialTypeEnum.APPLE,
//                            result.authorizationCode
//                        )
//                    }
//                    is SignInWithAppleResult.Failure -> {
//                        // Handle failure
//                    }
//                    is SignInWithAppleResult.Cancel -> {
//                        // Handle user cancel
//                    }
//                }
//            }
//            signInWithAppleButton?.callOnClick()
//        }
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
            webView.isVerticalFadingEdgeEnabled
            webView.isHorizontalScrollBarEnabled = false
            webView.isVerticalScrollBarEnabled = false
            instance?.let {
                webView.webViewClient = AppleLoginWebView(it)
            }
            webView.settings.javaScriptEnabled = true
            webView.loadUrl(url)
            appleLoginDialog?.setContentView(webView)
            appleLoginDialog?.show()
        }
    }

    private class AppleLoginWebView(val instance: AppleLoginHandlerWebView) : WebViewClient() {
        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            if (instance.redirectUri?.let { url?.startsWith(it) } == true) {
                // Close the dialog after getting the authorization code
                if (url?.contains("success=") == true) {

                }
                appleLoginDialog?.dismiss()
                val values = getUrlValues(url ?: "")
                if (values.isNotEmpty() && values["idToken"] != null) {
                    appleLoginDialog?.dismiss()
                    values["idToken"]?.let {
                        instance.socialLoginCallBack?.onSuccess(
                            SocialTypeEnum.APPLE,
                            it
                        )
                    }
                }
            }
        }

        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
            Log.i("TAG", request.url.toString())
            try {
                val values = getUrlValues(request.url.toString())

                // Get Values Fro URL and use the values as needed
                appleLoginDialog?.dismiss()
            } catch (e: UnsupportedEncodingException) {
                Log.e("Error", e.message ?: "")
            }
            return true
        }

        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            Log.i("TAG", url)
            if (instance.redirectUri?.let { url.startsWith(it) } == true) {
                // Close the dialog after getting the authorization code
                if (url.contains("success=")) {
                    appleLoginDialog?.dismiss()
                    val values = getUrlValues(url)
                    if (values.isNotEmpty() && values["idToken"] != null) {
                        appleLoginDialog?.dismiss()
                        values["idToken"]?.let {
                            instance.socialLoginCallBack?.onSuccess(
                                SocialTypeEnum.APPLE,
                                it
                            )
                        }
                    }
                }
                return true
            }
            return false
        }
//
//        @Nullable
//        override fun shouldInterceptRequest(
//            view: WebView,
//            request: WebResourceRequest
//        ): WebResourceResponse? {
//            Log.i("TAG", "shouldInterceptRequest: reuest url is " + request.url.toString())
//            try {
//                val values = getUrlValues(request.url.toString())
////                val email = values["email"]
////                val idToken = values["idToken"]
//                if (!values.isNullOrEmpty() && values["idToken"] != null) {
//                    appleLoginDialog?.dismiss()
//                    values["idToken"]?.let {
//                        instance.socialLoginCallBack?.onSuccess(
//                            SocialTypeEnum.APPLE,
//                            it
//                        )
//                    }
//                }
//            } catch (e: UnsupportedEncodingException) {
//                Log.e("Error", e.message ?: "")
//            }
//            return super.shouldInterceptRequest(view, request)
//        }

        override fun onPageFinished(view: WebView, url: String) {
            super.onPageFinished(view, url)
            val displayRectangle = Rect()
            val window: Window? = instance.getWindow()
            window?.decorView?.getWindowVisibleDisplayFrame(displayRectangle)
            val layoutparms = view.layoutParams
            layoutparms.height = (displayRectangle.height() * 0.9f).toInt()
            view.layoutParams = layoutparms
        }

        @Throws(UnsupportedEncodingException::class)
        fun getUrlValues(url: String): Map<String, String?> {
            val i = url.indexOf("?")
            val paramsMap: MutableMap<String, String?> = HashMap()
            if (i > -1) {
                val searchURL = url.substring(url.indexOf("?") + 1)
                val params = searchURL.split("&").toTypedArray()
                for (param in params) {
                    val temp = param.split("=").toTypedArray()
                    paramsMap[temp[0]] = URLDecoder.decode(temp[1], "UTF-8")
                }
            }
            return paramsMap
        }
    }
}