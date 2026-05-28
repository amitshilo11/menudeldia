package com.menudeldia.admin

import com.menudeldia.common.ApiPaths
import com.menudeldia.places.GooglePlacesClient
import com.menudeldia.places.PlacesException
import com.menudeldia.restaurant.RestaurantRepository
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

@RestController
@RequestMapping("${ApiPaths.V1}/admin/restaurants")
class AdminPhotosController(
    private val repo: RestaurantRepository,
    private val client: GooglePlacesClient,
) {
    private val log = LoggerFactory.getLogger(javaClass)

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
        return ResponseEntity.ok(restaurant.toAdminDto())
    }
}
