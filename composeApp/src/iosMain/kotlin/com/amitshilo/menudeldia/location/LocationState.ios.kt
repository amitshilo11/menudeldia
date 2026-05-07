package com.amitshilo.menudeldia.location

import androidx.compose.runtime.Composable

@Composable
actual fun rememberLocationState(): LocationState = LocationState(
    hasPermission = false,
    location = null,
)
