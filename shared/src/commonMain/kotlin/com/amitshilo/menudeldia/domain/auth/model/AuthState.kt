package com.amitshilo.menudeldia.domain.auth.model

sealed interface AuthState {
    data object Loading : AuthState
    data object NeedsAuth : AuthState
    data class Authenticated(val session: AuthSession) : AuthState
    data object Guest : AuthState
}
