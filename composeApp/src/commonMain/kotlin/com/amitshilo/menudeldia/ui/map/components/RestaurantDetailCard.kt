package com.amitshilo.menudeldia.ui.map.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import menudeldia.composeapp.generated.resources.close
import menudeldia.composeapp.generated.resources.closed_now
import menudeldia.composeapp.generated.resources.daily_menu
import menudeldia.composeapp.generated.resources.no_menu_today_short
import menudeldia.composeapp.generated.resources.open_now
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

private val cardShape = RoundedCornerShape(20.dp)

@Composable
fun RestaurantDetailCard(
    restaurant: Restaurant,
    onDismiss: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        shape = cardShape,
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable { onNavigateToDetail(restaurant.id) },
    ) {
        Column {
            PhotoSection(restaurant = restaurant)
            InfoSection(restaurant = restaurant, onDismiss = onDismiss)
        }
    }
}

@Composable
private fun PhotoSection(restaurant: Restaurant) {
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
            modifier = Modifier.align(Alignment.TopEnd).padding(12.dp),
        )
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
private fun InfoSection(restaurant: Restaurant, onDismiss: () -> Unit) {
    Column(modifier = Modifier.padding(start = 16.dp, end = 4.dp, top = 12.dp, bottom = 12.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.weight(1f)) {
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
            }
            IconButton(onClick = onDismiss) {
                Icon(
                    painter = painterResource(Res.drawable.close),
                    contentDescription = stringResource(Res.string.close),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(end = 12.dp),
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
                val priceStr = menuPrice.format(2)
                Text(
                    text = "${stringResource(Res.string.daily_menu)}  €$priceStr",
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
