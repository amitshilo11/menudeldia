package com.menudeldia.auth.dto

import java.util.UUID

data class UserDto(
    val id: UUID,
    val email: String?,
    val displayName: String?,
    val avatarUrl: String?,
)
