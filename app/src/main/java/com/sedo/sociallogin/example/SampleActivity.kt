package com.sedo.sociallogin.example

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.material.button.MaterialButton
import com.sedo.sociallogin.`interface`.SocialLoginCallBack
import com.sedo.sociallogin.data.enums.SocialTypeEnum
import com.sedo.sociallogin.helpers.AppleLoginHandler
import com.sedo.sociallogin.helpers.GoogleLoginHandler

class SampleActivity : AppCompatActivity() {

    var googleLoginHandler: GoogleLoginHandler? = null
    var appleLoginHandler: AppleLoginHandler? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sample)
        initSocialLogin()
        setUpListeners()
    }

    private fun setUpListeners() {
        findViewById<MaterialButton>(R.id.btnGoogleLogin)?.setOnClickListener {
            googleLoginHandler?.startMethod()
        }
        findViewById<MaterialButton>(R.id.btnAppleLogin)?.setOnClickListener {
            appleLoginHandler?.startMethod()
        }
    }

    private fun initSocialLogin() {
        try {
            if (googleLoginHandler == null)
                initGoogle()
            if (appleLoginHandler == null)
                initApple()
        } catch (e: Exception) {
            e
        }
    }

    private fun initGoogle() {
        googleLoginHandler =
            GoogleLoginHandler.getInstance(activity = this).apply {
                setRegisterResult(googleLoginResultLauncher)
            }
        googleLoginHandler?.showError(true)
        googleLoginHandler?.setCallBack(object : SocialLoginCallBack {
            override fun onSuccess(provider: SocialTypeEnum, token: String) {
                Toast.makeText(this@SampleActivity, token, Toast.LENGTH_SHORT).show()
            }

        })
        googleLoginHandler?.initMethod()
    }

    private var googleLoginResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                googleLoginHandler?.setResult(task)
            }
        }


    private fun initApple() {
        appleLoginHandler = AppleLoginHandler.getInstance(activity = this)
        appleLoginHandler?.showError(true)
        appleLoginHandler?.setCallBack(object : SocialLoginCallBack {
            override fun onSuccess(provider: SocialTypeEnum, token: String) {
                Toast.makeText(this@SampleActivity, token, Toast.LENGTH_SHORT).show()
            }
        })
        appleLoginHandler?.initMethod()
    }

}