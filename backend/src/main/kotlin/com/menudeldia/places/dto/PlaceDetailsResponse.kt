package com.menudeldia.places.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

/**
 * Subset of Google Places (New) Place Details response that we actually consume.
 * TODO B2.1.1: confirm field names against API docs; add localized text fields if needed.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class PlaceDetailsResponse(
    val id: String,
    val displayName: LocalizedText? = null,
    val formattedAddress: String? = null,
    val location: Location? = null,
    val internationalPhoneNumber: String? = null,
    val websiteUri: String? = null,
    val regularOpeningHours: OpeningHours? = null,
    val photos: List<Photo> = emptyList(),
    val rating: Double? = null,
    val userRatingCount: Int? = null,
    val editorialSummary: LocalizedText? = null,
    val generativeSummary: GenerativeSummary? = null,
    val reviews: List<Review> = emptyList(),
    val servesLunch: Boolean? = null,
    val servesVegetarianFood: Boolean? = null,
    val outdoorSeating: Boolean? = null,
    val reservable: Boolean? = null,
    val takeout: Boolean? = null,
) {
    data class LocalizedText(val text: String, val languageCode: String? = null)
    data class Location(val latitude: Double, val longitude: Double)
    data class OpeningHours(
        val periods: List<Map<String, Any>> = emptyList(),
        val weekdayDescriptions: List<String> = emptyList()
    )

    /** `name` looks like "places/{placeId}/photos/{photoId}". Use it for the Photos API call. */
    data class Photo(val name: String, val widthPx: Int, val heightPx: Int)

    data class GenerativeSummary(val overview: LocalizedText? = null)

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Review(
        val rating: Int? = null,
        val text: LocalizedText? = null,
        val originalText: LocalizedText? = null,
        val authorAttribution: AuthorAttribution? = null,
        val publishTime: String? = null,
        val relativePublishTimeDescription: String? = null,
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class AuthorAttribution(
        val displayName: String? = null,
        val photoUri: String? = null,
    )
}
