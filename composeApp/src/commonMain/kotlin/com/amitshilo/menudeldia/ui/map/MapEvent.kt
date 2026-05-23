package com.amitshilo.menudeldia.ui.map

import com.amitshilo.menudeldia.domain.model.SearchFilterState
import com.amitshilo.menudeldia.location.UserLocation

sealed class MapEvent {
    data class SelectRestaurant(val id: String) : MapEvent()
    data object ClearSelection : MapEvent()
    data class FilterChanged(val filter: SearchFilterState) : MapEvent()
    data object ClearFilters : MapEvent()
    data object Refresh : MapEvent()
    data class LocationChanged(val location: UserLocation?) : MapEvent()
    data object RecenterRequested : MapEvent()
}