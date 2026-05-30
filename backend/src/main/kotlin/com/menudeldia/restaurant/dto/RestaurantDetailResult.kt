package com.menudeldia.restaurant.dto

/**
 * Internal result of a single-restaurant lookup. The `etag` is a quoted strong validator built from
 * `places_fetched_at` + `updated_at`, so the value changes whenever Places enrichment refreshes the
 * row or an admin edits it.
 */
data class RestaurantDetailResult(
    val dto: RestaurantDto,
    val etag: String,
)
