package com.amitshilo.menudeldia.domain.auth.repository

import com.amitshilo.menudeldia.domain.auth.model.AuthSession
import com.amitshilo.menudeldia.domain.auth.model.AuthState
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    val state: StateFlow<AuthState>
    suspend fun signInWithGoogle(idToken: String): Result<AuthSession>
    suspend fun signInWithApple(idToken: String, rawNonce: String): Result<AuthSession>
    suspend fun continueAsGuest()

    /** Hits /me to validate + refresh session on cold start. */
    suspend fun refreshFromMe(): Result<AuthSession>
    suspend fun signOut()
}
