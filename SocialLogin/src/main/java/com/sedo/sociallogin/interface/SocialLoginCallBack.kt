package com.sedo.sociallogin.`interface`

import com.sedo.sociallogin.data.enums.SocialTypeEnum

interface SocialLoginCallBack {
    fun onSuccess(provider: SocialTypeEnum, token: String,code:String?=null)
    fun onFailure(message: String){}
}