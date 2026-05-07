package com.amitshilo.menudeldia.ui.detail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.amitshilo.menudeldia.domain.model.Dish
import com.amitshilo.menudeldia.domain.model.Menu
import com.amitshilo.menudeldia.domain.model.Restaurant
import menudeldia.composeapp.generated.resources.Res
import menudeldia.composeapp.generated.resources.arrow_back
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestaurantDetailScreen(restaurantId: String, navController: NavController) {
    val viewModel = viewModel(key = restaurantId) { DetailViewModel(restaurantId) }
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            painter = painterResource(Res.drawable.arrow_back),
                            contentDescription = "Back",
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun RestaurantDetailContent(
    restaurant: Restaurant,
    menu: Menu?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
    ) {
        AsyncImage(
            model = restaurant.thumbnailUrl,
            contentDescription = restaurant.name,
            modifier = Modifier.fillMaxWidth().height(200.dp),
            contentScale = ContentScale.Crop,
        )

        Column(modifier = Modifier.padding(16.dp)) {
            Text(restaurant.name, style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(4.dp))
            Text(
                restaurant.address,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (menu != null) {
                Spacer(Modifier.height(20.dp))
                val cents = (menu.price * 100).toLong()
                val priceStr = "${cents / 100}.${(cents % 100).toString().padStart(2, '0')}"
                Text(
                    "Menú del día — €$priceStr",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                if (menu.firsts.isNotEmpty()) DishSection("Primeros", menu.firsts)
                if (menu.seconds.isNotEmpty()) DishSection("Segundos", menu.seconds)
                if (menu.desserts.isNotEmpty()) DishSection("Postres", menu.desserts)
                menu.notes?.let { notes ->
                    Spacer(Modifier.height(8.dp))
                    Text(notes, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                Spacer(Modifier.height(16.dp))
                Text(
                    "Sin menú del día hoy",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (restaurant.openingHours.isNotEmpty()) {
                Spacer(Modifier.height(20.dp))
                HorizontalDivider()
                Spacer(Modifier.height(12.dp))
                Text("Horario", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                restaurant.openingHours.filter { !it.isClosed }.forEach { hours ->
                    Text(
                        "${
                            hours.dayOfWeek.name.take(3).lowercase()
                                .replaceFirstChar { it.uppercase() }
                        }: ${hours.openTime} – ${hours.closeTime}",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            restaurant.phone?.let { phone ->
                Spacer(Modifier.height(20.dp))
                HorizontalDivider()
                Spacer(Modifier.height(12.dp))
                Text("Teléfono", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                Text(phone, style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun DishSection(title: String, dishes: List<Dish>) {
    Spacer(Modifier.height(12.dp))
    Text(title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.secondary)
    Spacer(Modifier.height(4.dp))
    FlowRow {
        dishes.forEach { dish ->
            AssistChip(
                onClick = {},
                label = { Text(dish.name) },
                modifier = Modifier.padding(end = 6.dp, bottom = 4.dp),
            )
        }
    }
}
