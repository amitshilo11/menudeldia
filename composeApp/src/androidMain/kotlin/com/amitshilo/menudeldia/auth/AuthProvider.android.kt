package com.amitshilo.menudeldia.auth

import androidx.activity.ComponentActivity
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.amitshilo.menudeldia.BuildConfig
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential

actual class AuthProvider(private val activity: ComponentActivity) {

    actual suspend fun signInWithGoogle(): GoogleSignInResult {
        val option = GetSignInWithGoogleOption.Builder(BuildConfig.GOOGLE_WEB_CLIENT_ID).build()
        val request = GetCredentialRequest.Builder().addCredentialOption(option).build()
        val response = CredentialManager.create(activity).getCredential(activity, request)
        val cred =
            GoogleIdTokenCredential.createFrom((response.credential as CustomCredential).data)
        return GoogleSignInResult(cred.idToken)
    }

    actual suspend fun signInWithApple(): AppleSignInResult =
        error("Sign in with Apple is only available on iOS")

    actual suspend fun signOutPlatform() {
        CredentialManager.create(activity).clearCredentialState(ClearCredentialStateRequest())
    }
}
