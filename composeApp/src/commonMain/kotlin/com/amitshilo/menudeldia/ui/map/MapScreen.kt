package com.amitshilo.menudeldia.ui.map

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.amitshilo.menudeldia.location.rememberLocationState
import com.amitshilo.menudeldia.navigation.Screen
import menudeldia.composeapp.generated.resources.Res
import menudeldia.composeapp.generated.resources.clear_filters
import menudeldia.composeapp.generated.resources.empty_filtered
import menudeldia.composeapp.generated.resources.empty_filtered_sub
import menudeldia.composeapp.generated.resources.empty_no_menus
import menudeldia.composeapp.generated.resources.empty_no_menus_sub
import menudeldia.composeapp.generated.resources.error_title
import menudeldia.composeapp.generated.resources.my_location
import menudeldia.composeapp.generated.resources.recenter
import menudeldia.composeapp.generated.resources.recenter_on_me
import menudeldia.composeapp.generated.resources.restaurants_nearby
import menudeldia.composeapp.generated.resources.restaurants_of_total
import menudeldia.composeapp.generated.resources.retry
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

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

    LaunchedEffect(locationState.location) {
        viewModel.onUserLocationChanged(locationState.location)
    }

    when (val state = uiState) {
        is MapUiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }

        is MapUiState.Error -> ErrorState(
            message = state.message,
            onRetry = { viewModel.refresh() },
        )

        is MapUiState.Success -> {
            val isCardMode = state.selectedRestaurant != null
            val sheetPeekHeight = if (isCardMode) 0.dp else 160.dp
            val mapBottomPadding = if (isCardMode) detailCardHeight else sheetPeekHeight

            LaunchedEffect(isCardMode) {
                scaffoldState.bottomSheetState.partialExpand()
            }

            BoxWithConstraints(Modifier.fillMaxSize()) {
                val sheetMaxHeight = maxHeight * 0.8f
                Box(Modifier.fillMaxSize()) {
                    BottomSheetScaffold(
                        scaffoldState = scaffoldState,
                        sheetPeekHeight = sheetPeekHeight,
                        sheetDragHandle = { BottomSheetDefaults.DragHandle() },
                        sheetContent = {
                            RestaurantListSheet(
                                restaurants = state.restaurants,
                                selectedRestaurantId = state.selectedRestaurant?.id,
                                onRestaurantTap = { viewModel.selectRestaurant(it) },
                                filterState = state.filterState,
                                totalCount = state.allRestaurants.size,
                                onClearFilters = { viewModel.clearFilters() },
                                onRecenter = { recenterTrigger++ },
                                modifier = Modifier.heightIn(max = sheetMaxHeight),
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
                            filterState = state.filterState,
                            onFilterChange = { viewModel.onFilterChange(it) },
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
                                contentDescription = stringResource(Res.string.recenter_on_me),
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
    onClearFilters: () -> Unit,
    onRecenter: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val headerText = if (filterState.isActive) {
        stringResource(Res.string.restaurants_of_total, restaurants.size, totalCount)
    } else {
        stringResource(Res.string.restaurants_nearby, restaurants.size)
    }

    if (restaurants.isEmpty()) {
        EmptySheetState(
            modifier = modifier,
            isFiltered = filterState.isActive,
            onClearFilters = onClearFilters,
            onRecenter = onRecenter,
        )
        return
    }

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        modifier = modifier.fillMaxWidth(),
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

@Composable
private fun EmptySheetState(
    isFiltered: Boolean,
    onClearFilters: () -> Unit,
    onRecenter: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(36.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = "🍽", fontSize = 36.sp)
        }
        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(if (isFiltered) Res.string.empty_filtered else Res.string.empty_no_menus),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = stringResource(if (isFiltered) Res.string.empty_filtered_sub else Res.string.empty_no_menus_sub),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(20.dp))
        if (isFiltered) {
            Button(onClick = onClearFilters) { Text(stringResource(Res.string.clear_filters)) }
        } else {
            OutlinedButton(onClick = onRecenter) { Text(stringResource(Res.string.recenter)) }
        }
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 24.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(36.dp))
                    .background(MaterialTheme.colorScheme.errorContainer),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = "🍳", fontSize = 36.sp)
            }
            Spacer(Modifier.height(16.dp))
            Text(
                text = stringResource(Res.string.error_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(20.dp))
            Button(onClick = onRetry) { Text(stringResource(Res.string.retry)) }
        }
    }
}
