package com.menudeldia.places

import com.github.benmanes.caffeine.cache.Caffeine
import com.menudeldia.config.AppProperties
import com.menudeldia.photo.PhotoStorageService
import com.menudeldia.places.dto.PlaceDetailsResponse
import com.menudeldia.restaurant.Restaurant
import com.menudeldia.restaurant.RestaurantRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.UUID
import java.util.concurrent.TimeUnit

@Service
class PlacesEnrichmentService(
    private val client: GooglePlacesClient,
    private val photos: PhotoStorageService,
    private val repo: RestaurantRepository,
    private val props: AppProperties,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    private val inFlight = Caffeine.newBuilder()
        .expireAfterWrite(60, TimeUnit.SECONDS)
        .build<UUID, Boolean>()

    fun refreshIfStale(rows: List<Restaurant>) {
        val now = Instant.now()
        rows.filter { isStale(it, now) && inFlight.getIfPresent(it.id) == null }
            .sortedBy { it.placesFetchedAt ?: Instant.EPOCH }
            .take(props.google.placesRefreshBatchSize)
            .forEach { refresh(it) }
    }

    private fun refresh(row: Restaurant) {
        val placeId = row.googlePlaceId ?: return
        inFlight.put(row.id, true)
        try {
            val details = client.placeDetails(placeId)
            applyDetails(row, details)
            val photoNames = details.photos.take(5).map { it.name }
            row.photoCount = photos.downloadPhotos(row.id, photoNames)
            row.placesFetchedAt = Instant.now()
            repo.save(row)
            log.debug("Enriched restaurant {} ({})", row.name, row.id)
        } catch (ex: PlacesException) {
            log.warn("Enrichment failed for {} ({}): {}", row.name, row.id, ex.message)
        }
    }

    private fun applyDetails(row: Restaurant, details: PlaceDetailsResponse) {
        details.location?.let {
            row.lat = it.latitude
            row.lng = it.longitude
        }
        details.formattedAddress?.let { row.address = it }
        details.internationalPhoneNumber?.let { row.phone = it }
        details.websiteUri?.let { row.website = it }
        details.regularOpeningHours?.let { hours ->
            if (hours.weekdayDescriptions.isNotEmpty()) {
                row.openingHours = mapOf("weekdayDescriptions" to hours.weekdayDescriptions)
            }
        }
    }

    private fun isStale(row: Restaurant, now: Instant): Boolean =
        row.placesFetchedAt == null ||
                row.placesFetchedAt!! < now.minus(props.google.placesCacheTtl)
}
