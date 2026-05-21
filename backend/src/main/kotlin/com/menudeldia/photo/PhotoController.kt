package com.menudeldia.photo

import com.menudeldia.common.ApiPaths
import org.springframework.core.io.FileSystemResource
import org.springframework.core.io.Resource
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.nio.file.Files
import java.util.UUID

@RestController
@RequestMapping("${ApiPaths.V1}/restaurants")
class PhotoController(private val storage: PhotoStorageService) {

    @GetMapping("/{id}/photos/{n}")
    fun get(@PathVariable id: UUID, @PathVariable n: Int): ResponseEntity<Resource> {
        val path = storage.pathFor(id, n)
        if (!Files.exists(path)) return ResponseEntity.status(HttpStatus.NOT_FOUND).build()
        val etag = "\"${Files.getLastModifiedTime(path).toMillis().toString(16)}-$n\""
        return ResponseEntity.ok()
            .contentType(MediaType.IMAGE_JPEG)
            .header(HttpHeaders.CACHE_CONTROL, "public, max-age=86400, immutable")
            .eTag(etag)
            .body(FileSystemResource(path))
    }
}
