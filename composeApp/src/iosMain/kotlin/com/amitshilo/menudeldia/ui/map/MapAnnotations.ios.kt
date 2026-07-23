@file:OptIn(ExperimentalForeignApi::class)

package com.amitshilo.menudeldia.ui.map

import com.amitshilo.menudeldia.domain.model.Restaurant
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.CoreGraphics.CGPointMake
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSizeMake
import platform.CoreLocation.CLLocationCoordinate2DMake
import platform.MapKit.MKAnnotationProtocol
import platform.MapKit.MKAnnotationView
import platform.MapKit.MKMapView
import platform.MapKit.MKPointAnnotation
import platform.QuartzCore.CAShapeLayer
import platform.UIKit.UIBezierPath
import platform.UIKit.UIColor
import platform.UIKit.UIFont
import platform.UIKit.UILabel
import kotlin.math.PI

internal class RestaurantAnnotation : MKPointAnnotation() {
    var restaurantId: String = ""
    var isBubble: Boolean = false
    var isSelected: Boolean = false
    var hasMenu: Boolean = false
    var emoji: String = "🍽️"
    var priceText: String? = null
    var lat: Double = 0.0
    var lng: Double = 0.0
}

internal class BubbleAnnotationView(
    annotation: MKAnnotationProtocol?,
    reuseIdentifier: String?,
) : MKAnnotationView(annotation, reuseIdentifier) {

    private val shapeLayer = CAShapeLayer()
    private val emojiLabel = UILabel()
    private val priceLabel = UILabel()

    init {
        backgroundColor = UIColor.clearColor
        layer.masksToBounds = false
        layer.addSublayer(shapeLayer)
        emojiLabel.font = UIFont.systemFontOfSize(15.0)
        addSubview(emojiLabel)
        priceLabel.font = UIFont.boldSystemFontOfSize(12.0)
        addSubview(priceLabel)
        canShowCallout = false
    }

    fun configure(ann: RestaurantAnnotation, primaryColor: UIColor) {
        emojiLabel.text = ann.emoji
        priceLabel.text = ann.priceText
        priceLabel.setHidden(ann.priceText == null)

        priceLabel.textColor = when {
            ann.isSelected -> primaryColor
            !ann.hasMenu -> UIColor(white = 0.55, alpha = 1.0)
            else -> UIColor(white = 0.1, alpha = 1.0)
        }

        emojiLabel.sizeToFit()
        priceLabel.sizeToFit()

        val emojiW = emojiLabel.frame.useContents { size.width }
        val priceW = if (ann.priceText != null) priceLabel.frame.useContents { size.width } else 0.0
        val spacing = if (ann.priceText != null) 4.0 else 0.0
        val contentW = 10.0 + emojiW + spacing + priceW + 10.0
        val contentH = 20.0
        val pillH = 6.0 + contentH + 6.0
        val totalH = pillH + 8.0

        setFrame(CGRectMake(0.0, 0.0, contentW, totalH))
        shapeLayer.setFrame(CGRectMake(0.0, 0.0, contentW, totalH))
        shapeLayer.path = buildBubblePath(contentW, pillH, totalH).CGPath
        shapeLayer.fillColor = UIColor.whiteColor.CGColor
        shapeLayer.strokeColor = if (ann.isSelected) primaryColor.CGColor
        else UIColor(white = 0.8, alpha = 1.0).CGColor
        shapeLayer.lineWidth = if (ann.isSelected) 2.0 else 0.5

        layer.shadowColor = UIColor.blackColor.CGColor
        layer.shadowOpacity = if (ann.isSelected) 0.25f else 0.12f
        layer.shadowRadius = if (ann.isSelected) 4.0 else 2.0
        layer.setShadowOffset(CGSizeMake(0.0, 2.0))

        emojiLabel.setFrame(CGRectMake(10.0, 6.0, emojiW, contentH))
        if (ann.priceText != null) {
            priceLabel.setFrame(CGRectMake(10.0 + emojiW + spacing, 6.0, priceW, contentH))
        }

        setCenterOffset(CGPointMake(0.0, -(totalH / 2.0)))
    }

    private fun buildBubblePath(w: Double, pillH: Double, totalH: Double): UIBezierPath {
        val cr = 16.0
        val nw = 12.0
        val cx = w / 2.0
        val path = UIBezierPath()
        path.moveToPoint(CGPointMake(cr, 0.0))
        path.addLineToPoint(CGPointMake(w - cr, 0.0))
        path.addArcWithCenter(
            CGPointMake(w - cr, cr),
            radius = cr,
            startAngle = -PI / 2.0,
            endAngle = 0.0,
            clockwise = true
        )
        path.addLineToPoint(CGPointMake(w, pillH - cr))
        path.addArcWithCenter(
            CGPointMake(w - cr, pillH - cr),
            radius = cr,
            startAngle = 0.0,
            endAngle = PI / 2.0,
            clockwise = true
        )
        path.addLineToPoint(CGPointMake(cx + nw / 2.0, pillH))
        path.addLineToPoint(CGPointMake(cx, totalH))
        path.addLineToPoint(CGPointMake(cx - nw / 2.0, pillH))
        path.addLineToPoint(CGPointMake(cr, pillH))
        path.addArcWithCenter(
            CGPointMake(cr, pillH - cr),
            radius = cr,
            startAngle = PI / 2.0,
            endAngle = PI,
            clockwise = true
        )
        path.addLineToPoint(CGPointMake(0.0, cr))
        path.addArcWithCenter(
            CGPointMake(cr, cr),
            radius = cr,
            startAngle = PI,
            endAngle = 3.0 * PI / 2.0,
            clockwise = true
        )
        path.closePath()
        return path
    }
}

