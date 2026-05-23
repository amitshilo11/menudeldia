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
    val userRatingCount: Int? = null,
    val editorialSummary: String? = null,
    val aiSummary: String? = null,
    val reviews: List<ReviewDto> = emptyList(),
    val servesLunch: Boolean = false,
    val servesVegetarianFood: Boolean = false,
    val outdoorSeating: Boolean = false,
    val reservable: Boolean = false,
    val takeout: Boolean = false,
    val includesDessert: Boolean = false,
    val includesDrink: Boolean = false,
    val priceIncludesEn: List<String> = emptyList(),
)

@Serializable
data class ReviewDto(
    val authorName: String? = null,
    val authorPhotoUri: String? = null,
    val rating: Int? = null,
    val text: String? = null,
    val originalText: String? = null,
    val relativeTime: String? = null,
)

@Serializable
data class OpeningHoursDto(
    val dayOfWeek: Int,
    val openTime: String,
    val closeTime: String,
    val isClosed: Boolean = false,
)
