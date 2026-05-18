package com.menudeldia.auth

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

/** Reads `Authorization: Bearer <our-jwt>`, verifies, populates SecurityContext. */
@Component
class JwtAuthFilter(
    private val jwt: JwtService,
    private val users: UserService,
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val token = request.getHeader("Authorization")
            ?.takeIf { it.startsWith("Bearer ") }
            ?.removePrefix("Bearer ")
            ?.trim()

        if (token != null) {
            try {
                val userId = jwt.verify(token)
                val user = users.byId(userId)
                SecurityContextHolder.getContext().authentication =
                    UsernamePasswordAuthenticationToken(user, null, emptyList())
            } catch (_: Exception) {
                // swallow: downstream authorize will reject if endpoint requires auth.
            }
        }
        filterChain.doFilter(request, response)
    }
}
