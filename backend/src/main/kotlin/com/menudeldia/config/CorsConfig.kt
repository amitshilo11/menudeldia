package com.menudeldia.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

/**
 * CORS allowlist. Mobile apps don't trigger preflight on native HTTP, but a future web client will.
 * TODO B3.1.2: source allowed origins from env var, not hardcoded.
 */
@Configuration
class CorsConfig {

    fun corsConfigurationSource(): CorsConfigurationSource {
        val cors = CorsConfiguration().apply {
            allowedOrigins = listOf(
                "http://localhost:3000", // dev web client placeholder
            )
            allowedMethods = listOf("GET", "POST", "OPTIONS")
            allowedHeaders = listOf("Content-Type", "Authorization")
            allowCredentials = false
            maxAge = 3600L
        }
        return UrlBasedCorsConfigurationSource().apply {
            registerCorsConfiguration("/**", cors)
        }
    }
}
