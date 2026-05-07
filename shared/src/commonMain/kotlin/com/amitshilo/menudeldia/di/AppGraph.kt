package com.amitshilo.menudeldia.di

import com.amitshilo.menudeldia.data.remote.api.RestaurantApiService
import com.amitshilo.menudeldia.data.repository.RestaurantRepositoryImpl
import com.amitshilo.menudeldia.domain.repository.RestaurantRepository
import com.amitshilo.menudeldia.domain.usecase.GetNearbyRestaurantsUseCase
import com.amitshilo.menudeldia.domain.usecase.GetRestaurantDetailUseCase
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

@SingleIn(AppScope::class)
@DependencyGraph
interface AppGraph {

    val getNearbyRestaurantsUseCase: GetNearbyRestaurantsUseCase
    val getRestaurantDetailUseCase: GetRestaurantDetailUseCase

    @DependencyGraph.Factory
    interface Factory {
        fun create(baseUrl: String): AppGraph
    }

    companion object {
        @Provides
        @SingleIn(AppScope::class)
        fun provideJson(): Json = Json {
            ignoreUnknownKeys = true
            isLenient = true
        }

        @Provides
        @SingleIn(AppScope::class)
        fun provideHttpClient(json: Json): HttpClient = HttpClient {
            install(ContentNegotiation) { json(json) }
            install(Logging) { level = LogLevel.INFO }
        }

        @Provides
        @SingleIn(AppScope::class)
        fun provideApiService(client: HttpClient): RestaurantApiService =
            RestaurantApiService(client)

        @Provides
        @SingleIn(AppScope::class)
        fun provideRestaurantRepository(
            apiService: RestaurantApiService,
        ): RestaurantRepository = RestaurantRepositoryImpl(apiService)

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
