package com.amitshilo.menudeldia.location

import androidx.compose.runtime.Composable

data class UserLocation(val lat: Double, val lng: Double)

data class LocationState(
    val hasPermission: Boolean,
    val location: UserLocation?,
    val requestPermission: () -> Unit = {},
)

expect @Composable
fun rememberLocationState(): LocationState
