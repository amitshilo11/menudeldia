package com.amitshilo.menudeldia.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class RestaurantListResponse(
    val restaurants: List<RestaurantDto>,
)

@Serializable
data class RestaurantDto(
    val id: String,
    val name: String,
    val lat: Double,
    val lng: Double,
    val address: String,
    val phone: String? = null,
    val thumbnailUrl: String? = null,
    val photos: List<String> = emptyList(),
    val descriptionEs: String? = null,
    val descriptionEn: String? = null,
    val menuPrice: Double? = null,
    val currency: String = "EUR",
    val todayHasMenu: Boolean = false,
    val cuisineEmoji: String? = null,
    val cuisineType: String? = null,
    val openingHours: List<OpeningHoursDto> = emptyList(),
    val rating: Double? = null,
)

@Serializable
data class OpeningHoursDto(
    val dayOfWeek: Int,
    val openTime: String,
    val closeTime: String,
    val isClosed: Boolean = false,
)
