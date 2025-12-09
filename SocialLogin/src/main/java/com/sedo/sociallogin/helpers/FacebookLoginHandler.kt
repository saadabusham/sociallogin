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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FacebookLoginHandler private constructor(
    mActivity: ComponentActivity,
    mFragment: Fragment? = null
) : ISocialLogin(mActivity, mFragment) {

    private var facebookLoginButton: LoginButton? = null
    private var facebookCallbackManager: CallbackManager? = null

    override fun initMethod() {
        this.facebookCallbackManager = CallbackManager.Factory.create()
        this.facebookLoginButton = getContext()?.let { LoginButton(it) }
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
            showToast(msg = e.message ?: "Error")
        }
    }

    private fun registerFacebookCallback() {
        facebookCallbackManager?.let {
            facebookLoginButton?.registerCallback(
                it,
                object : FacebookCallback<LoginResult> {
                    override fun onSuccess(result: LoginResult) {
                        handleSuccess(result)
                    }

                    override fun onCancel() {
                        showToast(msg = "Facebook login canceled")
                    }

                    override fun onError(error: FacebookException) {
                        showToast(msg = error.message ?: "Error")
                    }
                })
        }
    }

    override fun handleSuccess(data: Any) {
        data as LoginResult
        val accessToken = data.accessToken.token
        CoroutineScope(Dispatchers.Main).launch {
            socialLoginCallBack?.onSuccess(
                SocialTypeEnum.FACEBOOK,
                accessToken
            )
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