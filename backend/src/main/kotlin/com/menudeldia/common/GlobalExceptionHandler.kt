package com.menudeldia.common

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

/**
 * Maps exceptions to ApiError JSON.
 * TODO B1.1.6: wire validation errors, not-found, rate-limit (429), and unexpected (500).
 */
@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun onValidation(ex: MethodArgumentNotValidException): ResponseEntity<ApiError> {
        // TODO: serialize field errors into the message.
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiError("validation_error", ex.message ?: "invalid request", 400))
    }

    @ExceptionHandler(NoSuchElementException::class)
    fun onNotFound(ex: NoSuchElementException): ResponseEntity<ApiError> =
        ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiError("not_found", ex.message ?: "not found", 404))

    @ExceptionHandler(Exception::class)
    fun onUnexpected(ex: Exception): ResponseEntity<ApiError> {
        // TODO: log with stack trace at ERROR.
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiError("internal_error", "An unexpected error occurred", 500))
    }
}
