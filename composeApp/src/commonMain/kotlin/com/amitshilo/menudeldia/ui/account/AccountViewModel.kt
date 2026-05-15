package com.amitshilo.menudeldia.ui.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amitshilo.menudeldia.auth.AuthProviderHolder
import com.amitshilo.menudeldia.di.AppGraphProvider
import com.amitshilo.menudeldia.domain.auth.model.AuthState
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AccountViewModel : ViewModel() {

    private val authRepository = AppGraphProvider.appGraph.authRepository

    val authState: StateFlow<AuthState> = authRepository.state

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
            AuthProviderHolder.current?.signOutPlatform()
        }
    }
}
