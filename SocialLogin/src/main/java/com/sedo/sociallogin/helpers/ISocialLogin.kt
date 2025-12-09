package com.sedo.sociallogin.helpers

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.sedo.sociallogin.`interface`.SocialLoginCallBack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

abstract class ISocialLogin(
    val activity: ComponentActivity?,
    val fragment: Fragment?,
    val clientId: String? = null,
    val redirectUri: String? = null,
    val fullUrl: String? = null
) {
    protected var showError: Boolean? = false
    protected var socialLoginCallBack: SocialLoginCallBack? = null
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
    fun getContext() = activity ?: fragment?.requireContext()
    fun getWindow() = activity?.window ?: fragment?.requireActivity()?.window
    fun showToast(msg: String, callback: Boolean = true) {
        getContext()?.let {
            Toast.makeText(it, msg, Toast.LENGTH_SHORT).show()
        }
        if (callback) {
            CoroutineScope(Dispatchers.Main).launch {
                socialLoginCallBack?.onFailure(msg)
            }
        }
    }
}
