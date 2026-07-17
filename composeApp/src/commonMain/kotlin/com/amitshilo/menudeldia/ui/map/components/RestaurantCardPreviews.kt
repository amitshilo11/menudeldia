package com.amitshilo.menudeldia.ui.map.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.amitshilo.menudeldia.ui.preview.previewRestaurant
import com.amitshilo.menudeldia.ui.preview.previewRestaurantNoMenu
import com.amitshilo.menudeldia.ui.theme.MenuTheme

@PreviewLightDark
@Composable
private fun PreviewRestaurantCardSelected() {
    MenuTheme { RestaurantCard(restaurant = previewRestaurant, isSelected = true, onClick = {}) }
}

@PreviewLightDark
@Composable
private fun PreviewRestaurantCardDefault() {
    MenuTheme { RestaurantCard(restaurant = previewRestaurant, isSelected = false, onClick = {}) }
}

@PreviewLightDark
@Composable
private fun PreviewRestaurantCardNoMenu() {
    MenuTheme {
        RestaurantCard(
            restaurant = previewRestaurantNoMenu,
            isSelected = false,
            onClick = {},
        )
    }
}
