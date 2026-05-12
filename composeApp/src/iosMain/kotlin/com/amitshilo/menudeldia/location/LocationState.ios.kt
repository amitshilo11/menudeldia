package com.amitshilo.menudeldia.location

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.CoreLocation.CLAuthorizationStatus
import platform.CoreLocation.CLLocation
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse
import platform.CoreLocation.kCLLocationAccuracyHundredMeters
import platform.Foundation.NSError
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun rememberLocationState(): LocationState {
    var hasPermission by remember { mutableStateOf(false) }
    var location by remember { mutableStateOf<UserLocation?>(null) }

    val manager = remember { CLLocationManager() }
    val delegate = remember {
        object : NSObject(), CLLocationManagerDelegateProtocol {
            override fun locationManager(manager: CLLocationManager, didUpdateLocations: List<*>) {
                val last = didUpdateLocations.lastOrNull() as? CLLocation ?: return
                last.coordinate.useContents {
                    location = UserLocation(lat = latitude, lng = longitude)
                }
            }

            override fun locationManager(manager: CLLocationManager, didFailWithError: NSError) {
                // Swallow — we render a "no location" fallback in the UI when location is null.
            }

            override fun locationManager(
                manager: CLLocationManager,
                didChangeAuthorizationStatus: CLAuthorizationStatus,
            ) {
                val granted =
                    didChangeAuthorizationStatus == kCLAuthorizationStatusAuthorizedWhenInUse ||
                            didChangeAuthorizationStatus == kCLAuthorizationStatusAuthorizedAlways
                hasPermission = granted
                if (granted) manager.startUpdatingLocation()
            }
        }
    }

    LaunchedEffect(Unit) {
        manager.delegate = delegate
        manager.desiredAccuracy = kCLLocationAccuracyHundredMeters
        manager.requestWhenInUseAuthorization()
    }

    DisposableEffect(Unit) {
        onDispose {
            manager.stopUpdatingLocation()
            manager.delegate = null
        }
    }

    return LocationState(hasPermission = hasPermission, location = location)
}
