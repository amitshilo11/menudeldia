package com.amitshilo.menudeldia.domain.usecase

import com.amitshilo.menudeldia.domain.model.Restaurant
import com.amitshilo.menudeldia.domain.repository.RestaurantRepository

class GetNearbyRestaurantsUseCase(
    private val repository: RestaurantRepository,
) {
    suspend operator fun invoke(
        lat: Double,
        lng: Double,
        radiusMeters: Int = 1000,
    ): List<Restaurant> = repository.getNearbyRestaurants(lat, lng, radiusMeters)
}
