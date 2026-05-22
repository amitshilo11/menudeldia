package com.amitshilo.menudeldia.auth

/** Set from MainActivity (Android) or MainViewController (iOS) before the Compose tree is created. */
object AuthProviderHolder {
    var current: AuthProvider? = null
}
