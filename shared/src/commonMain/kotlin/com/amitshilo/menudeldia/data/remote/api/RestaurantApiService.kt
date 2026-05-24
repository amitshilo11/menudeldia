package com.amitshilo.menudeldia.data.remote.api

import com.amitshilo.menudeldia.data.remote.dto.MenuDto
import com.amitshilo.menudeldia.data.remote.dto.RestaurantDto
import com.amitshilo.menudeldia.data.remote.dto.RestaurantListResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.isSuccess

class RestaurantApiService(private val client: HttpClient) {

    suspend fun getNearbyRestaurants(
        lat: Double,
        lng: Double,
        radiusMeters: Int,
    ): RestaurantListResponse = client.get("$BASE_PATH/restaurants") {
        parameter("lat", lat)
        parameter("lng", lng)
        parameter("radius", radiusMeters)
    }.body()

    suspend fun getRestaurantDetail(id: String): RestaurantDto =
        client.get("$BASE_PATH/restaurants/$id").body()

    suspend fun getTodayMenu(restaurantId: String): MenuDto? = try {
        val response = client.get("$BASE_PATH/restaurants/$restaurantId/menu/today")
        if (response.status.isSuccess()) response.body() else null
    } catch (_: ClientRequestException) {
        null
    } catch (_: ServerResponseException) {
        null
    }

    companion object {
        private const val BASE_PATH = "/api/v1"
    }
}
