package com.menudeldia.config

import com.menudeldia.auth.JwtAuthFilter
import com.menudeldia.common.ApiPaths
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

/**
 * Spring Security wiring.
 * - Public: /api/v1/health, /api/v1/restaurants/**, /api/v1/auth/**, photo endpoint.
 * - Authenticated: /api/v1/me/**.
 *
 * TODO B3.1.1: confirm matchers when controllers exist; add CSRF, headers, etc.
*/
@Configuration
class SecurityConfig(
private val jwtAuthFilter: JwtAuthFilter,
private val corsConfig: CorsConfig,
) {

@Bean
fun filterChain(http: HttpSecurity): SecurityFilterChain {
// TODO B3.1.1: full configuration — sketched below.
http
.csrf { it.disable() }
.cors { it.configurationSource(corsConfig.corsConfigurationSource()) }
.sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
.authorizeHttpRequests { auth ->
auth
.requestMatchers("${ApiPaths.V1}/health").permitAll()
.requestMatchers("${ApiPaths.V1}/auth/**").permitAll()
.requestMatchers("${ApiPaths.V1}/restaurants/**").permitAll()
.requestMatchers("${ApiPaths.V1}/me/**").authenticated()
.anyRequest().denyAll()
}
.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter::class.java)
.headers { headers ->
// TODO B4.1.2: HSTS, X-Frame-Options, CSP for image-src 'self'.
}
return http.build()
}
}
