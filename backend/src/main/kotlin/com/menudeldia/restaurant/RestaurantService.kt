package com.menudeldia.restaurant

import com.menudeldia.restaurant.dto.RestaurantDetailResult
import com.menudeldia.restaurant.dto.RestaurantQuery
import com.menudeldia.restaurant.dto.RestaurantSummaryDto
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.util.UUID

@Service
class RestaurantService(
    private val repo: RestaurantRepository,
    private val mapper: RestaurantMapper,
) {

    fun findNearby(query: RestaurantQuery): List<RestaurantSummaryDto> {
        val radius = query.radius.coerceIn(50, 10_000)
        val qLower = query.q?.takeIf { it.isNotBlank() }?.lowercase()

        val rows = repo.findNearby(
            lat = query.lat,
            lng = query.lng,
            radiusMeters = radius,
            qPattern = qLower?.let { "%$it%" },
            minPrice = query.minPrice?.let(BigDecimal::valueOf),
            maxPrice = query.maxPrice?.let(BigDecimal::valueOf),
        )

        val dtos = rows.map { mapper.toSummaryDto(it, query.lat, query.lng) }
            .let { list ->
                if (query.cuisine.isNotEmpty()) list.filter { it.cuisineType in query.cuisine } else list
            }
        return if (query.openNow) dtos.filter { it.isOpenNow } else dtos
    }

    fun byId(id: UUID): RestaurantDetailResult {
        val entity =
            repo.findById(id).orElseThrow { NoSuchElementException("Restaurant not found: $id") }
        val version = (entity.placesFetchedAt ?: entity.updatedAt).toEpochMilli()
        return RestaurantDetailResult(
            dto = mapper.toDto(entity),
            etag = "\"$version-${entity.updatedAt.toEpochMilli()}\"",
        )
    }
}
