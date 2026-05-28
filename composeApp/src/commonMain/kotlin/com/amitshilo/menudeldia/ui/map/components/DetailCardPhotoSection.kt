package com.amitshilo.menudeldia.ui.map.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.amitshilo.menudeldia.domain.model.Restaurant
import com.amitshilo.menudeldia.util.format
import com.amitshilo.menudeldia.util.isCurrentlyOpen
import menudeldia.composeapp.generated.resources.Res
import menudeldia.composeapp.generated.resources.close
import menudeldia.composeapp.generated.resources.closed_now
import menudeldia.composeapp.generated.resources.no_menu_today_short
import menudeldia.composeapp.generated.resources.open_now
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun DetailCardPhotoSection(
    restaurant: Restaurant,
    onDismiss: () -> Unit,
    shape: RoundedCornerShape,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .padding(12.dp)
            .clip(shape),
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
                shape = shape,
                modifier = Modifier.align(Alignment.TopStart).padding(12.dp),
            )
        }

        OpenStatusBadge(
            isOpen = restaurant.isCurrentlyOpen(),
            hasMenuToday = restaurant.todayHasMenu,
            shape = shape,
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
private fun RatingBadge(rating: Double, shape: RoundedCornerShape, modifier: Modifier = Modifier) {
    Surface(shape = shape, color = MaterialTheme.colorScheme.surface, modifier = modifier) {
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
private fun OpenStatusBadge(
    isOpen: Boolean,
    hasMenuToday: Boolean,
    shape: RoundedCornerShape,
    modifier: Modifier = Modifier,
) {
    val label = when {
        isOpen -> stringResource(Res.string.open_now)
        !hasMenuToday -> stringResource(Res.string.no_menu_today_short)
        else -> stringResource(Res.string.closed_now)
    }
    val bgColor =
        if (isOpen) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.surfaceVariant
    val textColor =
        if (isOpen) MaterialTheme.colorScheme.onTertiary else MaterialTheme.colorScheme.onSurfaceVariant
    Surface(shape = shape, color = bgColor, modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = textColor,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
        )
    }
}
