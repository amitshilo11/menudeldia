package com.menudeldia.admin

import com.menudeldia.restaurant.ReviewData
import java.math.BigDecimal

data class AdminRestaurantDto(
    val id: String,
    // -- Editable / CSV-mapped (and `hidden`, which is DB-only) ---------------
    val name: String,
    val cuisineType: String?,
    val cuisineEmoji: String?,
    val menuPrice: BigDecimal?,
    val priceAlt: String?,
    val menuDetailsRaw: String?,
    val includesDessert: Boolean,
    val includesDrink: Boolean,
    val daysFrom: String?,
    val daysTo: String?,
    val excludedDay: String?,
    val openTime: String?,
    val closeTime: String?,
    val phone: String?,
    val website: String?,
    val googleMapsUrl: String?,
    val googlePlaceId: String?,
    val hidden: Boolean,
    // -- Read-only / Google-enriched ------------------------------------------
    val address: String?,
    val lat: Double,
    val lng: Double,
    val rating: Double?,
    val userRatingCount: Int?,
    val editorialSummary: String?,
    val aiSummary: String?,
    val openingHours: Map<String, Any>,
    val servesLunch: Boolean,
    val servesVegetarian: Boolean,
    val outdoorSeating: Boolean,
    val reservable: Boolean,
    val takeout: Boolean,
    val reviews: List<ReviewData>,
    val placesFetchedAt: String?,
    val updatedAt: String,
    // -- Photos ---------------------------------------------------------------
    val photoNames: List<String>,
    val availablePhotoNames: List<String>,
)

data class AdminRestaurantUpdate(
    val name: String,
    val cuisineType: String?,
    val cuisineEmoji: String?,
    val menuPrice: BigDecimal?,
    val priceAlt: String?,
    val menuDetailsRaw: String?,
    val includesDessert: Boolean = false,
    val includesDrink: Boolean = false,
    val daysFrom: String?,
    val daysTo: String?,
    val excludedDay: String?,
    val openTime: String?,
    val closeTime: String?,
    val phone: String?,
    val website: String?,
    val googleMapsUrl: String?,
    val googlePlaceId: String?,
    val hidden: Boolean = false,
)

data class AdminRestaurantCreate(
    val name: String,
    val googlePlaceId: String?,
)

data class UpdatePhotosRequest(
    val photoNames: List<String>,
)
