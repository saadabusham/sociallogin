package com.sedo.sociallogin.helpers

import android.app.Activity
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.sedo.sociallogin.`interface`.SocialLoginCallBack

abstract class ISocialLogin(
    val activity: ComponentActivity?,
    val fragment: Fragment?,
    val clientId: String? = null,
    val redirectUri: String? = null,
    val fullUrl: String? = null
) {
    protected var showError: Boolean? = false
    protected var socialLoginCallBack: SocialLoginCallBack? = null
    protected var resultLauncher: ActivityResultLauncher<Intent>? = null
    protected var defaultResultLauncher: ActivityResultLauncher<Intent>? =
        fragment?.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                handleResult(task)
            }
        }
            ?: activity?.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                    handleResult(task)
                }
            }

    abstract fun initMethod()
    abstract fun startMethod()
    abstract fun handleSuccess(data: Any)
    open fun setResult(data: Any) {}
    open fun handleResult(data: Any) {}
    open fun showError(show: Boolean){
        this.showError = show
    }
    open fun setCallBack(socialLoginCallBack: SocialLoginCallBack){
        this.socialLoginCallBack = socialLoginCallBack
    }
    open fun setRegisterResult(resultLauncher: ActivityResultLauncher<Intent>){
        this.resultLauncher = resultLauncher
    }

    fun getContext() = activity ?: fragment?.requireContext()
    fun getWindow() = activity?.window ?: fragment?.requireActivity()?.window

}
