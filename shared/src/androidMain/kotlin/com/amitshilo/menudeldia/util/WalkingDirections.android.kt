package com.amitshilo.menudeldia.util

actual fun walkingDirectionsUri(lat: Double, lng: Double): String =
    "google.navigation:q=$lat,$lng&mode=w"
