package com.amitshilo.menudeldia.ui.map

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.amitshilo.menudeldia.location.rememberLocationState
import com.amitshilo.menudeldia.navigation.Screen
import menudeldia.composeapp.generated.resources.Res
import menudeldia.composeapp.generated.resources.my_location
import org.jetbrains.compose.resources.painterResource

private val detailCardHeight = 300.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(navController: NavController) {
    val viewModel: MapViewModel = viewModel { MapViewModel() }
    val uiState by viewModel.uiState.collectAsState()
    val scaffoldState = rememberBottomSheetScaffoldState()
    val locationState = rememberLocationState()
    var recenterTrigger by remember { mutableIntStateOf(0) }
    var filterPanelVisible by remember { mutableStateOf(false) }

    when (val state = uiState) {
        is MapUiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }

        is MapUiState.Error -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(state.message, color = MaterialTheme.colorScheme.error)
        }

        is MapUiState.Success -> {
            val isCardMode = state.selectedRestaurant != null
            val sheetPeekHeight = if (isCardMode) 0.dp else 160.dp
            val mapBottomPadding = if (isCardMode) detailCardHeight else sheetPeekHeight

            LaunchedEffect(isCardMode) {
                scaffoldState.bottomSheetState.partialExpand()
            }

            BoxWithConstraints(Modifier.fillMaxSize()) {
                Box(Modifier.fillMaxSize()) {
                    BottomSheetScaffold(
                        scaffoldState = scaffoldState,
                        sheetPeekHeight = sheetPeekHeight,
                        sheetContent = {
                            RestaurantListSheet(
                                restaurants = state.restaurants,
                                selectedRestaurantId = state.selectedRestaurant?.id,
                                onRestaurantTap = { viewModel.selectRestaurant(it) },
                                filterState = state.filterState,
                                totalCount = state.allRestaurants.size,
                            )
                        },
                    ) {
                        MapView(
                            restaurants = state.restaurants,
                            selectedRestaurantId = state.selectedRestaurant?.id,
                            userLocation = locationState.location,
                            isLocationEnabled = locationState.hasPermission,
                            recenterTrigger = recenterTrigger,
                            onRestaurantSelected = { viewModel.selectRestaurant(it) },
                            onMapTap = { viewModel.clearSelection() },
                            modifier = Modifier.fillMaxSize(),
                            bottomPadding = mapBottomPadding,
                        )
                    }

                    AnimatedVisibility(
                        visible = !isCardMode,
                        enter = slideInVertically { -it },
                        exit = slideOutVertically { -it },
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .statusBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                    ) {
                        MapSearchBar(
                            query = state.filterState.query,
                            activeFilterCount = state.filterState.activeCount.let {
                                if (state.filterState.query.isNotBlank()) it - 1 else it
                            },
                            onQueryChange = { viewModel.onSearchQueryChange(it) },
                            onFilterClick = { filterPanelVisible = true },
                        )
                    }

                    if (locationState.hasPermission) {
                        FloatingActionButton(
                            onClick = { recenterTrigger++ },
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .navigationBarsPadding()
                                .padding(end = 16.dp, bottom = mapBottomPadding + 16.dp),
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.primary,
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.my_location),
                                contentDescription = "Recenter on me",
                            )
                        }
                    }

                    AnimatedVisibility(
                        visible = isCardMode,
                        enter = slideInVertically { it },
                        exit = slideOutVertically { it },
                        modifier = Modifier.align(Alignment.BottomCenter),
                    ) {
                        state.selectedRestaurant?.let { restaurant ->
                            RestaurantDetailCard(
                                restaurant = restaurant,
                                onDismiss = { viewModel.clearSelection() },
                                onNavigateToDetail = {
                                    navController.navigate(Screen.RestaurantDetail.createRoute(it))
                                },
                                modifier = Modifier.navigationBarsPadding(),
                            )
                        }
                    }
                }

                if (filterPanelVisible) {
                    FilterPanel(
                        filterState = state.filterState,
                        allRestaurants = state.allRestaurants,
                        onFilterChange = { viewModel.onFilterChange(it) },
                        onDismiss = { filterPanelVisible = false },
                    )
                }
            }
        }
    }
}

@Composable
private fun RestaurantListSheet(
    restaurants: List<com.amitshilo.menudeldia.domain.model.Restaurant>,
    selectedRestaurantId: String?,
    onRestaurantTap: (String) -> Unit,
    filterState: com.amitshilo.menudeldia.domain.model.SearchFilterState,
    totalCount: Int,
) {
    val headerText = if (filterState.isActive) {
        "${restaurants.size} de $totalCount restaurantes"
    } else {
        "${restaurants.size} restaurantes cerca"
    }

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        item {
            Text(
                text = headerText,
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
        item { Spacer(Modifier.navigationBarsPadding().height(16.dp)) }
    }
}
