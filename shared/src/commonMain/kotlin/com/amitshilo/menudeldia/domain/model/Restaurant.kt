package com.amitshilo.menudeldia.domain.model

data class Restaurant(
    val id: String,
    val name: String,
    val lat: Double,
    val lng: Double,
    val address: String,
    val phone: String?,
    val thumbnailUrl: String?,
    val photos: List<String>,
    val descriptionEs: String?,
    val descriptionEn: String?,
    val openingHours: List<OpeningHours>,
    val menuPrice: Double?,
    val currency: String,
    val todayHasMenu: Boolean,
    val cuisineEmoji: String? = null,
    val cuisineType: String? = null,
    val distanceMeters: Double? = null,
)
