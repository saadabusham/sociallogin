package com.sedo.sociallogin.helpers

import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.fragment.app.Fragment
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginBehavior
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton
import com.sedo.sociallogin.data.enums.SocialTypeEnum
import com.sedo.sociallogin.utils.Constants.FacebookConstants.EMAIL_PERMISSION
import com.sedo.sociallogin.utils.Constants.FacebookConstants.PUBLIC_PROFILE_PERMISSION

class FacebookLoginHandler private constructor(
    mActivity: ComponentActivity,
    mFragment: Fragment? = null
) : ISocialLogin(mActivity, mFragment) {

    private var facebookLoginButton: LoginButton? = null
    private var facebookCallbackManager: CallbackManager? = null

    override fun initMethod() {
        this.facebookCallbackManager = CallbackManager.Factory.create()
        this.facebookLoginButton = LoginButton(getContext())
        this.facebookLoginButton?.loginBehavior = LoginBehavior.NATIVE_WITH_FALLBACK
        this.facebookLoginButton?.setReadPermissions(
            listOf(
                PUBLIC_PROFILE_PERMISSION,
                EMAIL_PERMISSION
            )
        )
        registerFacebookCallback()
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

    private fun registerFacebookCallback() {
        facebookLoginButton?.registerCallback(
            facebookCallbackManager,
            object : FacebookCallback<LoginResult> {
                override fun onSuccess(loginResult: LoginResult) {
                    handleSuccess(loginResult)
                }

                override fun onCancel() {
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

    override fun handleSuccess(loginResult: Any) {
        loginResult as LoginResult
        val accessToken = loginResult.accessToken.token
        socialLoginCallBack?.onSuccess(
            SocialTypeEnum.FACEBOOK,
            accessToken
        )
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