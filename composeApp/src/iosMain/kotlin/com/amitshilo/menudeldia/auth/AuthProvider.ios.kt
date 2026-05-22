package com.amitshilo.menudeldia.auth

import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * Called from Swift. Implement AppleSignInBridge + GoogleSignInBridge in iosApp/iosApp/Auth/
 * and pass a CompositeAuthBridge into MainViewController.
 *
 * Nonce contract: this class generates the raw nonce and passes it to the bridge.
 * The bridge must SHA-256 hash it before setting request.nonce (CryptoKit.SHA256).
 * The raw nonce is returned alongside identityToken so the backend can verify
 * sha256(rawNonce) == JWT nonce claim.
 */
interface IosAuthBridge {
    /** [rawNonce] is plain text; the bridge must hash it before sending to Apple. */
    fun signInWithApple(
        rawNonce: String,
        onResult: (identityToken: String?, error: String?) -> Unit
    )

    fun signInWithGoogle(onResult: (idToken: String?, error: String?) -> Unit)
    fun signOut()
}

actual class AuthProvider(private val bridge: IosAuthBridge) {

    actual suspend fun signInWithGoogle(): GoogleSignInResult = suspendCoroutine { cont ->
        bridge.signInWithGoogle { token, err ->
            if (token != null) cont.resume(GoogleSignInResult(token))
            else cont.resumeWithException(IllegalStateException(err ?: "Google Sign-In cancelled"))
        }
    }

    actual suspend fun signInWithApple(): AppleSignInResult = suspendCoroutine { cont ->
        val rawNonce = randomNonceHex(32)
        bridge.signInWithApple(rawNonce) { token, err ->
            if (token != null) cont.resume(AppleSignInResult(token, rawNonce))
            else cont.resumeWithException(IllegalStateException(err ?: "Apple Sign-In cancelled"))
        }
    }

    actual suspend fun signOutPlatform() {
        bridge.signOut()
    }
}

private fun randomNonceHex(length: Int): String {
    val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
    return (1..length).map { chars.random() }.joinToString("")
}
