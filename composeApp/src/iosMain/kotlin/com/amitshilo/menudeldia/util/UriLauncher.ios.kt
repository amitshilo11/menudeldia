package com.amitshilo.menudeldia.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.Foundation.NSCharacterSet
import platform.Foundation.NSString
import platform.Foundation.NSURL
import platform.Foundation.URLQueryAllowedCharacterSet
import platform.Foundation.stringByAddingPercentEncodingWithAllowedCharacters
import platform.UIKit.UIApplication

actual class UriLauncher {
    actual fun open(uri: String) {
        val encoded = (uri as NSString).stringByAddingPercentEncodingWithAllowedCharacters(
            NSCharacterSet.URLQueryAllowedCharacterSet,
        ) ?: uri
        val url = NSURL.URLWithString(encoded) ?: return
        UIApplication.sharedApplication.openURL(
            url,
            options = emptyMap<Any?, Any?>(),
            completionHandler = null
        )
    }
}

@Composable
actual fun rememberUriLauncher(): UriLauncher = remember { UriLauncher() }
