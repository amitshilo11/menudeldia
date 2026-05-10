package com.menudeldia.auth

import com.menudeldia.config.AppProperties
import org.springframework.stereotype.Service
import java.util.UUID

/**
 * Issues + verifies our session JWT (HS256).
 * TODO B3.2.1: implement with io.jsonwebtoken (jjwt). Subject = userId; ttl = props.auth.jwtTtl.
 */
@Service
class JwtService(
    private val props: AppProperties,
) {

    fun issue(userId: UUID): String {
        // TODO: build signed JWT, base64url-encode, return.
        TODO("Phase 3 — task B3.2.1")
    }

    fun verify(token: String): UUID {
        // TODO: parse + validate signature + expiry; return subject as UUID; throw on failure.
        TODO("Phase 3 — task B3.2.1")
    }
}
