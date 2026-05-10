package com.menudeldia.places

import com.menudeldia.config.AppProperties
import com.menudeldia.photo.PhotoStorageService
import com.menudeldia.restaurant.Restaurant
import com.menudeldia.restaurant.RestaurantRepository
import org.springframework.stereotype.Service
import java.time.Instant

/**
 * Refreshes restaurant rows from Google Places when their cached data is older than the configured TTL.
 *
 * Strategy (per ARCHITECTURE.md §6):
 *   - Per request, refresh up to props.google.placesRefreshBatchSize stale rows.
 *   - Use a Caffeine in-process map keyed by restaurantId with 60s TTL to dedupe concurrent refreshes.
 *   - On Google failure (429, 5xx, circuit open): log WARN and serve stale.
 *
 * TODO B2.2.1 / B2.2.2 / B2.2.3.
 */
@Service
class PlacesEnrichmentService(
    private val client: GooglePlacesClient,
    private val photos: PhotoStorageService,
    private val repo: RestaurantRepository,
    private val props: AppProperties,
) {

    /** Refreshes any stale rows in the supplied list (capped by props.placesRefreshBatchSize). */
    fun refreshIfStale(rows: List<Restaurant>) {
        // TODO: filter rows where placesFetchedAt is null or older than now - props.placesCacheTtl.
        //       For up to N stalest, in parallel (semaphore), call client + photos, save row.
        TODO("Phase 2 — task B2.2.1")
    }

    private fun isStale(row: Restaurant, now: Instant = Instant.now()): Boolean =
        row.placesFetchedAt == null ||
                row.placesFetchedAt!! < now.minus(props.google.placesCacheTtl)
}
