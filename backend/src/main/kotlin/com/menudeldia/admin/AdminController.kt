package com.menudeldia.admin

import com.menudeldia.common.ApiPaths
import com.menudeldia.places.PlacesEnrichmentService
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Profile("dev")
@RestController
@RequestMapping("${ApiPaths.V1}/admin")
class AdminController(
    private val enrichment: PlacesEnrichmentService,
    private val cbRegistry: CircuitBreakerRegistry,
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @PostMapping("/find-place-ids")
    fun findPlaceIds(@RequestParam(defaultValue = "50") limit: Int): Map<String, Any> {
        log.info("Admin: finding missing Google Place IDs for up to {} restaurants", limit)
        val count = enrichment.findMissingPlaceIds(limit)
        log.info("Admin: found and set {} place IDs", count)
        return mapOf("found" to count)
    }

    @PostMapping("/enrich")
    fun forceEnrich(@RequestParam(defaultValue = "50") limit: Int): Map<String, Any> {
        log.info("Admin: triggering enrichment for up to {} stale restaurants", limit)
        val count = enrichment.enrichAllStale(limit)
        log.info("Admin: enrichment triggered for {} restaurants", count)
        return mapOf("enriched" to count)
    }

    @GetMapping("/circuit-breakers")
    fun circuitBreakerStatus(): Map<String, Any> =
        cbRegistry.allCircuitBreakers.associate { cb ->
            cb.name to mapOf(
                "state" to cb.state.name,
                "failureRate" to cb.metrics.failureRate,
                "calls" to cb.metrics.numberOfBufferedCalls,
            )
        }

    @PostMapping("/circuit-breakers/reset")
    fun resetCircuitBreakers(): Map<String, Any> {
        cbRegistry.allCircuitBreakers.forEach { it.reset() }
        log.info("Admin: all circuit breakers reset")
        return mapOf("reset" to cbRegistry.allCircuitBreakers.map { it.name })
    }
}
