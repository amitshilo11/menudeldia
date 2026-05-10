package com.menudeldia.geo

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Geo helpers.
 * Prefer PostGIS (`ST_Distance`, `ST_DWithin`) for query-time geo work.
 * These helpers are only for in-memory computation when PostGIS isn't in scope.
 */
object GeoUtils {

    private const val EARTH_RADIUS_METERS = 6_371_000.0

    /** Great-circle distance between two lat/lng points in meters. */
    fun haversineMeters(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)
        val a = sin(dLat / 2).let { it * it } +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLng / 2).let { it * it }
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return EARTH_RADIUS_METERS * c
    }
}
