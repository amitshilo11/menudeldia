package com.amitshilo.menudeldia.ui.map.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.amitshilo.menudeldia.domain.model.Restaurant
import com.amitshilo.menudeldia.ui.preview.previewRestaurant
import com.amitshilo.menudeldia.ui.preview.previewRestaurantNoMenu
import com.amitshilo.menudeldia.ui.theme.MenuTheme

private val cardShape =
    RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp, bottomStart = 20.dp, bottomEnd = 20.dp)

@Composable
fun RestaurantDetailCard(
    restaurant: Restaurant,
    onDismiss: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        shape = cardShape,
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
    ) {
        Column {
            DetailCardPhotoSection(
                restaurant = restaurant,
                onDismiss = onDismiss,
            )
            DetailCardInfoSection(
                restaurant = restaurant,
                onNavigateToDetail = onNavigateToDetail,
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun PreviewDetailCardOpen() {
    MenuTheme {
        RestaurantDetailCard(
            restaurant = previewRestaurant,
            onDismiss = {},
            onNavigateToDetail = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun PreviewDetailCardClosed() {
    MenuTheme {
        RestaurantDetailCard(
            restaurant = previewRestaurantNoMenu,
            onDismiss = {},
            onNavigateToDetail = {},
        )
    }
}
