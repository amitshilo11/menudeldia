package com.menudeldia.auth

import com.menudeldia.config.AppProperties
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Duration
import java.util.UUID

class JwtServiceTest {

    private fun service(signingKey: String = "a".repeat(32), ttl: Duration = Duration.ofDays(30)) =
        JwtService(
            AppProperties(
                google = AppProperties.GoogleProps("", Duration.ofHours(1), 5),
                auth = AppProperties.AuthProps(
                    googleClientId = "",
                    appleClientId = "",
                    jwtSigningKey = signingKey,
                    jwtTtl = ttl,
                ),
                photos = AppProperties.PhotoProps("./var"),
                rateLimit = AppProperties.RateLimitProps(60, 10),
            )
        )

    @Test
    fun `issue and verify round-trip returns same userId`() {
        val svc = service()
        val userId = UUID.randomUUID()
        val token = svc.issue(userId)
        assertEquals(userId, svc.verify(token))
    }

    @Test
    fun `verify rejects tampered token`() {
        val svc = service()
        val token = svc.issue(UUID.randomUUID()) + "tampered"
        assertThrows<IllegalArgumentException> { svc.verify(token) }
    }

    @Test
    fun `verify rejects expired token`() {
        val svc = service(ttl = Duration.ofMillis(1))
        val token = svc.issue(UUID.randomUUID())
        Thread.sleep(10)
        assertThrows<IllegalArgumentException> { svc.verify(token) }
    }

    @Test
    fun `short signing key throws on first use`() {
        val svc = service(signingKey = "short")
        assertThrows<IllegalArgumentException> { svc.issue(UUID.randomUUID()) }
    }
}
