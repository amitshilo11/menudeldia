package com.menudeldia.common

import java.time.Instant

/** Wire shape for all error responses. Matches client expectation. */
data class ApiError(
    val error: String,
    val message: String,
    val status: Int,
    val timestamp: Instant = Instant.now(),
)
