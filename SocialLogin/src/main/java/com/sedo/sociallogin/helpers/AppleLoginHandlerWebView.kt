package com.sedo.sociallogin.helpers

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.view.Window
import android.webkit.*
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
            webView.settings.domStorageEnabled = true
            webView.addJavascriptInterface(FormDataInterface(), "FORMOUT")
            instance?.let {
                webView.webViewClient = AppleLoginWebView(it)
            }
            webView.loadUrl(url)
            appleLoginDialog?.setContentView(webView)
            appleLoginDialog?.show()
        }
    }

    private class AppleLoginWebView(val instance: AppleLoginHandlerWebView) : WebViewClient() {
        //        private val jsCode = "" + "function parseForm(form){" +
//                "var values='';" +
//                "for(var i=0 ; i< form.elements.length; i++){" +
//                "   values+=form.elements[i].name+'='+form.elements[i].value+'&'" +
//                "}" +
//                "var url=form.action;" +
//                "console.log('parse form fired');" +
//                "window.FORMOUT.processFormData(url,values);" +
//                "   }" +
//                "for(var i=0 ; i< document.forms.length ; i++){" +
//                "   parseForm(document.forms[i]);" +
//                "};"
        private val jsCode = "" + "function parseForm(form){" +
                "    var formData = new FormData(form);" +
//                "    // Convert formData object to URL-encoded string:" +
                "    var payload = new URLSearchParams(formData);" +
                "    const value = Object.fromEntries(formData.entries());"+
                "    console.log('parse form = '+ formData.get('code'));" +
                "    console.log('parse form = '+ formData.get('states'));" +
                "}" +
                "for(var i=0 ; i< document.forms.length ; i++){" +
                "   parseForm(document.forms[i]);" +
                "};"

        //        private val jsCode = "" + "function parseForm(form){" +
//                "console.log('parse form = '+ form);" +
//                "console.log('parse form1 = '+ form.toString());" +
//                "var values='';" +
//                "for(var i=0 ; i< form.elements.length; i++){" +
//                "console.log('parse'+ form.elements[i].);" +
//                "   values+=form.elements[i].name+'='+form.elements[i].value+'&'" +
//                "}" +
//                "var url=form.action;" +
//                "console.log('parse form fired');" +
//                "window.FORMOUT.processFormData(url,values);" +
//                "console.log(values);" +
//                "   }" +
//                "for(var i=0 ; i< document.forms.length ; i++){" +
//                "   parseForm(document.forms[i]);" +
//                "};"
//
//        private val jsCode = "" + "const constMock = window.fetch;" +
//                "window.fetch = function() {" +
////                "    if (arguments[1].method === 'post'){" +
////                "        bodyResults(arguments[1].body)" +
//                "        console.log(arguments[1].body);" +
////                "    }" +
////                "    return new Promise((resolve, reject) => {" +
////                "        constantMock.apply(this, arguments)" +
////                "            .then((response) => {" +
////                "                if(response.url.indexOf(\"/me\") > -1 && response.type != \"cors\"){" +
////                "                    console.log(response);" +
////                "                    // do something for specificconditions" +
////                "                }" +
////                "                resolve(response);" +
////                "            })" +
////                "            .catch((error) => {" +
////                "                reject(response);" +
////                "            })" +
////                "    });" +
//                "}"

        override

        fun shouldInterceptRequest(
            view: WebView?,
            request: WebResourceRequest?
        ): WebResourceResponse? {

            return super.shouldInterceptRequest(view, request)
        }

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

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
//            if (url.equals(instance.redirectUri)) {
////                Log.d(DEBUG_TAG,"return url cancelling");
//                view?.stopLoading();
//                return
//            }
            super.onPageStarted(view, url, favicon)
        }

        override fun onPageFinished(view: WebView, url: String) {

            val displayRectangle = Rect()
            val window: Window? = instance.getWindow()
            window?.decorView?.getWindowVisibleDisplayFrame(displayRectangle)
            val layoutparms = view.layoutParams
            layoutparms.height = (displayRectangle.height() * 0.9f).toInt()
            view.layoutParams = layoutparms
//            if (url == instance.redirectUri) {
//                return
//            }
            view.loadUrl("javascript:(function() { $jsCode})()");
            super.onPageFinished(view, url)
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

    private class FormDataInterface {
        @JavascriptInterface
        fun processFormData(url: String, formData: String) {
//            Log.d(DEBUG_TAG, "Url:$url form data $formData")
            if (url == instance?.redirectUri) {
                val map: HashMap<String, String> = HashMap()
                val values = formData.split("&").toTypedArray()
                for (pair in values) {
                    val nameValue = pair.split("=").toTypedArray()
                    if (nameValue.size == 2) {
//                        Log.d(DEBUG_TAG, "Name:" + nameValue[0] + " value:" + nameValue[1])
                        map[nameValue[0]] = nameValue[1]
                    }
                }
                return
            }
        }
    }
}