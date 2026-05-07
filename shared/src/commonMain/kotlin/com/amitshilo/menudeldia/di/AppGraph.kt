package com.amitshilo.menudeldia.di

import com.amitshilo.menudeldia.data.local.MockRestaurantRepository
import com.amitshilo.menudeldia.domain.repository.RestaurantRepository
import com.amitshilo.menudeldia.domain.usecase.GetNearbyRestaurantsUseCase
import com.amitshilo.menudeldia.domain.usecase.GetRestaurantDetailUseCase
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

@SingleIn(AppScope::class)
@DependencyGraph
interface AppGraph {

    val getNearbyRestaurantsUseCase: GetNearbyRestaurantsUseCase
    val getRestaurantDetailUseCase: GetRestaurantDetailUseCase

    companion object {
        @Provides
        @SingleIn(AppScope::class)
        fun provideRestaurantRepository(): RestaurantRepository = MockRestaurantRepository()

        @Provides
        fun provideGetNearbyRestaurantsUseCase(
            repository: RestaurantRepository,
        ): GetNearbyRestaurantsUseCase = GetNearbyRestaurantsUseCase(repository)

        @Provides
        fun provideGetRestaurantDetailUseCase(
            repository: RestaurantRepository,
        ): GetRestaurantDetailUseCase = GetRestaurantDetailUseCase(repository)
    }
}

object AppScope
