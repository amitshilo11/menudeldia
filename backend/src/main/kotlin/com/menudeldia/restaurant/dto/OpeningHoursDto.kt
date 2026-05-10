package com.menudeldia.restaurant.dto

/** One row per weekday in the detail screen. Matches shared module's OpeningHoursDto wire shape. */
data class OpeningHoursDto(
    /** 1 = Monday … 7 = Sunday (ISO 8601). */
    val dayOfWeek: Int,
    val openTime: String,
    val closeTime: String,
    val isClosed: Boolean = false,
)
