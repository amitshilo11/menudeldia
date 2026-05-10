package com.menudeldia.photo

import com.menudeldia.common.ApiPaths
import org.springframework.core.io.Resource
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

/**
 * Streams cached Google Photos through our domain.
 *
 * TODO B2.3.2: implement file streaming with `Cache-Control: public, max-age=86400, immutable`
 *              and ETag = `${places_fetched_at}:${n}`.
 */
@RestController
@RequestMapping("${ApiPaths.V1}/restaurants")
class PhotoController(
    private val storage: PhotoStorageService,
) {

    @GetMapping("/{id}/photos/{n}")
    fun get(@PathVariable id: UUID, @PathVariable n: Int): ResponseEntity<Resource> {
        // TODO: resolve path, 404 if missing, stream FileSystemResource with cache headers.
        TODO("Phase 2 — task B2.3.2")
    }
}
