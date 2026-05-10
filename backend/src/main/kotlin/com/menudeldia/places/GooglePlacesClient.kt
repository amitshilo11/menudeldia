package com.menudeldia.places

import com.menudeldia.config.AppProperties
import com.menudeldia.places.dto.PlaceDetailsResponse
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

/**
 * Thin wrapper around Google Places API (New).
 * Endpoints:
 *   - GET https://places.googleapis.com/v1/places/{placeId}        (Place Details)
 *   - GET https://places.googleapis.com/v1/{photoName}/media       (Place Photos)
 *
 * Headers: X-Goog-Api-Key, X-Goog-FieldMask.
 *
 * TODO B2.1.1 / B2.1.2: implement placeDetails + photo download with field mask.
 * TODO B2.1.3: wrap with @CircuitBreaker via Resilience4j; map 4xx/5xx to typed exceptions.
 */
@Component
class GooglePlacesClient(
    private val props: AppProperties,
) {
    private val http: RestClient = RestClient.builder()
        .baseUrl("https://places.googleapis.com/v1")
        .build()

    fun placeDetails(placeId: String): PlaceDetailsResponse {
        // TODO: GET /places/{placeId} with field mask
        //   "id,location,photos,regularOpeningHours,formattedAddress,internationalPhoneNumber,websiteUri,displayName"
        TODO("Phase 2 — task B2.1.1")
    }

    /** Returns raw bytes of the photo media. Caller is responsible for persistence. */
    fun photoBytes(photoName: String, maxHeightPx: Int = 800): ByteArray {
        // TODO: GET /{photoName}/media?maxHeightPx={...}
        TODO("Phase 2 — task B2.1.1")
    }
}
