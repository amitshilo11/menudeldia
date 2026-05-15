package com.amitshilo.menudeldia.util

import io.ktor.client.plugins.logging.Logger

actual val ktorLogger: Logger = object : Logger {
    override fun log(message: String) {
        println("network: $message")
    }
}
