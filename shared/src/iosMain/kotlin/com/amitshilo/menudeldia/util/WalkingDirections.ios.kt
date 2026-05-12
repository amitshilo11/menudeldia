package com.amitshilo.menudeldia.util

actual fun walkingDirectionsUri(lat: Double, lng: Double): String =
    "https://maps.apple.com/?daddr=$lat,$lng&dirflg=w"
