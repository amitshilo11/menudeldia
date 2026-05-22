package com.menudeldia.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
class CorsConfig(private val props: AppProperties) {

    fun corsConfigurationSource(): CorsConfigurationSource {
        val cors = CorsConfiguration().apply {
            allowedOrigins = props.cors.allowedOrigins
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
