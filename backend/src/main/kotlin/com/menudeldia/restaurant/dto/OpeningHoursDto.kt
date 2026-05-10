package com.menudeldia.restaurant.dto

/** One row per weekday in the detail screen. */
data class OpeningHoursDto(
    /** 0 = Monday … 6 = Sunday (ISO 8601). */
    val dayOfWeek: Int,
    val openLocal: String,   // "HH:MM"
    val closeLocal: String,  // "HH:MM"
)
