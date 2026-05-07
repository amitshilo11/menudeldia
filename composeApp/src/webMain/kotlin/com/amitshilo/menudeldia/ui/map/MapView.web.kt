package com.amitshilo.menudeldia.ui.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.amitshilo.menudeldia.domain.model.Restaurant

actual @Composable fun MapView(
    restaurants: List<Restaurant>,
    selectedRestaurantId: String?,
    onRestaurantSelected: (String) -> Unit,
    modifier: Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        Text("Map — Web coming soon", style = MaterialTheme.typography.bodyLarge)
    }
}
