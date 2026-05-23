package com.amitshilo.menudeldia.ui.map

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.amitshilo.menudeldia.location.UserLocation
import com.amitshilo.menudeldia.location.rememberLocationState
import com.amitshilo.menudeldia.navigation.Screen
import com.amitshilo.menudeldia.ui.map.components.ErrorState
import com.amitshilo.menudeldia.ui.map.components.FilterPanel
import com.amitshilo.menudeldia.ui.map.components.MapSearchBar
import com.amitshilo.menudeldia.ui.map.components.RestaurantDetailCard
import com.amitshilo.menudeldia.ui.map.components.RestaurantListSheet
import kotlinx.coroutines.flow.Flow
import menudeldia.composeapp.generated.resources.Res
import menudeldia.composeapp.generated.resources.my_location
import menudeldia.composeapp.generated.resources.recenter_on_me
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

private val ListSheetPeekHeight = 160.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(navController: NavController) {
    val viewModel: MapViewModel = viewModel { MapViewModel() }
    val uiState by viewModel.uiState.collectAsState()
    val locationState = rememberLocationState()

    LaunchedEffect(locationState.location) {
        viewModel.onEvent(MapEvent.LocationChanged(locationState.location))
    }

    when (val state = uiState) {
        MapUiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }

        is MapUiState.Error -> ErrorState(
            message = state.message,
            onRetry = { viewModel.onEvent(MapEvent.Refresh) },
        )

        is MapUiState.Success -> MapContent(
            state = state,
            hasLocationPermission = locationState.hasPermission,
            userLocation = locationState.location,
            onEvent = viewModel::onEvent,
            effects = viewModel.effects,
            onNavigateToDetail = { id ->
                navController.navigate(Screen.RestaurantDetail.createRoute(id))
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MapContent(
    state: MapUiState.Success,
    hasLocationPermission: Boolean,
    userLocation: UserLocation?,
    onEvent: (MapEvent) -> Unit,
    effects: Flow<MapEffect>,
    onNavigateToDetail: (String) -> Unit,
) {
    val scaffoldState = rememberBottomSheetScaffoldState()
    val density = LocalDensity.current
    var recenterTrigger by remember { mutableIntStateOf(0) }
    var filterPanelVisible by remember { mutableStateOf(false) }
    var detailCardHeightPx by remember { mutableIntStateOf(0) }

    val isCardMode = state.selectedRestaurant != null
    val peekHeightPx = with(density) { ListSheetPeekHeight.toPx() }
    val detailCardHeightDp = remember(detailCardHeightPx) {
        with(density) { if (detailCardHeightPx > 0) detailCardHeightPx.toDp() else 320.dp }
    }
    val fabBottomTarget =
        if (isCardMode) detailCardHeightDp + 16.dp else ListSheetPeekHeight + 16.dp
    val fabBottomPadding by animateDpAsState(targetValue = fabBottomTarget, label = "fabBottom")

    LaunchedEffect(Unit) {
        effects.collect { effect ->
            when (effect) {
                MapEffect.RecenterOnUser -> recenterTrigger++
            }
        }
    }

    LaunchedEffect(isCardMode) {
        scaffoldState.bottomSheetState.partialExpand()
    }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val sheetMaxHeight = maxHeight * 0.8f
        val sheetPeekHeight = if (isCardMode) 0.dp else ListSheetPeekHeight
        val mapBottomPadding = if (isCardMode) detailCardHeightDp else sheetPeekHeight
        val containerHeightPx = constraints.maxHeight.toFloat()
        val isSheetAbovePeek by remember(containerHeightPx) {
            derivedStateOf {
                try {
                    scaffoldState.bottomSheetState.requireOffset() < containerHeightPx - peekHeightPx - 1f
                } catch (_: IllegalStateException) {
                    false
                }
            }
        }

        Box(Modifier.fillMaxSize()) {
            BottomSheetScaffold(
                scaffoldState = scaffoldState,
                sheetPeekHeight = sheetPeekHeight,
                sheetDragHandle = { BottomSheetDefaults.DragHandle() },
                sheetContent = {
                    RestaurantListSheet(
                        restaurants = state.restaurants,
                        selectedRestaurantId = state.selectedRestaurant?.id,
                        filterState = state.filterState,
                        totalCount = state.allRestaurants.size,
                        onRestaurantTap = { onEvent(MapEvent.SelectRestaurant(it)) },
                        onClearFilters = { onEvent(MapEvent.ClearFilters) },
                        onRecenter = { onEvent(MapEvent.RecenterRequested) },
                        modifier = Modifier.heightIn(max = sheetMaxHeight),
                    )
                },
            ) {
                MapView(
                    restaurants = state.restaurants,
                    selectedRestaurantId = state.selectedRestaurant?.id,
                    userLocation = userLocation,
                    isLocationEnabled = hasLocationPermission,
                    recenterTrigger = recenterTrigger,
                    onRestaurantSelected = { onEvent(MapEvent.SelectRestaurant(it)) },
                    onMapTap = { onEvent(MapEvent.ClearSelection) },
                    modifier = Modifier.fillMaxSize(),
                    bottomPadding = mapBottomPadding,
                )
            }

            AnimatedVisibility(
                visible = !isCardMode && !isSheetAbovePeek,
                enter = slideInVertically { -it },
                exit = slideOutVertically { -it },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            ) {
                MapSearchBar(
                    filterState = state.filterState,
                    onFilterChange = { onEvent(MapEvent.FilterChanged(it)) },
                    onFilterClick = { filterPanelVisible = true },
                )
            }

            AnimatedVisibility(
                visible = hasLocationPermission && !isCardMode && !isSheetAbovePeek,
                enter = slideInVertically { it },
                exit = slideOutVertically { it },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = fabBottomPadding),
            ) {
                FloatingActionButton(
                    onClick = { onEvent(MapEvent.RecenterRequested) },
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
                        onDismiss = { onEvent(MapEvent.ClearSelection) },
                        onNavigateToDetail = onNavigateToDetail,
                        modifier = Modifier
                            .navigationBarsPadding()
                            .onSizeChanged { detailCardHeightPx = it.height },
                    )
                }
            }
        }

        if (filterPanelVisible) {
            FilterPanel(
                filterState = state.filterState,
                allRestaurants = state.allRestaurants,
                onFilterChange = { onEvent(MapEvent.FilterChanged(it)) },
                onDismiss = { filterPanelVisible = false },
            )
        }
    }
}
