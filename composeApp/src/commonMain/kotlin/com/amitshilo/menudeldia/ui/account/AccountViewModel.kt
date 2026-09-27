package com.amitshilo.menudeldia.ui.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amitshilo.menudeldia.auth.AuthProviderHolder
import com.amitshilo.menudeldia.di.AppGraphProvider
import com.amitshilo.menudeldia.domain.auth.model.AuthState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AccountViewModel : ViewModel() {

    private val authRepository = AppGraphProvider.appGraph.authRepository

    val authState: StateFlow<AuthState> = authRepository.state

    private val _isDeleting = MutableStateFlow(false)
    val isDeleting: StateFlow<Boolean> = _isDeleting.asStateFlow()

    /** Set when deletion fails, so the screen can say so instead of silently doing nothing. */
    private val _deleteFailed = MutableStateFlow(false)
    val deleteFailed: StateFlow<Boolean> = _deleteFailed.asStateFlow()

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
            AuthProviderHolder.current?.signOutPlatform()
        }
    }

    /**
     * Deletes the account server-side. Only on success is the platform sign-in provider
     * cleared too — a failed call must leave the user signed in to the account that still
     * exists, rather than stranding them outside it.
     */
    fun deleteAccount() {
        if (_isDeleting.value) return
        viewModelScope.launch {
            _isDeleting.value = true
            _deleteFailed.value = false
            authRepository.deleteAccount()
                .onSuccess { AuthProviderHolder.current?.signOutPlatform() }
                .onFailure { _deleteFailed.value = true }
            _isDeleting.value = false
        }
    }

    fun dismissDeleteError() {
        _deleteFailed.value = false
    }
}
