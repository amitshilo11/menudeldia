package com.amitshilo.menudeldia.ui.detail

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.amitshilo.menudeldia.domain.model.Dish
import com.amitshilo.menudeldia.domain.model.Menu
import com.amitshilo.menudeldia.domain.model.Restaurant
import com.amitshilo.menudeldia.ui.preview.previewMenu
import com.amitshilo.menudeldia.ui.preview.previewRestaurant
import com.amitshilo.menudeldia.ui.preview.previewRestaurantNoMenu
import com.amitshilo.menudeldia.ui.theme.MenuTheme
import com.amitshilo.menudeldia.util.currentLocalDateTime
import com.amitshilo.menudeldia.util.format
import com.amitshilo.menudeldia.util.isCurrentlyOpen
import com.amitshilo.menudeldia.util.rememberUriLauncher
import com.amitshilo.menudeldia.util.todayHours
import com.amitshilo.menudeldia.util.walkingDirectionsUri
import kotlinx.datetime.DayOfWeek
import menudeldia.composeapp.generated.resources.Res
import menudeldia.composeapp.generated.resources.arrow_back
import menudeldia.composeapp.generated.resources.back
import menudeldia.composeapp.generated.resources.closed_now
import menudeldia.composeapp.generated.resources.daily_menu_with_price
import menudeldia.composeapp.generated.resources.dish_desserts
import menudeldia.composeapp.generated.resources.dish_firsts
import menudeldia.composeapp.generated.resources.dish_seconds
import menudeldia.composeapp.generated.resources.get_directions
import menudeldia.composeapp.generated.resources.hours_header
import menudeldia.composeapp.generated.resources.my_location
import menudeldia.composeapp.generated.resources.no_menu_today_long
import menudeldia.composeapp.generated.resources.open_closes_at
import menudeldia.composeapp.generated.resources.open_now
import menudeldia.composeapp.generated.resources.phone_header
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestaurantDetailScreen(restaurantId: String, navController: NavController) {
    val viewModel = viewModel(key = restaurantId) { DetailViewModel(restaurantId) }
    val uiState by viewModel.uiState.collectAsState()

    val title = (uiState as? DetailUiState.Success)?.restaurant?.name.orEmpty()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleMedium,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            painter = painterResource(Res.drawable.arrow_back),
                            contentDescription = stringResource(Res.string.back),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        when (val state = uiState) {
            is DetailUiState.Loading -> Box(
                Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator() }

            is DetailUiState.Error -> Box(
                Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) { Text(state.message, color = MaterialTheme.colorScheme.error) }

            is DetailUiState.Success -> RestaurantDetailContent(
                restaurant = state.restaurant,
                menu = state.menu,
                modifier = Modifier.padding(innerPadding),
            )
        }
    }
}

