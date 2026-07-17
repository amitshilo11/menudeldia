package com.amitshilo.menudeldia.ui.map.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.amitshilo.menudeldia.domain.model.Restaurant
import com.amitshilo.menudeldia.ui.preview.previewRestaurant
import com.amitshilo.menudeldia.ui.preview.previewRestaurantNoMenu
import com.amitshilo.menudeldia.ui.theme.MenuTheme
import com.amitshilo.menudeldia.util.format
import com.amitshilo.menudeldia.util.rememberUriLauncher
import com.amitshilo.menudeldia.util.walkingDirectionsUri
import menudeldia.composeapp.generated.resources.Res
import menudeldia.composeapp.generated.resources.call_restaurant
import menudeldia.composeapp.generated.resources.get_directions
import menudeldia.composeapp.generated.resources.info
import menudeldia.composeapp.generated.resources.menu_del_dia_includes
import menudeldia.composeapp.generated.resources.more_info
import menudeldia.composeapp.generated.resources.my_location
import menudeldia.composeapp.generated.resources.phone
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun DetailCardInfoSection(
    restaurant: Restaurant,
    onNavigateToDetail: (String) -> Unit,
) {
    val uriLauncher = rememberUriLauncher()

    Column(modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp)) {
        NameDistanceRow(restaurant)
        Spacer(Modifier.height(4.dp))
        StarRatingRow(restaurant)
        Spacer(Modifier.height(10.dp))
        FeatureChipsRow(restaurant)
        Spacer(Modifier.height(12.dp))
        MenuIncludesSection(restaurant)
        Spacer(Modifier.height(12.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
        Spacer(Modifier.height(12.dp))
        ActionButtonRow(
            restaurant = restaurant,
            onDirections = {
                uriLauncher.open(walkingDirectionsUri(restaurant.lat, restaurant.lng))
            },
            onCall = { phone -> uriLauncher.open("tel:$phone") },
            onMoreInfo = { onNavigateToDetail(restaurant.id) },
        )
    }
}

@Composable
private fun NameDistanceRow(restaurant: Restaurant) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Text(
            text = restaurant.name,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        restaurant.distanceMeters?.let { meters ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(Res.drawable.my_location),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(14.dp),
                )
                Spacer(Modifier.width(3.dp))
                Text(
                    text = "${(meters / 1000.0).format(1)} km",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun StarRatingRow(restaurant: Restaurant) {
    val rating = restaurant.rating ?: return
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        val full = rating.toInt().coerceIn(0, 5)
        repeat(full) {
            Text(
                "★",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        repeat(5 - full) {
            Text(
                "☆",
                color = MaterialTheme.colorScheme.outlineVariant,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Spacer(Modifier.width(4.dp))
        Text(
            text = buildString {
                append(rating.format(1))
                restaurant.userRatingCount?.let { append(" · $it ratings") }
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun FeatureChipsRow(restaurant: Restaurant) {
    val chips = buildList {
        restaurant.cuisineType?.let { add("${restaurant.cuisineEmoji ?: "🍽"} $it") }
        if (restaurant.servesVegetarianFood) add("🌱 Vegan options")
        if (restaurant.outdoorSeating) add("☀️ Outdoor")
        if (restaurant.reservable) add("📅 Reservable")
        if (restaurant.takeout) add("🥡 Takeout")
    }
    if (chips.isEmpty()) return
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.horizontalScroll(rememberScrollState()),
    ) {
        chips.forEach { label ->
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                )
            }
        }
    }
}

@Composable
private fun MenuIncludesSection(restaurant: Restaurant) {
    if (restaurant.menuIncludes.isEmpty()) return
    Text(
        text = stringResource(Res.string.menu_del_dia_includes),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.SemiBold,
    )
    Spacer(Modifier.height(6.dp))
    Text(
        text = restaurant.menuIncludes.joinToString("  ·  ") { menuItemLabel(it) },
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.SemiBold,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

private fun menuItemLabel(item: String) = when (item.lowercase()) {
    "starter", "entrante", "primer" -> "✓ Starter"
    "main", "principal", "segundo" -> "✓ Main"
    "dessert", "postre" -> "☕ Dessert"
    "drink", "bebida" -> "🥤 Drink"
    "coffee", "café" -> "☕ Coffee"
    "bread", "pan" -> "🍞 Bread"
    else -> "✓ $item"
}

@Composable
private fun ActionButtonRow(
    restaurant: Restaurant,
    onDirections: () -> Unit,
    onCall: (String) -> Unit,
    onMoreInfo: () -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Button(
            onClick = onDirections,
            modifier = Modifier.weight(1f).height(52.dp),
            shape = RoundedCornerShape(16.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
        ) {
            Icon(painterResource(Res.drawable.my_location), null, Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                stringResource(Res.string.get_directions),
                style = MaterialTheme.typography.labelLarge
            )
        }
        restaurant.phone?.let { ph ->
            FilledTonalButton(
                onClick = { onCall(ph) },
                modifier = Modifier.size(52.dp),
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(0.dp),
            ) {
                Icon(
                    painterResource(Res.drawable.phone),
                    stringResource(Res.string.call_restaurant),
                    Modifier.size(20.dp)
                )
            }
        }
        FilledTonalButton(
            onClick = onMoreInfo,
            modifier = Modifier.size(52.dp),
            shape = RoundedCornerShape(16.dp),
            contentPadding = PaddingValues(0.dp),
        ) {
            Icon(
                painterResource(Res.drawable.info),
                stringResource(Res.string.more_info),
                Modifier.size(20.dp)
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun PreviewInfoSectionOpen() {
    MenuTheme {
        Surface(color = MaterialTheme.colorScheme.surfaceContainerLow) {
            DetailCardInfoSection(restaurant = previewRestaurant, onNavigateToDetail = {})
        }
    }
}

@PreviewLightDark
@Composable
private fun PreviewInfoSectionNoMenu() {
    MenuTheme {
        Surface(color = MaterialTheme.colorScheme.surfaceContainerLow) {
            DetailCardInfoSection(restaurant = previewRestaurantNoMenu, onNavigateToDetail = {})
        }
    }
}
