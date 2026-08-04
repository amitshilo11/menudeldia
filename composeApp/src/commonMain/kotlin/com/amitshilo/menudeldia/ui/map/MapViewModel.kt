package com.amitshilo.menudeldia.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amitshilo.menudeldia.di.AppGraphProvider
import com.amitshilo.menudeldia.domain.model.Restaurant
import com.amitshilo.menudeldia.domain.model.SearchFilterState
import com.amitshilo.menudeldia.domain.usecase.FilterRestaurantsUseCase
import com.amitshilo.menudeldia.domain.usecase.IsBestPicksWindowUseCase
import com.amitshilo.menudeldia.domain.usecase.RecommendRestaurantsUseCase
import com.amitshilo.menudeldia.domain.usecase.SortPicksByLabelUseCase
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
import menudeldia.composeapp.generated.resources.Res
import menudeldia.composeapp.generated.resources.error_failed_to_load_restaurants
import org.jetbrains.compose.resources.getString
import kotlin.math.abs

private const val BARCELONA_CENTER_LAT = 41.3851
private const val BARCELONA_CENTER_LNG = 2.1734
private const val INITIAL_SEARCH_RADIUS_METERS = 3000.0
private const val MAX_SEARCH_RADIUS_METERS = 10_000.0
private const val MIN_SEARCH_RADIUS_METERS = 50.0
private const val MAP_IDLE_DEBOUNCE_MS = 500L
private const val MOVE_THRESHOLD_FRACTION = 0.2
private const val RADIUS_THRESHOLD_FRACTION = 0.15
private const val LOCATION_UPDATE_THRESHOLD_METERS = 20.0

/**
 * How many candidates the recommender draws for the picks sheet. Wider than the three
 * label slots so [SortPicksByLabelUseCase] has something to actually choose from.
 */
private const val PICK_CANDIDATE_COUNT = 6

class MapViewModel : ViewModel() {

    private val useCase = AppGraphProvider.appGraph.getNearbyRestaurantsUseCase
    private val filterUseCase = FilterRestaurantsUseCase()
    private val recommendUseCase = RecommendRestaurantsUseCase()
    private val sortPicksUseCase = SortPicksByLabelUseCase()
    private val picksWindowUseCase = IsBestPicksWindowUseCase()

    private val _allRestaurants = MutableStateFlow<List<Restaurant>>(emptyList())
    private val _selectedRestaurant = MutableStateFlow<Restaurant?>(null)
    private val _filterState = MutableStateFlow(SearchFilterState())
    private val _loadError = MutableStateFlow<String?>(null)
    private val _isLoading = MutableStateFlow(true)
    private val _userLocation = MutableStateFlow<UserLocation?>(null)

    private val _bestPicks = MutableStateFlow<List<Restaurant>>(emptyList())
    val bestPicks: StateFlow<List<Restaurant>> = _bestPicks

    private val _fetchGeneration = MutableStateFlow(0)
    val fetchGeneration: StateFlow<Int> = _fetchGeneration

    private val _showBestPicks = MutableStateFlow(picksWindowUseCase())
    val showBestPicks: StateFlow<Boolean> = _showBestPicks

    private val _effects = Channel<MapEffect>(Channel.BUFFERED)
    val effects: Flow<MapEffect> = _effects.receiveAsFlow()

    private var searchLat = BARCELONA_CENTER_LAT
    private var searchLng = BARCELONA_CENTER_LNG
    private var searchRadius = INITIAL_SEARCH_RADIUS_METERS
    private var mapIdleJob: Job? = null

    /**
     * Today's pick candidates, kept around so the label slots can be re-filled when the
     * user's location changes without re-rolling the daily selection underneath them.
     */
    private var pickCandidates: List<Restaurant> = emptyList()

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
        val previous = _userLocation.value
        if (location == previous) return
        // Below-threshold moves still update the stored location (so later comparisons stay
        // anchored to the newest fix) but skip the distance recompute across every restaurant.
        if (location != null && previous != null &&
            haversineMeters(previous.lat, previous.lng, location.lat, location.lng) <
            LOCATION_UPDATE_THRESHOLD_METERS
        ) {
            _userLocation.value = location
            return
        }
        _userLocation.value = location
        refreshDistancesForLocation(location)
    }

    /**
     * Re-measures every distance against [location]. Runs on *every* fix, not just the
     * first: the first location a device hands us is typically a coarse cached one, and
     * pinning distances to it leaves the entire list — and the picks sheet — measured
     * from wherever the phone happened to be last.
     */
    private fun refreshDistancesForLocation(location: UserLocation?) {
        pickCandidates = pickCandidates.withDistancesFrom(location)
        _bestPicks.value = sortPicksUseCase(pickCandidates)
        _selectedRestaurant.value = _selectedRestaurant.value
            ?.let { listOf(it).withDistancesFrom(location).first() }
        val current = _allRestaurants.value
        if (current.isEmpty()) return
        _allRestaurants.value = current.withDistancesFrom(location).sortedByProximity()
    }

    /**
     * A null [location] means the distance is genuinely unknown, so it stays null and the
     * UI omits it rather than quietly reporting the distance from the map centre as if it
     * were measured from the user.
     */
    private fun List<Restaurant>.withDistancesFrom(location: UserLocation?): List<Restaurant> =
        map { restaurant ->
            restaurant.copy(
                distanceMeters = location?.let {
                    haversineMeters(it.lat, it.lng, restaurant.lat, restaurant.lng)
                },
            )
        }

    /** Nearest first, falling back to the search centre while the location is unknown. */
    private fun List<Restaurant>.sortedByProximity(): List<Restaurant> =
        sortedBy { it.distanceMeters ?: haversineMeters(searchLat, searchLng, it.lat, it.lng) }

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
                val sorted = raw.withDistancesFrom(loc).sortedByProximity()
                _allRestaurants.value = sorted
                _fetchGeneration.value++
                // The selection is meant to be stable for the day, so draw it once and from
                // then on only re-measure it — panning the map must not re-roll the picks.
                pickCandidates = if (pickCandidates.isEmpty()) {
                    recommendUseCase(sorted, count = PICK_CANDIDATE_COUNT)
                } else {
                    pickCandidates.withDistancesFrom(loc)
                }
                _bestPicks.value = sortPicksUseCase(pickCandidates)
                _loadError.value = null
            } catch (e: Exception) {
                _loadError.value =
                    e.message ?: getString(Res.string.error_failed_to_load_restaurants)
            } finally {
                _isLoading.value = false
            }
        }
    }
}
