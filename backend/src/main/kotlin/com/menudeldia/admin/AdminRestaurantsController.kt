package com.menudeldia.admin

import com.menudeldia.common.ApiPaths
import com.menudeldia.places.GooglePlacesClient
import com.menudeldia.places.PlacesEnrichmentService
import com.menudeldia.places.PlacesEnrichmentService.Companion.BARCELONA_PLACEHOLDER_LAT
import com.menudeldia.places.PlacesEnrichmentService.Companion.BARCELONA_PLACEHOLDER_LNG
import com.menudeldia.places.PlacesException
import com.menudeldia.restaurant.Restaurant
import com.menudeldia.restaurant.RestaurantRepository
import com.menudeldia.restaurant.ReviewData
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class AdminRestaurantDto(
    val id: String,
    // -- Editable / CSV-mapped (and `hidden`, which is DB-only) ---------------
    val name: String,
    val cuisineType: String?,
    val cuisineEmoji: String?,
    val menuPrice: BigDecimal?,
    val priceAlt: String?,
    val menuDetailsRaw: String?,
    val includesDessert: Boolean,
    val includesDrink: Boolean,
    val daysFrom: String?,
    val daysTo: String?,
    val excludedDay: String?,
    val openTime: String?,
    val closeTime: String?,
    val phone: String?,
    val website: String?,
    val googleMapsUrl: String?,
    val googlePlaceId: String?,
    val hidden: Boolean,
    // -- Read-only / Google-enriched ------------------------------------------
    val address: String?,
    val lat: Double,
    val lng: Double,
    val rating: Double?,
    val userRatingCount: Int?,
    val editorialSummary: String?,
    val aiSummary: String?,
    val openingHours: Map<String, Any>,
    val servesLunch: Boolean,
    val servesVegetarian: Boolean,
    val outdoorSeating: Boolean,
    val reservable: Boolean,
    val takeout: Boolean,
    val reviews: List<ReviewData>,
    val placesFetchedAt: String?,
    // -- Photos (unchanged from previous portal) ------------------------------
    val photoNames: List<String>,
    val availablePhotoNames: List<String>,
)

data class AdminRestaurantUpdate(
    val name: String,
    val cuisineType: String?,
    val cuisineEmoji: String?,
    val menuPrice: BigDecimal?,
    val priceAlt: String?,
    val menuDetailsRaw: String?,
    val includesDessert: Boolean = false,
    val includesDrink: Boolean = false,
    val daysFrom: String?,
    val daysTo: String?,
    val excludedDay: String?,
    val openTime: String?,
    val closeTime: String?,
    val phone: String?,
    val website: String?,
    val googleMapsUrl: String?,
    val googlePlaceId: String?,
    val hidden: Boolean = false,
)

data class AdminRestaurantCreate(
    val name: String,
    val googlePlaceId: String?,
)

data class UpdatePhotosRequest(
    val photoNames: List<String>,
)

