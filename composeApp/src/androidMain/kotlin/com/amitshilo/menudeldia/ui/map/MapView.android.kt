package com.amitshilo.menudeldia.ui.map

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.amitshilo.menudeldia.domain.model.Restaurant
import com.amitshilo.menudeldia.location.UserLocation
import com.amitshilo.menudeldia.util.haversineMeters
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MarkerComposable
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState

private val barcelonaCenter = LatLng(MapDefaults.barcelonaCenterLat, MapDefaults.barcelonaCenterLng)

actual @Composable fun MapView(
    restaurants: List<Restaurant>,
    selectedRestaurantId: String?,
    userLocation: UserLocation?,
    isLocationEnabled: Boolean,
    recenterTrigger: Int,
    onRestaurantSelected: (String) -> Unit,
    onMapTap: () -> Unit,
    onMapIdle: (lat: Double, lng: Double, radiusMeters: Double) -> Unit,
    modifier: Modifier,
    bottomPadding: Dp,
) {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(barcelonaCenter, MapDefaults.defaultZoom)
    }
    val density = LocalDensity.current
    val collisionPxSq = remember(density) {
        val px = with(density) { MapDefaults.collisionRadiusDp.dp.toPx() }
        px * px
    }
    var bubbleIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var hasMovedToUser by remember { mutableStateOf(false) }
    var mapLoaded by remember { mutableStateOf(false) }

    LaunchedEffect(userLocation) {
        if (userLocation != null && !hasMovedToUser) {
            hasMovedToUser = true
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(userLocation.lat, userLocation.lng),
                    MapDefaults.focusZoom
                ),
                durationMs = MapDefaults.userMoveAnimMs,
            )
        }
    }

    LaunchedEffect(selectedRestaurantId) {
        val selected = restaurants.find { it.id == selectedRestaurantId }
        if (selected != null) {
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(selected.lat, selected.lng),
                    MapDefaults.focusZoom
                ),
                durationMs = MapDefaults.selectionAnimMs,
            )
        }
    }

    LaunchedEffect(recenterTrigger) {
        if (recenterTrigger > 0 && userLocation != null) {
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(userLocation.lat, userLocation.lng), MapDefaults.focusZoom,
                ),
                durationMs = MapDefaults.recenterAnimMs,
            )
        }
    }

    LaunchedEffect(cameraPositionState.isMoving) {
        if (cameraPositionState.isMoving) return@LaunchedEffect
        val projection = cameraPositionState.projection ?: return@LaunchedEffect
        val bounds = projection.visibleRegion.latLngBounds
        val center = bounds.center
        val ne = bounds.northeast
        val radiusMeters =
            haversineMeters(center.latitude, center.longitude, ne.latitude, ne.longitude)
        onMapIdle(center.latitude, center.longitude, radiusMeters)
    }

    LaunchedEffect(cameraPositionState.isMoving, restaurants, selectedRestaurantId, mapLoaded) {
        if (cameraPositionState.isMoving) return@LaunchedEffect
        val projection = cameraPositionState.projection ?: return@LaunchedEffect
        bubbleIds = pickBubbleIds(restaurants, selectedRestaurantId, { r ->
            val pt = projection.toScreenLocation(LatLng(r.lat, r.lng))
            Pair(pt.x.toFloat(), pt.y.toFloat())
        }, collisionPxSq)
    }

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
        properties = MapProperties(
            isMyLocationEnabled = isLocationEnabled,
            mapStyleOptions = MapStyleOptions(
                """[{"featureType":"poi","stylers":[{"visibility":"off"}]},{"featureType":"transit","stylers":[{"visibility":"off"}]}]"""
            ),
        ),
        uiSettings = MapUiSettings(
            myLocationButtonEnabled = false,
            zoomControlsEnabled = false,
            mapToolbarEnabled = false,
        ),
        contentPadding = PaddingValues(bottom = bottomPadding),
        onMapClick = { onMapTap() },
        onMapLoaded = { mapLoaded = true },
    ) {
        restaurants.forEach { restaurant ->
            val isBubble = restaurant.id in bubbleIds
            val isSelected = restaurant.id == selectedRestaurantId
            key(restaurant.id) {
                val pinAlpha = rememberPinAppearAlpha()
                if (isBubble) {
                    MarkerComposable(
                        keys = arrayOf<Any>(restaurant.id, isSelected),
                        state = rememberMarkerState(
                            position = LatLng(
                                restaurant.lat,
                                restaurant.lng
                            )
                        ),
                        alpha = pinAlpha,
                        zIndex = 1f,
                        title = restaurant.name,
                        onClick = {
                            onRestaurantSelected(restaurant.id)
                            true
                        },
                    ) {
                        PriceMarker(
                            emoji = restaurant.cuisineEmoji ?: "🍽️",
                            price = restaurant.menuPrice,
                            isSelected = isSelected,
                            hasMenu = restaurant.todayHasMenu,
                        )
                    }
                } else {
                    MarkerComposable(
                        keys = arrayOf<Any>(restaurant.id),
                        state = rememberMarkerState(
                            position = LatLng(
                                restaurant.lat,
                                restaurant.lng
                            )
                        ),
                        alpha = pinAlpha,
                        zIndex = 0f,
                        anchor = Offset(0.5f, 0.5f),
                        title = restaurant.name,
                        onClick = {
                            onRestaurantSelected(restaurant.id)
                            true
                        },
                    ) {
                        DotMarker()
                    }
                }
            }
        }
    }
}
