package com.amitshilo.menudeldia.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amitshilo.menudeldia.auth.AuthProviderHolder
import com.amitshilo.menudeldia.di.AppGraphProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface LoginUiState {
    data object Idle : LoginUiState
    data object Loading : LoginUiState
    data class Error(val message: String) : LoginUiState
}

class LoginViewModel : ViewModel() {

    private val authRepository = AppGraphProvider.appGraph.authRepository
    private val authProvider get() = AuthProviderHolder.current

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun signInWithGoogle() {
        val provider = authProvider ?: run {
            _uiState.value = LoginUiState.Error("Sign-in not available on this platform")
            return
        }
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            runCatching { provider.signInWithGoogle() }
                .onSuccess { result ->
                    authRepository.signInWithGoogle(result.idToken)
                        .onFailure {
                            _uiState.value = LoginUiState.Error(it.message ?: "Sign-in failed")
                        }
                        .onSuccess { _uiState.value = LoginUiState.Idle }
                }
                .onFailure {
                    _uiState.value = LoginUiState.Error(it.message ?: "Sign-in cancelled")
                }
        }
    }

    fun signInWithApple() {
        val provider = authProvider ?: run {
            _uiState.value = LoginUiState.Error("Sign-in not available on this platform")
            return
        }
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            runCatching { provider.signInWithApple() }
                .onSuccess { result ->
                    authRepository.signInWithApple(result.identityToken, result.rawNonce)
                        .onFailure {
                            _uiState.value = LoginUiState.Error(it.message ?: "Sign-in failed")
                        }
                        .onSuccess { _uiState.value = LoginUiState.Idle }
                }
                .onFailure {
                    _uiState.value = LoginUiState.Error(it.message ?: "Sign-in cancelled")
                }
        }
    }

    fun continueAsGuest() {
        viewModelScope.launch { authRepository.continueAsGuest() }
    }

    fun clearError() {
        _uiState.value = LoginUiState.Idle
    }
}
