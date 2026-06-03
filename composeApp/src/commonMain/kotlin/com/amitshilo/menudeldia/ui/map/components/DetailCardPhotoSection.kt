package com.amitshilo.menudeldia.ui.map.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontStyle
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
import com.amitshilo.menudeldia.util.todayHours
import kotlinx.datetime.LocalTime
import menudeldia.composeapp.generated.resources.Res
import menudeldia.composeapp.generated.resources.close
import menudeldia.composeapp.generated.resources.closed_now
import menudeldia.composeapp.generated.resources.no_menu_today_short
import menudeldia.composeapp.generated.resources.open_closes
import menudeldia.composeapp.generated.resources.open_now
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun DetailCardPhotoSection(
    restaurant: Restaurant,
    onDismiss: () -> Unit,
) {
    val isOpen = restaurant.isCurrentlyOpen()
    val closeTime = todayHours(restaurant.openingHours)?.closeTime

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .padding(12.dp)
            .clip(RoundedCornerShape(20.dp)),
    ) {
        if (restaurant.thumbnailUrl != null) {
            AsyncImage(
                model = restaurant.thumbnailUrl,
                contentDescription = restaurant.name,
                modifier = Modifier.fillMaxWidth().height(220.dp),
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
                        listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.5f)
                        )
                    ),
                ),
        )

        OpenStatusBadge(
            isOpen = isOpen,
            hasMenuToday = restaurant.todayHasMenu,
            closeTime = closeTime,
            modifier = Modifier.align(Alignment.TopStart).padding(12.dp),
        )

        restaurant.menuPrice?.takeIf { restaurant.todayHasMenu }?.let { price ->
            PriceBadge(
                price = price,
                modifier = Modifier.align(Alignment.BottomEnd).padding(12.dp),
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(10.dp)
                .size(34.dp)
                .clip(CircleShape)
                .clickable { onDismiss() },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(Res.drawable.close),
                contentDescription = stringResource(Res.string.close),
                tint = Color.White,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun PriceBadge(price: Double, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
        ) {
            Text(
                text = "menú del día",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimary,
                fontStyle = FontStyle.Italic,
            )
            Text(
                text = "€${price.format(2)}",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun OpenStatusBadge(
    isOpen: Boolean,
    hasMenuToday: Boolean,
    closeTime: LocalTime?,
    modifier: Modifier = Modifier,
) {
    val label = when {
        isOpen && closeTime != null ->
            stringResource(
                Res.string.open_closes,
                "${closeTime.hour.toString().padStart(2, '0')}:${
                    closeTime.minute.toString().padStart(2, '0')
                }",
            )
        isOpen -> stringResource(Res.string.open_now)
        !hasMenuToday -> stringResource(Res.string.no_menu_today_short)
        else -> stringResource(Res.string.closed_now)
    }
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
        ) {
            if (isOpen) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(MaterialTheme.colorScheme.tertiary, CircleShape),
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = if (isOpen) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun PreviewPhotoSectionWithMenu() {
    MenuTheme {
        DetailCardPhotoSection(restaurant = previewRestaurant, onDismiss = {})
    }
}

@PreviewLightDark
@Composable
private fun PreviewPhotoSectionNoMenu() {
    MenuTheme {
        DetailCardPhotoSection(restaurant = previewRestaurantNoMenu, onDismiss = {})
    }
}
