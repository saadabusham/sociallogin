package com.sedo.sociallogin.helpers

import android.content.Intent
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.Fragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.sedo.sociallogin.`interface`.SocialLoginCallBack
import com.sedo.sociallogin.data.enums.SocialTypeEnum


class GoogleLoginHandler private constructor(
    mActivity: ComponentActivity? = null,
    mFragment: Fragment? = null,
    clientId: String?
) : ISocialLogin(mActivity, mFragment, clientId) {

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
                )
                    .also { INSTANCE = it }
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

    private var mGoogleSignInClient: GoogleSignInClient? = null

    //  Google Login issues
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
        val account = getContext()?.let { GoogleSignIn.getLastSignedInAccount(it) }
        if (account == null) {
            if (resultLauncher != null) {
                resultLauncher?.launch(mGoogleSignInClient?.signInIntent)
            } else {
                defaultResultLauncher?.launch(mGoogleSignInClient?.signInIntent)
            }
        } else {
            mGoogleSignInClient?.signOut()
        }
    }

    override fun setResult(completedTask: Any) {
        handleResult(completedTask)
    }

    override fun handleResult(completedTask: Any) {
        try {
            completedTask as Task<GoogleSignInAccount>
            val account =
                completedTask.getResult(ApiException::class.java)
            if (account?.serverAuthCode != null) {
                account.account?.let {
//                    getToken(account.serverAuthCode)
                    account.idToken?.let { it1 ->
                        socialLoginCallBack?.onSuccess(
                            SocialTypeEnum.GOOGLE,
                            it1
                        )
                    }
                }
            }
        } catch (e: ApiException) {
            getContext()?.let {
                Toast.makeText(it, "signInResult:failed code=" + e.statusCode, Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

}