package com.menudeldia.restaurant.dto

import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Min

data class RestaurantQuery(
    @field:DecimalMin("-90.0") @field:DecimalMax("90.0")
    val lat: Double,

    @field:DecimalMin("-180.0") @field:DecimalMax("180.0")
    val lng: Double,

    @field:Min(1)
    val radius: Int = 2_000,

    val q: String? = null,
    val openNow: Boolean = false,
    val cuisine: List<String> = emptyList(),
    val minPrice: Double? = null,
    val maxPrice: Double? = null,
)
