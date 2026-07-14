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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
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
import com.amitshilo.menudeldia.ui.preview.previewRestaurants
import com.amitshilo.menudeldia.ui.theme.MenuTheme
import com.amitshilo.menudeldia.util.format
import menudeldia.composeapp.generated.resources.Res
import menudeldia.composeapp.generated.resources.best_picks_title
import menudeldia.composeapp.generated.resources.ic_distance
import menudeldia.composeapp.generated.resources.ic_hotel_class
import menudeldia.composeapp.generated.resources.ic_money_bag
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

// ── Pick-type accent colours ──────────────────────────────────────────────────
private val AmberPick = Color(0xFFF5A623)
private val GreenPick = Color(0xFF4CAF50)
private val PurplePick = Color(0xFF7B61FF)

private enum class PickType(val color: Color, val label: String, val icon: DrawableResource) {
    BestRated(AmberPick, "BEST RATED", Res.drawable.ic_hotel_class),
    BestPrice(GreenPick, "BEST PRICE", Res.drawable.ic_money_bag),
    Closest(PurplePick, "CLOSEST", Res.drawable.ic_distance),
}

private fun pickTypeAt(index: Int) = when (index) {
    0 -> PickType.BestRated
    1 -> PickType.BestPrice
    else -> PickType.Closest
}

// ── Sheet ─────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BestPicksSheet(
    picks: List<Restaurant>,
    onDismiss: () -> Unit,
    onPickTap: (String) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        BestPicksContent(
            picks = picks,
            onPickTap = { id ->
                onDismiss()
                onPickTap(id)
            },
        )
    }
}

// ── Content ───────────────────────────────────────────────────────────────────

@Composable
private fun BestPicksContent(
    picks: List<Restaurant>,
    onPickTap: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
    ) {
        Text(
            text = stringResource(Res.string.best_picks_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "${picks.size} great options, every day near you.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(20.dp))
        picks.forEachIndexed { index, restaurant ->
            BestPickCard(
                restaurant = restaurant,
                pickType = pickTypeAt(index),
                onClick = { onPickTap(restaurant.id) },
                modifier = Modifier.padding(bottom = 12.dp),
            )
        }
        Spacer(Modifier.height(4.dp))
        UpdatedFooter()
        Spacer(Modifier.navigationBarsPadding().height(16.dp))
    }
}

// ── Pick card ─────────────────────────────────────────────────────────────────

@Composable
private fun BestPickCard(
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
        }
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

// ── Footer ────────────────────────────────────────────────────────────────────

@Composable
private fun UpdatedFooter() {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // TODO: Icon(painterResource(Res.drawable.clock), tint = primary, modifier = Modifier.size(20.dp))
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text("⏰", fontSize = 10.sp)
            }
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Updated daily at 11:00",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "Fresh deals from real restaurants",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            // TODO: Icon(painterResource(Res.drawable.sparkle), tint = primary, modifier = Modifier.size(20.dp))
            Text("✨", fontSize = 16.sp)
        }
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

@PreviewLightDark
@Composable
private fun PreviewBestPicksContent() {
    MenuTheme {
        Surface {
            BestPicksContent(
                picks = previewRestaurants.take(3),
                onPickTap = {},
            )
        }
    }
}
