package com.menudeldia.restaurant

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

/**
 * Curated restaurant + cached Google Places enrichment.
 * Wire shape lives in dto/RestaurantDto.kt — never expose this entity directly.
 *
 * TODO B1.4.1: add the `geom GEOGRAPHY(Point, 4326)` column. JPA can't write a generated column,
 *              so map it as `@Generated(event = INSERT, UPDATE) @Column(insertable = false, updatable = false)`
 *              with type `org.locationtech.jts.geom.Point`. Hibernate-spatial handles the mapping.
 */
@Entity
@Table(name = "restaurants")
class Restaurant(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(nullable = false)
    var name: String,

    var address: String? = null,

    @Column(nullable = false)
    var lat: Double,

    @Column(nullable = false)
    var lng: Double,

    var phone: String? = null,
    var website: String? = null,

    @Column(name = "google_place_id", unique = true)
    var googlePlaceId: String? = null,

    @Column(name = "cuisine_type")
    var cuisineType: String? = null,

    @Column(name = "cuisine_emoji")
    var cuisineEmoji: String? = null,

    @Column(name = "description_es", columnDefinition = "TEXT")
    var descriptionEs: String? = null,

    @Column(name = "description_en", columnDefinition = "TEXT")
    var descriptionEn: String? = null,

    @Column(name = "menu_price")
    var menuPrice: BigDecimal? = null,

    @Column(nullable = false)
    var currency: String = "EUR",

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "price_includes_es", columnDefinition = "jsonb")
    var priceIncludesEs: List<String> = emptyList(),

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "price_includes_en", columnDefinition = "jsonb")
    var priceIncludesEn: List<String> = emptyList(),

    /** Map of `mon`..`fri` -> "HH:MM-HH:MM". Mon–Fri only; lunch window. */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "weekday_hours", columnDefinition = "jsonb")
    var weekdayHours: Map<String, String> = emptyMap(),

    /** Full-week opening hours from Google Places (for detail screen). */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "opening_hours", columnDefinition = "jsonb")
    var openingHours: Map<String, Any> = emptyMap(),

    @Column(name = "photo_count", nullable = false)
    var photoCount: Int = 0,

    @Column(name = "places_fetched_at")
    var placesFetchedAt: Instant? = null,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant = Instant.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now(),
)
