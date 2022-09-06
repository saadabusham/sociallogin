package com.sedo.sociallogin.helpers

import android.content.Intent
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.OAuthProvider
import com.sedo.sociallogin.`interface`.SocialLoginCallBack
import com.sedo.sociallogin.data.enums.SocialTypeEnum

class AppleLoginHandler private constructor(
    mActivity: ComponentActivity? = null,
    mFragment: Fragment? = null
) : ISocialLogin(mActivity, mFragment) {

    companion object {

        @Volatile
        private var instance: AppleLoginHandler? = null

        fun getInstance(
            activity: ComponentActivity? = null,
            fragment: Fragment? = null
        ): AppleLoginHandler =
            instance
                ?: AppleLoginHandler(
                    activity, fragment
                ).also { instance = it }

        fun recreateInstance(
            activity: ComponentActivity? = null,
            fragment: Fragment? = null
        ): AppleLoginHandler {
            instance = null
            return instance
                ?: AppleLoginHandler(
                    activity, fragment
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

    }

    override fun startMethod() {
        val provider = OAuthProvider.newBuilder("apple.com")
        provider.scopes = arrayOf("email", "name").toMutableList()
        provider.addCustomParameter("locale", "en")

        val auth: FirebaseAuth = FirebaseAuth.getInstance()
        val pending = auth.pendingAuthResult

        pending?.addOnSuccessListener { authResult ->
            //success
            val user = authResult.user
            user?.getIdToken(false)?.addOnSuccessListener {
                it.token?.let { it1 ->
                    instance?.socialLoginCallBack?.onSuccess(
                        SocialTypeEnum.APPLE,
                        it1
                    )
                }
            }?.addOnFailureListener {

            }
        }?.addOnFailureListener { e ->
            Log.d("aaaa", "Apple addOnFailureListener -> " + e.message)
        }
        fragment?.requireActivity()?.let {
            auth.startActivityForSignInWithProvider(it, provider.build())
                .addOnSuccessListener { authResult ->
                    // Sign-in successful!

                    val user = authResult.user
                    user?.getIdToken(false)?.addOnSuccessListener {
                        it.token?.let { it1 ->
                            instance?.socialLoginCallBack?.onSuccess(
                                SocialTypeEnum.APPLE,
                                it1
                            )
                        }
                    }?.addOnFailureListener {

                    }
                    //                val abc= user!!.providerData[1]
                    //                if (abc.uid != "") {
                    //                    id=abc.uid
                    //                }
                    //                if (!user.displayName.isNullOrEmpty()) {
                    //                    userName = user!!.displayName!!
                    //                }else{
                    //                    userName=""
                    //                }
                    //
                    //                if (user.email != "") {
                    //                    mail= user!!.email!!
                    //                }

                    // logA("Apple Sign In Success -> “
                }
                .addOnFailureListener { e ->
                    // logA("Apple Sign In Fail -> " + e.message)
                    e.printStackTrace()
                }
        }
    }
}