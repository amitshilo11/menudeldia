package com.menudeldia.places

import com.menudeldia.config.AppProperties
import com.menudeldia.places.dto.PhotoMediaResponse
import com.menudeldia.places.dto.PlaceDetailsResponse
import com.menudeldia.places.dto.TextSearchResponse
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.RestClient

@Component
class GooglePlacesClient(private val props: AppProperties) {

    private val log = LoggerFactory.getLogger(javaClass)

    private val http: RestClient = RestClient.builder()
        .baseUrl("https://places.googleapis.com/v1")
        .build()

    companion object {
        private const val FIELD_MASK =
            "id,location,photos,regularOpeningHours,formattedAddress,internationalPhoneNumber,websiteUri," +
                    "displayName,rating,userRatingCount,editorialSummary,generativeSummary,reviews," +
                    "servesLunch,servesVegetarianFood,outdoorSeating,reservable,takeout"
    }

    @CircuitBreaker(name = "googlePlaces", fallbackMethod = "placeDetailsFallback")
    fun placeDetails(placeId: String): PlaceDetailsResponse {
        val apiKey = props.google.placesApiKey
        if (apiKey.isBlank()) {
            log.error("GOOGLE_PLACES_API_KEY is empty! Enrichment will fail.")
            throw PlacesException.ApiError("API Key is missing")
        }
        return try {
            http.get()
                .uri("/places/{id}", placeId)
                .header("X-Goog-Api-Key", apiKey)
                .header("X-Goog-FieldMask", FIELD_MASK)
                .retrieve()
                .body(PlaceDetailsResponse::class.java)
                ?: throw PlacesException.ApiError("Empty response for place $placeId")
        } catch (ex: HttpClientErrorException) {
            log.warn(
                "Google Places 4xx for place {}: {} — {}",
                placeId,
                ex.statusCode,
                ex.responseBodyAsString
            )
            throw PlacesException.ApiError("Client error ${ex.statusCode} for place $placeId", ex)
        } catch (ex: HttpServerErrorException) {
            log.warn(
                "Google Places 5xx for place {}: {} — {}",
                placeId,
                ex.statusCode,
                ex.responseBodyAsString
            )
            throw PlacesException.ApiError("Server error ${ex.statusCode} for place $placeId", ex)
        }
    }

    @CircuitBreaker(name = "googlePlaces", fallbackMethod = "photoBytesFallback")
    fun photoBytes(photoName: String, maxHeightPx: Int): ByteArray {
        return try {
            val mediaResponse = http.get()
                .uri("/$photoName/media?maxHeightPx=$maxHeightPx&skipHttpRedirect=true")
                .header("X-Goog-Api-Key", props.google.placesApiKey)
                .retrieve()
                .body(PhotoMediaResponse::class.java)
                ?: throw PlacesException.ApiError("Empty photo metadata response for $photoName")

            val bytes = RestClient.create().get()
                .uri(mediaResponse.photoUri)
                .retrieve()
                .body(ByteArray::class.java)
                ?: throw PlacesException.ApiError("Empty photo bytes response for $photoName")

            if (bytes.size < 100) {
                log.warn(
                    "Photo {} download is suspiciously small ({} bytes): {}",
                    photoName,
                    bytes.size,
                    String(bytes.take(100).toByteArray())
                )
            }
            bytes
        } catch (ex: HttpClientErrorException) {
            log.warn("Google Places 4xx for photo {}: {}", photoName, ex.statusCode)
            throw PlacesException.ApiError("Client error ${ex.statusCode} for photo $photoName", ex)
        } catch (ex: HttpServerErrorException) {
            log.warn("Google Places 5xx for photo {}: {}", photoName, ex.statusCode)
            throw PlacesException.ApiError("Server error ${ex.statusCode} for photo $photoName", ex)
        }
    }

    @CircuitBreaker(name = "googlePlaces", fallbackMethod = "searchTextFallback")
    fun searchText(query: String): TextSearchResponse {
        return try {
            http.post()
                .uri("/places:searchText")
                .header("X-Goog-Api-Key", props.google.placesApiKey)
                .header("X-Goog-FieldMask", "places.id,places.displayName,places.formattedAddress")
                .contentType(MediaType.APPLICATION_JSON)
                .body(mapOf("textQuery" to query))
                .retrieve()
                .body(TextSearchResponse::class.java)
                ?: TextSearchResponse()
        } catch (ex: HttpClientErrorException) {
            log.warn("Google Places 4xx for text search '{}': {}", query, ex.statusCode)
            throw PlacesException.ApiError("Client error ${ex.statusCode} for query '$query'", ex)
        } catch (ex: HttpServerErrorException) {
            log.warn("Google Places 5xx for text search '{}': {}", query, ex.statusCode)
            throw PlacesException.ApiError("Server error ${ex.statusCode} for query '$query'", ex)
        }
    }

    @Suppress("UNUSED_PARAMETER")
    private fun placeDetailsFallback(placeId: String, ex: Throwable): PlaceDetailsResponse =
        throw PlacesException.Unavailable("Google Places unavailable for place $placeId", ex)

    @Suppress("UNUSED_PARAMETER")
    private fun photoBytesFallback(photoName: String, maxHeightPx: Int, ex: Throwable): ByteArray =
        throw PlacesException.Unavailable("Google Places unavailable for photo $photoName", ex)

    @Suppress("UNUSED_PARAMETER")
    private fun searchTextFallback(query: String, ex: Throwable): TextSearchResponse =
        throw PlacesException.Unavailable("Google Places unavailable for text search '$query'", ex)
}
