package com.amitshilo.menudeldia.ui.preview

import com.amitshilo.menudeldia.domain.model.Dish
import com.amitshilo.menudeldia.domain.model.Menu
import com.amitshilo.menudeldia.domain.model.OpeningHours
import com.amitshilo.menudeldia.domain.model.Restaurant
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.Month

internal val previewRestaurant = Restaurant(
    id = "1",
    name = "Demasié - Rambla Catalunya 43",
    address = "Rambla Catalunya 43, Barcelona",
    lat = 41.3917,
    lng = 2.1649,
    phone = "+34 931 23 45 67",
    thumbnailUrl = "https://picsum.photos/seed/r1/400/300",
    photos = listOf(
        "https://picsum.photos/seed/r6a/800/600",
        "https://picsum.photos/seed/r6a/800/600"
    ),
    descriptionEs = "Restaurante moderno con cocina catalana de mercado.",
    descriptionEn = "Modern restaurant with Catalan market cuisine.",
    openingHours = listOf(
        OpeningHours(DayOfWeek.MONDAY, LocalTime(13, 0), LocalTime(16, 0), isClosed = false),
        OpeningHours(DayOfWeek.TUESDAY, LocalTime(13, 0), LocalTime(16, 0), isClosed = false),
        OpeningHours(DayOfWeek.WEDNESDAY, LocalTime(13, 0), LocalTime(16, 0), isClosed = false),
        OpeningHours(DayOfWeek.THURSDAY, LocalTime(13, 0), LocalTime(16, 0), isClosed = false),
        OpeningHours(DayOfWeek.FRIDAY, LocalTime(13, 0), LocalTime(16, 0), isClosed = false),
        OpeningHours(DayOfWeek.SATURDAY, LocalTime(13, 0), LocalTime(16, 0), isClosed = false),
        OpeningHours(DayOfWeek.SUNDAY, LocalTime(13, 0), LocalTime(16, 0), isClosed = true),
    ),
    menuPrice = 12.50,
    currency = "EUR",
    todayHasMenu = true,
    cuisineType = "Catalana",
    cuisineEmoji = "🥘",
    distanceMeters = 850.0,
    rating = 4.3,
)

internal val previewRestaurantNoMenu = previewRestaurant.copy(
    id = "2",
    name = "Bar El Tío",
    address = "Carrer de Provença 12, Barcelona",
    thumbnailUrl = "https://images.unsplash.com/photo-1555396273-367ea4eb4db5?auto=format&fit=crop&w=800&q=80",
    todayHasMenu = false,
    menuPrice = null,
    rating = 3.8,
    distanceMeters = 1400.0,
)

internal val previewMenu = Menu(
    id = "m1",
    restaurantId = "1",
    date = LocalDate(2026, Month.MAY, 10),
    price = 12.50,
    currency = "EUR",
    firsts = listOf(Dish("Ensalada mixta"), Dish("Gazpacho"), Dish("Croquetas")),
    seconds = listOf(Dish("Pollo al ajillo"), Dish("Merluza a la romana")),
    desserts = listOf(Dish("Crema catalana"), Dish("Fruta del tiempo")),
    notes = "Pan y bebida incluidos.",
)

internal val previewRestaurants = listOf(
    previewRestaurant,
    previewRestaurantNoMenu,
    previewRestaurant.copy(id = "3", name = "Pizzeria Napoletana", cuisineType = "Italiana"),
    previewRestaurant.copy(id = "4", name = "Sushi Bar", cuisineType = "Japonesa"),
)
