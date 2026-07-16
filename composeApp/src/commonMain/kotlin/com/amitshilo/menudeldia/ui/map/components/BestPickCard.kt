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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.SubcomposeAsyncImage
import com.amitshilo.menudeldia.domain.model.Restaurant
import com.amitshilo.menudeldia.ui.designsystem.component.menuShimmer
import com.amitshilo.menudeldia.ui.designsystem.component.rememberMenuShimmer
import com.amitshilo.menudeldia.util.format
import menudeldia.composeapp.generated.resources.Res
import menudeldia.composeapp.generated.resources.ic_distance
import menudeldia.composeapp.generated.resources.ic_hotel_class
import menudeldia.composeapp.generated.resources.ic_money_bag
import menudeldia.composeapp.generated.resources.menu_del_dia_includes
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

// ── Pick-type accent colours ──────────────────────────────────────────────────
private val AmberPick = Color(0xFFF5A623)
private val GreenPick = Color(0xFF4CAF50)
private val PurplePick = Color(0xFF7B61FF)

internal enum class PickType(val color: Color, val label: String, val icon: DrawableResource) {
    BestRated(AmberPick, "BEST RATED", Res.drawable.ic_hotel_class),
    BestPrice(GreenPick, "BEST PRICE", Res.drawable.ic_money_bag),
    Closest(PurplePick, "CLOSEST", Res.drawable.ic_distance),
}

internal fun pickTypeAt(index: Int) = when (index) {
    0 -> PickType.BestRated
    1 -> PickType.BestPrice
    else -> PickType.Closest
}

// ── Pick card ─────────────────────────────────────────────────────────────────

@Composable
internal fun BestPickCard(
    restaurant: Restaurant,
    pickType: PickType,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column {
            // Coloured top stripe
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(pickType.color),
            )
            // Photo with all overlays
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
            ) {
                PickPhoto(restaurant)

                // Top-left: single pill chip
                PickBadge(
                    pickType = pickType,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp),
                )

                // Top-right: price
                restaurant.menuPrice?.let { price ->
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(8.dp),
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

                // Bottom-left: rating + distance
                val hasRating = restaurant.rating != null
                val hasDistance = restaurant.distanceMeters != null
                if (hasRating || hasDistance) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(8.dp)
                            .background(Color.Black.copy(alpha = 0.45f), RoundedCornerShape(999.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        restaurant.rating?.let { rating ->
                            Text("★", style = MaterialTheme.typography.bodySmall, color = AmberPick)
                            Text(
                                text = " ${rating.format(1)}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                            )
                        }
                        restaurant.distanceMeters?.let { meters ->
                            val dist = if (meters < 1000) "${meters.toInt()} m" else "${
                                (meters / 1000.0).format(1)
                            } km"
                            Text(
                                text = " · $dist",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.85f),
                            )
                        }
                    }
                }
            }
            // Name below the photo
            Text(
                text = restaurant.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            )
            if (restaurant.menuIncludes.isNotEmpty()) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                MenuIncludesRow(
                    restaurant = restaurant,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                )
            }
        }
    }
}

@Composable
private fun MenuIncludesRow(restaurant: Restaurant, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text = stringResource(Res.string.menu_del_dia_includes),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = restaurant.menuIncludes.joinToString("  ·  ") { menuItemLabel(it) },
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

// ── Badge ─────────────────────────────────────────────────────────────────────

@Composable
private fun PickBadge(pickType: PickType, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = pickType.color,
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Icon(
                painter = painterResource(pickType.icon),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(14.dp),
            )
            Text(
                text = pickType.label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
        }
    }
}

// ── Photo ─────────────────────────────────────────────────────────────────────

@Composable
private fun PickPhoto(restaurant: Restaurant) {
    if (restaurant.thumbnailUrl != null) {
        val shimmer = rememberMenuShimmer()
        SubcomposeAsyncImage(
            model = restaurant.thumbnailUrl,
            contentDescription = restaurant.name,
            modifier = Modifier.fillMaxSize(),
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
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Text("🍽", fontSize = 28.sp)
        }
    }
}
