package com.menudeldia.seed

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.menudeldia.restaurant.Restaurant
import com.menudeldia.restaurant.RestaurantRepository
import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class SeederService(
    private val repo: RestaurantRepository,
    private val objectMapper: ObjectMapper,
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @EventListener(ApplicationReadyEvent::class)
    fun seedIfEmpty() {
        if (repo.count() > 0) return

        val stream = javaClass.getResourceAsStream("/seed.json") ?: run {
            log.warn("seed.json not found on classpath — skipping seed")
            return
        }
        val records: List<SeedRecord> = stream.use {
            objectMapper.readValue(it, object : TypeReference<List<SeedRecord>>() {})
        }

        val entities = records.map { it.toRestaurant() }
        repo.saveAll(entities)
        log.info("Seeded ${entities.size} restaurants from seed.json")
    }

    private fun SeedRecord.toRestaurant() = Restaurant(
        name = name,
        lat = lat ?: 0.0,
        lng = lng ?: 0.0,
        hidden = lat == null || lng == null,
        googlePlaceId = googlePlaceId,
        phone = phone,
        website = website,
        menuPrice = menuPrice,
        cuisineType = cuisineType,
        cuisineEmoji = cuisineEmoji,
        priceIncludesEs = priceIncludesEs,
        priceIncludesEn = priceIncludesEn,
        weekdayHours = weekdayHours,
        vegetarianOptions = vegetarianOptions,
        glutenFreeOptions = glutenFreeOptions,
    )
}
