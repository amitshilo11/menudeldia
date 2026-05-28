package com.menudeldia.common

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.benmanes.caffeine.cache.Caffeine
import com.menudeldia.config.AppProperties
import io.github.bucket4j.Bandwidth
import io.github.bucket4j.Bucket
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.core.annotation.Order
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.time.Duration

@Component
@Order(-200)
class RateLimitFilter(
    private val props: AppProperties,
    private val mapper: ObjectMapper,
) : OncePerRequestFilter() {

    private val authBuckets = Caffeine.newBuilder()
        .expireAfterAccess(Duration.ofMinutes(2))
        .build<String, Bucket> { newBucket(props.rateLimit.authRpm) }

    private val readBuckets = Caffeine.newBuilder()
        .expireAfterAccess(Duration.ofMinutes(2))
        .build<String, Bucket> { newBucket(props.rateLimit.readRpm) }

    private val adminBuckets = Caffeine.newBuilder()
        .expireAfterAccess(Duration.ofMinutes(5))
        .build<String, Bucket> { newBucket(props.rateLimit.adminRpm) }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val path = request.requestURI
        val ip = request.remoteAddr

        val bucket: Bucket? = when {
            path.startsWith("${ApiPaths.V1}/auth/") -> authBuckets.get(ip)
            path.startsWith("${ApiPaths.V1}/admin/") -> adminBuckets.get(ip)
            path.startsWith("${ApiPaths.V1}/restaurants") ||
                    path.startsWith("${ApiPaths.V1}/me") -> readBuckets.get(ip)

            else -> null
        }

        if (bucket != null && !bucket.tryConsume(1)) {
            response.status = 429
            response.contentType = MediaType.APPLICATION_JSON_VALUE
            mapper.writeValue(
                response.outputStream,
                ApiError("rate_limited", "Too many requests", 429)
            )
            return
        }

        filterChain.doFilter(request, response)
    }

    private fun newBucket(rpm: Int): Bucket =
        Bucket.builder()
            .addLimit(
                Bandwidth.builder().capacity(rpm.toLong())
                    .refillIntervally(rpm.toLong(), Duration.ofMinutes(1)).build()
            )
            .build()
}
