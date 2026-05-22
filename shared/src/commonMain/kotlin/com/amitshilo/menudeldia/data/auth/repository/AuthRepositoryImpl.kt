package com.amitshilo.menudeldia.data.auth.repository

import com.amitshilo.menudeldia.data.auth.local.SessionStore
import com.amitshilo.menudeldia.data.auth.remote.AuthApiService
import com.amitshilo.menudeldia.domain.auth.model.AuthSession
import com.amitshilo.menudeldia.domain.auth.model.AuthState
import com.amitshilo.menudeldia.domain.auth.model.AuthUser
import com.amitshilo.menudeldia.domain.auth.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthRepositoryImpl(
    private val apiService: AuthApiService,
    private val sessionStore: SessionStore,
) : AuthRepository {

    private val _state = MutableStateFlow<AuthState>(AuthState.Loading)
    override val state: StateFlow<AuthState> = _state.asStateFlow()

    init {
        val session = sessionStore.sessionFlow.value
        val isGuest = sessionStore.isGuestFlow.value
        _state.value = when {
            session != null -> AuthState.Authenticated(session)
            isGuest -> AuthState.Guest
            else -> AuthState.NeedsAuth
        }
    }

    override suspend fun signInWithGoogle(idToken: String): Result<AuthSession> = runCatching {
        val response = apiService.signInWithGoogle(idToken)
        val session = AuthSession(
            accessToken = response.accessToken,
            user = AuthUser(
                response.user.id,
                response.user.email,
                response.user.displayName,
                response.user.avatarUrl
            ),
        )
        sessionStore.save(session)
        _state.value = AuthState.Authenticated(session)
        session
    }

    override suspend fun signInWithApple(idToken: String, rawNonce: String): Result<AuthSession> =
        runCatching {
            val response = apiService.signInWithApple(idToken, rawNonce)
            val session = AuthSession(
                accessToken = response.accessToken,
                user = AuthUser(
                    response.user.id,
                    response.user.email,
                    response.user.displayName,
                    response.user.avatarUrl
                ),
            )
            sessionStore.save(session)
            _state.value = AuthState.Authenticated(session)
            session
        }

    override suspend fun continueAsGuest() {
        sessionStore.saveGuest()
        _state.value = AuthState.Guest
    }

    override suspend fun refreshFromMe(): Result<AuthSession> = runCatching {
        val user = apiService.me()
        val currentToken = sessionStore.currentToken() ?: error("No token to refresh with")
        val session = AuthSession(
            accessToken = currentToken,
            user = AuthUser(user.id, user.email, user.displayName, user.avatarUrl),
        )
        sessionStore.save(session)
        _state.value = AuthState.Authenticated(session)
        session
    }.onFailure {
        sessionStore.clear()
        _state.value = AuthState.NeedsAuth
    }

    override suspend fun signOut() {
        sessionStore.clear()
        _state.value = AuthState.NeedsAuth
    }
}
