package com.amitshilo.menudeldia.data.repository

import com.amitshilo.menudeldia.data.mapper.toDomain
import com.amitshilo.menudeldia.data.remote.api.RestaurantApiService
import com.amitshilo.menudeldia.domain.model.Menu
import com.amitshilo.menudeldia.domain.model.Restaurant
import com.amitshilo.menudeldia.domain.repository.RestaurantRepository

class RestaurantRepositoryImpl(
    private val apiService: RestaurantApiService,
) : RestaurantRepository {

    override suspend fun getNearbyRestaurants(
        lat: Double,
        lng: Double,
        radiusMeters: Int,
    ): List<Restaurant> = apiService
        .getNearbyRestaurants(lat, lng, radiusMeters)
        .restaurants
        .map { it.toDomain() }

    override suspend fun getRestaurantDetail(id: String): Restaurant =
        apiService.getRestaurantDetail(id).toDomain()

    override suspend fun getTodayMenu(restaurantId: String): Menu? =
        apiService.getTodayMenu(restaurantId)?.toDomain()
}
