package com.amitshilo.menudeldia.ui.map

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.amitshilo.menudeldia.domain.model.Restaurant
import com.amitshilo.menudeldia.location.UserLocation

@Composable
expect fun MapView(
    restaurants: List<Restaurant>,
    selectedRestaurantId: String?,
    userLocation: UserLocation?,
    isLocationEnabled: Boolean,
    recenterTrigger: Int,
    onRestaurantSelected: (String) -> Unit,
    onMapTap: () -> Unit,
    /** The user started panning or zooming the map — distinct from programmatic camera moves. */
    onMapGesture: () -> Unit,
    onMapIdle: (lat: Double, lng: Double, radiusMeters: Double) -> Unit,
    modifier: Modifier = Modifier,
    bottomPadding: Dp,
)
