package com.menudeldia.restaurant

import com.menudeldia.common.ApiPaths
import com.menudeldia.restaurant.dto.RestaurantDto
import com.menudeldia.restaurant.dto.RestaurantListResponse
import com.menudeldia.restaurant.dto.RestaurantQuery
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
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
    fun byId(@PathVariable id: UUID): RestaurantDto =
        service.byId(id)
}
