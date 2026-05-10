package com.menudeldia.auth

import org.springframework.stereotype.Service
import java.time.Instant

/**
 * Upserts users from verified ID-token claims.
 * TODO B3.3.5: implement upsertFromIdToken and lookup helpers.
 */
@Service
class UserService(
    private val users: UserRepository,
) {

    fun upsertFromIdToken(provider: String, claims: VerifiedClaims): User {
        // TODO: find by (provider, externalId); create or update display fields; set lastLogin = now.
        TODO("Phase 3 — task B3.3.5")
    }

    fun byId(id: java.util.UUID): User =
        users.findById(id).orElseThrow { NoSuchElementException("user $id not found") }
}

/** Subset of claims we actually use after token verification. */
data class VerifiedClaims(
    val sub: String,
    val email: String?,
    val name: String?,
    val picture: String?,
)
