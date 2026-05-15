package com.menudeldia.auth

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.menudeldia.config.AppProperties
import org.springframework.stereotype.Component

@Component
class GoogleIdTokenVerifierBean(private val props: AppProperties) {

    private val verifier: GoogleIdTokenVerifier by lazy {
        // Accept all configured OAuth client IDs (web, Android, iOS may differ).
        val audiences = props.auth.googleClientId
            .split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
        GoogleIdTokenVerifier.Builder(NetHttpTransport(), GsonFactory.getDefaultInstance())
            .setAudience(audiences)
            .build()
    }

    fun verify(idToken: String): VerifiedClaims {
        val token: GoogleIdToken = verifier.verify(idToken)
            ?: throw IllegalArgumentException("Google ID token verification failed")
        val payload = token.payload
        return VerifiedClaims(
            sub = payload.subject,
            email = payload.email,
            name = payload["name"] as? String,
            picture = payload["picture"] as? String,
        )
    }
}
