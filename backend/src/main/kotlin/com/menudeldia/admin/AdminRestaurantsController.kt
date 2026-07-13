package com.menudeldia.admin

import com.menudeldia.common.ApiPaths
import com.menudeldia.places.PlacesEnrichmentService
import com.menudeldia.places.PlacesEnrichmentService.Companion.PLACEHOLDER_LAT
import com.menudeldia.places.PlacesEnrichmentService.Companion.PLACEHOLDER_LNG
import com.menudeldia.restaurant.Cuisine
import com.menudeldia.restaurant.Restaurant
import com.menudeldia.restaurant.RestaurantRepository
import com.menudeldia.restaurant.parseMenuIncludes
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

@RestController
@RequestMapping("${ApiPaths.V1}/admin/restaurants")
class AdminRestaurantsController(
    private val repo: RestaurantRepository,
    private val csv: CsvFileService,
    private val enrichment: PlacesEnrichmentService,
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @GetMapping
    fun list(): List<AdminRestaurantDto> =
        repo.findAll().sortedBy { it.name }.map { it.toAdminDto() }

    @GetMapping("/{id}")
    fun byId(@PathVariable id: UUID): AdminRestaurantDto =
        (repo.findById(id).orElse(null) ?: throw notFound(id)).toAdminDto()

    @PostMapping
    fun create(@RequestBody body: AdminRestaurantCreate): AdminRestaurantDto {
        val name = body.name.trim()
        require(name.isNotEmpty()) { "name required" }
        val newRow = Restaurant(
            name = name,
            lat = PLACEHOLDER_LAT,
            lng = PLACEHOLDER_LNG,
            hidden = true,
            googlePlaceId = body.googlePlaceId?.trim()?.ifEmpty { null },
        )
        val saved = repo.save(newRow)
        log.info("Admin created restaurant {} ({})", saved.name, saved.id)
        Thread({
            try {
                repo.findById(saved.id).ifPresent { enrichment.refresh(it) }
            } catch (ex: Exception) {
                log.warn("Background enrich failed for {}: {}", saved.id, ex.message)
            }
        }, "admin-enrich-${saved.id}").apply { isDaemon = true }.start()
        return saved.toAdminDto()
    }

    @PutMapping("/{id}")
    fun update(
        @PathVariable id: UUID,
        @RequestBody body: AdminRestaurantUpdate,
    ): AdminRestaurantDto {
        val row = repo.findById(id).orElse(null) ?: throw notFound(id)
        val newPlaceId = body.googlePlaceId?.trim()?.ifEmpty { null }
        if (newPlaceId != null && newPlaceId != row.googlePlaceId &&
            repo.existsByGooglePlaceIdAndIdNot(newPlaceId, id)
        ) {
            throw ResponseStatusException(
                HttpStatus.CONFLICT,
                "Google Place ID '$newPlaceId' is already assigned to another restaurant",
            )
        }
        row.applyAdminUpdate(body)
        val saved = repo.save(row)
        log.info("Admin updated restaurant {} ({})", saved.name, saved.id)
        return saved.toAdminDto()
    }

    @PostMapping("/{id}/enrich")
    fun enrich(@PathVariable id: UUID): Map<String, Any?> {
        val row = repo.findById(id).orElse(null) ?: throw notFound(id)
        val error = enrichment.refresh(row)
        return if (error == null) {
            mapOf("ok" to true, "message" to null)
        } else {
            mapOf("ok" to false, "message" to error)
        }
    }

    @PostMapping("/sync-csv", consumes = ["multipart/form-data"])
    fun syncFromCsv(@RequestParam("file") file: MultipartFile): Map<String, Any> {
        val rows = file.inputStream.bufferedReader().use { csv.parseRows(it) }
        var created = 0;
        var updated = 0;
        var skipped = 0
        rows.forEach { row ->
            try {
                val existing = when {
                    row.googlePlaceId != null -> repo.findByGooglePlaceId(row.googlePlaceId)
                    else -> repo.findByNameIgnoreCase(row.name)
                }
                if (existing != null) {
                    existing.applyCsvRow(row)
                    repo.save(existing)
                    updated++
                } else {
                    val emoji = row.cuisineType?.let { ct ->
                        runCatching { Cuisine.valueOf(ct.uppercase()).emoji }.getOrNull()
                    }
                    repo.save(
                        Restaurant(
                            name = row.name,
                            lat = PLACEHOLDER_LAT,
                            lng = PLACEHOLDER_LNG,
                            hidden = true,
                            cuisineType = row.cuisineType,
                            cuisineEmoji = emoji,
                            menuPrice = row.menuPrice,
                            menuDetailsRaw = row.menuDetailsRaw,
                            priceIncludesEn = parseMenuIncludes(row.menuDetailsRaw),
                            vegetarianOptions = row.vegetarianOptions,
                            glutenFreeOptions = row.glutenFreeOptions,
                            daysFrom = row.daysFrom,
                            daysTo = row.daysTo,
                            excludedDay = row.excludedDay,
                            openTime = row.openTime,
                            closeTime = row.closeTime,
                            phone = row.phone,
                            website = row.website,
                            googleMapsUrl = row.googleMapsUrl,
                            googlePlaceId = row.googlePlaceId,
                        )
                    )
                    created++
                }
            } catch (ex: Exception) {
                log.warn("CSV sync skipped row '{}': {}", row.name, ex.message)
                skipped++
            }
        }
        log.info("CSV sync done — created={} updated={} skipped={}", created, updated, skipped)
        return mapOf("created" to created, "updated" to updated, "skipped" to skipped)
    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: UUID): ResponseEntity<Void> {
        val row = repo.findById(id).orElse(null) ?: throw notFound(id)
        val name = row.name
        repo.delete(row)
        log.info("Admin deleted restaurant {} ({})", name, id)
        return ResponseEntity.noContent().build()
    }

    private fun notFound(id: UUID): ResponseStatusException =
        ResponseStatusException(HttpStatus.NOT_FOUND, "Restaurant not found: $id")
}
