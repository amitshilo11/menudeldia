package com.menudeldia.restaurant

import com.menudeldia.places.PlacesEnrichmentService
import com.menudeldia.restaurant.dto.RestaurantDto
import com.menudeldia.restaurant.dto.RestaurantQuery
import com.menudeldia.restaurant.dto.RestaurantSummaryDto
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.util.UUID

@Service
class RestaurantService(
    private val repo: RestaurantRepository,
    private val mapper: RestaurantMapper,
    private val enrichment: PlacesEnrichmentService,
) {

    fun findNearby(query: RestaurantQuery): List<RestaurantSummaryDto> {
        var rows = repo.findNearby(query.lat, query.lng, query.radius)

        if (!query.q.isNullOrBlank()) {
            val q = query.q.lowercase()
            rows = rows.filter {
                it.name.lowercase().contains(q) ||
                        it.cuisineType?.lowercase()?.contains(q) == true
            }
        }
        if (query.cuisine.isNotEmpty()) {
            rows = rows.filter { it.cuisineType in query.cuisine }
        }
        if (query.minPrice != null) {
            val min = BigDecimal.valueOf(query.minPrice)
            rows = rows.filter { r -> r.menuPrice == null || r.menuPrice!! >= min }
        }
        if (query.maxPrice != null) {
            val max = BigDecimal.valueOf(query.maxPrice)
            rows = rows.filter { r -> r.menuPrice == null || r.menuPrice!! <= max }
        }

        enrichment.refreshIfStale(rows)

        val dtos = rows.map { mapper.toSummaryDto(it, query.lat, query.lng) }

        return if (query.openNow) dtos.filter { it.isOpenNow } else dtos
    }

    fun byId(id: UUID): RestaurantDto {
        val entity =
            repo.findById(id).orElseThrow { NoSuchElementException("Restaurant not found: $id") }
        enrichment.refreshIfStale(listOf(entity))
        return mapper.toDto(entity)
    }
}
