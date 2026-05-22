package com.amitshilo.menudeldia.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

actual class UriLauncher(private val context: Context) {
    actual fun open(uri: String) {
        runCatching {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }
}

@Composable
actual fun rememberUriLauncher(): UriLauncher {
    val context = LocalContext.current
    return remember(context) { UriLauncher(context) }
}
