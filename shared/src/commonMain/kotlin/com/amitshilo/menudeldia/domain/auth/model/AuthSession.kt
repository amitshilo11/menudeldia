package com.amitshilo.menudeldia.domain.auth.model

data class AuthSession(
    val accessToken: String,
    val user: AuthUser,
)

data class AuthUser(
    val id: String,
    val email: String?,
    val displayName: String?,
    val avatarUrl: String?,
)
