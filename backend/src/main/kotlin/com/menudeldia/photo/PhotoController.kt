package com.menudeldia.photo

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
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("${ApiPaths.V1}/restaurants")
class PhotoController(
    private val repo: RestaurantRepository,
    private val client: GooglePlacesClient,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @GetMapping("/{id}/photos/{n}")
    fun get(@PathVariable id: UUID, @PathVariable n: Int): ResponseEntity<ByteArray> {
        val restaurant = repo.findById(id).orElse(null)
            ?: return ResponseEntity.notFound().build()
        val name = restaurant.photoNames.getOrNull(n)
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
            log.warn(
                "Photo proxy failed for restaurant {} index {} (name={}): {}",
                id,
                n,
                name,
                ex.message
            )
            ResponseEntity.status(HttpStatus.BAD_GATEWAY).build()
        }
    }
}
