package com.sedo.sociallogin.helpers

import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GetTokenResult
import com.google.firebase.auth.OAuthProvider
import com.sedo.sociallogin.data.enums.SocialTypeEnum

class AppleLoginHandler private constructor(
    mActivity: ComponentActivity,
    mFragment: Fragment? = null
) : ISocialLogin(mActivity, mFragment) {

    var provider: OAuthProvider.Builder? = null
    var firebaseAuth: FirebaseAuth? = null

    override fun initMethod() {
        provider = OAuthProvider.newBuilder("apple.com")
        provider?.scopes = arrayOf("email", "name").toMutableList()
        provider?.addCustomParameter("locale", "en")
        firebaseAuth = FirebaseAuth.getInstance()
    }

    override fun startMethod() {
        try {
            val pending = firebaseAuth?.pendingAuthResult
            if (pending != null) {
                firebaseAuth?.signOut()
            }
            launchAppleLogin()
        } catch (e: Exception) {
            getContext()?.let {
                Toast.makeText(it, e.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun launchAppleLogin() {
        activity?.let {
            provider?.build()?.let { it1 ->
                firebaseAuth?.startActivityForSignInWithProvider(it, it1)
                    ?.addOnSuccessListener { authResult ->
                        val user = authResult.user
                        user?.getIdToken(false)?.addOnSuccessListener {
                            handleSuccess(it)
                        }?.addOnFailureListener {

                        }
                    }?.addOnFailureListener { e ->
                        // logA("Apple Sign In Fail -> " + e.message)
                        e.printStackTrace()
                    }
            }
        }
    }

    override fun handleSuccess(data: Any) {
        data as GetTokenResult
        data.token?.let { it1 ->
            instance?.socialLoginCallBack?.onSuccess(
                SocialTypeEnum.APPLE,
                it1
            )
        }
    }

    companion object {

        @Volatile
        private var instance: AppleLoginHandler? = null

        fun getInstance(
            activity: ComponentActivity,
            fragment: Fragment? = null
        ): AppleLoginHandler =
            instance
                ?: AppleLoginHandler(
                    activity, fragment
                ).also { instance = it }

        fun recreateInstance(
            activity: ComponentActivity,
            fragment: Fragment? = null
        ): AppleLoginHandler {
            instance = null
            return instance
                ?: AppleLoginHandler(
                    activity, fragment
                ).also { instance = it }
        }
    }

}