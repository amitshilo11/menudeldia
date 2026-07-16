package com.menudeldia.places

import com.github.benmanes.caffeine.cache.Caffeine
import com.menudeldia.config.AppProperties
import com.menudeldia.places.dto.PlaceDetailsResponse
import com.menudeldia.restaurant.Restaurant
import com.menudeldia.restaurant.RestaurantRepository
import com.menudeldia.restaurant.ReviewData
import com.menudeldia.restaurant.parseMenuIncludes
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

    data class EnrichmentStats(
        val attempted: Int,
        val succeeded: Int,
        val failed: Int,
        val failureReason: String? = null,
    )

    /** Fetches all stale rows (up to [limit]) and enriches them. Returns stats including failures. */
    fun enrichAllStale(limit: Int = 50): EnrichmentStats {
        val resolved = findMissingPlaceIds(limit)
        if (resolved > 0) log.info("Resolved {} missing place IDs before enrichment", resolved)
        val cutoff = Instant.now().minus(props.google.placesCacheTtl)
        val rows = repo.findStale(cutoff, PageRequest.of(0, limit))
        var succeeded = 0
        var failed = 0
        var lastFailReason: String? = null
        rows.forEach { row ->
            val err = refresh(row)
            if (err == null) succeeded++ else {
                failed++; lastFailReason = err
            }
        }
        return EnrichmentStats(
            attempted = rows.size,
            succeeded = succeeded,
            failed = failed,
            failureReason = lastFailReason
        )
    }

    /**
     * Synchronous enrichment of a single restaurant.
     * Returns null on success, or an error message on failure.
     */
    fun refresh(row: Restaurant): String? {
        val placeId = row.googlePlaceId ?: run {
            val msg = "No google_place_id set"
            log.warn("Skipping enrichment for {} ({}) — {}", row.name, row.id, msg)
            return msg
        }
        inFlight.put(row.id, true)
        return try {
            val details = client.placeDetails(placeId)
            applyDetails(row, details)
            val allNames = details.photos.map { it.name }
            row.availablePhotoNames = allNames
            // Google's photo resource names go stale, so photoNames must be re-derived from
            // the freshly fetched allNames on every refresh rather than reusing old strings.
            val curated = row.curatedPhotoIndices.mapNotNull { allNames.getOrNull(it) }
            row.photoNames = curated.ifEmpty { allNames.take(5) }
            row.placesFetchedAt = Instant.now()
            row.updatedAt = Instant.now()
            repo.save(row)
            log.info("Enriched restaurant {} ({})", row.name, row.id)
            val ratings = row.userRatingCount ?: 0
            if (ratings > 0 && allNames.isEmpty())
                log.warn(
                    "PLACES_CONTENT_MISSING photos — {} ratings but Google returned 0 photos for {} ({}). Check API key entitlements in Google Cloud Console.",
                    ratings,
                    row.name,
                    row.id
                )
            if (ratings > 0 && details.reviews.isEmpty())
                log.warn(
                    "PLACES_CONTENT_MISSING reviews — {} ratings but Google returned 0 reviews for {} ({}). Check API key entitlements in Google Cloud Console.",
                    ratings,
                    row.name,
                    row.id
                )
            if (ratings > 0 && details.editorialSummary == null && details.generativeSummary == null)
                log.warn(
                    "PLACES_CONTENT_MISSING summaries — {} ratings but Google returned no editorial/AI summary for {} ({}). Check API key entitlements in Google Cloud Console.",
                    ratings,
                    row.name,
                    row.id
                )
            null
        } catch (ex: PlacesException) {
            val rootCause = generateSequence(ex as Throwable) { it.cause }.last()
            val msg = rootCause.message ?: ex.message ?: "Unknown Places error"
            log.warn(
                "Enrichment failed for {} ({}): {} | root: {}",
                row.name,
                row.id,
                ex.message,
                msg
            )
            msg
        }
    }

    private fun applyDetails(row: Restaurant, details: PlaceDetailsResponse) {
        // Admin-editable CSV fields: fill-when-empty only, so manual edits in the admin
        // portal are never silently overwritten by a refresh.
        if (row.lat == PLACEHOLDER_LAT && row.lng == PLACEHOLDER_LNG) {
            details.location?.let {
                row.lat = it.latitude
                row.lng = it.longitude
                row.hidden = false
            }
        }
        if (row.priceIncludesEn.isEmpty()) row.priceIncludesEn =
            parseMenuIncludes(row.menuDetailsRaw)
        if (row.address.isNullOrBlank()) details.formattedAddress?.let { row.address = it }
        if (row.phone.isNullOrBlank()) details.internationalPhoneNumber?.let { row.phone = it }
        if (row.website.isNullOrBlank()) details.websiteUri?.let { row.website = it }
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

    companion object {
        const val PLACEHOLDER_LAT = 0.0
        const val PLACEHOLDER_LNG = 0.0
    }
}
