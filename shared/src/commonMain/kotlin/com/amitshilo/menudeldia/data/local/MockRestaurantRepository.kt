package com.amitshilo.menudeldia.data.local

import com.amitshilo.menudeldia.domain.model.Menu
import com.amitshilo.menudeldia.domain.model.Restaurant
import com.amitshilo.menudeldia.domain.repository.RestaurantRepository
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

class MockRestaurantRepository : RestaurantRepository {

    override suspend fun getNearbyRestaurants(lat: Double, lng: Double, radiusMeters: Int): List<Restaurant> =
        mockRestaurants
            .map { it.copy(distanceMeters = haversineMeters(lat, lng, it.lat, it.lng)) }
            .filter { it.distanceMeters!! <= radiusMeters }
            .sortedBy { it.distanceMeters }

    override suspend fun getRestaurantDetail(id: String): Restaurant =
        mockRestaurants.first { it.id == id }

    override suspend fun getTodayMenu(restaurantId: String): Menu? =
        mockMenus[restaurantId]
}

private fun haversineMeters(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
    val R = 6_371_000.0
    val φ1 = lat1 * PI / 180
    val φ2 = lat2 * PI / 180
    val Δφ = (lat2 - lat1) * PI / 180
    val Δλ = (lng2 - lng1) * PI / 180
    val a = sin(Δφ / 2).pow(2) + cos(φ1) * cos(φ2) * sin(Δλ / 2).pow(2)
    return R * 2 * atan2(sqrt(a), sqrt(1 - a))
}
