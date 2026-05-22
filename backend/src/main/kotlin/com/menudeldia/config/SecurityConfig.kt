package com.menudeldia.config

import com.menudeldia.auth.JsonAuthenticationEntryPoint
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
    private val props: AppProperties,
    private val entryPoint: JsonAuthenticationEntryPoint,
) {

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .cors { it.configurationSource(corsConfig.corsConfigurationSource()) }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                    .requestMatchers("/actuator/**")
                    .access(AdminTokenAuthorizationManager(props.adminToken))
                    .requestMatchers("${ApiPaths.V1}/health").permitAll()
                    .requestMatchers("${ApiPaths.V1}/auth/**").permitAll()
                    .requestMatchers("${ApiPaths.V1}/restaurants/**").permitAll()
                    .requestMatchers("${ApiPaths.V1}/admin/**").permitAll()
                    .requestMatchers("${ApiPaths.V1}/me/**").authenticated()
                    .anyRequest().denyAll()
            }
            .exceptionHandling { it.authenticationEntryPoint(entryPoint) }
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter::class.java)
            .headers { h ->
                h.httpStrictTransportSecurity { hsts ->
                    hsts.maxAgeInSeconds(31_536_000).includeSubDomains(true)
                }
                h.frameOptions { it.deny() }
                h.contentTypeOptions { }
                h.contentSecurityPolicy { csp ->
                    csp.policyDirectives("default-src 'none'; img-src 'self'; style-src 'self'; script-src 'none'; connect-src 'self'; frame-ancestors 'none'")
                }
            }
        return http.build()
    }
}
