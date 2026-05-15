package com.amitshilo.menudeldia.util

import io.ktor.client.plugins.logging.Logger
import timber.log.Timber

actual val ktorLogger: Logger = object : Logger {
    override fun log(message: String) {
        Timber.tag("network").d(message)
    }
}
