@file:OptIn(ExperimentalForeignApi::class, ExperimentalComposeUiApi::class)

package com.amitshilo.menudeldia.ui.map

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.viewinterop.UIKitInteropInteractionMode
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitView
import com.amitshilo.menudeldia.domain.model.Restaurant
import com.amitshilo.menudeldia.location.UserLocation
import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreLocation.CLLocationCoordinate2DMake
import platform.Foundation.NSSelectorFromString
import platform.MapKit.MKCoordinateRegionMakeWithDistance
import platform.MapKit.MKMapView
import platform.UIKit.UIColor
import platform.UIKit.UIEdgeInsetsMake
import platform.UIKit.UITapGestureRecognizer

private fun Color.toUIColor(): UIColor = UIColor(
    red = red.toDouble(),
    green = green.toDouble(),
    blue = blue.toDouble(),
    alpha = alpha.toDouble(),
)

/**
 * What annotation sync actually reads off a [Restaurant] — deliberately excludes
 * `distanceMeters`, which changes on every location fix and otherwise would restart the sync
 * `LaunchedEffect` on every GPS tick even though nothing it draws depends on distance.
 */
private data class AnnotationSyncKey(
    val id: String,
    val lat: Double,
    val lng: Double,
    val name: String,
    val hasMenu: Boolean,
    val emoji: String?,
    val priceText: Double?,
)

private fun Restaurant.toAnnotationSyncKey() = AnnotationSyncKey(
    id = id,
    lat = lat,
    lng = lng,
    name = name,
    hasMenu = todayHasMenu,
    emoji = cuisineEmoji,
    priceText = menuPrice,
)

private fun MKMapView.focusOn(delegate: MapDelegate, lat: Double, lng: Double) {
    delegate.isProgrammaticMove = true
    setRegion(
        MKCoordinateRegionMakeWithDistance(
            CLLocationCoordinate2DMake(lat, lng),
            MapDefaults.focusDistanceMeters,
            MapDefaults.focusDistanceMeters,
        ),
        animated = true,
    )
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
    onMapGesture: () -> Unit,
    onMapIdle: (lat: Double, lng: Double, radiusMeters: Double) -> Unit,
    modifier: Modifier,
    bottomPadding: Dp,
) {
    val primaryColor = MaterialTheme.colorScheme.primary.toUIColor()
    val annotationManager = remember { AnnotationManager() }
    val delegate = remember { MapDelegate(annotationManager, primaryColor) }
    val mapView = remember {
        MKMapView().apply {
            this.delegate = delegate
            showsPointsOfInterest = false
            showsTraffic = false
            showsScale = false
            delegate.isProgrammaticMove = true
            setRegion(
                MKCoordinateRegionMakeWithDistance(
                    CLLocationCoordinate2DMake(
                        MapDefaults.barcelonaCenterLat,
                        MapDefaults.barcelonaCenterLng
                    ),
                    MapDefaults.defaultRadiusMeters * 2,
                    MapDefaults.defaultRadiusMeters * 2,
                ),
                animated = false,
            )
        }
    }
    val tapTarget = remember {
        MapTapRecognizerTarget(mapView).also { target ->
            val recognizer = UITapGestureRecognizer(
                target = target,
                action = NSSelectorFromString("handleTap:"),
            )
            recognizer.delegate = target
            recognizer.cancelsTouchesInView = false
            mapView.addGestureRecognizer(recognizer)
        }
    }

    SideEffect {
        delegate.onRestaurantSelected = onRestaurantSelected
        delegate.onMapTap = onMapTap
        delegate.onMapGesture = onMapGesture
        delegate.onMapIdle = onMapIdle
        tapTarget.onTap = onMapTap
    }

    var hasMovedToUser by remember { mutableStateOf(false) }

    LaunchedEffect(userLocation) {
        if (userLocation != null && !hasMovedToUser) {
            hasMovedToUser = true
            mapView.focusOn(delegate, userLocation.lat, userLocation.lng)
        }
    }

    LaunchedEffect(selectedRestaurantId) {
        val selected =
            delegate.restaurants.find { it.id == selectedRestaurantId } ?: return@LaunchedEffect
        mapView.focusOn(delegate, selected.lat, selected.lng)
    }

    LaunchedEffect(recenterTrigger) {
        if (recenterTrigger <= 0) return@LaunchedEffect
        val loc = userLocation ?: return@LaunchedEffect
        mapView.focusOn(delegate, loc.lat, loc.lng)
    }

    // Annotation syncing walks every restaurant and asks MapKit to project each coordinate, so it
    // is kept out of the interop `update` block: that runs on every recomposition, which would
    // otherwise put this whole pass on the critical path of every bottom-sheet drag frame.
    //
    // Keyed on `annotationSyncKeys` rather than `restaurants` directly: the list is re-emitted on
    // every location fix with fresh `distanceMeters`, which nothing here reads, so keying on the
    // raw list would re-run this whole pass (and its MapKit annotation churn) once per GPS tick.
    val annotationSyncKeys = restaurants.map { it.toAnnotationSyncKey() }
    LaunchedEffect(annotationSyncKeys, selectedRestaurantId, primaryColor) {
        delegate.restaurants = restaurants
        delegate.selectedId = selectedRestaurantId
        delegate.primaryColor = primaryColor
        annotationManager.sync(mapView, restaurants, selectedRestaurantId, primaryColor)
        delegate.refreshBubbleClassification(mapView)
    }

    LaunchedEffect(isLocationEnabled) {
        mapView.showsUserLocation = isLocationEnabled
    }

    LaunchedEffect(bottomPadding) {
        mapView.setLayoutMargins(UIEdgeInsetsMake(0.0, 0.0, bottomPadding.value.toDouble(), 0.0))
    }

    UIKitView(
        factory = { mapView },
        modifier = modifier,
        // The map is not inside a scrollable Compose container, so there is nothing for Compose to
        // intercept. Cooperative mode would hold every touch back by 150ms before MapKit sees it,
        // which is exactly what makes panning feel sluggish.
        properties = UIKitInteropProperties(
            interactionMode = UIKitInteropInteractionMode.NonCooperative,
            isNativeAccessibilityEnabled = false,
        ),
    )
}
