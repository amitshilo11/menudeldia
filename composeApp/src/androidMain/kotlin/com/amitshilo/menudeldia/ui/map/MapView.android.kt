package com.amitshilo.menudeldia.ui.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.amitshilo.menudeldia.domain.model.Restaurant
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState

private val barcelonaCenter = LatLng(41.3851, 2.1734)

actual @Composable fun MapView(
    restaurants: List<Restaurant>,
    selectedRestaurantId: String?,
    onRestaurantSelected: (String) -> Unit,
    modifier: Modifier,
) {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(barcelonaCenter, 14f)
    }

    LaunchedEffect(selectedRestaurantId) {
        val selected = restaurants.find { it.id == selectedRestaurantId }
        if (selected != null) {
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(LatLng(selected.lat, selected.lng), 15f),
                durationMs = 500,
            )
        }
    }

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
    ) {
        restaurants.forEach { restaurant ->
            Marker(
                state = rememberMarkerState(position = LatLng(restaurant.lat, restaurant.lng)),
                title = restaurant.name,
                snippet = restaurant.menuPrice?.let { "€${formatPrice(it)}" },
                onClick = {
                    onRestaurantSelected(restaurant.id)
                    false
                },
            )
        }
    }
}

private fun formatPrice(price: Double): String {
    val cents = (price * 100).toLong()
    val euros = cents / 100
    val centsPart = cents % 100
    return "$euros.${centsPart.toString().padStart(2, '0')}"
}
