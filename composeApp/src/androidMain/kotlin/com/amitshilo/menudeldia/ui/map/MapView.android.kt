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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.amitshilo.menudeldia.domain.model.Restaurant
import com.amitshilo.menudeldia.location.UserLocation
import com.amitshilo.menudeldia.util.haversineMeters
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.Projection
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MarkerComposable
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState

private val barcelonaCenter = LatLng(41.3851, 2.1734)

private class BubblePinShape(
    private val cornerRadius: Dp,
    private val notchWidth: Dp,
    private val notchHeight: Dp,
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val cr = with(density) { cornerRadius.toPx() }
        val nw = with(density) { notchWidth.toPx() }
        val nh = with(density) { notchHeight.toPx() }
        val pillBottom = size.height - nh
        val cx = size.width / 2f
        val path = Path().apply {
            moveTo(cr, 0f)
            lineTo(size.width - cr, 0f)
            arcTo(Rect(size.width - 2 * cr, 0f, size.width, 2 * cr), -90f, 90f, false)
            lineTo(size.width, pillBottom - cr)
            arcTo(
                Rect(size.width - 2 * cr, pillBottom - 2 * cr, size.width, pillBottom),
                0f,
                90f,
                false
            )
            lineTo(cx + nw / 2, pillBottom)
            lineTo(cx, size.height)
            lineTo(cx - nw / 2, pillBottom)
            lineTo(cr, pillBottom)
            arcTo(Rect(0f, pillBottom - 2 * cr, 2 * cr, pillBottom), 90f, 90f, false)
            lineTo(0f, cr)
            arcTo(Rect(0f, 0f, 2 * cr, 2 * cr), 180f, 90f, false)
            close()
        }
        return Outline.Generic(path)
    }
}

private fun computeBubbleIds(
    restaurants: List<Restaurant>,
    projection: Projection,
    selectedId: String?,
    collisionPxSq: Float,
): Set<String> {
    val sorted = restaurants.sortedWith(
        compareByDescending<Restaurant> { it.id == selectedId }
            .thenByDescending { it.todayHasMenu }
            .thenBy { it.id },
    )
    val claimed = mutableListOf<Pair<Float, Float>>()
    val bubbleIds = mutableSetOf<String>()
    for (r in sorted) {
        val pt = projection.toScreenLocation(LatLng(r.lat, r.lng))
        val x = pt.x.toFloat()
        val y = pt.y.toFloat()
        val overlaps = claimed.any { (bx, by) ->
            val dx = x - bx
            val dy = y - by
            dx * dx + dy * dy < collisionPxSq
        }
        if (!overlaps) {
            claimed += Pair(x, y)
            bubbleIds += r.id
        }
    }
    return bubbleIds
}

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
        position = CameraPosition.fromLatLngZoom(barcelonaCenter, 14f)
    }
    val density = LocalDensity.current
    val collisionPxSq = remember(density) {
        val px = with(density) { 96.dp.toPx() }
        px * px
    }
    var bubbleIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var hasMovedToUser by remember { mutableStateOf(false) }

    LaunchedEffect(userLocation) {
        if (userLocation != null && !hasMovedToUser) {
            hasMovedToUser = true
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(LatLng(userLocation.lat, userLocation.lng), 15f),
                durationMs = 600,
            )
        }
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

    LaunchedEffect(cameraPositionState.isMoving, restaurants, selectedRestaurantId) {
        if (cameraPositionState.isMoving) return@LaunchedEffect
        val projection = cameraPositionState.projection ?: return@LaunchedEffect
        bubbleIds = computeBubbleIds(restaurants, projection, selectedRestaurantId, collisionPxSq)
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
    ) {
        restaurants.forEach { restaurant ->
            val isBubble = restaurant.id in bubbleIds
            val isSelected = restaurant.id == selectedRestaurantId
            key(restaurant.id) {
                if (isBubble) {
                    MarkerComposable(
                        keys = arrayOf<Any>(restaurant.id, isSelected),
                        state = rememberMarkerState(
                            position = LatLng(
                                restaurant.lat,
                                restaurant.lng
                            )
                        ),
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

@Composable
private fun DotMarker() {
    Box(
        modifier = Modifier
            .size(12.dp)
            .shadow(2.dp, CircleShape)
            .background(MaterialTheme.colorScheme.primary, CircleShape)
            .border(1.5.dp, Color.White, CircleShape),
    )
}

@Composable
private fun PriceMarker(
    emoji: String,
    price: Double?,
    isSelected: Boolean,
    hasMenu: Boolean,
) {
    val shape =
        remember { BubblePinShape(cornerRadius = 16.dp, notchWidth = 12.dp, notchHeight = 8.dp) }
    val bgColor = Color.White
    val textColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        !hasMenu -> MaterialTheme.colorScheme.onSurfaceVariant
        else -> MaterialTheme.colorScheme.onSurface
    }
    val borderModifier = when {
        isSelected -> Modifier.border(2.dp, MaterialTheme.colorScheme.primary, shape)
        else -> Modifier.border(0.5.dp, Color(0xFFCCCCCC), shape)
    }

    Box(
        modifier = Modifier
            .shadow(elevation = if (isSelected) 8.dp else 3.dp, shape = shape)
            .background(color = bgColor, shape = shape)
            .then(borderModifier)
            .padding(start = 10.dp, end = 10.dp, top = 6.dp, bottom = 14.dp),
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
