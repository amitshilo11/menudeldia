package com.amitshilo.menudeldia.data.auth.remote

import com.amitshilo.menudeldia.data.auth.remote.dto.ApiErrorDto
import com.amitshilo.menudeldia.data.auth.remote.dto.AuthUserDto
import com.amitshilo.menudeldia.data.auth.remote.dto.SignInRequestDto
import com.amitshilo.menudeldia.data.auth.remote.dto.SignInResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess

class AuthApiService(private val client: HttpClient) {

    suspend fun signInWithGoogle(idToken: String): SignInResponseDto =
        client.post("$BASE_PATH/auth/google") {
            contentType(ContentType.Application.Json)
            setBody(SignInRequestDto(idToken = idToken))
        }.bodyOrThrow()

    suspend fun signInWithApple(idToken: String, rawNonce: String): SignInResponseDto =
        client.post("$BASE_PATH/auth/apple") {
            contentType(ContentType.Application.Json)
            setBody(SignInRequestDto(idToken = idToken, nonce = rawNonce))
        }.bodyOrThrow()

    suspend fun me(): AuthUserDto =
        client.get("$BASE_PATH/me").body()

    /** Erases the account server-side. Returns 204 with no body. */
    suspend fun deleteAccount() {
        val response = client.delete("$BASE_PATH/me")
        if (!response.status.isSuccess()) {
            val apiError = runCatching { response.body<ApiErrorDto>() }.getOrNull()
            throw IllegalStateException(
                apiError?.message ?: "Account deletion failed (${response.status.value})"
            )
        }
    }

    private suspend inline fun <reified T> HttpResponse.bodyOrThrow(): T {
        if (!status.isSuccess()) {
            val apiError = runCatching { body<ApiErrorDto>() }.getOrNull()
            throw IllegalStateException(apiError?.message ?: "Sign-in failed (${status.value})")
        }
        return body()
    }

    companion object {
        private const val BASE_PATH = "/api/v1"
    }
}
