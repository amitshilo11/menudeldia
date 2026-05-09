package com.amitshilo.menudeldia.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amitshilo.menudeldia.di.AppGraphProvider
import com.amitshilo.menudeldia.domain.model.Restaurant
import com.amitshilo.menudeldia.domain.model.SearchFilterState
import com.amitshilo.menudeldia.domain.usecase.FilterRestaurantsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MapViewModel : ViewModel() {

    private val useCase = AppGraphProvider.appGraph.getNearbyRestaurantsUseCase
    private val filterUseCase = FilterRestaurantsUseCase()

    private val _allRestaurants = MutableStateFlow<List<Restaurant>>(emptyList())
    private val _selectedRestaurant = MutableStateFlow<Restaurant?>(null)
    private val _filterState = MutableStateFlow(SearchFilterState())
    private val _loadError = MutableStateFlow<String?>(null)
    private val _isLoading = MutableStateFlow(true)

    val uiState: StateFlow<MapUiState> = combine(
        _isLoading,
        _loadError,
        _allRestaurants,
        _selectedRestaurant,
        _filterState,
    ) { isLoading, error, all, selected, filter ->
        when {
            isLoading -> MapUiState.Loading
            error != null -> MapUiState.Error(error)
            else -> MapUiState.Success(
                restaurants = filterUseCase(all, filter),
                allRestaurants = all,
                selectedRestaurant = selected,
                filterState = filter,
            )
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, MapUiState.Loading)

    init {
        loadRestaurants()
    }

    private fun loadRestaurants() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _allRestaurants.value = useCase(lat = 41.3851, lng = 2.1734, radiusMeters = 5000)
                _loadError.value = null
            } catch (e: Exception) {
                _loadError.value = e.message ?: "Failed to load restaurants"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun selectRestaurant(id: String) {
        _selectedRestaurant.value = _allRestaurants.value.find { it.id == id }
    }

    fun clearSelection() {
        _selectedRestaurant.value = null
    }

    fun onSearchQueryChange(query: String) {
        _filterState.value = _filterState.value.copy(query = query)
    }

    fun onFilterChange(state: SearchFilterState) {
        _filterState.value = state
    }

    fun clearFilters() {
        _filterState.value = SearchFilterState()
    }
}
