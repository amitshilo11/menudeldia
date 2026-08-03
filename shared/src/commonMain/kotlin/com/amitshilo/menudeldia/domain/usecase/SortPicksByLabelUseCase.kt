package com.amitshilo.menudeldia.domain.usecase

import com.amitshilo.menudeldia.domain.model.Restaurant

/**
 * Takes the pool of candidates (any order) and fills the fixed label slots the UI
 * expects: [0] Best Rated · [1] Best Price · [2] Closest.
 *
 * Feed this *more* candidates than there are slots. With exactly three the last slot
 * is whatever survives the first two picks, so the "Closest" badge ends up on a
 * restaurant that merely wasn't the best rated or the cheapest.
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

        remaining.minByOrNull { it.distanceMeters ?: Double.MAX_VALUE }
            ?.also { remaining.remove(it); result.add(it) }

        return result
    }
}
