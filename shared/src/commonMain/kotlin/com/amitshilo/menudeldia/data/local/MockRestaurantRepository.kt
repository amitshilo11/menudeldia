package com.amitshilo.menudeldia.data.local

import com.amitshilo.menudeldia.domain.model.Menu
import com.amitshilo.menudeldia.domain.model.Restaurant
import com.amitshilo.menudeldia.domain.repository.RestaurantRepository
import com.amitshilo.menudeldia.util.haversineMeters

class MockRestaurantRepository : RestaurantRepository {

    override suspend fun getNearbyRestaurants(
        lat: Double,
        lng: Double,
        radiusMeters: Int
    ): List<Restaurant> =
        mockRestaurants
            .map { it.copy(distanceMeters = haversineMeters(lat, lng, it.lat, it.lng)) }
            .filter { it.distanceMeters!! <= radiusMeters }
            .sortedBy { it.distanceMeters }

    override suspend fun getRestaurantDetail(id: String): Restaurant =
        mockRestaurants.first { it.id == id }

    override suspend fun getTodayMenu(restaurantId: String): Menu? =
        mockMenus[restaurantId]
}
