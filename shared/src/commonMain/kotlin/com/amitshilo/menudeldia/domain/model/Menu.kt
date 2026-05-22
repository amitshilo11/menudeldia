package com.amitshilo.menudeldia.domain.model

import kotlinx.datetime.LocalDate

data class Menu(
    val id: String,
    val restaurantId: String,
    val date: LocalDate,
    val price: Double,
    val currency: String,
    val firsts: List<Dish>,
    val seconds: List<Dish>,
    val desserts: List<Dish>,
    val notes: String? = null,
)

data class Dish(
    val name: String,
)