internal class DotAnnotationView(
    annotation: MKAnnotationProtocol?,
    reuseIdentifier: String?,
) : MKAnnotationView(annotation, reuseIdentifier) {

    private val dotLayer = CAShapeLayer()

    init {
        backgroundColor = UIColor.clearColor
        layer.masksToBounds = false
        layer.addSublayer(dotLayer)
        canShowCallout = false

        val size = 12.0
        setFrame(CGRectMake(0.0, 0.0, size, size))
        val path =
            UIBezierPath.bezierPathWithOvalInRect(CGRectMake(1.0, 1.0, size - 2.0, size - 2.0))
        dotLayer.setFrame(CGRectMake(0.0, 0.0, size, size))
        dotLayer.path = path.CGPath
        dotLayer.strokeColor = UIColor.whiteColor.CGColor
        dotLayer.lineWidth = 1.5
        layer.shadowColor = UIColor.blackColor.CGColor
        layer.shadowOpacity = 0.2f
        layer.shadowRadius = 2.0
        layer.setShadowOffset(CGSizeMake(0.0, 1.0))
    }

    fun configure(primaryColor: UIColor) {
        dotLayer.fillColor = primaryColor.CGColor
    }
}

internal class AnnotationManager {
    val annotations = mutableMapOf<String, RestaurantAnnotation>()

    fun sync(
        mapView: MKMapView,
        restaurants: List<Restaurant>,
        selectedId: String?,
        primaryColor: UIColor,
    ) {
        val newIds = restaurants.map { it.id }.toSet()

        val stale = annotations.keys.filter { it !in newIds }
        stale.forEach { id -> annotations.remove(id)?.let { mapView.removeAnnotation(it) } }

        restaurants.filter { it.id !in annotations }.forEach { r ->
            val ann = RestaurantAnnotation().apply {
                restaurantId = r.id
                lat = r.lat
                lng = r.lng
                setCoordinate(CLLocationCoordinate2DMake(r.lat, r.lng))
                setTitle(r.name)
                isBubble = false
                isSelected = r.id == selectedId
                hasMenu = r.todayHasMenu
                emoji = r.cuisineEmoji ?: "🍽️"
                priceText = r.menuPrice?.let { formatPrice(it) }
            }
            annotations[r.id] = ann
            mapView.addAnnotation(ann)
        }

        for (r in restaurants) {
            val ann = annotations[r.id] ?: continue
            val newSelected = r.id == selectedId
            if (ann.isSelected != newSelected) {
                ann.isSelected = newSelected
                (mapView.viewForAnnotation(ann) as? BubbleAnnotationView)?.configure(
                    ann,
                    primaryColor
                )
            }
        }
    }

    fun refreshBubbles(
        mapView: MKMapView,
        bubbleIds: Set<String>,
        selectedId: String?,
        primaryColor: UIColor,
    ) {
        for ((id, ann) in annotations) {
            val newBubble = id in bubbleIds
            if (ann.isBubble == newBubble) continue
            ann.isBubble = newBubble
            mapView.removeAnnotation(ann)
            mapView.addAnnotation(ann)
        }
    }

    private fun formatPrice(price: Double): String {
        val cents = (price * 100).toLong()
        return "€${cents / 100}.${(cents % 100).toString().padStart(2, '0')}"
    }
}
