package com.amitshilo.menudeldia.ui.map.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.amitshilo.menudeldia.domain.model.Restaurant
import com.amitshilo.menudeldia.ui.preview.previewRestaurant
import com.amitshilo.menudeldia.ui.preview.previewRestaurantNoMenu
import com.amitshilo.menudeldia.ui.theme.MenuTheme
import com.amitshilo.menudeldia.util.format
import com.amitshilo.menudeldia.util.isCurrentlyOpen
import menudeldia.composeapp.generated.resources.Res
import menudeldia.composeapp.generated.resources.closed_now
import menudeldia.composeapp.generated.resources.daily_menu
import menudeldia.composeapp.generated.resources.open_now
import org.jetbrains.compose.resources.stringResource

@Composable
fun RestaurantCard(
    restaurant: Restaurant,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 1.dp),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Thumbnail(
                thumbnailUrl = restaurant.thumbnailUrl,
                contentDescription = restaurant.name,
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(8.dp)),
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = restaurant.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = restaurant.address,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.padding(top = 4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OpenBadge(isOpen = restaurant.isCurrentlyOpen())
                    restaurant.cuisineType?.let { cuisine ->
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = cuisine,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    if (restaurant.servesVegetarianFood) {
                        Spacer(Modifier.width(6.dp))
                        Text(text = "🌱", fontSize = 12.sp)
                    }
                    if (restaurant.isGlutenFreeFriendly) {
                        Spacer(Modifier.width(6.dp))
                        Text(text = "🌾", fontSize = 12.sp)
                    }
                    restaurant.distanceMeters?.let { dist ->
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "·  " + if (dist < 1000) "${dist.toInt()}m"
                            else "${(dist / 1000.0).format(1)}km",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                val menuPrice = restaurant.menuPrice
                if (restaurant.todayHasMenu && menuPrice != null) {
                    Spacer(Modifier.padding(top = 4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stringResource(Res.string.daily_menu),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "€${menuPrice.format(2)}",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Thumbnail(
    thumbnailUrl: String?,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    if (thumbnailUrl != null) {
        AsyncImage(
            model = thumbnailUrl,
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = ContentScale.Crop,
        )
    } else {
        Box(
            modifier = modifier.background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = "🍽", fontSize = 24.sp)
        }
    }
}

@Composable
private fun OpenBadge(isOpen: Boolean) {
    val bg = if (isOpen) MaterialTheme.colorScheme.tertiaryContainer
    else MaterialTheme.colorScheme.surfaceVariant
    val fg = if (isOpen) MaterialTheme.colorScheme.onTertiaryContainer
    else MaterialTheme.colorScheme.onSurfaceVariant
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 2.dp),
    ) {
        Text(
            text = stringResource(if (isOpen) Res.string.open_now else Res.string.closed_now),
            style = MaterialTheme.typography.labelSmall,
            color = fg,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

// ── Previews ────────────────────────────────────────────────────────────────

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
        RestaurantCard(restaurant = previewRestaurantNoMenu, isSelected = false, onClick = {})
    }
}
