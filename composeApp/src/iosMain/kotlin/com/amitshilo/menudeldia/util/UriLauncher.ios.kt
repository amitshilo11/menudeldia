package com.amitshilo.menudeldia.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.Foundation.NSURL
import platform.UIKit.UIApplication

actual class UriLauncher {
    actual fun open(uri: String) {
        val url = NSURL.URLWithString(uri) ?: return
        UIApplication.sharedApplication.openURL(url)
    }
}

@Composable
actual fun rememberUriLauncher(): UriLauncher = remember { UriLauncher() }
