package com.sedo.sociallogin.helpers

import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.OAuthProvider
import com.sedo.sociallogin.`interface`.SocialLoginCallBack
import com.sedo.sociallogin.data.enums.SocialTypeEnum

class AppleLoginHandler private constructor(
    mActivity: ComponentActivity,
    mFragment: Fragment? = null
) : ISocialLogin(mActivity, mFragment) {

    var provider: OAuthProvider.Builder? = null
    var firebaseAuth: FirebaseAuth? = null

    override fun showError(show: Boolean) {
        this.showError = show
    }

    override fun setCallBack(socialLoginCallBack: SocialLoginCallBack) {
        this.socialLoginCallBack = socialLoginCallBack
    }

    override fun setRegisterResult(resultLauncher: ActivityResultLauncher<Intent>) {
        this.resultLauncher = resultLauncher
    }


    //  Apple Login issues
    override fun initMethod() {
        provider = OAuthProvider.newBuilder("apple.com")
        provider?.scopes = arrayOf("email", "name").toMutableList()
        provider?.addCustomParameter("locale", "en")
        firebaseAuth = FirebaseAuth.getInstance()
    }

    override fun startMethod() {
        try {
            val pending = firebaseAuth?.pendingAuthResult
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
            activity?.let {
                provider?.build()?.let { it1 ->
                    firebaseAuth?.startActivityForSignInWithProvider(it, it1)
                        ?.addOnSuccessListener { authResult ->
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
                        }?.addOnFailureListener { e ->
                            // logA("Apple Sign In Fail -> " + e.message)
                            e.printStackTrace()
                        }
                }
            }
        } catch (e: Exception) {
            getContext()?.let {
                Toast.makeText(it, e.message, Toast.LENGTH_SHORT).show()
            }
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