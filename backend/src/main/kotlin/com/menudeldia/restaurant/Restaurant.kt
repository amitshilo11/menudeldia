package com.menudeldia.restaurant

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import org.locationtech.jts.geom.Point
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

@JsonIgnoreProperties(ignoreUnknown = true)
data class ReviewData(
    val authorName: String? = null,
    val authorPhotoUri: String? = null,
    val rating: Int? = null,
    val text: String? = null,
    val originalText: String? = null,
    val relativeTime: String? = null,
)

/**
 * Curated restaurant + cached Google Places enrichment.
 * Wire shape lives in dto/RestaurantDto.kt — never expose this entity directly.
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

    /** Generated PostGIS geography column — Postgres computes from lat/lng; never write from JPA. */
    @Column(name = "geom", insertable = false, updatable = false)
    var geom: Point? = null,

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

    /** Original `menu_details` text from CSV — e.g. "Starter + Main + Dessert + Drink". */
    @Column(name = "menu_details_raw", columnDefinition = "TEXT")
    var menuDetailsRaw: String? = null,

    @Column(name = "vegetarian_options", nullable = false)
    var vegetarianOptions: Boolean = false,

    @Column(name = "gluten_free_options", nullable = false)
    var glutenFreeOptions: Boolean = false,

    @Column(name = "days_from", length = 8)
    var daysFrom: String? = null,

    @Column(name = "days_to", length = 8)
    var daysTo: String? = null,

    @Column(name = "excluded_day", length = 8)
    var excludedDay: String? = null,

    @Column(name = "open_time", length = 5)
    var openTime: String? = null,

    @Column(name = "close_time", length = 5)
    var closeTime: String? = null,

    @Column(name = "google_maps_url", length = 512)
    var googleMapsUrl: String? = null,

    /** Admin-toggled visibility flag. Hidden rows are excluded from the public feed. */
    @Column(nullable = false)
    var hidden: Boolean = false,

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

    var rating: Double? = null,

    @Column(name = "user_rating_count")
    var userRatingCount: Int? = null,

    @Column(name = "editorial_summary", columnDefinition = "TEXT")
    var editorialSummary: String? = null,

    @Column(name = "ai_summary", columnDefinition = "TEXT")
    var aiSummary: String? = null,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "reviews", columnDefinition = "jsonb")
    var reviews: List<ReviewData> = emptyList(),

    @Column(name = "serves_lunch", nullable = false)
    var servesLunch: Boolean = false,

    @Column(name = "serves_vegetarian", nullable = false)
    var servesVegetarian: Boolean = false,

    @Column(name = "outdoor_seating", nullable = false)
    var outdoorSeating: Boolean = false,

    @Column(nullable = false)
    var reservable: Boolean = false,

    @Column(nullable = false)
    var takeout: Boolean = false,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "photo_names", columnDefinition = "jsonb")
    var photoNames: List<String> = emptyList(),

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "available_photo_names", columnDefinition = "jsonb")
    var availablePhotoNames: List<String> = emptyList(),

    // Positions within availablePhotoNames that were curated by an admin. Photo resource
    // strings go stale and are re-fetched on every enrichment, so curation is tracked by
    // position rather than by the literal name — see PlacesEnrichmentService.refresh().
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "curated_photo_indices", columnDefinition = "jsonb")
    var curatedPhotoIndices: List<Int> = emptyList(),

    @Column(name = "places_fetched_at")
    var placesFetchedAt: Instant? = null,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant = Instant.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now(),
)
