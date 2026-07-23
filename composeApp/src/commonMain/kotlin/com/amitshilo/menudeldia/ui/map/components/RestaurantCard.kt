package com.amitshilo.menudeldia.ui.map.components

import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.SubcomposeAsyncImage
import com.amitshilo.menudeldia.domain.model.Restaurant
import com.amitshilo.menudeldia.ui.designsystem.component.menuShimmer
import com.amitshilo.menudeldia.ui.designsystem.component.rememberMenuShimmer
import com.amitshilo.menudeldia.ui.preview.previewRestaurant
import com.amitshilo.menudeldia.ui.preview.previewRestaurantNoMenu
import com.amitshilo.menudeldia.ui.theme.MenuTheme
import com.amitshilo.menudeldia.util.format
import com.amitshilo.menudeldia.util.isCurrentlyOpen
import com.amitshilo.menudeldia.util.opensAtToday
import com.amitshilo.menudeldia.util.todayHours
import kotlinx.datetime.LocalTime
import menudeldia.composeapp.generated.resources.Res
import menudeldia.composeapp.generated.resources.ic_directions_walk
import menudeldia.composeapp.generated.resources.no_menu_today_short
import menudeldia.composeapp.generated.resources.open_now
import menudeldia.composeapp.generated.resources.opens_at
import org.jetbrains.compose.resources.painterResource
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
    val elevation by animateDpAsState(if (isSelected) 3.dp else 1.dp)

    Card(
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
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
                restaurant.rating?.let { rating ->
                    RatingBadge(
                        rating = rating,
                        userRatingCount = restaurant.userRatingCount,
                        modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp),
                    )
                }
                CardChipsRow(
                    restaurant = restaurant,
                    modifier = Modifier.align(Alignment.BottomStart).padding(8.dp),
                )
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.align(Alignment.TopEnd).padding(8.dp),
                ) {
                    if (!restaurant.todayHasMenu) {
                        NoMenuBadge()
                    }
                    if (isOpen || opensAt != null) {
                        if (!restaurant.todayHasMenu) {
                            Spacer(Modifier.height(6.dp))
                        }
                        StatusBadge(isOpen, closeTime, opensAt)
                    }
                }
            }
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = restaurant.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    restaurant.distanceMeters?.let { meters ->
                        DistanceLabel(meters)
                    }
                }
                if (restaurant.menuIncludes.isNotEmpty()) {
                    Spacer(Modifier.height(10.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                    Spacer(Modifier.height(10.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        MenuIncludesRow(restaurant, modifier = Modifier.weight(1f))
                        if (restaurant.todayHasMenu) {
                            restaurant.menuPrice?.let { price ->
                                Spacer(Modifier.width(8.dp))
                                PriceText(price = price)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PriceText(price: Double, modifier: Modifier = Modifier) {
    Text(
        text = "€${price.format(2)}",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier,
    )
}

@Composable
private fun NoMenuBadge() {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = Color.Black.copy(alpha = 0.45f),
    ) {
        Text(
            text = stringResource(Res.string.no_menu_today_short),
            style = MaterialTheme.typography.labelMedium,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
        )
    }
}

@Composable
private fun StatusBadge(isOpen: Boolean, closeTime: LocalTime?, opensAt: LocalTime?) {
    val statusText = when {
        isOpen && closeTime != null ->
            "Open · ${closeTime.hour.toString().padStart(2, '0')}:${
                closeTime.minute.toString().padStart(2, '0')
            }"
        isOpen -> stringResource(Res.string.open_now)
        opensAt != null -> stringResource(
            Res.string.opens_at,
            "${opensAt.hour.toString().padStart(2, '0')}:${opensAt.minute.toString().padStart(2, '0')}",
        )
        else -> return
    }
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = if (isOpen) MaterialTheme.colorScheme.tertiary else Color.Black.copy(alpha = 0.45f),
    ) {
        Text(
            text = statusText,
            style = MaterialTheme.typography.labelMedium,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
        )
    }
}

@Composable
private fun RatingBadge(rating: Double, userRatingCount: Int?, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = Color.Black.copy(alpha = 0.45f),
        modifier = modifier,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        ) {
            Text(
                "★",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = " ${rating.format(1)}",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
            )
            userRatingCount?.let { count ->
                Text(
                    text = " ($count)",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.85f),
                )
            }
        }
    }
}

@Composable
private fun DistanceLabel(meters: Double, modifier: Modifier = Modifier) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        Icon(
            painter = painterResource(Res.drawable.ic_directions_walk),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(14.dp),
        )
        Spacer(Modifier.width(2.dp))
        Text(
            text = "${(meters / 1000.0).format(1)} km",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
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
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .menuShimmer(shimmer)
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest),
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
