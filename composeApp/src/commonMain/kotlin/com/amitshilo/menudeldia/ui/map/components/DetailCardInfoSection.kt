package com.amitshilo.menudeldia.ui.map.components

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.amitshilo.menudeldia.domain.model.Restaurant
import com.amitshilo.menudeldia.util.format
import com.amitshilo.menudeldia.util.rememberUriLauncher
import com.amitshilo.menudeldia.util.walkingDirectionsUri
import menudeldia.composeapp.generated.resources.Res
import menudeldia.composeapp.generated.resources.call_restaurant
import menudeldia.composeapp.generated.resources.daily_menu
import menudeldia.composeapp.generated.resources.get_directions
import menudeldia.composeapp.generated.resources.info
import menudeldia.composeapp.generated.resources.more_info
import menudeldia.composeapp.generated.resources.my_location
import menudeldia.composeapp.generated.resources.no_menu_today_short
import menudeldia.composeapp.generated.resources.phone
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun DetailCardInfoSection(
    restaurant: Restaurant,
    onNavigateToDetail: (String) -> Unit,
) {
    val uriLauncher = rememberUriLauncher()

    Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 14.dp)) {
        Text(
            text = restaurant.name,
            style = MaterialTheme.typography.titleMedium,
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
        DividerLine()
        Spacer(Modifier.height(6.dp))

        // "🥘 Catalana · Starter · Main · Dessert · Drink"
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

        DividerLine()
        Spacer(Modifier.height(8.dp))

        DistanceAndPriceRow(restaurant)

        Spacer(Modifier.height(10.dp))

        ActionButtonRow(
            restaurant = restaurant,
            onDirections = {
                uriLauncher.open(
                    walkingDirectionsUri(
                        restaurant.lat,
                        restaurant.lng
                    )
                )
            },
            onCall = { phone -> uriLauncher.open("tel:$phone") },
            onMoreInfo = { onNavigateToDetail(restaurant.id) },
        )
    }
}

@Composable
private fun DividerLine() = HorizontalDivider(
    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
    thickness = 1.dp,
)

@Composable
private fun DistanceAndPriceRow(restaurant: Restaurant) {
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
}

@Composable
private fun ActionButtonRow(
    restaurant: Restaurant,
    onDirections: () -> Unit,
    onCall: (String) -> Unit,
    onMoreInfo: () -> Unit,
) {
    val buttonShape = RoundedCornerShape(16.dp)
    val buttonPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp)
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        FilledTonalButton(
            onClick = onDirections,
            modifier = Modifier.weight(1f),
            shape = buttonShape,
            contentPadding = buttonPadding,
        ) {
            ActionButtonContent(Res.drawable.my_location, Res.string.get_directions)
        }
        restaurant.phone?.let { ph ->
            FilledTonalButton(
                onClick = { onCall(ph) },
                modifier = Modifier.weight(1f),
                shape = buttonShape,
                contentPadding = buttonPadding,
            ) {
                ActionButtonContent(Res.drawable.phone, Res.string.call_restaurant)
            }
        }
        FilledTonalButton(
            onClick = onMoreInfo,
            modifier = Modifier.weight(1f),
            shape = buttonShape,
            contentPadding = buttonPadding,
        ) {
            ActionButtonContent(Res.drawable.info, Res.string.more_info)
        }
    }
}

@Composable
private fun ActionButtonContent(
    icon: org.jetbrains.compose.resources.DrawableResource,
    label: org.jetbrains.compose.resources.StringResource,
) {
    Icon(
        painter = painterResource(icon),
        contentDescription = null,
        modifier = Modifier.size(14.dp),
    )
    Spacer(Modifier.width(4.dp))
    Text(
        text = stringResource(label),
        style = MaterialTheme.typography.labelSmall,
        maxLines = 1,
    )
}
