package com.menudeldia.auth

import com.menudeldia.common.ApiError
import com.nimbusds.jose.JOSEException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.security.GeneralSecurityException

/** Maps auth verification exceptions to 401 responses. */
@RestControllerAdvice
class AuthExceptionAdvice {

    @ExceptionHandler(
        GeneralSecurityException::class,
        JOSEException::class,
        IllegalArgumentException::class
    )
    fun onAuthError(ex: Exception): ResponseEntity<ApiError> =
        ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(ApiError("unauthorized", ex.message ?: "Authentication failed", 401))
}
