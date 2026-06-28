package com.amitshilo.menudeldia.domain.usecase

import com.amitshilo.menudeldia.domain.model.Restaurant
import kotlin.math.exp
import kotlin.math.ln
import kotlin.random.Random
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

private const val WEIGHT_RATING = 0.40
private const val WEIGHT_POPULARITY = 0.20
private const val WEIGHT_DISTANCE = 0.20
private const val WEIGHT_PRICE = 0.10
private const val WEIGHT_DISHES = 0.10
private const val POOL_SIZE = 12
private const val POPULARITY_CAP = 500.0
private const val DISTANCE_DECAY_METERS = 1500.0
private const val PRICE_MIN = 9.0
private const val PRICE_MAX = 20.0
private const val MISSING_SIGNAL_PENALTY = 0.05

class RecommendRestaurantsUseCase {

    operator fun invoke(
        restaurants: List<Restaurant>,
        count: Int = 3,
        seed: Long = currentEpochDay(),
    ): List<Restaurant> {
        if (restaurants.isEmpty()) return emptyList()

        val candidates = restaurants.filter { it.todayHasMenu }.takeIf { it.size >= count }
            ?: restaurants

        val scored = candidates.map { it to score(it) }
        val pool = scored.sortedByDescending { it.second }.take(POOL_SIZE)

        return weightedSample(pool, count, seed)
    }

    private fun score(r: Restaurant): Double {
        var s = 0.0
        var penalty = 0.0

        val rating = r.rating
        if (rating != null) {
            s += WEIGHT_RATING * (rating / 5.0)
        } else {
            penalty += MISSING_SIGNAL_PENALTY
        }

        val reviewCount = r.userRatingCount
        if (reviewCount != null && reviewCount > 0) {
            s += WEIGHT_POPULARITY * (ln(1.0 + reviewCount) / ln(1.0 + POPULARITY_CAP))
        }

        val distMeters = r.distanceMeters
        if (distMeters != null) {
            s += WEIGHT_DISTANCE * exp(-distMeters / DISTANCE_DECAY_METERS)
        }

        val price = r.menuPrice
        if (price != null) {
            val normalized = 1.0 - ((price - PRICE_MIN) / (PRICE_MAX - PRICE_MIN)).coerceIn(0.0, 1.0)
            s += WEIGHT_PRICE * normalized
        }

        val dishCount = r.menuIncludes.size
        s += WEIGHT_DISHES * (dishCount / 4.0).coerceAtMost(1.0)

        return (s - penalty).coerceAtLeast(0.0)
    }

    private fun weightedSample(
        pool: List<Pair<Restaurant, Double>>,
        count: Int,
        seed: Long,
    ): List<Restaurant> {
        val rng = Random(seed)
        val remaining = pool.map { (r, w) -> r to w * w }.toMutableList()
        val result = mutableListOf<Restaurant>()

        repeat(minOf(count, remaining.size)) {
            val totalWeight = remaining.sumOf { it.second }
            if (totalWeight <= 0.0) {
                result.add(remaining.removeAt(0).first)
                return@repeat
            }
            var pick = rng.nextDouble() * totalWeight
            val idx = remaining.indexOfFirst { (_, w) ->
                pick -= w
                pick <= 0.0
            }.takeIf { it >= 0 } ?: (remaining.size - 1)
            result.add(remaining.removeAt(idx).first)
        }

        return result
    }
}

@OptIn(ExperimentalTime::class)
internal fun currentEpochDay(): Long =
    Clock.System.now().toEpochMilliseconds() / 86_400_000L
