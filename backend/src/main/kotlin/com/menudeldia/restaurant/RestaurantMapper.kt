package com.menudeldia.restaurant

import com.menudeldia.restaurant.dto.RestaurantDto
import org.springframework.stereotype.Component

/**
 * Entity -> DTO mapping. Computes derived fields (`distanceMeters`, `isOpenNow`).
 *
 * TODO B1.4.5: map all fields; build photo URLs as `/api/v1/restaurants/{id}/photos/{n}` for n in 0..photoCount-1.
 * TODO B1.4.5: compute distance via PostGIS in the SELECT (preferred) or via Haversine here.
 * TODO B1.4.5: compute isOpenNow against `weekdayHours` in Europe/Madrid timezone.
 */
@Component
class RestaurantMapper {

    fun toDto(
        entity: Restaurant,
        originLat: Double? = null,
        originLng: Double? = null
    ): RestaurantDto {
        TODO("Phase 1 — task B1.4.5")
    }
}
