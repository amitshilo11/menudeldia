package com.amitshilo.menudeldia.data.auth.local

import com.amitshilo.menudeldia.domain.auth.model.AuthSession
import com.amitshilo.menudeldia.domain.auth.model.AuthUser
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SessionStore(private val settings: Settings) {

    private val _sessionFlow = MutableStateFlow<AuthSession?>(null)
    val sessionFlow: StateFlow<AuthSession?> = _sessionFlow.asStateFlow()

    private val _isGuestFlow = MutableStateFlow(false)
    val isGuestFlow: StateFlow<Boolean> = _isGuestFlow.asStateFlow()

    init {
        _isGuestFlow.value = settings.getBoolean(KEY_IS_GUEST, false)
        val token = settings.getStringOrNull(KEY_ACCESS_TOKEN)
        if (token != null) {
            _sessionFlow.value = AuthSession(
                accessToken = token,
                user = AuthUser(
                    id = settings.getString(KEY_USER_ID, ""),
                    email = settings.getStringOrNull(KEY_USER_EMAIL),
                    displayName = settings.getStringOrNull(KEY_USER_DISPLAY_NAME),
                    avatarUrl = settings.getStringOrNull(KEY_USER_AVATAR_URL),
                ),
            )
        }
    }

    fun currentToken(): String? = _sessionFlow.value?.accessToken

    fun save(session: AuthSession) {
        settings.putString(KEY_ACCESS_TOKEN, session.accessToken)
        settings.putString(KEY_USER_ID, session.user.id)
        session.user.email?.let { settings.putString(KEY_USER_EMAIL, it) }
        session.user.displayName?.let { settings.putString(KEY_USER_DISPLAY_NAME, it) }
        session.user.avatarUrl?.let { settings.putString(KEY_USER_AVATAR_URL, it) }
        settings.putBoolean(KEY_IS_GUEST, false)
        _isGuestFlow.value = false
        _sessionFlow.value = session
    }

    fun saveGuest() {
        settings.putBoolean(KEY_IS_GUEST, true)
        _isGuestFlow.value = true
        _sessionFlow.value = null
    }

    fun clear() {
        settings.remove(KEY_ACCESS_TOKEN)
        settings.remove(KEY_USER_ID)
        settings.remove(KEY_USER_EMAIL)
        settings.remove(KEY_USER_DISPLAY_NAME)
        settings.remove(KEY_USER_AVATAR_URL)
        settings.putBoolean(KEY_IS_GUEST, false)
        _isGuestFlow.value = false
        _sessionFlow.value = null
    }

    companion object {
        private const val KEY_ACCESS_TOKEN = "auth.access_token"
        private const val KEY_USER_ID = "auth.user_id"
        private const val KEY_USER_EMAIL = "auth.user_email"
        private const val KEY_USER_DISPLAY_NAME = "auth.user_display_name"
        private const val KEY_USER_AVATAR_URL = "auth.user_avatar_url"
        private const val KEY_IS_GUEST = "auth.is_guest"
    }
}
