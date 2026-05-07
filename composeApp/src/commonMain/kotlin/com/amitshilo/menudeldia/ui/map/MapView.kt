package com.amitshilo.menudeldia.ui.map

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.amitshilo.menudeldia.domain.model.Restaurant

expect @Composable fun MapView(
    restaurants: List<Restaurant>,
    selectedRestaurantId: String?,
    onRestaurantSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
)
