package com.amitshilo.menudeldia.domain.usecase

import com.amitshilo.menudeldia.domain.model.Restaurant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RecommendRestaurantsUseCaseTest {

    private val useCase = RecommendRestaurantsUseCase()
    private val fixedSeed = 20260624L

    private fun restaurant(
        id: String,
        rating: Double? = null,
        userRatingCount: Int? = null,
        distanceMeters: Double? = null,
        menuPrice: Double? = null,
        menuIncludes: List<String> = emptyList(),
        todayHasMenu: Boolean = true,
    ) = Restaurant(
        id = id,
        name = "Restaurant $id",
        lat = 41.3851,
        lng = 2.1734,
        address = "",
        phone = null,
        thumbnailUrl = null,
        photos = emptyList(),
        descriptionEs = null,
        descriptionEn = null,
        openingHours = emptyList(),
        menuPrice = menuPrice,
        currency = "EUR",
        todayHasMenu = todayHasMenu,
        cuisineEmoji = null,
        cuisineType = null,
        distanceMeters = distanceMeters,
        rating = rating,
        userRatingCount = userRatingCount,
        menuIncludes = menuIncludes,
    )

    @Test
    fun `returns empty list when input is empty`() {
        val result = useCase(emptyList(), seed = fixedSeed)
        assertEquals(emptyList(), result)
    }

    @Test
    fun `returns at most count items`() {
        val restaurants = (1..20).map { restaurant(it.toString(), rating = 4.0, distanceMeters = 500.0) }
        val result = useCase(restaurants, count = 3, seed = fixedSeed)
        assertEquals(3, result.size)
    }

    @Test
    fun `returns fewer than count when input is smaller`() {
        val restaurants = listOf(
            restaurant("1", rating = 4.0),
            restaurant("2", rating = 3.5),
        )
        val result = useCase(restaurants, count = 3, seed = fixedSeed)
        assertTrue(result.size <= 2)
    }

    @Test
    fun `same seed produces same picks`() {
        val restaurants = (1..15).map {
            restaurant(it.toString(), rating = 3.0 + it * 0.1, distanceMeters = it * 100.0)
        }
        val first = useCase(restaurants, count = 3, seed = fixedSeed)
        val second = useCase(restaurants, count = 3, seed = fixedSeed)
        assertEquals(first.map { it.id }, second.map { it.id })
    }

    @Test
    fun `different seeds can produce different picks`() {
        val restaurants = (1..20).map {
            restaurant(it.toString(), rating = 3.0 + it * 0.05, distanceMeters = it * 80.0)
        }
        val results = (1L..10L).map { day -> useCase(restaurants, count = 3, seed = day).map { it.id } }
        val unique = results.toSet()
        assertTrue(unique.size > 1, "Expected different picks across seeds but all were the same")
    }

    @Test
    fun `prefers restaurants with todayHasMenu when enough exist`() {
        val noMenu = (1..5).map { restaurant(it.toString(), todayHasMenu = false, rating = 5.0) }
        val withMenu = (6..10).map { restaurant(it.toString(), todayHasMenu = true, rating = 3.0) }
        val result = useCase(noMenu + withMenu, count = 3, seed = fixedSeed)
        assertTrue(result.all { it.todayHasMenu }, "Expected all picks to have menu today")
    }

    @Test
    fun `falls back to all restaurants when too few have menu today`() {
        val withMenu = listOf(restaurant("1", todayHasMenu = true))
        val noMenu = (2..10).map { restaurant(it.toString(), todayHasMenu = false) }
        val result = useCase(withMenu + noMenu, count = 3, seed = fixedSeed)
        assertEquals(3, result.size)
    }

    @Test
    fun `no duplicates in result`() {
        val restaurants = (1..10).map { restaurant(it.toString(), rating = 4.0, distanceMeters = 300.0) }
        val result = useCase(restaurants, count = 3, seed = fixedSeed)
        assertEquals(result.size, result.map { it.id }.distinct().size)
    }

    @Test
    fun `highly rated nearby restaurant appears more often across seeds`() {
        val topRestaurant = restaurant(
            id = "top",
            rating = 5.0,
            userRatingCount = 500,
            distanceMeters = 100.0,
            menuPrice = 10.0,
            menuIncludes = listOf("Starter", "Main", "Dessert", "Drink"),
        )
        val others = (1..15).map {
            restaurant(it.toString(), rating = 2.0, distanceMeters = 3000.0)
        }
        val appearances = (1L..50L).count { seed ->
            useCase(listOf(topRestaurant) + others, count = 3, seed = seed)
                .any { it.id == "top" }
        }
        assertTrue(appearances >= 35, "Top restaurant appeared only $appearances/50 times, expected ≥35")
    }
}