@RestController
@RequestMapping("${ApiPaths.V1}/admin/restaurants")
class AdminRestaurantsController(
    private val repo: RestaurantRepository,
    private val client: GooglePlacesClient,
    private val csv: CsvFileService,
    private val enrichment: PlacesEnrichmentService,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    // ---------------------------------------------------------------- queries

    @GetMapping
    fun list(): List<AdminRestaurantDto> =
        repo.findAll().sortedBy { it.name }.map { it.toDto() }

    @GetMapping("/{id}")
    fun byId(@PathVariable id: UUID): AdminRestaurantDto =
        (repo.findById(id).orElse(null) ?: throw notFound(id)).toDto()

    // ---------------------------------------------------------------- create

    @PostMapping
    fun create(@RequestBody body: AdminRestaurantCreate): AdminRestaurantDto {
        val name = body.name.trim()
        require(name.isNotEmpty()) { "name required" }
        val newRow = Restaurant(
            name = name,
            lat = BARCELONA_PLACEHOLDER_LAT,
            lng = BARCELONA_PLACEHOLDER_LNG,
            googlePlaceId = body.googlePlaceId?.trim()?.ifEmpty { null },
        )
        val saved = repo.save(newRow)
        log.info("Admin created restaurant {} ({})", saved.name, saved.id)
        csv.writeAll(repo.findAll())
        enrichInBackground(saved.id)
        return saved.toDto()
    }

    // ---------------------------------------------------------------- update

    @PutMapping("/{id}")
    fun update(
        @PathVariable id: UUID,
        @RequestBody body: AdminRestaurantUpdate,
    ): AdminRestaurantDto {
        val row = repo.findById(id).orElse(null) ?: throw notFound(id)
        val before = row.csvSignature()
        row.applyUpdate(body)
        val saved = repo.save(row)
        val csvChanged = saved.csvSignature() != before
        if (csvChanged) csv.writeAll(repo.findAll())
        log.info(
            "Admin updated restaurant {} ({}); csvChanged={}",
            saved.name,
            saved.id,
            csvChanged
        )
        enrichInBackground(saved.id)
        return saved.toDto()
    }

    // ---------------------------------------------------------------- delete

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: UUID): ResponseEntity<Void> {
        val row = repo.findById(id).orElse(null) ?: throw notFound(id)
        val name = row.name
        repo.delete(row)
        csv.writeAll(repo.findAll())
        log.info("Admin deleted restaurant {} ({})", name, id)
        return ResponseEntity.noContent().build()
    }

    // ---------------------------------------------------------------- photos

    @GetMapping("/{id}/available-photos/{n}")
    fun availablePhoto(@PathVariable id: UUID, @PathVariable n: Int): ResponseEntity<ByteArray> {
        val restaurant = repo.findById(id).orElse(null) ?: return ResponseEntity.notFound().build()
        val name = restaurant.availablePhotoNames.getOrNull(n)
            ?: return ResponseEntity.notFound().build()
        return try {
            val bytes = client.photoBytes(name, 800)
            val etag = "\"${name.hashCode().toString(16)}\""
            ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .header(HttpHeaders.CACHE_CONTROL, "public, max-age=86400, immutable")
                .eTag(etag)
                .body(bytes)
        } catch (ex: PlacesException) {
            log.warn("Available photo proxy failed for {} idx {}: {}", id, n, ex.message)
            ResponseEntity.status(HttpStatus.BAD_GATEWAY).build()
        }
    }

    @PostMapping("/{id}/photos")
    fun updatePhotos(
        @PathVariable id: UUID,
        @RequestBody body: UpdatePhotosRequest,
    ): ResponseEntity<AdminRestaurantDto> {
        val restaurant = repo.findById(id).orElse(null) ?: return ResponseEntity.notFound().build()
        val available = restaurant.availablePhotoNames.toSet()
        val invalid = body.photoNames.filter { it !in available }
        if (invalid.isNotEmpty()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown photo names: $invalid")
        }
        restaurant.photoNames = body.photoNames
        repo.save(restaurant)
        log.info(
            "Updated photo curation for {} ({}): {} photos",
            restaurant.name,
            id,
            body.photoNames.size
        )
        return ResponseEntity.ok(restaurant.toDto())
    }

    // ---------------------------------------------------------------- helpers

    private fun notFound(id: UUID): ResponseStatusException =
        ResponseStatusException(HttpStatus.NOT_FOUND, "Restaurant not found: $id")

    /**
     * Fire-and-forget enrichment refresh on a daemon thread. Photo curation is
     * preserved by [PlacesEnrichmentService.refresh] (line 84-88). Errors are
     * logged, not surfaced to the caller — the save itself already succeeded.
     */
    private fun enrichInBackground(id: UUID) {
        Thread({
            try {
                repo.findById(id).ifPresent { enrichment.refresh(it) }
            } catch (ex: Exception) {
                log.warn("Background enrich failed for {}: {}", id, ex.message)
            }
        }, "admin-enrich-$id").apply { isDaemon = true }.start()
    }

    private fun Restaurant.applyUpdate(body: AdminRestaurantUpdate) {
        name = body.name.trim().ifEmpty { name }
        cuisineType = body.cuisineType?.blankToNull()
        cuisineEmoji = body.cuisineEmoji?.blankToNull()
        menuPrice = body.menuPrice
        priceAlt = body.priceAlt?.blankToNull()
        menuDetailsRaw = body.menuDetailsRaw?.blankToNull()
        includesDessert = body.includesDessert
        includesDrink = body.includesDrink
        daysFrom = body.daysFrom?.blankToNull()
        daysTo = body.daysTo?.blankToNull()
        excludedDay = body.excludedDay?.blankToNull()
        openTime = body.openTime?.blankToNull()
        closeTime = body.closeTime?.blankToNull()
        phone = body.phone?.blankToNull()
        website = body.website?.blankToNull()
        googleMapsUrl = body.googleMapsUrl?.blankToNull()
        googlePlaceId = body.googlePlaceId?.blankToNull()
        hidden = body.hidden
        updatedAt = Instant.now()
    }

    /** Tuple of every CSV-mapped field — equality means CSV doesn't need to be rewritten. */
    private fun Restaurant.csvSignature() = listOf(
        name, cuisineType, menuPrice, priceAlt, menuDetailsRaw,
        includesDessert, includesDrink, daysFrom, daysTo, excludedDay,
        openTime, closeTime, phone, website, googleMapsUrl, googlePlaceId,
    )

    private fun Restaurant.toDto() = AdminRestaurantDto(
        id = id.toString(),
        name = name,
        cuisineType = cuisineType,
        cuisineEmoji = cuisineEmoji,
        menuPrice = menuPrice,
        priceAlt = priceAlt,
        menuDetailsRaw = menuDetailsRaw,
        includesDessert = includesDessert,
        includesDrink = includesDrink,
        daysFrom = daysFrom,
        daysTo = daysTo,
        excludedDay = excludedDay,
        openTime = openTime,
        closeTime = closeTime,
        phone = phone,
        website = website,
        googleMapsUrl = googleMapsUrl,
        googlePlaceId = googlePlaceId,
        hidden = hidden,
        address = address,
        lat = lat,
        lng = lng,
        rating = rating,
        userRatingCount = userRatingCount,
        editorialSummary = editorialSummary,
        aiSummary = aiSummary,
        openingHours = openingHours,
        servesLunch = servesLunch,
        servesVegetarian = servesVegetarian,
        outdoorSeating = outdoorSeating,
        reservable = reservable,
        takeout = takeout,
        reviews = reviews,
        placesFetchedAt = placesFetchedAt?.toString(),
        photoNames = photoNames,
        availablePhotoNames = availablePhotoNames,
    )

    private fun String.blankToNull(): String? = trim().ifEmpty { null }
}
