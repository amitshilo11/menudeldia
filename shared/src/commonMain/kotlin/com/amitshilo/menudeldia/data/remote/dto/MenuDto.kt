package com.amitshilo.menudeldia.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class MenuDto(
    val id: String,
    val restaurantId: String,
    val date: String,
    val price: Double,
    val currency: String = "EUR",
    val firsts: List<String> = emptyList(),
    val seconds: List<String> = emptyList(),
    val desserts: List<String> = emptyList(),
    val notes: String? = null,
)
