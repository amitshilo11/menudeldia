package com.amitshilo.menudeldia.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amitshilo.menudeldia.di.AppGraphProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MapViewModel : ViewModel() {

    private val useCase = AppGraphProvider.appGraph.getNearbyRestaurantsUseCase

    private val _uiState = MutableStateFlow<MapUiState>(MapUiState.Loading)
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    init {
        loadRestaurants()
    }

    private fun loadRestaurants() {
        viewModelScope.launch {
            _uiState.value = try {
                val restaurants = useCase(lat = 41.3851, lng = 2.1734, radiusMeters = 5000)
                MapUiState.Success(restaurants = restaurants)
            } catch (e: Exception) {
                MapUiState.Error(e.message ?: "Failed to load restaurants")
            }
        }
    }

    fun selectRestaurant(id: String) {
        val current = _uiState.value as? MapUiState.Success ?: return
        _uiState.value = current.copy(selectedRestaurant = current.restaurants.find { it.id == id })
    }

    fun clearSelection() {
        val current = _uiState.value as? MapUiState.Success ?: return
        _uiState.value = current.copy(selectedRestaurant = null)
    }
}
