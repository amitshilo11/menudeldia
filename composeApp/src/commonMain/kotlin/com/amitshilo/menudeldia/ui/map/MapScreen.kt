package com.amitshilo.menudeldia.ui.map

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.amitshilo.menudeldia.domain.model.Restaurant
import com.amitshilo.menudeldia.location.UserLocation
import com.amitshilo.menudeldia.location.rememberLocationState
import com.amitshilo.menudeldia.navigation.Screen
import com.amitshilo.menudeldia.ui.map.components.BestPicksSheet
import com.amitshilo.menudeldia.ui.map.components.ErrorState
import com.amitshilo.menudeldia.ui.map.components.FilterPanel
import com.amitshilo.menudeldia.ui.map.components.MapBottomSheet
import com.amitshilo.menudeldia.ui.map.components.MapSearchBar
import com.amitshilo.menudeldia.ui.map.components.MapSheetPeekHeight
import com.amitshilo.menudeldia.ui.map.components.MapSheetValue
import com.amitshilo.menudeldia.ui.map.components.RecenterFab
import com.amitshilo.menudeldia.ui.map.components.RestaurantDetailCard
import com.amitshilo.menudeldia.ui.map.components.RestaurantListSheet
import com.amitshilo.menudeldia.ui.map.components.rememberMapSheetState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import menudeldia.composeapp.generated.resources.Res
import menudeldia.composeapp.generated.resources.logo_wide
import org.jetbrains.compose.resources.painterResource

@Composable
fun MapScreen(navController: NavController) {
    val viewModel: MapViewModel = viewModel { MapViewModel() }
    val uiState by viewModel.uiState.collectAsState()
    val bestPicks by viewModel.bestPicks.collectAsState()
    val showBestPicks by viewModel.showBestPicks.collectAsState()
    val fetchGeneration by viewModel.fetchGeneration.collectAsState()
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
            bestPicks = bestPicks,
            showBestPicks = showBestPicks,
            fetchGeneration = fetchGeneration,
            onDismissBestPicks = viewModel::dismissBestPicks,
            onNavigateToDetail = { navController.navigate(Screen.RestaurantDetail.createRoute(it)) },
        )
    }
}

@Composable
private fun MapContent(
    state: MapUiState.Success,
    hasLocationPermission: Boolean,
    userLocation: UserLocation?,
    onEvent: (MapEvent) -> Unit,
    effects: Flow<MapEffect>,
    bestPicks: List<Restaurant>,
    showBestPicks: Boolean,
    fetchGeneration: Int,
    onDismissBestPicks: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
) {
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    var recenterTrigger by remember { mutableIntStateOf(0) }
    var filterPanelVisible by remember { mutableStateOf(false) }
    var detailCardHeightPx by remember { mutableIntStateOf(0) }

    val isCardMode = state.selectedRestaurant != null
    val detailCardHeightDp = remember(detailCardHeightPx) {
        with(density) { if (detailCardHeightPx > 0) detailCardHeightPx.toDp() else 320.dp }
    }

    LaunchedEffect(Unit) {
        effects.collect {
            when (it) {
                MapEffect.RecenterOnUser -> recenterTrigger++
            }
        }
    }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val containerHeight = maxHeight
        val sheetState = rememberMapSheetState(constraints.maxHeight.toFloat())

        // Deliberately derived from the *settled* anchor rather than the live drag offset: the
        // map only needs to know where the sheet came to rest, and reading the offset here would
        // recompose the whole screen — and re-sync every map annotation — on every drag frame.
        val sheetTarget = sheetState.targetValue
        val sheetVisibleHeight = when (sheetTarget) {
            MapSheetValue.Peek -> MapSheetPeekHeight
            MapSheetValue.Half -> containerHeight * 0.5f
            MapSheetValue.Expanded -> containerHeight * 0.9f
        }
        val isSheetExpanded = sheetTarget == MapSheetValue.Expanded

        // MapKit reports a region change many times over a single pan, so guard against
        // re-targeting — each `animateTo` cancels the last one and the sheet would never settle.
        val collapseSheet = {
            if (sheetState.targetValue != MapSheetValue.Peek) {
                scope.launch { sheetState.animateTo(MapSheetValue.Peek) }
            }
        }

        LaunchedEffect(isCardMode) {
            if (isCardMode) sheetState.animateTo(MapSheetValue.Peek)
        }

        Box(Modifier.fillMaxSize()) {
            MapView(
                restaurants = state.restaurants,
                selectedRestaurantId = state.selectedRestaurant?.id,
                userLocation = userLocation,
                isLocationEnabled = hasLocationPermission,
                recenterTrigger = recenterTrigger,
                onRestaurantSelected = { onEvent(MapEvent.SelectRestaurant(it)) },
                onMapTap = {
                    onEvent(MapEvent.ClearSelection)
                    collapseSheet()
                },
                onMapGesture = collapseSheet,
                onMapIdle = { lat, lng, radius -> onEvent(MapEvent.MapIdle(lat, lng, radius)) },
                modifier = Modifier.fillMaxSize(),
                bottomPadding = if (isCardMode) detailCardHeightDp else sheetVisibleHeight,
            )

            AnimatedVisibility(
                visible = !isCardMode,
                enter = slideInVertically { it },
                exit = slideOutVertically { it },
                modifier = Modifier.align(Alignment.BottomCenter),
            ) {
                MapBottomSheet(sheetState, sheetHeight = containerHeight * 0.9f) {
                    RestaurantListSheet(
                        restaurants = state.restaurants,
                        selectedRestaurantId = state.selectedRestaurant?.id,
                        filterState = state.filterState,
                        totalCount = state.allRestaurants.size,
                        isLoading = state.isLoading,
                        fetchGeneration = fetchGeneration,
                        onRestaurantTap = { onEvent(MapEvent.SelectRestaurant(it)) },
                        onClearFilters = { onEvent(MapEvent.ClearFilters) },
                        onRecenter = { onEvent(MapEvent.RecenterRequested) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            AnimatedVisibility(
                visible = !isCardMode && !isSheetExpanded,
                enter = slideInVertically { -it },
                exit = slideOutVertically { -it },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
            ) {
                Column(horizontalAlignment = Alignment.Start) {
                    Image(
                        painter = painterResource(Res.drawable.logo_wide),
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .width(130.dp)
                            .padding(vertical = 8.dp),
                    )
                    MapSearchBar(
                        filterState = state.filterState,
                        onFilterChange = { onEvent(MapEvent.FilterChanged(it)) },
                        onFilterClick = { filterPanelVisible = true },
                    )
                }
            }

            RecenterFab(
                visible = hasLocationPermission && !isCardMode && !isSheetExpanded,
                bottomPadding = if (isCardMode) detailCardHeightDp else sheetVisibleHeight,
                onClick = { onEvent(MapEvent.RecenterRequested) },
            )

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

            AnimatedVisibility(
                visible = state.isLoading,
                enter = slideInVertically { -it },
                exit = slideOutVertically { -it },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .fillMaxWidth(),
            ) {
                LinearProgressIndicator(Modifier.fillMaxWidth())
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

        if (showBestPicks && bestPicks.isNotEmpty()) {
            BestPicksSheet(
                picks = bestPicks,
                onDismiss = onDismissBestPicks,
                onPickTap = onNavigateToDetail,
            )
        }
    }
}
