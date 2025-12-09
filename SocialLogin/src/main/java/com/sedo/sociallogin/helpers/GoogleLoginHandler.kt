package com.sedo.sociallogin.helpers

import android.os.CancellationSignal
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.credentials.CredentialManager
import androidx.credentials.CredentialManagerCallback
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import androidx.fragment.app.Fragment
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.sedo.sociallogin.data.enums.SocialTypeEnum
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class GoogleLoginHandler private constructor(
    mActivity: ComponentActivity? = null,
    mFragment: Fragment? = null,
    clientId: String?
) : ISocialLogin(mActivity, mFragment, clientId) {

    private var credentialManager: CredentialManager? = null

    override fun initMethod() {
        credentialManager = getContext()?.let { CredentialManager.create(it) }
    }

    override fun startMethod() {
        try {
            launchGoogleLogin()
        } catch (e: Exception) {
            showToast(msg = e.message ?: "Error")
        }
    }

    private fun launchGoogleLogin() {
        val context = getContext() ?: return
        val id = clientId ?: return

        val googleIdOption = GetGoogleIdOption.Builder()
            .setServerClientId(id)
            .setFilterByAuthorizedAccounts(false)
            .setAutoSelectEnabled(true)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        val cancellationSignal = CancellationSignal()
        cancellationSignal.setOnCancelListener {
            showToast(msg = "Canceled")
        }

        credentialManager?.getCredentialAsync(
            context = context,
            request = request,
            cancellationSignal = cancellationSignal,
            executor = { obj: Runnable? -> obj!!.run() },
            callback = object :
                CredentialManagerCallback<GetCredentialResponse, GetCredentialException> {
                override fun onResult(result: GetCredentialResponse) {
                    setResult(result)
                }

                override fun onError(e: GetCredentialException) {
                    showToast(msg = "Google login failed: ${e.message}")
                }
            }
        )
    }

    override fun setResult(data: Any) {
        handleResult(data)
    }

    override fun handleResult(data: Any) {
        try {
            val res = data as GetCredentialResponse
            val credential = res.credential
            val googleIdTokenCredential: GoogleIdTokenCredential =
                GoogleIdTokenCredential.createFrom((credential.data))

            handleSuccess(googleIdTokenCredential)

        } catch (e: Exception) {
            showToast(msg = "Google login failed: ${e.message}")
        }
    }

    override fun handleSuccess(data: Any) {
        data as GoogleIdTokenCredential
        val token = data.idToken
        CoroutineScope(Dispatchers.Main).launch {
            socialLoginCallBack?.onSuccess(
                SocialTypeEnum.GOOGLE, token
            )
        }
    }

    companion object {

        @Volatile
        private var INSTANCE: GoogleLoginHandler? = null

        fun getInstance(
            activity: ComponentActivity? = null,
            fragment: Fragment? = null,
            clientId: String? = null
        ): GoogleLoginHandler =
            INSTANCE
                ?: GoogleLoginHandler(
                    activity, fragment, clientId
                ).also { INSTANCE = it }

        fun recreateInstance(
            activity: ComponentActivity? = null,
            fragment: Fragment? = null,
            clientId: String? = null
        ): GoogleLoginHandler {
            INSTANCE = null
            return INSTANCE
                ?: GoogleLoginHandler(
                    activity, fragment, clientId
                ).also { INSTANCE = it }
        }
    }

}