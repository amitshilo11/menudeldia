package com.amitshilo.menudeldia.auth

/** Platform-specific sign-in operations. Implement per-platform (Android: Credential Manager; iOS: ASAuthorizationController + GIDSignIn). */
expect class AuthProvider {
    suspend fun signInWithGoogle(): GoogleSignInResult
    suspend fun signInWithApple(): AppleSignInResult
    suspend fun signOutPlatform()
}

data class GoogleSignInResult(val idToken: String)
data class AppleSignInResult(val identityToken: String, val rawNonce: String)
