package com.amitshilo.menudeldia.ui.map.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import com.amitshilo.menudeldia.util.rememberUriLauncher
import com.amitshilo.menudeldia.util.walkingDirectionsUri
import menudeldia.composeapp.generated.resources.Res
import menudeldia.composeapp.generated.resources.call_restaurant
import menudeldia.composeapp.generated.resources.close
import menudeldia.composeapp.generated.resources.closed_now
import menudeldia.composeapp.generated.resources.daily_menu
import menudeldia.composeapp.generated.resources.get_directions
import menudeldia.composeapp.generated.resources.info
import menudeldia.composeapp.generated.resources.more_info
import menudeldia.composeapp.generated.resources.my_location
import menudeldia.composeapp.generated.resources.no_menu_today_short
import menudeldia.composeapp.generated.resources.open_now
import menudeldia.composeapp.generated.resources.phone
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

private val cardShape =
    RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp, bottomStart = 20.dp, bottomEnd = 20.dp)

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
            PhotoSection(restaurant = restaurant, onDismiss = onDismiss)
            InfoSection(
                restaurant = restaurant,
                onNavigateToDetail = onNavigateToDetail,
            )
        }
    }
}

@Composable
private fun PhotoSection(restaurant: Restaurant, onDismiss: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .padding(12.dp)
            .clip(cardShape),
    ) {
        if (restaurant.thumbnailUrl != null) {
            AsyncImage(
                model = restaurant.thumbnailUrl,
                contentDescription = restaurant.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                contentScale = ContentScale.Crop,
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) { Text(text = "🍽", fontSize = 64.sp) }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.5f)),
                    ),
                ),
        )

        restaurant.rating?.let { rating ->
            RatingBadge(
                rating = rating,
                modifier = Modifier.align(Alignment.TopStart).padding(12.dp),
            )
        }

        OpenStatusBadge(
            isOpen = restaurant.isCurrentlyOpen(),
            hasMenuToday = restaurant.todayHasMenu,
            modifier = Modifier.align(Alignment.BottomStart).padding(12.dp),
        )

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(10.dp)
                .size(34.dp)
                .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                .clip(CircleShape)
                .clickable { onDismiss() },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(Res.drawable.close),
                contentDescription = stringResource(Res.string.close),
                tint = Color.White,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@Composable
private fun RatingBadge(rating: Double, modifier: Modifier = Modifier) {
    Surface(
        shape = cardShape,
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
        ) {
            Text(
                text = rating.format(1),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(text = "★", color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun OpenStatusBadge(isOpen: Boolean, hasMenuToday: Boolean, modifier: Modifier = Modifier) {
    val label = when {
        isOpen -> stringResource(Res.string.open_now)
        !hasMenuToday -> stringResource(Res.string.no_menu_today_short)
        else -> stringResource(Res.string.closed_now)
    }
    val bgColor =
        if (isOpen) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.surfaceVariant
    val textColor =
        if (isOpen) MaterialTheme.colorScheme.onTertiary else MaterialTheme.colorScheme.onSurfaceVariant
    Surface(
        shape = cardShape,
        color = bgColor,
        modifier = modifier,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = textColor,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
        )
    }
}

@Composable
private fun InfoSection(restaurant: Restaurant, onNavigateToDetail: (String) -> Unit) {
    val uriLauncher = rememberUriLauncher()

    Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 12.dp)) {
        Text(
            text = restaurant.name,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
        )
        Text(
            text = restaurant.address,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
        )

        Spacer(Modifier.height(8.dp))
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
            thickness = 1.dp
        )
        Spacer(Modifier.height(6.dp))

        // One combined row: "🥘 Catalana  ·  Starter  ·  Main  ·  Dessert  ·  Drink"
        val infoLine = (
                listOfNotNull(
                    buildString {
                        restaurant.cuisineEmoji?.let { append("$it ") }
                        restaurant.cuisineType?.let { append(it) }
                    }.trim().takeIf { it.isNotEmpty() }
                ) + restaurant.menuIncludes
                ).joinToString("  ·  ")

        if (infoLine.isNotEmpty()) {
            Text(
                text = infoLine,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(6.dp))
        }

        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
            thickness = 1.dp
        )
        Spacer(Modifier.height(8.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            restaurant.distanceMeters?.let { meters ->
                Text(
                    text = "${(meters / 1000.0).format(1)} km",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.width(8.dp))
            }

            Spacer(Modifier.weight(1f))

            val menuPrice = restaurant.menuPrice
            if (restaurant.todayHasMenu && menuPrice != null) {
                Text(
                    text = "${stringResource(Res.string.daily_menu)}  €${menuPrice.format(2)}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                )
            } else if (!restaurant.todayHasMenu) {
                Text(
                    text = stringResource(Res.string.no_menu_today_short),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        // Navigate + Call + More Info buttons (uniform OutlinedButton style)
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            FilledTonalButton(
                onClick = {
                    uriLauncher.open(
                        walkingDirectionsUri(
                            restaurant.lat,
                            restaurant.lng
                        )
                    )
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp),
            ) {
                Icon(
                    painter = painterResource(Res.drawable.my_location),
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = stringResource(Res.string.get_directions),
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                )
            }
            restaurant.phone?.let { ph ->
                OutlinedButton(
                    onClick = { uriLauncher.open("tel:$ph") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp),
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.phone),
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = stringResource(Res.string.call_restaurant),
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                    )
                }
            }
            OutlinedButton(
                onClick = { onNavigateToDetail(restaurant.id) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp),
            ) {
                Icon(
                    painter = painterResource(Res.drawable.info),
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = stringResource(Res.string.more_info),
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                )
            }
        }
    }
}

// ── Previews ────────────────────────────────────────────────────────────────

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
