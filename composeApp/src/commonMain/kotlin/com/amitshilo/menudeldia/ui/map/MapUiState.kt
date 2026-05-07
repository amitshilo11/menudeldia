package com.amitshilo.menudeldia.ui.map

import com.amitshilo.menudeldia.domain.model.Restaurant

sealed class MapUiState {
    data object Loading : MapUiState()
    data class Success(
        val restaurants: List<Restaurant>,
        val selectedRestaurant: Restaurant? = null,
    ) : MapUiState()
    data class Error(val message: String) : MapUiState()
}
