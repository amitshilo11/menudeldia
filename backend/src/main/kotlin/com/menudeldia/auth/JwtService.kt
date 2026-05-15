package com.menudeldia.auth

import com.menudeldia.config.AppProperties
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Service
import java.util.Date
import java.util.UUID

@Service
class JwtService(private val props: AppProperties) {

    private val key by lazy {
        val raw = props.auth.jwtSigningKey
        require(raw.length >= 32) { "jwt-signing-key must be at least 32 characters" }
        Keys.hmacShaKeyFor(raw.toByteArray(Charsets.UTF_8))
    }

    fun issue(userId: UUID): String {
        val now = System.currentTimeMillis()
        val exp = now + props.auth.jwtTtl.toMillis()
        return Jwts.builder()
            .subject(userId.toString())
            .issuedAt(Date(now))
            .expiration(Date(exp))
            .signWith(key)
            .compact()
    }

    fun verify(token: String): UUID {
        val claims = try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token).payload
        } catch (ex: JwtException) {
            throw IllegalArgumentException("Invalid or expired JWT: ${ex.message}", ex)
        }
        return UUID.fromString(claims.subject)
    }
}
