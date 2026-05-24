package com.amitshilo.menudeldia.ui.detail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.amitshilo.menudeldia.domain.model.Restaurant
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDateTime
import menudeldia.composeapp.generated.resources.Res
import menudeldia.composeapp.generated.resources.closed_now
import menudeldia.composeapp.generated.resources.hours_header
import menudeldia.composeapp.generated.resources.open_closes_at
import menudeldia.composeapp.generated.resources.open_now
import org.jetbrains.compose.resources.stringResource

@Composable
fun HoursSection(restaurant: Restaurant, now: LocalDateTime, modifier: Modifier = Modifier) {
    Text(stringResource(Res.string.hours_header), style = MaterialTheme.typography.titleMedium)
    Spacer(Modifier.height(8.dp))
    restaurant.openingHours.filter { !it.isClosed }.forEach { hours ->
        val isToday = hours.dayOfWeek == now.dayOfWeek
        Text(
            text = "${dayShort(hours.dayOfWeek)}: ${hours.openTime} – ${hours.closeTime}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
            color = if (isToday) MaterialTheme.colorScheme.onSurface
            else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
fun OpenStatusBadge(isOpen: Boolean, closesAt: String?, modifier: Modifier = Modifier) {
    val bg = if (isOpen) MaterialTheme.colorScheme.tertiaryContainer
    else MaterialTheme.colorScheme.surfaceVariant
    val fg = if (isOpen) MaterialTheme.colorScheme.onTertiaryContainer
    else MaterialTheme.colorScheme.onSurfaceVariant
    val label = when {
        isOpen && closesAt != null -> stringResource(Res.string.open_closes_at, closesAt)
        isOpen -> stringResource(Res.string.open_now)
        else -> stringResource(Res.string.closed_now)
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = fg)
    }
}

private fun dayShort(day: DayOfWeek): String =
    day.name.take(3).lowercase().replaceFirstChar { it.uppercase() }
