package com.amitshilo.menudeldia.data.mapper

import com.amitshilo.menudeldia.data.remote.dto.MenuDto
import com.amitshilo.menudeldia.data.remote.dto.OpeningHoursDto
import com.amitshilo.menudeldia.data.remote.dto.RestaurantDto
import com.amitshilo.menudeldia.domain.model.Dish
import com.amitshilo.menudeldia.domain.model.Menu
import com.amitshilo.menudeldia.domain.model.OpeningHours
import com.amitshilo.menudeldia.domain.model.Restaurant
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

fun RestaurantDto.toDomain(): Restaurant = Restaurant(
    id = id,
    name = name,
    lat = lat,
    lng = lng,
    address = address,
    phone = phone,
    thumbnailUrl = thumbnailUrl,
    photos = photos,
    descriptionEs = descriptionEs,
    descriptionEn = descriptionEn,
    openingHours = openingHours.map { it.toDomain() },
    menuPrice = menuPrice,
    currency = currency,
    todayHasMenu = todayHasMenu,
    cuisineEmoji = cuisineEmoji,
    cuisineType = cuisineType,
    rating = rating,
)

fun OpeningHoursDto.toDomain(): OpeningHours = OpeningHours(
    dayOfWeek = DayOfWeek(dayOfWeek),
    openTime = LocalTime.parse(openTime),
    closeTime = LocalTime.parse(closeTime),
    isClosed = isClosed,
)

fun MenuDto.toDomain(): Menu = Menu(
    id = id,
    restaurantId = restaurantId,
    date = LocalDate.parse(date),
    price = price,
    currency = currency,
    firsts = firsts.map { Dish(it) },
    seconds = seconds.map { Dish(it) },
    desserts = desserts.map { Dish(it) },
    notes = notes,
)
