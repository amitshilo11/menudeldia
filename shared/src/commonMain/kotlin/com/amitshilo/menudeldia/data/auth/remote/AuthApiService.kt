package com.amitshilo.menudeldia.data.auth.remote

import com.amitshilo.menudeldia.data.auth.remote.dto.AuthUserDto
import com.amitshilo.menudeldia.data.auth.remote.dto.SignInRequestDto
import com.amitshilo.menudeldia.data.auth.remote.dto.SignInResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class AuthApiService(private val client: HttpClient) {

    suspend fun signInWithGoogle(idToken: String): SignInResponseDto =
        client.post("$BASE_PATH/auth/google") {
            contentType(ContentType.Application.Json)
            setBody(SignInRequestDto(idToken = idToken))
        }.body()

    suspend fun signInWithApple(idToken: String, rawNonce: String): SignInResponseDto =
        client.post("$BASE_PATH/auth/apple") {
            contentType(ContentType.Application.Json)
            setBody(SignInRequestDto(idToken = idToken, nonce = rawNonce))
        }.body()

    suspend fun me(): AuthUserDto =
        client.get("$BASE_PATH/me").body()

    companion object {
        private const val BASE_PATH = "/api/v1"
    }
}
