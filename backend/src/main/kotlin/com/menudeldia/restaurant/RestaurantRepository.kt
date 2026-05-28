package com.menudeldia.restaurant

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.math.BigDecimal
import java.util.UUID

@Repository
interface RestaurantRepository : JpaRepository<Restaurant, UUID> {

    /**
     * PostGIS-backed nearby query with all set-shaping filters pushed down to SQL.
     *
     * Null/empty params disable the matching clause (`IS NULL` / `cuisineEmpty` short-circuits).
     * Rows with `menu_price IS NULL` are kept regardless of min/max — pricing data may be missing.
     * `openNow` is NOT applied here; it requires Europe/Madrid time + JSONB weekday_hours.
     */
    @Query(
        value = """
            SELECT r.*
            FROM restaurants r
            WHERE r.hidden = FALSE
              AND ST_DWithin(
                  r.geom,
                  ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geography,
                  :radiusMeters
              )
              AND (CAST(:qPattern AS TEXT) IS NULL
                   OR LOWER(r.name) LIKE :qPattern
                   OR LOWER(r.cuisine_type) LIKE :qPattern)
              AND (CAST(:minPrice AS NUMERIC) IS NULL OR r.menu_price IS NULL OR r.menu_price >= :minPrice)
              AND (CAST(:maxPrice AS NUMERIC) IS NULL OR r.menu_price IS NULL OR r.menu_price <= :maxPrice)
            ORDER BY r.geom <-> ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geography
            LIMIT 200
        """,
        nativeQuery = true,
    )
    fun findNearby(
        @Param("lat") lat: Double,
        @Param("lng") lng: Double,
        @Param("radiusMeters") radiusMeters: Int,
        @Param("qPattern") qPattern: String?,
        @Param("minPrice") minPrice: BigDecimal?,
        @Param("maxPrice") maxPrice: BigDecimal?,
    ): List<Restaurant>

    /** Finds rows whose Places enrichment is null or older than the given cutoff, oldest first. */
    @Query(
        """
            SELECT r FROM Restaurant r
            WHERE r.placesFetchedAt IS NULL OR r.placesFetchedAt < :cutoff
            ORDER BY r.placesFetchedAt ASC NULLS FIRST
        """
    )
    fun findStale(
        @Param("cutoff") cutoff: java.time.Instant,
        pageable: org.springframework.data.domain.Pageable
    ): List<Restaurant>

    @Query("SELECT r FROM Restaurant r WHERE r.googlePlaceId IS NULL")
    fun findWithoutPlaceId(): List<Restaurant>
}
