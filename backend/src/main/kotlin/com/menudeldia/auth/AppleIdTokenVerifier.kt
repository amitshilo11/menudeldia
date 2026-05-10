package com.menudeldia.auth

import org.springframework.stereotype.Component

/**
 * Verifies Apple identity tokens against https://appleid.apple.com/auth/keys.
 * TODO B3.3.3: use com.nimbusds.jose with cached JWKS source. Audience = props.auth.appleClientId.
 */
@Component
class AppleIdTokenVerifier {

    fun verify(identityToken: String): VerifiedClaims {
        // TODO: validate signature + aud + iss == 'https://appleid.apple.com'; extract claims.
        TODO("Phase 3 — task B3.3.3")
    }
}
