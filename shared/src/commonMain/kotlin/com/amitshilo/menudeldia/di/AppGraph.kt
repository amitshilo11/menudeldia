package com.amitshilo.menudeldia.di

import com.amitshilo.menudeldia.data.auth.local.SessionStore
import com.amitshilo.menudeldia.data.auth.remote.AuthApiService
import com.amitshilo.menudeldia.data.auth.repository.AuthRepositoryImpl
import com.amitshilo.menudeldia.data.remote.api.RestaurantApiService
import com.amitshilo.menudeldia.data.remote.apiBaseUrl
import com.amitshilo.menudeldia.data.repository.RestaurantRepositoryImpl
import com.amitshilo.menudeldia.domain.auth.repository.AuthRepository
import com.amitshilo.menudeldia.domain.repository.RestaurantRepository
import com.amitshilo.menudeldia.domain.usecase.GetNearbyRestaurantsUseCase
import com.amitshilo.menudeldia.domain.usecase.GetRestaurantDetailUseCase
import com.amitshilo.menudeldia.util.ktorLogger
import com.russhwolf.settings.Settings
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import io.ktor.client.HttpClient
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

@SingleIn(AppScope::class)
@DependencyGraph
interface AppGraph {

    val getNearbyRestaurantsUseCase: GetNearbyRestaurantsUseCase
    val getRestaurantDetailUseCase: GetRestaurantDetailUseCase
    val authRepository: AuthRepository

    companion object {
        @Provides
        @SingleIn(AppScope::class)
        fun provideSettings(): Settings = Settings()

        @Provides
        @SingleIn(AppScope::class)
        fun provideSessionStore(settings: Settings): SessionStore = SessionStore(settings)

        @Provides
        @SingleIn(AppScope::class)
        fun provideHttpClient(sessionStore: SessionStore): HttpClient = HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                })
            }
            install(Logging) {
                level = LogLevel.ALL
                logger = ktorLogger
            }
            install(Auth) {
                bearer {
                    loadTokens {
                        sessionStore.currentToken()?.let { BearerTokens(it, "") }
                    }
                    // Skip auth header on sign-in endpoints — no token exists yet.
                    sendWithoutRequest { request ->
                        request.url.pathSegments.getOrNull(2) != "auth"
                    }
                }
            }
            defaultRequest { url(apiBaseUrl) }
        }

        @Provides
        @SingleIn(AppScope::class)
        fun provideRestaurantApiService(client: HttpClient): RestaurantApiService =
            RestaurantApiService(client)

        @Provides
        @SingleIn(AppScope::class)
        fun provideRestaurantRepository(apiService: RestaurantApiService): RestaurantRepository =
            RestaurantRepositoryImpl(apiService)

        @Provides
        fun provideGetNearbyRestaurantsUseCase(
            repository: RestaurantRepository,
        ): GetNearbyRestaurantsUseCase = GetNearbyRestaurantsUseCase(repository)

        @Provides
        fun provideGetRestaurantDetailUseCase(
            repository: RestaurantRepository,
        ): GetRestaurantDetailUseCase = GetRestaurantDetailUseCase(repository)

        @Provides
        @SingleIn(AppScope::class)
        fun provideAuthApiService(client: HttpClient): AuthApiService = AuthApiService(client)

        @Provides
        @SingleIn(AppScope::class)
        fun provideAuthRepository(
            apiService: AuthApiService,
            sessionStore: SessionStore,
        ): AuthRepository = AuthRepositoryImpl(apiService, sessionStore)
    }
}

object AppScope
