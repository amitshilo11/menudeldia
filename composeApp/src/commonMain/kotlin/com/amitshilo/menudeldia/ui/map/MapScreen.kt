package com.amitshilo.menudeldia.ui.map

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(navController: NavController) {
    val viewModel: MapViewModel = viewModel { MapViewModel() }
    val uiState by viewModel.uiState.collectAsState()
    val scaffoldState = rememberBottomSheetScaffoldState()

    when (val state = uiState) {
        is MapUiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }

        is MapUiState.Error -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(state.message, color = MaterialTheme.colorScheme.error)
        }

        is MapUiState.Success -> {
            BottomSheetScaffold(
                scaffoldState = scaffoldState,
                sheetPeekHeight = 120.dp,
                sheetContent = {
                    RestaurantListSheet(
                        restaurants = state.restaurants,
                        selectedRestaurant = state.selectedRestaurant,
                        onRestaurantSelected = { viewModel.selectRestaurant(it.id) },
                        onRestaurantDetail = { navController.navigate("detail/${it.id}") },
                    )
                },
            ) {
                MapView(
                    restaurants = state.restaurants,
                    selectedRestaurantId = state.selectedRestaurant?.id,
                    onRestaurantSelected = { viewModel.selectRestaurant(it) },
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

@Composable
private fun RestaurantListSheet(
    restaurants: List<com.amitshilo.menudeldia.domain.model.Restaurant>,
    selectedRestaurant: com.amitshilo.menudeldia.domain.model.Restaurant?,
    onRestaurantSelected: (com.amitshilo.menudeldia.domain.model.Restaurant) -> Unit,
    onRestaurantDetail: (com.amitshilo.menudeldia.domain.model.Restaurant) -> Unit,
) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        item {
            Text(
                text = "${restaurants.size} restaurantes cerca",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp),
            )
        }
        items(restaurants, key = { it.id }) { restaurant ->
            RestaurantCard(
                restaurant = restaurant,
                isSelected = restaurant.id == selectedRestaurant?.id,
                onClick = {
                    onRestaurantSelected(restaurant)
                    onRestaurantDetail(restaurant)
                },
                modifier = Modifier.padding(bottom = 8.dp),
            )
        }
        item { Spacer(Modifier.height(16.dp)) }
    }
}
