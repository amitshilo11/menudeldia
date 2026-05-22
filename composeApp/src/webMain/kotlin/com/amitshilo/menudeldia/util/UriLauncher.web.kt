package com.amitshilo.menudeldia.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.browser.window

actual class UriLauncher {
    actual fun open(uri: String) {
        window.open(uri, "_blank")
    }
}

@Composable
actual fun rememberUriLauncher(): UriLauncher = remember { UriLauncher() }
