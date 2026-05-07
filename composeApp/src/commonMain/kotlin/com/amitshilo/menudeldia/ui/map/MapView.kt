package com.amitshilo.menudeldia.ui.map

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.amitshilo.menudeldia.domain.model.Restaurant
import com.amitshilo.menudeldia.location.UserLocation

expect @Composable fun MapView(
    restaurants: List<Restaurant>,
    selectedRestaurantId: String?,
    userLocation: UserLocation?,
    isLocationEnabled: Boolean,
    recenterTrigger: Int,
    onRestaurantSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
)
