package com.menudeldia.restaurant.dto

import java.math.BigDecimal
import java.util.UUID

/**
 * Wire shape returned to the client. Mirrors shared module's `RestaurantDto`.
 * TODO B1.4.5: confirm field names match shared/.../data/remote/dto/RestaurantDto.kt before merging.
 */
data class RestaurantDto(
    val id: UUID,
    val name: String,
    val lat: Double,
    val lng: Double,
    val address: String?,
    val phone: String?,
    val thumbnailUrl: String?,
    val photos: List<String>,
    val descriptionEs: String?,
    val descriptionEn: String?,
    val openingHours: List<OpeningHoursDto>,
    val menuPrice: BigDecimal?,
    val currency: String,
    val todayHasMenu: Boolean,
    val cuisineEmoji: String?,
    val cuisineType: String?,
    val distanceMeters: Double?,
    val isOpenNow: Boolean,
)
