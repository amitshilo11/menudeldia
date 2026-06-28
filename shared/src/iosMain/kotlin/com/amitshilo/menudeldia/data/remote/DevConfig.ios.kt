@file:OptIn(kotlin.experimental.ExperimentalNativeApi::class)

package com.amitshilo.menudeldia.data.remote

actual val apiBaseUrl: String =
    if (Platform.isDebugBinary) "http://localhost:8080"
    else "https://menudiz.duckdns.org"
actual val useMockData: Boolean = Platform.isDebugBinary
