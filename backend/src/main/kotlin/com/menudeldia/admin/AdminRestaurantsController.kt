package com.menudeldia.admin

import com.menudeldia.common.ApiPaths
import com.menudeldia.places.PlacesEnrichmentService
import com.menudeldia.places.PlacesEnrichmentService.Companion.BARCELONA_PLACEHOLDER_LAT
import com.menudeldia.places.PlacesEnrichmentService.Companion.BARCELONA_PLACEHOLDER_LNG
import com.menudeldia.restaurant.Restaurant
import com.menudeldia.restaurant.RestaurantRepository
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
import org.springframework.web.bind.annotation.RestController
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
            lat = BARCELONA_PLACEHOLDER_LAT,
            lng = BARCELONA_PLACEHOLDER_LNG,
            googlePlaceId = body.googlePlaceId?.trim()?.ifEmpty { null },
        )
        val saved = repo.save(newRow)
        log.info("Admin created restaurant {} ({})", saved.name, saved.id)
        csv.writeAll(repo.findAll())
        enrichInBackground(saved.id)
        return saved.toAdminDto()
    }

    @PutMapping("/{id}")
    fun update(
        @PathVariable id: UUID,
        @RequestBody body: AdminRestaurantUpdate,
    ): AdminRestaurantDto {
        val row = repo.findById(id).orElse(null) ?: throw notFound(id)
        val before = row.csvSignature()
        row.applyAdminUpdate(body)
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
        return saved.toAdminDto()
    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: UUID): ResponseEntity<Void> {
        val row = repo.findById(id).orElse(null) ?: throw notFound(id)
        val name = row.name
        repo.delete(row)
        csv.writeAll(repo.findAll())
        log.info("Admin deleted restaurant {} ({})", name, id)
        return ResponseEntity.noContent().build()
    }

    private fun notFound(id: UUID): ResponseStatusException =
        ResponseStatusException(HttpStatus.NOT_FOUND, "Restaurant not found: $id")

    /**
     * Fire-and-forget enrichment refresh on a daemon thread. Photo curation is preserved by
     * [PlacesEnrichmentService.refresh]. Errors are logged, not surfaced — the save itself
     * already succeeded.
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
}
