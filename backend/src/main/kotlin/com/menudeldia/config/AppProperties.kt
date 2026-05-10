package com.menudeldia.config

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

/**
 * Typesafe binding for the `menudeldia.*` block in application.yml.
 * TODO: validate non-empty `auth.jwtSigningKey` in prod profile (use @Validated + JSR-303).
 */
@ConfigurationProperties("menudeldia")
data class AppProperties(
    val google: GoogleProps,
    val auth: AuthProps,
    val photos: PhotoProps,
    val rateLimit: RateLimitProps,
) {
    data class GoogleProps(
        val placesApiKey: String,
        val placesCacheTtl: Duration,
        val placesRefreshBatchSize: Int,
    )

    data class AuthProps(
        val googleClientId: String,
        val appleClientId: String,
        val jwtSigningKey: String,
        val jwtTtl: Duration,
    )

    data class PhotoProps(
        val storageRoot: String,
    )

    data class RateLimitProps(
        val readRpm: Int,
        val authRpm: Int,
    )
}
