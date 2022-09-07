package com.sedo.sociallogin.helpers

import android.content.Intent
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.Fragment
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.FacebookSdk
import com.facebook.login.LoginBehavior
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton
import com.sedo.sociallogin.`interface`.SocialLoginCallBack
import com.sedo.sociallogin.data.enums.SocialTypeEnum

class FacebookLoginHandler private constructor(
    mActivity: ComponentActivity,
    mFragment: Fragment? = null
) : ISocialLogin(mActivity, mFragment) {
    private var facebookLoginButton: LoginButton? = null
    private var facebookCallbackManager: CallbackManager? = null

    override fun showError(show: Boolean) {
        this.showError = show
    }

    override fun setCallBack(socialLoginCallBack: SocialLoginCallBack) {
        this.socialLoginCallBack = socialLoginCallBack
    }

    override fun setRegisterResult(resultLauncher: ActivityResultLauncher<Intent>) {
        this.resultLauncher = resultLauncher
    }


    //  Facebook Login issues
    override fun initMethod() {
        this.facebookCallbackManager = CallbackManager.Factory.create()
        this.facebookLoginButton = LoginButton(getContext())
        this.facebookLoginButton?.loginBehavior = LoginBehavior.NATIVE_WITH_FALLBACK
        this.facebookLoginButton?.setReadPermissions(
            listOf(
                "public_profile",
                "email"
            )
        )
        registerFacebookCallback()
    }

    private fun registerFacebookCallback() {
        facebookLoginButton?.registerCallback(
            facebookCallbackManager,
            object : FacebookCallback<LoginResult> {
                override fun onSuccess(loginResult: LoginResult) {
                    val accessToken = loginResult.accessToken.token
                    socialLoginCallBack?.onSuccess(
                        SocialTypeEnum.FACEBOOK,
                        accessToken
                    )
//                    val g = GraphRequest.newMeRequest(
//                        loginResult.accessToken
//                    ) { dataObject, response ->
//                        Log.d("face", dataObject.toString())
//                        socialLoginCallBack?.onSuccess(
//                            SocialTypeEnum.FACEBOOK,
//                            accesstoken
//                        )
//                    }
//                    val para = Bundle()
//                    para.putString("fields", "id,first_name,last_name,email,gender")
//                    g.parameters = para
//                    g.executeAsync()
                }

                override fun onCancel() {
                    // App code
                    getContext()?.let {
                        Toast.makeText(it, "Facebook login canceled", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onError(error: FacebookException) {
                    getContext()?.let {
                        Toast.makeText(it, error.message, Toast.LENGTH_SHORT).show()
                    }
                }
            })
    }

    override fun startMethod() {
        try {
            facebookLoginButton?.callOnClick()
        } catch (e: Exception) {
            getContext()?.let {
                Toast.makeText(it, e.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {

        @Volatile
        private var instance: FacebookLoginHandler? = null

        fun getInstance(
            activity: ComponentActivity,
            fragment: Fragment? = null
        ): FacebookLoginHandler =
            instance
                ?: FacebookLoginHandler(
                    activity, fragment
                ).also { instance = it }

        fun recreateInstance(
            activity: ComponentActivity,
            fragment: Fragment? = null
        ): FacebookLoginHandler {
            instance = null
            return instance
                ?: FacebookLoginHandler(
                    activity, fragment
                ).also { instance = it }
        }
    }

}