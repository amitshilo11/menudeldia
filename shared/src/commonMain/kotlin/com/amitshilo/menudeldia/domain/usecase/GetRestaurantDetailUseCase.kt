package com.amitshilo.menudeldia.domain.usecase

import com.amitshilo.menudeldia.domain.model.Menu
import com.amitshilo.menudeldia.domain.model.Restaurant
import com.amitshilo.menudeldia.domain.repository.RestaurantRepository

class GetRestaurantDetailUseCase(
    private val repository: RestaurantRepository,
) {
    suspend fun getDetail(id: String): Restaurant = repository.getRestaurantDetail(id)
    suspend fun getTodayMenu(restaurantId: String): Menu? = repository.getTodayMenu(restaurantId)
}
