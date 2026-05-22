package com.menudeldia.common

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/** Liveness probe. Spring Actuator's /actuator/health is more thorough but admin-scoped. */
@RestController
@RequestMapping("${ApiPaths.V1}/health")
class HealthController {

    @GetMapping
    fun health(): String = "OK"
}