@Composable
internal fun RestaurantDetailContent(
    restaurant: Restaurant,
    menu: Menu?,
    modifier: Modifier = Modifier,
) {
    val uriLauncher = rememberUriLauncher()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
    ) {
        PhotoHeader(thumbnailUrl = restaurant.thumbnailUrl, contentDescription = restaurant.name)

        Column(modifier = Modifier.padding(16.dp)) {
            Text(restaurant.name, style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(4.dp))
            AddressAndDistance(
                address = restaurant.address,
                distanceMeters = restaurant.distanceMeters,
            )

            if (restaurant.servesVegetarianFood || restaurant.isGlutenFreeFriendly) {
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (restaurant.servesVegetarianFood) {
                        DietaryBadge(text = "Vegano", icon = "🌱")
                    }
                    if (restaurant.isGlutenFreeFriendly) {
                        DietaryBadge(text = "Sin gluten", icon = "🌾")
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    uriLauncher.open(walkingDirectionsUri(restaurant.lat, restaurant.lng))
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
            ) {
                Icon(
                    painter = painterResource(Res.drawable.my_location),
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp),
                )
                Text(stringResource(Res.string.get_directions), fontWeight = FontWeight.SemiBold)
            }

            if (menu != null) {
                Spacer(Modifier.height(20.dp))
                Text(
                    stringResource(Res.string.daily_menu_with_price, menu.price.format(2)),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                )
                if (menu.firsts.isNotEmpty()) DishSection(
                    stringResource(Res.string.dish_firsts),
                    menu.firsts
                )
                if (menu.seconds.isNotEmpty()) DishSection(
                    stringResource(Res.string.dish_seconds),
                    menu.seconds
                )
                if (menu.desserts.isNotEmpty()) DishSection(
                    stringResource(Res.string.dish_desserts),
                    menu.desserts
                )
                menu.notes?.let { notes ->
                    Spacer(Modifier.height(12.dp))
                    Text(
                        notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                Spacer(Modifier.height(16.dp))
                Text(
                    stringResource(Res.string.no_menu_today_long),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            DescriptionSection(restaurant)

            if (restaurant.openingHours.isNotEmpty()) {
                Spacer(Modifier.height(20.dp))
                HorizontalDivider()
                Spacer(Modifier.height(12.dp))
                HoursSection(restaurant)
            }

            restaurant.phone?.let { phone ->
                Spacer(Modifier.height(20.dp))
                HorizontalDivider()
                Spacer(Modifier.height(12.dp))
                Text(
                    stringResource(Res.string.phone_header),
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = phone,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable { uriLauncher.open("tel:$phone") },
                )
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun PhotoHeader(thumbnailUrl: String?, contentDescription: String) {
    if (thumbnailUrl != null) {
        AsyncImage(
            model = thumbnailUrl,
            contentDescription = contentDescription,
            modifier = Modifier.fillMaxWidth().height(220.dp),
            contentScale = ContentScale.Crop,
        )
    } else {
        PhotoFallback(modifier = Modifier.fillMaxWidth().height(220.dp))
    }
}

@Composable
internal fun PhotoFallback(modifier: Modifier = Modifier, cornerShape: RoundedCornerShape? = null) {
    val base = modifier.background(MaterialTheme.colorScheme.primaryContainer)
    val shaped = if (cornerShape != null) base.clip(cornerShape) else base
    Box(modifier = shaped, contentAlignment = Alignment.Center) {
        Text(text = "🍽", fontSize = 64.sp)
    }
}

@Composable
private fun AddressAndDistance(address: String, distanceMeters: Double?) {
    val distanceLabel = distanceMeters?.let { d ->
        if (d < 1000) "${d.toInt()}m" else "${(d / 1000.0).format(1)}km"
    }
    val text = if (distanceLabel != null) "$address · $distanceLabel" else address
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun DescriptionSection(restaurant: Restaurant) {
    val lang = Locale.current.language
    val description = when (lang) {
        "es", "ca" -> restaurant.descriptionEs ?: restaurant.descriptionEn
        else -> restaurant.descriptionEn ?: restaurant.descriptionEs
    } ?: return
    Spacer(Modifier.height(16.dp))
    Text(
        text = description,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface,
    )
}

@Composable
private fun HoursSection(restaurant: Restaurant) {
    val now = currentLocalDateTime()
    val isOpen = restaurant.isCurrentlyOpen(now)
    val today = todayHours(restaurant.openingHours, now)

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(stringResource(Res.string.hours_header), style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.padding(start = 12.dp))
        OpenStatusInline(isOpen = isOpen, closesAt = today?.closeTime?.toString())
    }
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
private fun DietaryBadge(text: String, icon: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
        Text(text = icon, fontSize = 14.sp)
        Spacer(Modifier.width(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
        )
    }
}

@Composable
private fun OpenStatusInline(isOpen: Boolean, closesAt: String?) {
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
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = fg)
    }
}

private fun dayShort(day: DayOfWeek): String =
    day.name.take(3).lowercase().replaceFirstChar { it.uppercase() }

@Composable
internal fun DishSection(title: String, dishes: List<Dish>) {
    Spacer(Modifier.height(12.dp))
    Text(title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.secondary)
    Spacer(Modifier.height(4.dp))
    Column {
        dishes.forEach { dish ->
            Text(
                text = dish.name,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(vertical = 2.dp),
            )
        }
    }
}

// ── Previews ────────────────────────────────────────────────────────────────

@PreviewLightDark
@Composable
private fun PreviewDetailContentWithMenu() {
    MenuTheme { RestaurantDetailContent(restaurant = previewRestaurant, menu = previewMenu) }
}

@PreviewLightDark
@Composable
private fun PreviewDetailContentNoMenu() {
    MenuTheme { RestaurantDetailContent(restaurant = previewRestaurantNoMenu, menu = null) }
}
