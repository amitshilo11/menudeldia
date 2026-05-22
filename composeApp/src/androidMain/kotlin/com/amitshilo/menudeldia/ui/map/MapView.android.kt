@file:OptIn(MapsComposeExperimentalApi::class)

package com.amitshilo.menudeldia.ui.map

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.clustering.ClusterItem
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MapsComposeExperimentalApi
import com.google.maps.android.compose.clustering.Clustering
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch

private val barcelonaCenter = LatLng(41.3851, 2.1734)

private class RestaurantClusterItem(val restaurant: Restaurant) : ClusterItem {
    private val pos = LatLng(restaurant.lat, restaurant.lng)
    override fun getPosition() = pos
    override fun getTitle() = restaurant.name
    override fun getSnippet() = null
    override fun getZIndex() = 0f
}

@Composable
actual fun MapView(
    restaurants: List<Restaurant>,
    selectedRestaurantId: String?,
    userLocation: UserLocation?,
    isLocationEnabled: Boolean,
    recenterTrigger: Int,
    onRestaurantSelected: (String) -> Unit,
    onMapTap: () -> Unit,
    modifier: Modifier,
    bottomPadding: Dp,
) {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(barcelonaCenter, 14f)
    }
    val coroutineScope = rememberCoroutineScope()

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

    val clusterItems = remember(restaurants) { restaurants.map { RestaurantClusterItem(it) } }
    val isAnySelected = selectedRestaurantId != null

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
        properties = MapProperties(isMyLocationEnabled = isLocationEnabled),
        uiSettings = MapUiSettings(
            myLocationButtonEnabled = false,
            zoomControlsEnabled = false,
            mapToolbarEnabled = false,
        ),
        contentPadding = PaddingValues(bottom = bottomPadding),
        onMapClick = { onMapTap() },
    ) {
        Clustering(
            items = clusterItems,
            onClusterClick = { cluster ->
                val boundsBuilder = LatLngBounds.builder()
                cluster.items.forEach { boundsBuilder.include(it.getPosition()) }
                val bounds = boundsBuilder.build()
                coroutineScope.launch {
                    val degenerate = bounds.northeast.latitude == bounds.southwest.latitude &&
                            bounds.northeast.longitude == bounds.southwest.longitude
                    cameraPositionState.animate(
                        update = if (degenerate) {
                            CameraUpdateFactory.newLatLngZoom(
                                bounds.center, cameraPositionState.position.zoom + 2f,
                            )
                        } else {
                            CameraUpdateFactory.newLatLngBounds(bounds, 120)
                        },
                        durationMs = 400,
                    )
                }
                true
            },
            onClusterItemClick = { item ->
                onRestaurantSelected(item.restaurant.id)
                true
            },
            clusterContent = { cluster ->
                ClusterCircle(count = cluster.size, dimmed = isAnySelected)
            },
            clusterItemContent = { item ->
                val isSelected = item.restaurant.id == selectedRestaurantId
                val dimmed = isAnySelected && !isSelected
                Box(modifier = if (dimmed) Modifier.alpha(0.4f) else Modifier) {
                    PriceMarker(
                        emoji = item.restaurant.cuisineEmoji ?: "🍽️",
                        price = item.restaurant.menuPrice,
                        isSelected = isSelected,
                        hasMenu = item.restaurant.todayHasMenu,
                    )
                }
            },
            onClusterManager = { cm ->
                // 2+ overlapping markers merge into a cluster circle
                @Suppress("UNCHECKED_CAST")
                (cm.renderer as? DefaultClusterRenderer<RestaurantClusterItem>)?.setMinClusterSize(1)
            },
        )
    }
}

@Composable
private fun ClusterCircle(count: Int, dimmed: Boolean) {
    val size = when {
        count < 6 -> 44.dp
        count < 16 -> 52.dp
        count < 50 -> 60.dp
        else -> 68.dp
    }
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .alpha(if (dimmed) 0.35f else 1f)
            .size(size)
            .shadow(elevation = 6.dp, shape = CircleShape)
            .background(color = MaterialTheme.colorScheme.primary, shape = CircleShape),
    ) {
        Text(
            text = count.toString(),
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun PriceMarker(
    emoji: String,
    price: Double?,
    isSelected: Boolean,
    hasMenu: Boolean,
) {
    val bgColor = if (!hasMenu) MaterialTheme.colorScheme.surfaceVariant else Color.White
    val textColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        !hasMenu -> MaterialTheme.colorScheme.onSurfaceVariant
        else -> MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = Modifier
            .shadow(elevation = if (isSelected) 8.dp else 3.dp, shape = RoundedCornerShape(20.dp))
            .background(color = bgColor, shape = RoundedCornerShape(20.dp))
            .then(
                if (isSelected) {
                    Modifier.border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(20.dp),
                    )
                } else {
                    Modifier
                },
            )
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
