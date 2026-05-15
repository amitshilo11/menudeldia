package com.amitshilo.menudeldia.data.auth.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SignInRequestDto(
    @SerialName("idToken") val idToken: String,
    @SerialName("nonce") val nonce: String? = null,
)

@Serializable
data class SignInResponseDto(
    @SerialName("accessToken") val accessToken: String,
    @SerialName("user") val user: AuthUserDto,
)

@Serializable
data class AuthUserDto(
    @SerialName("id") val id: String,
    @SerialName("email") val email: String? = null,
    @SerialName("displayName") val displayName: String? = null,
    @SerialName("avatarUrl") val avatarUrl: String? = null,
)
