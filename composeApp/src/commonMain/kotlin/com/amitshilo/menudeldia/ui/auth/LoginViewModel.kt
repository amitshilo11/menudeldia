package com.amitshilo.menudeldia.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amitshilo.menudeldia.auth.AuthProviderHolder
import com.amitshilo.menudeldia.di.AppGraphProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import menudeldia.composeapp.generated.resources.Res
import menudeldia.composeapp.generated.resources.sign_in_cancelled
import menudeldia.composeapp.generated.resources.sign_in_failed
import menudeldia.composeapp.generated.resources.sign_in_unavailable
import org.jetbrains.compose.resources.getString

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
        val provider = authProvider
        if (provider == null) {
            viewModelScope.launch {
                _uiState.value = LoginUiState.Error(getString(Res.string.sign_in_unavailable))
            }
            return
        }
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            runCatching { provider.signInWithGoogle() }
                .onSuccess { result ->
                    authRepository.signInWithGoogle(result.idToken)
                        .onFailure {
                            _uiState.value = LoginUiState.Error(
                                it.message ?: getString(Res.string.sign_in_failed)
                            )
                        }
                        .onSuccess { _uiState.value = LoginUiState.Idle }
                }
                .onFailure {
                    _uiState.value =
                        LoginUiState.Error(it.message ?: getString(Res.string.sign_in_cancelled))
                }
        }
    }

    fun signInWithApple() {
        val provider = authProvider
        if (provider == null) {
            viewModelScope.launch {
                _uiState.value = LoginUiState.Error(getString(Res.string.sign_in_unavailable))
            }
            return
        }
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            runCatching { provider.signInWithApple() }
                .onSuccess { result ->
                    authRepository.signInWithApple(result.identityToken, result.rawNonce)
                        .onFailure {
                            _uiState.value = LoginUiState.Error(
                                it.message ?: getString(Res.string.sign_in_failed)
                            )
                        }
                        .onSuccess { _uiState.value = LoginUiState.Idle }
                }
                .onFailure {
                    _uiState.value =
                        LoginUiState.Error(it.message ?: getString(Res.string.sign_in_cancelled))
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
