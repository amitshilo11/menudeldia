package com.amitshilo.menudeldia.ui.detail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.amitshilo.menudeldia.domain.model.Menu
import com.amitshilo.menudeldia.domain.model.Restaurant
import com.amitshilo.menudeldia.ui.detail.components.AboutSection
import com.amitshilo.menudeldia.ui.detail.components.ActionButtonsRow
import com.amitshilo.menudeldia.ui.detail.components.ContactSection
import com.amitshilo.menudeldia.ui.detail.components.FeaturesSection
import com.amitshilo.menudeldia.ui.detail.components.HoursSection
import com.amitshilo.menudeldia.ui.detail.components.MenuCard
import com.amitshilo.menudeldia.ui.detail.components.OpenStatusBadge
import com.amitshilo.menudeldia.ui.detail.components.PhotoCarousel
import com.amitshilo.menudeldia.ui.detail.components.ReviewsSection
import com.amitshilo.menudeldia.ui.preview.previewMenu
import com.amitshilo.menudeldia.ui.preview.previewRestaurant
import com.amitshilo.menudeldia.ui.preview.previewRestaurantNoMenu
import com.amitshilo.menudeldia.ui.theme.MenuTheme
import com.amitshilo.menudeldia.util.currentLocalDateTime
import com.amitshilo.menudeldia.util.format
import com.amitshilo.menudeldia.util.isCurrentlyOpen
import com.amitshilo.menudeldia.util.todayHours
import menudeldia.composeapp.generated.resources.Res
import menudeldia.composeapp.generated.resources.arrow_back
import menudeldia.composeapp.generated.resources.back
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                ),
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
    val now = currentLocalDateTime()
    val isOpen = restaurant.isCurrentlyOpen(now)
    val today = todayHours(restaurant.openingHours, now)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
    ) {
        PhotoCarousel(
            photos = restaurant.photos,
            thumbnailUrl = restaurant.thumbnailUrl,
            contentDescription = restaurant.name,
        )

        Column(modifier = Modifier.padding(16.dp)) {
            Text(restaurant.name, style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(4.dp))
            RestaurantMetaRow(restaurant)
            Spacer(Modifier.height(2.dp))
            Text(
                text = restaurant.address,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(10.dp))
            OpenStatusBadge(isOpen = isOpen, closesAt = today?.closeTime?.toString())
            Spacer(Modifier.height(16.dp))
            ActionButtonsRow(lat = restaurant.lat, lng = restaurant.lng, phone = restaurant.phone)
            Spacer(Modifier.height(20.dp))
            MenuCard(restaurant = restaurant, menu = menu)
            AboutSection(restaurant = restaurant)
            FeaturesSection(restaurant = restaurant)
            if (restaurant.reviews.isNotEmpty()) {
                Spacer(Modifier.height(24.dp))
                ReviewsSection(reviews = restaurant.reviews)
            }
            if (restaurant.openingHours.isNotEmpty()) {
                Spacer(Modifier.height(24.dp))
                HoursSection(restaurant = restaurant, now = now)
            }
            restaurant.phone?.let { phone ->
                Spacer(Modifier.height(24.dp))
                ContactSection(phone = phone)
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun RestaurantMetaRow(restaurant: Restaurant) {
    val parts = buildList {
        restaurant.cuisineEmoji?.let { emoji ->
            val label = restaurant.cuisineType?.let { "$emoji $it" } ?: emoji
            add(label)
        }
        restaurant.rating?.let { rating ->
            val count = restaurant.userRatingCount?.let { " ($it)" } ?: ""
            add("★ ${rating.format(1)}$count")
        }
        restaurant.distanceMeters?.let { d ->
            add(if (d < 1000) "${d.toInt()}m" else "${(d / 1000.0).format(1)}km")
        }
    }
    if (parts.isEmpty()) return
    Text(
        text = parts.joinToString(" · "),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
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
