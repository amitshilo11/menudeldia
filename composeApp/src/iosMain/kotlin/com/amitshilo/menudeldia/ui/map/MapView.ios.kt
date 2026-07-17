@file:OptIn(ExperimentalForeignApi::class)

package com.amitshilo.menudeldia.ui.map

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.viewinterop.UIKitView
import com.amitshilo.menudeldia.domain.model.Restaurant
import com.amitshilo.menudeldia.location.UserLocation
import com.amitshilo.menudeldia.util.haversineMeters
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCSignatureOverride
import kotlinx.cinterop.useContents
import platform.CoreGraphics.CGAffineTransformMakeScale
import platform.CoreLocation.CLLocationCoordinate2DMake
import platform.MapKit.MKAnnotationProtocol
import platform.MapKit.MKAnnotationView
import platform.MapKit.MKCoordinateRegionMakeWithDistance
import platform.MapKit.MKMapView
import platform.MapKit.MKMapViewDelegateProtocol
import platform.UIKit.UIColor
import platform.UIKit.UIEdgeInsetsMake
import platform.UIKit.UIView
import platform.darwin.NSObject

private fun Color.toUIColor(): UIColor = UIColor(
    red = red.toDouble(),
    green = green.toDouble(),
    blue = blue.toDouble(),
    alpha = alpha.toDouble(),
)

private class MapDelegate(
    val annotationManager: AnnotationManager,
    var primaryColor: UIColor,
) : NSObject(), MKMapViewDelegateProtocol {

    var restaurants: List<Restaurant> = emptyList()
    var selectedId: String? = null
    var onRestaurantSelected: (String) -> Unit = {}
    var onMapTap: () -> Unit = {}
    var onMapIdle: (Double, Double, Double) -> Unit = { _, _, _ -> }
    private var isRefreshingBubbles = false

    private val collisionPxSq =
        (MapDefaults.collisionRadiusDp * MapDefaults.collisionRadiusDp).toFloat()

    override fun mapView(
        mapView: MKMapView,
        viewForAnnotation: MKAnnotationProtocol
    ): MKAnnotationView? {
        val ann = viewForAnnotation as? RestaurantAnnotation ?: return null
        return if (ann.isBubble) {
            val view =
                mapView.dequeueReusableAnnotationViewWithIdentifier("bubble") as? BubbleAnnotationView
                    ?: BubbleAnnotationView(ann, "bubble")
            view.configure(ann, primaryColor)
            view
        } else {
            val view =
                mapView.dequeueReusableAnnotationViewWithIdentifier("dot") as? DotAnnotationView
                    ?: DotAnnotationView(ann, "dot")
            view.configure(primaryColor)
            view
        }
    }

    @ObjCSignatureOverride
    override fun mapView(mapView: MKMapView, didAddAnnotationViews: List<*>) {
        didAddAnnotationViews.filterIsInstance<MKAnnotationView>().forEach { view ->
            view.alpha = 0.0
            view.transform = CGAffineTransformMakeScale(0.4, 0.4)
            UIView.animateWithDuration(
                duration = MapDefaults.pinAppearAnimMs / 1000.0,
                animations = {
                    view.alpha = 1.0
                    view.transform = CGAffineTransformMakeScale(1.0, 1.0)
                },
            )
        }
    }

    @ObjCSignatureOverride
    override fun mapView(mapView: MKMapView, didSelectAnnotationView: MKAnnotationView) {
        val ann = didSelectAnnotationView.annotation as? RestaurantAnnotation ?: return
        onRestaurantSelected(ann.restaurantId)
    }

    @ObjCSignatureOverride
    override fun mapView(mapView: MKMapView, didDeselectAnnotationView: MKAnnotationView) {
        if (!isRefreshingBubbles) onMapTap()
    }

    override fun mapView(mapView: MKMapView, regionDidChangeAnimated: Boolean) {
        val region = mapView.region
        val centerLat = region.useContents { center.latitude }
        val centerLng = region.useContents { center.longitude }
        val latDelta = region.useContents { span.latitudeDelta }
        val lngDelta = region.useContents { span.longitudeDelta }
        val radiusMeters = haversineMeters(
            centerLat,
            centerLng,
            centerLat + latDelta / 2,
            centerLng + lngDelta / 2
        )
        onMapIdle(centerLat, centerLng, radiusMeters)

        val bubbleIds = pickBubbleIds(restaurants, selectedId, { r ->
            mapView.convertCoordinate(
                CLLocationCoordinate2DMake(r.lat, r.lng),
                toPointToView = mapView,
            ).useContents { Pair(x.toFloat(), y.toFloat()) }
        }, collisionPxSq)
        isRefreshingBubbles = true
        annotationManager.refreshBubbles(mapView, bubbleIds, selectedId, primaryColor)
        isRefreshingBubbles = false
    }
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

    SideEffect {
        delegate.onRestaurantSelected = onRestaurantSelected
        delegate.onMapTap = onMapTap
        delegate.onMapIdle = onMapIdle
    }

    var hasMovedToUser by remember { mutableStateOf(false) }

    LaunchedEffect(userLocation) {
        if (userLocation != null && !hasMovedToUser) {
            hasMovedToUser = true
            mapView.setRegion(
                MKCoordinateRegionMakeWithDistance(
                    CLLocationCoordinate2DMake(userLocation.lat, userLocation.lng),
                    MapDefaults.focusDistanceMeters,
                    MapDefaults.focusDistanceMeters,
                ),
                animated = true,
            )
        }
    }

    LaunchedEffect(selectedRestaurantId) {
        val selected =
            delegate.restaurants.find { it.id == selectedRestaurantId } ?: return@LaunchedEffect
        mapView.setRegion(
            MKCoordinateRegionMakeWithDistance(
                CLLocationCoordinate2DMake(selected.lat, selected.lng),
                MapDefaults.focusDistanceMeters,
                MapDefaults.focusDistanceMeters,
            ),
            animated = true,
        )
    }

    LaunchedEffect(recenterTrigger) {
        if (recenterTrigger <= 0) return@LaunchedEffect
        val loc = userLocation ?: return@LaunchedEffect
        mapView.setRegion(
            MKCoordinateRegionMakeWithDistance(
                CLLocationCoordinate2DMake(loc.lat, loc.lng),
                MapDefaults.focusDistanceMeters,
                MapDefaults.focusDistanceMeters,
            ),
            animated = true,
        )
    }

    UIKitView(
        factory = { mapView },
        update = { mv ->
            mv.showsUserLocation = isLocationEnabled
            delegate.restaurants = restaurants
            delegate.selectedId = selectedRestaurantId
            delegate.primaryColor = primaryColor
            annotationManager.sync(mv, restaurants, selectedRestaurantId, primaryColor)
            mv.setLayoutMargins(UIEdgeInsetsMake(0.0, 0.0, bottomPadding.value.toDouble(), 0.0))
        },
        modifier = modifier,
    )
}
