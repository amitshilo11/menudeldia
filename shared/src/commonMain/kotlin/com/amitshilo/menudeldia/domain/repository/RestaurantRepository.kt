package com.amitshilo.menudeldia.domain.repository

import com.amitshilo.menudeldia.domain.model.Menu
import com.amitshilo.menudeldia.domain.model.Restaurant

interface RestaurantRepository {
    suspend fun getNearbyRestaurants(lat: Double, lng: Double, radiusMeters: Int): List<Restaurant>
    suspend fun getRestaurantDetail(id: String): Restaurant
    suspend fun getTodayMenu(restaurantId: String): Menu?
}
