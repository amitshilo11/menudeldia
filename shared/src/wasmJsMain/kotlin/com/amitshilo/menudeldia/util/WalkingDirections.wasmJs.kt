package com.amitshilo.menudeldia.util

actual fun walkingDirectionsUri(lat: Double, lng: Double): String =
    "https://www.google.com/maps/dir/?api=1&destination=$lat,$lng&travelmode=walking"
