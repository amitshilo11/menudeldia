package com.amitshilo.menudeldia.ui.map.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.SubcomposeAsyncImage
import com.amitshilo.menudeldia.ui.designsystem.component.ShimmerBone
import com.amitshilo.menudeldia.ui.designsystem.component.menuShimmer
import com.amitshilo.menudeldia.ui.designsystem.component.rememberMenuShimmer
import com.amitshilo.menudeldia.domain.model.Restaurant
import com.amitshilo.menudeldia.ui.preview.previewRestaurant
import com.amitshilo.menudeldia.ui.preview.previewRestaurantNoMenu
import com.amitshilo.menudeldia.ui.theme.MenuTheme
import com.amitshilo.menudeldia.util.format
import com.amitshilo.menudeldia.util.isCurrentlyOpen
import com.amitshilo.menudeldia.util.opensAtToday
import com.amitshilo.menudeldia.util.todayHours
import kotlinx.datetime.LocalTime
import menudeldia.composeapp.generated.resources.Res
import menudeldia.composeapp.generated.resources.no_menu_today_short
import menudeldia.composeapp.generated.resources.open_now
import menudeldia.composeapp.generated.resources.opens_at
import org.jetbrains.compose.resources.stringResource

@Composable
fun RestaurantCard(
    restaurant: Restaurant,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isOpen = restaurant.isCurrentlyOpen()
    val closeTime = todayHours(restaurant.openingHours)?.closeTime
    val opensAt = if (!isOpen) restaurant.opensAtToday() else null

    Card(
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 3.dp else 1.dp),
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
            ) {
                Thumbnail(
                    thumbnailUrl = restaurant.thumbnailUrl,
                    contentDescription = restaurant.name,
                    modifier = Modifier.matchParentSize(),
                )
                if (restaurant.todayHasMenu) {
                    restaurant.menuPrice?.let { price ->
                        CardPriceBadge(
                            price = price,
                            modifier = Modifier.align(Alignment.TopEnd).padding(8.dp),
                        )
                    }
                }
                CardChipsRow(
                    restaurant = restaurant,
                    modifier = Modifier.align(Alignment.BottomStart).padding(8.dp),
                )
            }
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = restaurant.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f).padding(end = 8.dp),
                    )
                    if (!restaurant.todayHasMenu) {
                        NoMenuBadge()
                    }
                }
                Spacer(Modifier.height(4.dp))
                InfoRow(restaurant, isOpen, closeTime, opensAt)
            }
        }
    }
}

@Composable
private fun CardPriceBadge(price: Double, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier,
    ) {
        Text(
            text = "€${price.format(2)}",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
        )
    }
}

@Composable
private fun NoMenuBadge() {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Text(
            text = stringResource(Res.string.no_menu_today_short),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
        )
    }
}

@Composable
private fun InfoRow(restaurant: Restaurant, isOpen: Boolean, closeTime: LocalTime?, opensAt: LocalTime?) {
    val statusText: String? = when {
        isOpen && closeTime != null ->
            "Open · ${closeTime.hour.toString().padStart(2, '0')}:${
                closeTime.minute.toString().padStart(2, '0')
            }"
        isOpen -> stringResource(Res.string.open_now)
        opensAt != null -> stringResource(
            Res.string.opens_at,
            "${opensAt.hour.toString().padStart(2, '0')}:${opensAt.minute.toString().padStart(2, '0')}",
        )
        else -> null
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        restaurant.rating?.let { rating ->
            Text(
                "★",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = " ${rating.format(1)}",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            restaurant.userRatingCount?.let { count ->
                Text(
                    text = " ($count)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (restaurant.distanceMeters != null || statusText != null) {
                Text(
                    " · ",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outlineVariant
                )
            }
        }
        restaurant.distanceMeters?.let { meters ->
            Text(
                text = "${(meters / 1000.0).format(1)} km",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (statusText != null) {
                Text(
                    " · ",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outlineVariant
                )
            }
        }
        if (statusText != null) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .background(
                        color = if (isOpen) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.outlineVariant,
                        shape = CircleShape,
                    ),
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = statusText,
                style = MaterialTheme.typography.bodySmall,
                color = if (isOpen) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun CardChipsRow(restaurant: Restaurant, modifier: Modifier = Modifier) {
    val chips = buildList {
        restaurant.cuisineType?.let { add("${restaurant.cuisineEmoji ?: "🍽"} $it") }
        if (restaurant.servesVegetarianFood) add("🌱 Vegan")
    }
    if (chips.isEmpty()) return
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        chips.forEach { label ->
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                )
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
        val shimmer = rememberMenuShimmer()
        SubcomposeAsyncImage(
            model = thumbnailUrl,
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = ContentScale.Crop,
            loading = {
                ShimmerBone(
                    modifier = Modifier.fillMaxSize().menuShimmer(shimmer),
                    shape = RoundedCornerShape(0.dp),
                )
            },
        )
    } else {
        Box(
            modifier = modifier.background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = "🍽", fontSize = 28.sp)
        }
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
        RestaurantCard(
            restaurant = previewRestaurantNoMenu,
            isSelected = false,
            onClick = {},
        )
    }
}
