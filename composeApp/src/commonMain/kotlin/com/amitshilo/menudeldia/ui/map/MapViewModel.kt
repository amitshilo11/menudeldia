package com.amitshilo.menudeldia.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amitshilo.menudeldia.di.AppGraphProvider
import com.amitshilo.menudeldia.domain.model.Restaurant
import com.amitshilo.menudeldia.domain.model.SearchFilterState
import com.amitshilo.menudeldia.domain.usecase.FilterRestaurantsUseCase
import com.amitshilo.menudeldia.domain.usecase.RecommendRestaurantsUseCase
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
import kotlin.math.abs

private const val BARCELONA_CENTER_LAT = 41.3851
private const val BARCELONA_CENTER_LNG = 2.1734
private const val INITIAL_SEARCH_RADIUS_METERS = 3000.0
private const val MAX_SEARCH_RADIUS_METERS = 10_000.0
private const val MIN_SEARCH_RADIUS_METERS = 50.0
private const val MAP_IDLE_DEBOUNCE_MS = 500L
private const val MOVE_THRESHOLD_FRACTION = 0.2
private const val RADIUS_THRESHOLD_FRACTION = 0.15

class MapViewModel : ViewModel() {

    private val useCase = AppGraphProvider.appGraph.getNearbyRestaurantsUseCase
    private val filterUseCase = FilterRestaurantsUseCase()
    private val recommendUseCase = RecommendRestaurantsUseCase()

    private val _allRestaurants = MutableStateFlow<List<Restaurant>>(emptyList())
    private val _selectedRestaurant = MutableStateFlow<Restaurant?>(null)
    private val _filterState = MutableStateFlow(SearchFilterState())
    private val _loadError = MutableStateFlow<String?>(null)
    private val _isLoading = MutableStateFlow(true)
    private val _userLocation = MutableStateFlow<UserLocation?>(null)

    private val _bestPicks = MutableStateFlow<List<Restaurant>>(emptyList())
    val bestPicks: StateFlow<List<Restaurant>> = _bestPicks

    private val _showBestPicks = MutableStateFlow(true)
    val showBestPicks: StateFlow<Boolean> = _showBestPicks

    private val _effects = Channel<MapEffect>(Channel.BUFFERED)
    val effects: Flow<MapEffect> = _effects.receiveAsFlow()

    private var searchLat = BARCELONA_CENTER_LAT
    private var searchLng = BARCELONA_CENTER_LNG
    private var searchRadius = INITIAL_SEARCH_RADIUS_METERS
    private var mapIdleJob: Job? = null

    val uiState: StateFlow<MapUiState> = combine(
        _isLoading,
        _loadError,
        _allRestaurants,
        _selectedRestaurant,
        _filterState,
    ) { loading, error, all, selected, filter ->
        when {
            // Only block the whole screen with an error if we have nothing to show.
            error != null && all.isEmpty() -> MapUiState.Error(error)
            // Otherwise render the map immediately — markers/list fill in as data
            // arrives, and `isLoading` drives a lightweight inline indicator.
            else -> MapUiState.Success(
                restaurants = filterUseCase(all, filter),
                allRestaurants = all,
                selectedRestaurant = selected,
                filterState = filter,
                isLoading = loading,
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

    fun dismissBestPicks() {
        _showBestPicks.value = false
    }

    private fun updateLocation(location: UserLocation?) {
        if (location == _userLocation.value) return
        val hadLocation = _userLocation.value != null
        _userLocation.value = location
        if (location != null && !hadLocation) {
            refreshDistancesForLocation(location)
        }
    }

    private fun refreshDistancesForLocation(location: UserLocation) {
        val current = _allRestaurants.value
        if (current.isEmpty()) return
        val refreshed = current
            .map {
                it.copy(
                    distanceMeters = haversineMeters(
                        location.lat,
                        location.lng,
                        it.lat,
                        it.lng
                    )
                )
            }
            .sortedBy { it.distanceMeters }
        _allRestaurants.value = refreshed
        _bestPicks.value = recommendUseCase(refreshed)
        println("[MapViewModel] fallback refresh: ${refreshed.size} restaurants re-sorted from real location")
    }

    private fun onMapIdle(lat: Double, lng: Double, radiusMeters: Double) {
        val clamped = radiusMeters.coerceIn(MIN_SEARCH_RADIUS_METERS, MAX_SEARCH_RADIUS_METERS)
        val movedMeters = haversineMeters(searchLat, searchLng, lat, lng)
        val radiusChange = abs(clamped - searchRadius) / searchRadius
        if (movedMeters < searchRadius * MOVE_THRESHOLD_FRACTION && radiusChange < RADIUS_THRESHOLD_FRACTION) return
        searchLat = lat
        searchLng = lng
        searchRadius = clamped
        mapIdleJob?.cancel()
        mapIdleJob = viewModelScope.launch {
            delay(MAP_IDLE_DEBOUNCE_MS)
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
                val raw =
                    useCase(lat = searchLat, lng = searchLng, radiusMeters = searchRadius.toInt())
                // Read location AFTER the API call so we get the real location if it arrived
                // during the network round-trip (avoids a race on cold start).
                val loc = _userLocation.value
                val userLat = loc?.lat ?: searchLat
                val userLng = loc?.lng ?: searchLng
                val sorted = raw
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
                _allRestaurants.value = sorted
                // Refresh picks whenever real location is known; fall back to once on first load.
                val shouldRefreshPicks = sorted.isNotEmpty() &&
                        (_userLocation.value != null || _bestPicks.value.isEmpty())
                if (shouldRefreshPicks) {
                    _bestPicks.value = recommendUseCase(sorted)
                }
                _loadError.value = null
            } catch (e: Exception) {
                _loadError.value = e.message ?: "Failed to load restaurants"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
