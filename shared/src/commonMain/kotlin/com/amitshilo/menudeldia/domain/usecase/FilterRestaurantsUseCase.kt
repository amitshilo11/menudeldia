package com.amitshilo.menudeldia.domain.usecase

import com.amitshilo.menudeldia.domain.model.Restaurant
import com.amitshilo.menudeldia.domain.model.SearchFilterState

class FilterRestaurantsUseCase {
    operator fun invoke(restaurants: List<Restaurant>, state: SearchFilterState): List<Restaurant> {
        var result = restaurants
        if (state.query.isNotBlank()) {
            val q = state.query.trim().lowercase()
            result = result.filter {
                it.name.lowercase().contains(q) ||
                        it.cuisineType?.lowercase()?.contains(q) == true
            }
        }
        if (state.openNowOnly) result = result.filter { it.todayHasMenu }
        state.minPrice?.let { min ->
            result = result.filter { it.menuPrice != null && it.menuPrice >= min }
        }
        state.maxPrice?.let { max ->
            result = result.filter { it.menuPrice != null && it.menuPrice <= max }
        }
        state.cuisineType?.let { ct -> result = result.filter { it.cuisineType == ct } }
        state.maxDistanceMeters?.let { d ->
            result = result.filter { it.distanceMeters != null && it.distanceMeters <= d }
        }
        return result
    }
}
