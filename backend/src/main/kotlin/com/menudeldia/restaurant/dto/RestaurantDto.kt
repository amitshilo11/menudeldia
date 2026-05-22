package com.menudeldia.restaurant.dto

/**
 * Wire shape returned to the client. Core fields mirror shared module's RestaurantDto.
 * Extra fields (distanceMeters, isOpenNow, priceIncludes*) are additive — client ignores unknowns.
 */
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
    val distanceMeters: Double? = null,
    val isOpenNow: Boolean = false,
    val priceIncludesEs: List<String> = emptyList(),
    val priceIncludesEn: List<String> = emptyList(),
)
