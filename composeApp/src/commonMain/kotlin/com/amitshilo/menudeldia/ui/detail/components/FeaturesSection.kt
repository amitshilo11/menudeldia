package com.amitshilo.menudeldia.ui.detail.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.amitshilo.menudeldia.domain.model.Restaurant
import menudeldia.composeapp.generated.resources.Res
import menudeldia.composeapp.generated.resources.feature_outdoor_seating
import menudeldia.composeapp.generated.resources.feature_reservable
import menudeldia.composeapp.generated.resources.feature_takeout
import menudeldia.composeapp.generated.resources.feature_vegetarian
import menudeldia.composeapp.generated.resources.features_header
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FeaturesSection(restaurant: Restaurant, modifier: Modifier = Modifier) {
    val features = buildList {
        if (restaurant.servesVegetarianFood) add("🌱" to Res.string.feature_vegetarian)
        if (restaurant.outdoorSeating) add("🌳" to Res.string.feature_outdoor_seating)
        if (restaurant.reservable) add("📅" to Res.string.feature_reservable)
        if (restaurant.takeout) add("📦" to Res.string.feature_takeout)
    }
    if (features.isEmpty()) return

    Spacer(Modifier.height(24.dp))
    SectionHeader(stringResource(Res.string.features_header))
    Spacer(Modifier.height(8.dp))
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        features.forEach { (icon, labelRes) ->
            FeatureChip(text = stringResource(labelRes), icon = icon)
        }
    }
}
