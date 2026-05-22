package com.amitshilo.menudeldia.util

import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

private const val EARTH_RADIUS_METERS = 6_371_000.0

/**
 * Great-circle distance between two lat/lng pairs in meters.
 */
fun haversineMeters(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
    val phi1 = lat1 * PI / 180
    val phi2 = lat2 * PI / 180
    val deltaPhi = (lat2 - lat1) * PI / 180
    val deltaLambda = (lng2 - lng1) * PI / 180
    val a = sin(deltaPhi / 2).pow(2) + cos(phi1) * cos(phi2) * sin(deltaLambda / 2).pow(2)
    return EARTH_RADIUS_METERS * 2 * atan2(sqrt(a), sqrt(1 - a))
}
