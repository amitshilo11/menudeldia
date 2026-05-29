package com.amitshilo.menudeldia.data.mapper

import com.amitshilo.menudeldia.data.remote.apiBaseUrl
import com.amitshilo.menudeldia.data.remote.dto.MenuDto
import com.amitshilo.menudeldia.data.remote.dto.OpeningHoursDto
import com.amitshilo.menudeldia.data.remote.dto.RestaurantDto
import com.amitshilo.menudeldia.domain.model.Dish
import com.amitshilo.menudeldia.domain.model.Menu
import com.amitshilo.menudeldia.domain.model.OpeningHours
import com.amitshilo.menudeldia.domain.model.Restaurant
import com.amitshilo.menudeldia.domain.model.Review
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
    thumbnailUrl = thumbnailUrl?.let { if (it.startsWith("/")) "$apiBaseUrl$it" else it },
    photos = photos.map { if (it.startsWith("/")) "$apiBaseUrl$it" else it },
    descriptionEs = descriptionEs,
    descriptionEn = descriptionEn,
    openingHours = openingHours.map { it.toDomain() },
    menuPrice = menuPrice,
    currency = currency,
    todayHasMenu = todayHasMenu,
    cuisineEmoji = cuisineEmoji,
    cuisineType = cuisineType,
    rating = rating,
    userRatingCount = userRatingCount,
    editorialSummary = editorialSummary,
    aiSummary = aiSummary,
    reviews = reviews.map { r ->
        Review(
            authorName = r.authorName,
            authorPhotoUri = r.authorPhotoUri,
            rating = r.rating,
            text = r.text,
            originalText = r.originalText,
            relativeTime = r.relativeTime,
        )
    },
    servesLunch = servesLunch,
    servesVegetarianFood = servesVegetarianFood,
    servesGlutenFree = servesGlutenFree,
    outdoorSeating = outdoorSeating,
    reservable = reservable,
    takeout = takeout,
    includesDessert = includesDessert,
    includesDrink = includesDrink,
    menuIncludes = priceIncludesEn,
)

fun OpeningHoursDto.toDomain(): OpeningHours = OpeningHours(
    dayOfWeek = DayOfWeek(dayOfWeek),
    openTime = if (isClosed || openTime.isEmpty()) LocalTime(0, 0) else LocalTime.parse(openTime),
    closeTime = if (isClosed || closeTime.isEmpty()) LocalTime(
        0,
        0
    ) else LocalTime.parse(closeTime),
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
