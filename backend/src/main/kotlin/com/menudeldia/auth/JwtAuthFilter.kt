package com.menudeldia.auth

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

/**
 * Reads `Authorization: Bearer <our-jwt>`, verifies, populates SecurityContext.
 * Public endpoints simply skip when header is absent.
 *
 * TODO B3.2.2: implement parsing + verification + SecurityContext population.
 */
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
            // TODO: jwt.verify(token) -> User; on failure, leave SecurityContext empty.
            //       SecurityContext shouldn't write if already authenticated for the request.
            try {
                val userId = jwt.verify(token)
                val user = users.byId(userId)
                val auth = UsernamePasswordAuthenticationToken(user, null, emptyList())
                SecurityContextHolder.getContext().authentication = auth
            } catch (_: Exception) {
                // swallow: downstream authorize will reject if endpoint requires auth.
            }
        }
        filterChain.doFilter(request, response)
    }
}
