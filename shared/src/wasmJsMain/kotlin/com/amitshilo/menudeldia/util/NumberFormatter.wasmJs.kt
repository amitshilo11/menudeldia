package com.amitshilo.menudeldia.util

actual fun Double.format(decimals: Int): String {
    // Basic implementation for WasmJs if native is not easily accessible
    val s = this.toString()
    val dotIndex = s.indexOf('.')
    return if (dotIndex == -1) {
        if (decimals > 0) s + "." + "0".repeat(decimals) else s
    } else {
        val currentDecimals = s.length - dotIndex - 1
        if (currentDecimals < decimals) {
            s + "0".repeat(decimals - currentDecimals)
        } else if (currentDecimals > decimals) {
            s.substring(0, dotIndex + decimals + 1)
        } else {
            s
        }
    }
}
