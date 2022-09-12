package com.sedo.sociallogin.helpers

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.util.Log
import android.view.Window
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.RequiresApi
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
            webView.settings.javaScriptCanOpenWindowsAutomatically = true
            webView.settings.javaScriptEnabled = true
            instance?.let {
                webView.webViewClient = AppleLoginWebView(it)
            }
            webView.loadUrl(url)
            appleLoginDialog?.setContentView(webView)
            appleLoginDialog?.show()
        }
    }

    private class AppleLoginWebView(val instance: AppleLoginHandlerWebView) : WebViewClient() {
        // for API levels < 24
        override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
            return isUrlOverridden(view, Uri.parse(url))
        }

        @RequiresApi(Build.VERSION_CODES.N)
        override fun shouldOverrideUrlLoading(
            view: WebView?,
            request: WebResourceRequest?
        ): Boolean {
            return isUrlOverridden(view, request?.url)
        }

        private fun isUrlOverridden(view: WebView?, url: Uri?): Boolean {
            return when {
                url == null -> {
                    false
                }
                url.toString().contains("appleid.apple.com") -> {
                    view?.loadUrl(url.toString())
                    true
                }
                instance.redirectUri?.let { url.toString().contains(it) } == true -> {
//                    Log.d(SignInWithAppleButton.SIGN_IN_WITH_APPLE_LOG_TAG, "Web view was forwarded to redirect URI")

                    val codeParameter = url.getQueryParameter("code")
                    val idToken = url.getQueryParameter("idToken")
                    val stateParameter = url.getQueryParameter("state")

                    when {
                        codeParameter == null -> {
//                            callback(SignInWithAppleResult.Failure(IllegalArgumentException("code not returned")))
                        }
//                        stateParameter != attempt.state -> {
////                            callback(SignInWithAppleResult.Failure(IllegalArgumentException("state does not match")))
//                        }
                        else -> {
                            instance.socialLoginCallBack?.onSuccess(
                                SocialTypeEnum.APPLE,
                                codeParameter
                            )
                        }
                    }

                    true
                }
                else -> {
                    false
                }
            }
        }

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