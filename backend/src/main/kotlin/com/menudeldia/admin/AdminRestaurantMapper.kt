package com.menudeldia.admin

import com.menudeldia.restaurant.Restaurant
import java.time.Instant

internal fun Restaurant.toAdminDto() = AdminRestaurantDto(
    id = id.toString(),
    name = name,
    cuisineType = cuisineType,
    cuisineEmoji = cuisineEmoji,
    menuPrice = menuPrice,
    menuDetailsRaw = menuDetailsRaw,
    vegetarianOptions = vegetarianOptions,
    glutenFreeOptions = glutenFreeOptions,
    daysFrom = daysFrom,
    daysTo = daysTo,
    excludedDay = excludedDay,
    openTime = openTime,
    closeTime = closeTime,
    phone = phone,
    website = website,
    googleMapsUrl = googleMapsUrl,
    googlePlaceId = googlePlaceId,
    hidden = hidden,
    address = address,
    lat = lat,
    lng = lng,
    rating = rating,
    userRatingCount = userRatingCount,
    editorialSummary = editorialSummary,
    aiSummary = aiSummary,
    openingHours = openingHours,
    servesLunch = servesLunch,
    servesVegetarian = servesVegetarian,
    outdoorSeating = outdoorSeating,
    reservable = reservable,
    takeout = takeout,
    reviews = reviews,
    placesFetchedAt = placesFetchedAt?.toString(),
    updatedAt = updatedAt.toString(),
    photoNames = photoNames,
    availablePhotoNames = availablePhotoNames,
)

internal fun Restaurant.applyAdminUpdate(body: AdminRestaurantUpdate) {
    name = body.name.trim().ifEmpty { name }
    cuisineType = body.cuisineType?.blankToNull()
    cuisineEmoji = body.cuisineEmoji?.blankToNull()
    menuPrice = body.menuPrice
    menuDetailsRaw = body.menuDetailsRaw?.blankToNull()
    vegetarianOptions = body.vegetarianOptions
    glutenFreeOptions = body.glutenFreeOptions
    daysFrom = body.daysFrom?.blankToNull()
    daysTo = body.daysTo?.blankToNull()
    excludedDay = body.excludedDay?.blankToNull()
    openTime = body.openTime?.blankToNull()
    closeTime = body.closeTime?.blankToNull()
    phone = body.phone?.blankToNull()
    website = body.website?.blankToNull()
    googleMapsUrl = body.googleMapsUrl?.blankToNull()
    googlePlaceId = body.googlePlaceId?.blankToNull()
    hidden = body.hidden
    updatedAt = Instant.now()
}

/** Tuple of every CSV-mapped field — equality means CSV doesn't need to be rewritten. */
internal fun Restaurant.csvSignature() = listOf(
    name, cuisineType, menuPrice, menuDetailsRaw,
    vegetarianOptions, glutenFreeOptions, daysFrom, daysTo, excludedDay,
    openTime, closeTime, phone, website, googleMapsUrl, googlePlaceId,
)

private fun String.blankToNull(): String? = trim().ifEmpty { null }
