package com.menudeldia.auth

import org.springframework.stereotype.Service
import java.time.Instant
import java.util.UUID

@Service
class UserService(private val users: UserRepository) {

    fun upsertFromIdToken(provider: String, claims: VerifiedClaims): User {
        val existing = users.findByProviderAndExternalId(provider, claims.sub)
        val now = Instant.now()
        return if (existing != null) {
            // Never blank name/email — Apple only returns them on first sign-in.
            if (claims.email != null) existing.email = claims.email
            if (claims.name != null) existing.displayName = claims.name
            if (claims.picture != null) existing.avatarUrl = claims.picture
            existing.lastLogin = now
            users.save(existing)
        } else {
            users.save(
                User(
                    provider = provider,
                    externalId = claims.sub,
                    email = claims.email,
                    displayName = claims.name,
                    avatarUrl = claims.picture,
                    lastLogin = now,
                )
            )
        }
    }

    fun byId(id: UUID): User =
        users.findById(id).orElseThrow { NoSuchElementException("user $id not found") }
}

/** Subset of claims extracted after token verification. */
data class VerifiedClaims(
    val sub: String,
    val email: String?,
    val name: String?,
    val picture: String?,
)
