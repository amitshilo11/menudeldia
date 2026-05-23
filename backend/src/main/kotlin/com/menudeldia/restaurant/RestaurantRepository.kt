package com.menudeldia.restaurant

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.UUID

/**
 * Restaurant data access.
 * TODO B1.4.3: implement findNearby using PostGIS ST_DWithin via @Query nativeQuery=true.
 *              Filters: q (ILIKE on name/cuisine), openNow (computed in service), cuisine, price range.
 *              Order by ST_Distance ASC.
 */
@Repository
interface RestaurantRepository : JpaRepository<Restaurant, UUID> {

    @Query(
        value = """
            SELECT r.*
            FROM restaurants r
            WHERE ST_DWithin(
                r.geom,
                ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geography,
                :radiusMeters
            )
            ORDER BY r.geom <-> ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geography
            LIMIT 200
        """,
        nativeQuery = true,
    )
    fun findNearby(
        @Param("lat") lat: Double,
        @Param("lng") lng: Double,
        @Param("radiusMeters") radiusMeters: Int,
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
