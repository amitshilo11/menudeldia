package com.amitshilo.menudeldia.ui.root

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amitshilo.menudeldia.di.AppGraphProvider
import com.amitshilo.menudeldia.domain.auth.model.AuthState
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RootViewModel : ViewModel() {

    private val authRepository = AppGraphProvider.appGraph.authRepository

    val authState: StateFlow<AuthState> = authRepository.state

    init {
        // On cold start with a stored token, validate it against /me.
        if (authState.value is AuthState.Authenticated) {
            viewModelScope.launch { authRepository.refreshFromMe() }
        }
    }
}
