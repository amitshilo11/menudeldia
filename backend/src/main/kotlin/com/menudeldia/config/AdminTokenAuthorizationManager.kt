package com.menudeldia.config

import jakarta.servlet.http.HttpServletRequest
import org.springframework.security.authorization.AuthorizationDecision
import org.springframework.security.authorization.AuthorizationManager
import org.springframework.security.core.Authentication
import org.springframework.security.web.access.intercept.RequestAuthorizationContext
import java.util.function.Supplier

class AdminTokenAuthorizationManager(private val adminToken: String) :
    AuthorizationManager<RequestAuthorizationContext> {

    @Suppress("OVERRIDE_DEPRECATION")
    override fun check(
        authentication: Supplier<Authentication>,
        context: RequestAuthorizationContext,
    ): AuthorizationDecision {
        if (adminToken.isEmpty()) return AuthorizationDecision(false)
        val request: HttpServletRequest = context.request
        val supplied = request.getHeader("X-Admin-Token") ?: return AuthorizationDecision(false)
        return AuthorizationDecision(
            MessageDigest.isEqual(
                adminToken.toByteArray(),
                supplied.toByteArray()
            )
        )
    }
}

private object MessageDigest {
    fun isEqual(a: ByteArray, b: ByteArray): Boolean =
        java.security.MessageDigest.isEqual(a, b)
}
