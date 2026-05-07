package com.amitshilo.menudeldia.ui.map

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.amitshilo.menudeldia.ui.detail.DetailUiState
import com.amitshilo.menudeldia.ui.detail.DetailViewModel
import com.amitshilo.menudeldia.ui.detail.RestaurantDetailContent
import menudeldia.composeapp.generated.resources.Res
import menudeldia.composeapp.generated.resources.arrow_back
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen() {
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

        is MapUiState.Success -> BoxWithConstraints(Modifier.fillMaxSize()) {
            val listPeekHeight = 120.dp
            val detailPeekHeight = maxHeight * 0.6f

            val sheetPeekHeight =
                if (state.selectedRestaurant != null) detailPeekHeight else listPeekHeight

            // Animate to the correct peek position whenever selection changes
            LaunchedEffect(state.selectedRestaurant?.id) {
                scaffoldState.bottomSheetState.partialExpand()
            }

            BottomSheetScaffold(
                scaffoldState = scaffoldState,
                sheetPeekHeight = sheetPeekHeight,
                sheetContent = {
                    if (state.selectedRestaurant != null) {
                        DetailSheet(
                            restaurantId = state.selectedRestaurant.id,
                            onBack = { viewModel.clearSelection() },
                        )
                    } else {
                        RestaurantListSheet(
                            restaurants = state.restaurants,
                            selectedRestaurantId = state.selectedRestaurant?.id,
                            onRestaurantTap = { viewModel.selectRestaurant(it) },
                        )
                    }
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
private fun DetailSheet(restaurantId: String, onBack: () -> Unit) {
    val detailViewModel = viewModel(key = restaurantId) { DetailViewModel(restaurantId) }
    val detailState by detailViewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    painter = painterResource(Res.drawable.arrow_back),
                    contentDescription = "Back",
                )
            }
            Text(
                text = (detailState as? DetailUiState.Success)?.restaurant?.name ?: "",
                style = MaterialTheme.typography.titleMedium,
            )
        }

        when (val s = detailState) {
            is DetailUiState.Loading -> Box(
                modifier = Modifier.fillMaxWidth().height(200.dp),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator() }

            is DetailUiState.Error -> Box(
                modifier = Modifier.fillMaxWidth().height(200.dp),
                contentAlignment = Alignment.Center,
            ) { Text(s.message, color = MaterialTheme.colorScheme.error) }

            is DetailUiState.Success -> RestaurantDetailContent(
                restaurant = s.restaurant,
                menu = s.menu,
            )
        }
    }
}

@Composable
private fun RestaurantListSheet(
    restaurants: List<com.amitshilo.menudeldia.domain.model.Restaurant>,
    selectedRestaurantId: String?,
    onRestaurantTap: (String) -> Unit,
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
                isSelected = restaurant.id == selectedRestaurantId,
                onClick = { onRestaurantTap(restaurant.id) },
                modifier = Modifier.padding(bottom = 8.dp),
            )
        }
        item { Spacer(Modifier.height(16.dp)) }
    }
}
