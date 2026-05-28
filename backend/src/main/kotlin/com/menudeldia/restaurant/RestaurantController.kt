package com.menudeldia.restaurant

import com.menudeldia.common.ApiPaths
import com.menudeldia.restaurant.dto.RestaurantDto
import com.menudeldia.restaurant.dto.RestaurantListResponse
import com.menudeldia.restaurant.dto.RestaurantQuery
import jakarta.validation.Valid
import org.springframework.http.CacheControl
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.context.request.WebRequest
import java.time.Duration
import java.util.UUID

@RestController
@RequestMapping("${ApiPaths.V1}/restaurants")
class RestaurantController(
    private val service: RestaurantService,
) {

    @GetMapping
    fun list(@Valid @ModelAttribute query: RestaurantQuery): RestaurantListResponse =
        RestaurantListResponse(service.findNearby(query))

    @GetMapping("/{id}")
    fun byId(@PathVariable id: UUID, request: WebRequest): ResponseEntity<RestaurantDto> {
        val result = service.byId(id)
        if (request.checkNotModified(result.etag)) {
            return ResponseEntity.status(304).eTag(result.etag).build()
        }
        return ResponseEntity.ok()
            .eTag(result.etag)
            .cacheControl(
                CacheControl.maxAge(Duration.ofSeconds(60)).mustRevalidate().cachePrivate()
            )
            .body(result.dto)
    }
}
