package com.amitshilo.menudeldia.util

actual fun Double.format(decimals: Int): String {
    return asDynamic().toFixed(decimals).toString()
}
