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
    val clientId:String?=null
) {
    abstract fun showError(show: Boolean)
    abstract fun setCallBack(socialLoginCallBack: SocialLoginCallBack)
    abstract fun setRegisterResult(resultLauncher: ActivityResultLauncher<Intent>)
    abstract fun initMethod()
    abstract fun startMethod()
    open fun setResult(data: Any){}
    open fun handleResult(data: Any){}

    fun getContext() = activity ?: fragment?.requireContext()
    fun getWindow() = activity?.window ?: fragment?.requireActivity()?.window

    protected var showError: Boolean? = false
    protected var socialLoginCallBack: SocialLoginCallBack? = null
    protected var resultLauncher: ActivityResultLauncher<Intent>? = null
    protected var defaultResultLauncher: ActivityResultLauncher<Intent>? =
        activity?.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                handleResult(task)
            }
        }
            ?: fragment?.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                    handleResult(task)
                }
            }
}
