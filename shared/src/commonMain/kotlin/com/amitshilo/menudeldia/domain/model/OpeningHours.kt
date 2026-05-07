package com.amitshilo.menudeldia.domain.model

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime

data class OpeningHours(
    val dayOfWeek: DayOfWeek,
    val openTime: LocalTime,
    val closeTime: LocalTime,
    val isClosed: Boolean = false,
)
