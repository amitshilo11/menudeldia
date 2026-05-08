package com.amitshilo.menudeldia.ui.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.amitshilo.menudeldia.domain.model.Restaurant
import com.amitshilo.menudeldia.location.UserLocation
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MarkerComposable
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState

private val barcelonaCenter = LatLng(41.3851, 2.1734)

actual @Composable fun MapView(
    restaurants: List<Restaurant>,
    selectedRestaurantId: String?,
    userLocation: UserLocation?,
    isLocationEnabled: Boolean,
    recenterTrigger: Int,
    onRestaurantSelected: (String) -> Unit,
    modifier: Modifier,
    bottomPadding: Dp,
) {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(barcelonaCenter, 14f)
    }

    LaunchedEffect(selectedRestaurantId) {
        val selected = restaurants.find { it.id == selectedRestaurantId }
        if (selected != null) {
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(LatLng(selected.lat, selected.lng), 15f),
                durationMs = 400,
            )
        }
    }

    LaunchedEffect(recenterTrigger) {
        if (recenterTrigger > 0 && userLocation != null) {
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(userLocation.lat, userLocation.lng), 15f,
                ),
                durationMs = 400,
            )
        }
    }

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
        properties = MapProperties(isMyLocationEnabled = isLocationEnabled),
        uiSettings = MapUiSettings(myLocationButtonEnabled = false),
        contentPadding = PaddingValues(bottom = bottomPadding),
    ) {
        restaurants.forEach { restaurant ->
            val isSelected = restaurant.id == selectedRestaurantId
            MarkerComposable(
                keys = arrayOf<Any>(restaurant.id, isSelected),
                state = rememberMarkerState(position = LatLng(restaurant.lat, restaurant.lng)),
                title = restaurant.name,
                onClick = {
                    onRestaurantSelected(restaurant.id)
                    false
                },
            ) {
                PriceMarker(
                    emoji = restaurant.cuisineEmoji ?: "🍽️",
                    price = restaurant.menuPrice,
                    isSelected = isSelected,
                )
            }
        }
    }
}

@Composable
private fun PriceMarker(
    emoji: String,
    price: Double?,
    isSelected: Boolean,
) {
    val bgColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.White
    val textColor =
        if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface

    Box(
        modifier = Modifier
            .shadow(elevation = if (isSelected) 6.dp else 3.dp, shape = RoundedCornerShape(20.dp))
            .background(color = bgColor, shape = RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(text = emoji, fontSize = 15.sp)
            if (price != null) {
                val cents = (price * 100).toLong()
                val priceStr = "${cents / 100}.${(cents % 100).toString().padStart(2, '0')}"
                Text(
                    text = "€$priceStr",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                )
            }
        }
    }
}
