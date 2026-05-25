package com.menudeldia.places

import com.github.benmanes.caffeine.cache.Caffeine
import com.menudeldia.config.AppProperties
import com.menudeldia.places.dto.PlaceDetailsResponse
import com.menudeldia.restaurant.Restaurant
import com.menudeldia.restaurant.RestaurantRepository
import com.menudeldia.restaurant.ReviewData
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.UUID
import java.util.concurrent.TimeUnit

@Service
class PlacesEnrichmentService(
    private val client: GooglePlacesClient,
    private val repo: RestaurantRepository,
    private val props: AppProperties,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    private val inFlight = Caffeine.newBuilder()
        .expireAfterWrite(60, TimeUnit.SECONDS)
        .build<UUID, Boolean>()

    /**
     * For every restaurant without a google_place_id, runs a text search to find and set it.
     * Returns the number of IDs successfully found.
     * After this, call [enrichAllStale] to pull photos/ratings for the newly linked restaurants.
     */
    fun findMissingPlaceIds(limit: Int = 50): Int {
        val rows = repo.findWithoutPlaceId().take(limit)
        var found = 0
        rows.forEach { row ->
            val query = "${row.name} Barcelona"
            try {
                val result = client.searchText(query)
                val placeId = result.places.firstOrNull()?.id
                if (placeId != null) {
                    row.googlePlaceId = placeId
                    repo.save(row)
                    found++
                    log.info("Found place ID {} for {} ({})", placeId, row.name, row.id)
                } else {
                    log.warn("No Google Place found for '{}' ({})", row.name, row.id)
                }
            } catch (ex: PlacesException) {
                log.warn("Text search failed for {} ({}): {}", row.name, row.id, ex.message)
            }
        }
        return found
    }

    /** Fetches all stale rows (up to [limit]) and enriches them. Returns the count processed. */
    fun enrichAllStale(limit: Int = 50): Int {
        val resolved = findMissingPlaceIds(limit)
        if (resolved > 0) log.info("Resolved {} missing place IDs before enrichment", resolved)
        val cutoff = Instant.now().minus(props.google.placesCacheTtl)
        val rows = repo.findStale(cutoff, PageRequest.of(0, limit))
        rows.forEach { refresh(it) }
        return rows.size
    }

    fun refreshIfStale(rows: List<Restaurant>) {
        val now = Instant.now()
        rows.filter { isStale(it, now) && inFlight.getIfPresent(it.id) == null }
            .sortedBy { it.placesFetchedAt ?: Instant.EPOCH }
            .take(props.google.placesRefreshBatchSize)
            .forEach { refresh(it) }
    }

    private fun refresh(row: Restaurant) {
        val placeId = row.googlePlaceId ?: run {
            log.warn("Skipping enrichment for {} ({}) — no google_place_id", row.name, row.id)
            return
        }
        inFlight.put(row.id, true)
        try {
            val details = client.placeDetails(placeId)
            applyDetails(row, details)
            row.photoNames = details.photos.take(5).map { it.name }
            row.placesFetchedAt = Instant.now()
            repo.save(row)
            log.info("Enriched restaurant {} ({})", row.name, row.id)
        } catch (ex: PlacesException) {
            val rootCause = generateSequence(ex as Throwable) { it.cause }.last()
            log.warn(
                "Enrichment failed for {} ({}): {} | root: {}",
                row.name,
                row.id,
                ex.message,
                rootCause.message
            )
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
        details.rating?.let { row.rating = it }
        details.userRatingCount?.let { row.userRatingCount = it }
        details.editorialSummary?.text?.let { row.editorialSummary = it }
        details.generativeSummary?.overview?.text?.let { row.aiSummary = it }
        if (details.reviews.isNotEmpty()) {
            row.reviews = details.reviews.map { r ->
                ReviewData(
                    authorName = r.authorAttribution?.displayName,
                    authorPhotoUri = r.authorAttribution?.photoUri,
                    rating = r.rating,
                    text = r.text?.text,
                    originalText = r.originalText?.text,
                    relativeTime = r.relativePublishTimeDescription,
                )
            }
        }
        details.servesLunch?.let { row.servesLunch = it }
        details.servesVegetarianFood?.let { row.servesVegetarian = it }
        details.outdoorSeating?.let { row.outdoorSeating = it }
        details.reservable?.let { row.reservable = it }
        details.takeout?.let { row.takeout = it }
    }

    private fun isStale(row: Restaurant, now: Instant): Boolean =
        row.placesFetchedAt == null ||
                row.placesFetchedAt!! < now.minus(props.google.placesCacheTtl)
}
