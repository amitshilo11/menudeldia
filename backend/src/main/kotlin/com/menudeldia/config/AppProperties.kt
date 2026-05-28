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
    val cors: CorsProps,
    val rateLimit: RateLimitProps,
    val adminToken: String = "",
    val csv: CsvProps = CsvProps(),
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

    data class CorsProps(
        val allowedOrigins: List<String>,
    )

    data class RateLimitProps(
        val readRpm: Int,
        val authRpm: Int,
        val adminRpm: Int,
    )

    /**
     * Admin portal round-trip CSV.
     * [path] is resolved against the JVM working dir; the default points at the repo root
     * when the backend is launched from the project root (which `./gradlew :backend:bootRun` does).
     */
    data class CsvProps(
        val path: String = "./restaurants_db_ready.csv",
    )
}
