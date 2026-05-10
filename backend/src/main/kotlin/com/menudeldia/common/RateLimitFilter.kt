package com.menudeldia.common

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

/**
 * Per-IP rate limiting. Stricter on /auth/*, looser on reads.
 * TODO B4.1.1: implement with Bucket4j; key on client IP (respect X-Forwarded-For when behind Caddy).
 *              60/min for reads, 10/min for /auth/*. Return 429 with Retry-After header.
*/
@Component
class RateLimitFilter : OncePerRequestFilter() {

override fun doFilterInternal(
request: HttpServletRequest,
response: HttpServletResponse,
filterChain: FilterChain,
) {
// TODO: implement bucket lookup per IP, consume token, on empty -> 429.
filterChain.doFilter(request, response)
}
}
