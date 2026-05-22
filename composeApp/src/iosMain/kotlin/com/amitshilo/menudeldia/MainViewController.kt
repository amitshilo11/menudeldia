package com.amitshilo.menudeldia

import androidx.compose.ui.window.ComposeUIViewController
import com.amitshilo.menudeldia.auth.AuthProvider
import com.amitshilo.menudeldia.auth.AuthProviderHolder
import com.amitshilo.menudeldia.auth.IosAuthBridge

fun MainViewController(bridge: IosAuthBridge) = ComposeUIViewController {
    AuthProviderHolder.current = AuthProvider(bridge)
    App()
}