package com.amitshilo.menudeldia.util

import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertTrue

class GeoUtilsTest {

    @Test
    fun `zero distance for identical points`() {
        val d = haversineMeters(41.3851, 2.1734, 41.3851, 2.1734)
        assertTrue(d < 0.01, "Expected ~0 meters, got $d")
    }

    @Test
    fun `barcelona to madrid is roughly 505 km`() {
        // Barcelona (Plaça de Catalunya) → Madrid (Puerta del Sol)
        val d = haversineMeters(41.3870, 2.1701, 40.4170, -3.7038)
        val km = d / 1000.0
        assertTrue(km in 490.0..520.0, "Expected ~505 km, got $km km")
    }

    @Test
    fun `nearby barcelona points under 1km`() {
        // Rambla Catalunya 43 → Plaça de Catalunya, both in central BCN
        val d = haversineMeters(41.3917, 2.1649, 41.3870, 2.1701)
        assertTrue(d in 100.0..1000.0, "Expected 100m–1km, got ${d}m")
    }

    @Test
    fun `symmetric — order does not matter`() {
        val a = haversineMeters(41.3851, 2.1734, 40.4170, -3.7038)
        val b = haversineMeters(40.4170, -3.7038, 41.3851, 2.1734)
        assertTrue(abs(a - b) < 0.01, "Distance should be symmetric")
    }
}
