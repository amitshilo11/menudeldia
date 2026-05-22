package com.amitshilo.menudeldia.util

/**
 * Platform-specific URI that opens a walking-directions screen in the native maps app
 * (Google Maps on Android, Apple Maps on iOS, generic Google Maps web URL elsewhere).
 */
expect fun walkingDirectionsUri(lat: Double, lng: Double): String
