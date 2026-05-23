package com.menudeldia.places.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class TextSearchResponse(
    val places: List<PlaceResult> = emptyList(),
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class PlaceResult(
        val id: String,
        val displayName: DisplayName? = null,
        val formattedAddress: String? = null,
    )

    data class DisplayName(val text: String)
}
