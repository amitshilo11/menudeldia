package com.menudeldia.auth.dto

data class SignInResponse(
    val accessToken: String,
    val user: UserDto,
)
