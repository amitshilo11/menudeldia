package com.menudeldia.restaurant

import com.menudeldia.places.PlacesEnrichmentService
import com.menudeldia.restaurant.dto.RestaurantDto
import com.menudeldia.restaurant.dto.RestaurantQuery
import org.springframework.stereotype.Service
import java.util.UUID

/**
 * Orchestrates repo reads, optional Places enrichment, and DTO mapping.
 *
 * TODO B1.4.4: implement findNearby — call repo, then enrich stale rows, then map.
 * TODO B2.2.3: wire enrichment.
 */
@Service
class RestaurantService(
    private val repo: RestaurantRepository,
    private val mapper: RestaurantMapper,
    private val enrichment: PlacesEnrichmentService,
) {

    fun findNearby(query: RestaurantQuery): List<RestaurantDto> {
        // TODO: repo.findNearby(...) -> filter by q/openNow/cuisine/price -> enrichment.refreshIfStale -> map.
        TODO("Phase 1 — task B1.4.4")
    }

    fun byId(id: UUID): RestaurantDto {
        // TODO: repo.findById -> enrichment.refreshIfStale(listOf(it)) -> mapper.toDto.
        TODO("Phase 1 — task B1.4.4")
    }
}
