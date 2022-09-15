package com.sedo.sociallogin.helpers

import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.fragment.app.Fragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.sedo.sociallogin.data.enums.SocialTypeEnum

class GoogleLoginHandler private constructor(
    mActivity: ComponentActivity? = null,
    mFragment: Fragment? = null,
    clientId: String?
) : ISocialLogin(mActivity, mFragment, clientId) {

    private var mGoogleSignInClient: GoogleSignInClient? = null

    override fun initMethod() {
        getContext().let {
            val gso =
                clientId
                    .let {
                        it?.let { it1 ->
                            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                .requestServerAuthCode(
                                    it1, true
                                )
                                .requestIdToken(it)
                                .requestEmail()
                                .build()
                        }
                    }
            mGoogleSignInClient = it?.let { it1 ->
                gso?.let { it2 ->
                    GoogleSignIn.getClient(
                        it1,
                        it2
                    )
                }
            }
        }
    }

    override fun startMethod() {
        try {
            val account = getContext()?.let { GoogleSignIn.getLastSignedInAccount(it) }
            if (account != null) {
                mGoogleSignInClient?.signOut()?.addOnSuccessListener {
                    launchGoogleLogin()
                }
            } else {
                launchGoogleLogin()
            }
        } catch (e: Exception) {
            getContext()?.let {
                Toast.makeText(it, e.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun launchGoogleLogin() {
        if (resultLauncher != null) {
            resultLauncher?.launch(mGoogleSignInClient?.signInIntent)
        } else {
            defaultResultLauncher?.launch(mGoogleSignInClient?.signInIntent)
        }
    }

    override fun setResult(data: Any) {
        handleResult(data)
    }

    override fun handleResult(data: Any) {
        try {
            data as Task<GoogleSignInAccount>
            val account =
                data.getResult(ApiException::class.java)
            if (account?.serverAuthCode != null) {
                handleSuccess(account)
            }
        } catch (e: ApiException) {
            getContext()?.let {
                Toast.makeText(it, "signInResult:failed code=" + e.statusCode, Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    override fun handleSuccess(account: Any) {
        account as GoogleSignInAccount
        account.idToken?.let { it1 ->
            socialLoginCallBack?.onSuccess(
                SocialTypeEnum.GOOGLE,
                it1
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