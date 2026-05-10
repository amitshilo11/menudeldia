package com.menudeldia.seed

import com.menudeldia.restaurant.RestaurantRepository
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

/**
 * On boot, if `restaurants` is empty, load seed.json and persist all rows.
 * Lat/lng are filled in later by the first PlacesEnrichmentService call.
 *
 * TODO B1.5.5: implement — read classpath:seed.json via Jackson, map SeedRecord -> Restaurant, saveAll.
 */
@Component
class SeederService(
    private val repo: RestaurantRepository,
) {

    @EventListener(ApplicationReadyEvent::class)
    fun seedIfEmpty() {
        if (repo.count() > 0) return
        // TODO: load classpath:seed.json, build entities, repo.saveAll(...).
        //       Use placeholder lat/lng (Barcelona center) until first enrichment.
    }
}
