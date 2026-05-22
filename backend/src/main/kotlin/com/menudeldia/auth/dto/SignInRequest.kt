package com.menudeldia.auth.dto

import jakarta.validation.constraints.NotBlank

data class SignInRequest(
    @field:NotBlank
    val idToken: String,
    /** Raw nonce — required for Apple Sign-In to prevent replay; optional for Google. */
    val nonce: String? = null,
)
