package com.amitshilo.menudeldia.util

import java.util.Locale

actual fun Double.format(decimals: Int): String {
    return String.format(Locale.US, "%.${decimals}f", this)
}
