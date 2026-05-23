package com.amitshilo.menudeldia.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amitshilo.menudeldia.di.AppGraphProvider
import com.amitshilo.menudeldia.domain.model.Restaurant
import com.amitshilo.menudeldia.domain.model.SearchFilterState
import com.amitshilo.menudeldia.domain.usecase.FilterRestaurantsUseCase
import com.amitshilo.menudeldia.location.UserLocation
import com.amitshilo.menudeldia.util.haversineMeters
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val BARCELONA_CENTER_LAT = 41.3851
private const val BARCELONA_CENTER_LNG = 2.1734
private const val INITIAL_SEARCH_RADIUS_METERS = 3000.0
private const val MAX_SEARCH_RADIUS_METERS = 10_000.0
private const val MIN_SEARCH_RADIUS_METERS = 50.0

class MapViewModel : ViewModel() {

    private val useCase = AppGraphProvider.appGraph.getNearbyRestaurantsUseCase
    private val filterUseCase = FilterRestaurantsUseCase()

    private val _allRestaurants = MutableStateFlow<List<Restaurant>>(emptyList())
    private val _selectedRestaurant = MutableStateFlow<Restaurant?>(null)
    private val _filterState = MutableStateFlow(SearchFilterState())
    private val _loadError = MutableStateFlow<String?>(null)
    private val _isLoading = MutableStateFlow(true)
    private val _isFirstLoad = MutableStateFlow(true)
    private val _userLocation = MutableStateFlow<UserLocation?>(null)

    private val _effects = Channel<MapEffect>(Channel.BUFFERED)
    val effects: Flow<MapEffect> = _effects.receiveAsFlow()

    private var searchLat = BARCELONA_CENTER_LAT
    private var searchLng = BARCELONA_CENTER_LNG
    private var searchRadius = INITIAL_SEARCH_RADIUS_METERS
    private var mapIdleJob: Job? = null

    val uiState: StateFlow<MapUiState> = combine(
        combine(_isLoading, _isFirstLoad) { loading, firstLoad -> loading && firstLoad },
        _loadError,
        _allRestaurants,
        _selectedRestaurant,
        _filterState,
    ) { isInitialLoading, error, all, selected, filter ->
        when {
            isInitialLoading -> MapUiState.Loading
            error != null && all.isEmpty() -> MapUiState.Error(error)
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

    fun onEvent(event: MapEvent) {
        when (event) {
            is MapEvent.SelectRestaurant -> selectRestaurant(event.id)
            MapEvent.ClearSelection -> _selectedRestaurant.value = null
            is MapEvent.FilterChanged -> _filterState.value = event.filter
            MapEvent.ClearFilters -> _filterState.value = SearchFilterState()
            MapEvent.Refresh -> loadRestaurants()
            is MapEvent.LocationChanged -> updateLocation(event.location)
            MapEvent.RecenterRequested -> viewModelScope.launch { _effects.send(MapEffect.RecenterOnUser) }
            is MapEvent.MapIdle -> onMapIdle(event.lat, event.lng, event.radiusMeters)
        }
    }

    private fun updateLocation(location: UserLocation?) {
        if (location == _userLocation.value) return
        _userLocation.value = location
    }

    private fun onMapIdle(lat: Double, lng: Double, radiusMeters: Double) {
        val clamped = radiusMeters.coerceIn(MIN_SEARCH_RADIUS_METERS, MAX_SEARCH_RADIUS_METERS)
        if (searchLat == lat && searchLng == lng && searchRadius == clamped) return
        searchLat = lat
        searchLng = lng
        searchRadius = clamped
        mapIdleJob?.cancel()
        mapIdleJob = viewModelScope.launch {
            delay(300)
            loadRestaurants()
        }
    }

    private fun selectRestaurant(id: String) {
        _selectedRestaurant.value = _allRestaurants.value.find { it.id == id }
    }

    private fun loadRestaurants() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val loc = _userLocation.value
                val userLat = loc?.lat ?: searchLat
                val userLng = loc?.lng ?: searchLng
                val raw =
                    useCase(lat = searchLat, lng = searchLng, radiusMeters = searchRadius.toInt())
                _allRestaurants.value = raw
                    .map {
                        it.copy(
                            distanceMeters = haversineMeters(
                                userLat,
                                userLng,
                                it.lat,
                                it.lng
                            )
                        )
                    }
                    .sortedBy { it.distanceMeters }
                _loadError.value = null
            } catch (e: Exception) {
                _loadError.value = e.message ?: "Failed to load restaurants"
            } finally {
                _isLoading.value = false
                _isFirstLoad.value = false
            }
        }
    }
}
