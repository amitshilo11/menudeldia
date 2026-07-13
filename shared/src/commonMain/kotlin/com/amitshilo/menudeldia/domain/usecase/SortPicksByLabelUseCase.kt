package com.amitshilo.menudeldia.domain.usecase

import com.amitshilo.menudeldia.domain.model.Restaurant

/**
 * Takes the pool of picks (any order) and returns them sorted into the fixed
 * label slots the UI expects: [0] Best Rated · [1] Best Price · [2] Closest.
 */
class SortPicksByLabelUseCase {

    operator fun invoke(picks: List<Restaurant>): List<Restaurant> {
        if (picks.isEmpty()) return emptyList()

        val remaining = picks.toMutableList()
        val result = mutableListOf<Restaurant>()

        remaining.maxByOrNull { it.rating ?: -1.0 }
            ?.also { remaining.remove(it); result.add(it) }

        remaining.minByOrNull { it.menuPrice ?: Double.MAX_VALUE }
            ?.also { remaining.remove(it); result.add(it) }

        remaining.firstOrNull()
            ?.also { result.add(it) }

        return result
    }
}
