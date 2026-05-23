package com.menudeldia.common

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.servlet.resource.NoResourceFoundException

@RestControllerAdvice
class GlobalExceptionHandler {

    private val log = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun onValidation(ex: MethodArgumentNotValidException): ResponseEntity<ApiError> {
        val details = ex.bindingResult.fieldErrors.joinToString("; ") {
            "${it.field}: ${it.defaultMessage}"
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiError("validation_error", details, 400))
    }

    @ExceptionHandler(MissingServletRequestParameterException::class)
    fun onMissingParam(ex: MissingServletRequestParameterException): ResponseEntity<ApiError> =
        ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiError("validation_error", "${ex.parameterName} is required", 400))

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun onTypeMismatch(ex: MethodArgumentTypeMismatchException): ResponseEntity<ApiError> =
        ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiError("validation_error", "${ex.name}: invalid value '${ex.value}'", 400))

    @ExceptionHandler(NoSuchElementException::class)
    fun onNotFound(ex: NoSuchElementException): ResponseEntity<ApiError> =
        ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiError("not_found", ex.message ?: "not found", 404))

    @ExceptionHandler(NoResourceFoundException::class)
    fun onNoResource(ex: NoResourceFoundException): ResponseEntity<ApiError> =
        ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiError("not_found", "Resource not found", 404))

    @ExceptionHandler(Exception::class)
    fun onUnexpected(ex: Exception): ResponseEntity<ApiError> {
        log.error("Unexpected error", ex)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiError("internal_error", "An unexpected error occurred", 500))
    }
}
