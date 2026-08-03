package com.amitshilo.menudeldia.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource

@SuppressLint("MissingPermission")
@Composable
actual fun rememberLocationState(): LocationState {
    val context = LocalContext.current

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    var location by remember { mutableStateOf<UserLocation?>(null) }

    val fusedClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasPermission = granted }

    LaunchedEffect(hasPermission) {
        if (!hasPermission) return@LaunchedEffect
        // The cached fix lands instantly, so the map has something to centre on — but it
        // can be hours old and kilometres away, so it only fills the gap until a real fix
        // arrives. Distances are measured from whichever of the two is current.
        fusedClient.lastLocation.addOnSuccessListener { loc ->
            if (location == null) {
                loc?.let { location = UserLocation(it.latitude, it.longitude) }
            }
        }
        fusedClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            CancellationTokenSource().token,
        ).addOnSuccessListener { loc ->
            loc?.let { location = UserLocation(it.latitude, it.longitude) }
        }
    }

    return LocationState(
        hasPermission = hasPermission,
        location = location,
        requestPermission = { launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION) },
    )
}
