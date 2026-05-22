package com.menudeldia.places

sealed class PlacesException(message: String, cause: Throwable? = null) :
    RuntimeException(message, cause) {
    class ApiError(message: String, cause: Throwable? = null) : PlacesException(message, cause)
    class Unavailable(message: String, cause: Throwable? = null) : PlacesException(message, cause)
}
