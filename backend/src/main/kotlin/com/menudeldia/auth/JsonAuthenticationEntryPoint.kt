package com.menudeldia.auth

import com.fasterxml.jackson.databind.ObjectMapper
import com.menudeldia.common.ApiError
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.MediaType
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component

@Component
class JsonAuthenticationEntryPoint(private val mapper: ObjectMapper) : AuthenticationEntryPoint {

    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException,
    ) {
        response.status = 401
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        mapper.writeValue(
            response.outputStream,
            ApiError("unauthorized", "Authentication required", 401)
        )
    }
}
