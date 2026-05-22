package com.amitshilo.menudeldia.auth

// Web platform is out of scope for v1. Stubs satisfy the expect declaration.
actual class AuthProvider {
    actual suspend fun signInWithGoogle(): GoogleSignInResult = error("Not supported on web (v2+)")
    actual suspend fun signInWithApple(): AppleSignInResult = error("Not supported on web (v2+)")
    actual suspend fun signOutPlatform() = Unit
}
