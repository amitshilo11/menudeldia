@file:OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)

package com.amitshilo.menudeldia.ui.map

import com.amitshilo.menudeldia.domain.model.Restaurant
import com.amitshilo.menudeldia.util.haversineMeters
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCAction
import kotlinx.cinterop.ObjCSignatureOverride
import kotlinx.cinterop.useContents
import platform.CoreGraphics.CGAffineTransformMakeScale
import platform.CoreLocation.CLLocationCoordinate2DMake
import platform.MapKit.MKAnnotationProtocol
import platform.MapKit.MKAnnotationView
import platform.MapKit.MKMapView
import platform.MapKit.MKMapViewDelegateProtocol
import platform.UIKit.UIColor
import platform.UIKit.UIGestureRecognizer
import platform.UIKit.UIGestureRecognizerDelegateProtocol
import platform.UIKit.UIGestureRecognizerStateEnded
import platform.UIKit.UITapGestureRecognizer
import platform.UIKit.UIView
import platform.darwin.NSObject

internal class MapDelegate(
    val annotationManager: AnnotationManager,
    var primaryColor: UIColor,
) : NSObject(), MKMapViewDelegateProtocol {

    var restaurants: List<Restaurant> = emptyList()
    var selectedId: String? = null
    var onRestaurantSelected: (String) -> Unit = {}
    var onMapTap: () -> Unit = {}
    var onMapGesture: () -> Unit = {}
    var onMapIdle: (Double, Double, Double) -> Unit = { _, _, _ -> }
    private var isRefreshingBubbles = false

    /**
     * Set around our own `setRegion` calls so the region-will-change callback can tell a camera
     * move we asked for apart from one the user's fingers started.
     */
    var isProgrammaticMove = false

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

    @ObjCSignatureOverride
    override fun mapView(mapView: MKMapView, regionWillChangeAnimated: Boolean) {
        if (!isProgrammaticMove) onMapGesture()
    }

    @ObjCSignatureOverride
    override fun mapView(mapView: MKMapView, regionDidChangeAnimated: Boolean) {
        isProgrammaticMove = false
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
        refreshBubbleClassification(mapView)
    }

    fun refreshBubbleClassification(mapView: MKMapView) {
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

/**
 * `MKMapView` only reports taps that deselect an annotation, so a tap on empty map does nothing
 * on its own. This recognizer fills that gap, ignoring taps that land on an annotation so the
 * selection isn't cleared the instant it is made.
 */
internal class MapTapRecognizerTarget(
    private val mapView: MKMapView,
) : NSObject(), UIGestureRecognizerDelegateProtocol {

    var onTap: () -> Unit = {}

    @ObjCAction
    fun handleTap(recognizer: UITapGestureRecognizer) {
        if (recognizer.state != UIGestureRecognizerStateEnded) return
        val hit = mapView.hitTest(recognizer.locationInView(mapView), withEvent = null)
        if (hit.isWithinAnnotation()) return
        onTap()
    }

    override fun gestureRecognizer(
        gestureRecognizer: UIGestureRecognizer,
        shouldRecognizeSimultaneouslyWithGestureRecognizer: UIGestureRecognizer,
    ): Boolean = true

    private fun UIView?.isWithinAnnotation(): Boolean {
        var view = this
        while (view != null) {
            if (view is MKAnnotationView) return true
            view = view.superview
        }
        return false
    }
}
