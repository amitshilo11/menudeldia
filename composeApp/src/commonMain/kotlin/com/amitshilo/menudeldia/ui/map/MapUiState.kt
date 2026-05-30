package com.amitshilo.menudeldia.ui.map

import com.amitshilo.menudeldia.domain.model.Restaurant
import com.amitshilo.menudeldia.domain.model.SearchFilterState

sealed class MapUiState {
    data object Loading : MapUiState()
    data class Success(
        val restaurants: List<Restaurant>,
        val allRestaurants: List<Restaurant>,
        val selectedRestaurant: Restaurant? = null,
        val filterState: SearchFilterState = SearchFilterState(),
        val isLoading: Boolean = false,
    ) : MapUiState()
    data class Error(val message: String) : MapUiState()
}
