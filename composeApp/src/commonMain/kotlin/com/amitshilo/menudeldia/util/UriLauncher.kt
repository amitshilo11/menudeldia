package com.amitshilo.menudeldia.util

import androidx.compose.runtime.Composable

/**
 * Opens an arbitrary URI in the platform's default handler (browser, dialer, maps, ...).
 * Errors are swallowed silently — callers should validate URIs before invoking.
 */
expect class UriLauncher {
    fun open(uri: String)
}

@Composable
expect fun rememberUriLauncher(): UriLauncher
