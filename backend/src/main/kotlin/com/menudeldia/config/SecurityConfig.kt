package com.menudeldia.config

import com.menudeldia.auth.JwtAuthFilter
import com.menudeldia.common.ApiPaths
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
class SecurityConfig(
    private val jwtAuthFilter: JwtAuthFilter,
    private val corsConfig: CorsConfig,
) {

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .cors { it.configurationSource(corsConfig.corsConfigurationSource()) }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers("${ApiPaths.V1}/health").permitAll()
                    .requestMatchers("${ApiPaths.V1}/auth/**").permitAll()
                    .requestMatchers("${ApiPaths.V1}/restaurants/**").permitAll()
                    .requestMatchers("${ApiPaths.V1}/admin/**").permitAll()
                    .requestMatchers("${ApiPaths.V1}/me/**").authenticated()
                    .anyRequest().denyAll()
            }
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter::class.java)
            .headers { }  // Phase 4 B4.1.2: add HSTS, X-Frame-Options, CSP
        return http.build()
    }
}
