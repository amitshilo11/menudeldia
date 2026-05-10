package com.menudeldia.auth

import org.springframework.stereotype.Component

/**
 * Verifies Google ID tokens against Google's JWKS.
 * TODO B3.3.2: use com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier.
 *              Audience = props.auth.googleClientId.
 */
@Component
class GoogleIdTokenVerifierBean {

    fun verify(idToken: String): VerifiedClaims {
        // TODO: validate signature + audience + issuer; extract sub/email/name/picture.
        TODO("Phase 3 — task B3.3.2")
    }
}
