package com.sedo.sociallogin.utils

object Constants {

    object AppleConstants {
        const val AUTHURL = "https://appleid.apple.com/auth/authorize"
        const val SCOPE = "name%20email"
        const val RESPONSE_TYPE = "code%20id_token"
        const val RESPONSE_MODE = "fragment"
    }

    object StravaConstants {
        const val AUTHURL = "https://www.strava.com/oauth/authorize"
        const val RESPONSE_TYPE = "code"
        const val APPROVAL_TYPE = "auto"
        const val SCOPE = "activity:write,read"
    }

    object FacebookConstants {
        const val PUBLIC_PROFILE_PERMISSION = "public_profile"
        const val EMAIL_PERMISSION = "email"
    }
}