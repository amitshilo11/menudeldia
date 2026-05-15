package com.menudeldia.auth

import com.menudeldia.config.AppProperties
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.jwk.source.JWKSourceBuilder
import com.nimbusds.jose.proc.JWSVerificationKeySelector
import com.nimbusds.jose.proc.SecurityContext
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.proc.DefaultJWTProcessor
import org.springframework.stereotype.Component
import java.net.URI
import java.security.MessageDigest

@Component
class AppleIdTokenVerifier(private val props: AppProperties) {

    private val jwtProcessor by lazy {
        val jwkSource = JWKSourceBuilder
            .create<SecurityContext>(URI("https://appleid.apple.com/auth/keys").toURL())
            .build()
        val keySelector = JWSVerificationKeySelector<SecurityContext>(JWSAlgorithm.RS256, jwkSource)
        DefaultJWTProcessor<SecurityContext>().apply { jwsKeySelector = keySelector }
    }

    /** [rawNonce] is the plain nonce generated on the client; Apple's JWT carries sha256 of it. */
    fun verify(identityToken: String, rawNonce: String?): VerifiedClaims {
        val claims: JWTClaimsSet = try {
            jwtProcessor.process(identityToken, null)
        } catch (ex: Exception) {
            throw IllegalArgumentException(
                "Apple identity token verification failed: ${ex.message}",
                ex
            )
        }

        require(claims.issuer == "https://appleid.apple.com") { "Unexpected Apple token issuer" }
        require(props.auth.appleClientId in claims.audience) { "Apple token audience mismatch" }

        if (rawNonce != null) {
            val expectedNonce = sha256Hex(rawNonce)
            val tokenNonce = claims.getStringClaim("nonce")
            require(tokenNonce == expectedNonce) { "Apple token nonce mismatch" }
        }

        return VerifiedClaims(
            sub = claims.subject,
            email = claims.getStringClaim("email"),
            name = null,   // Apple never includes name in JWT; it's only in the first-time UI callback
            picture = null,
        )
    }

    private fun sha256Hex(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(input.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }
}
