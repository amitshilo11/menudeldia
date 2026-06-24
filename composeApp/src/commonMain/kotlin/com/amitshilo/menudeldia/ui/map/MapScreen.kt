package com.amitshilo.menudeldia.ui.map

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
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
import com.amitshilo.menudeldia.ui.map.components.MapSearchBar
import com.amitshilo.menudeldia.ui.map.components.RestaurantDetailCard
import com.amitshilo.menudeldia.ui.map.components.RestaurantListSheet
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import menudeldia.composeapp.generated.resources.Res
import menudeldia.composeapp.generated.resources.my_location
import menudeldia.composeapp.generated.resources.recenter_on_me
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import kotlin.math.abs
import kotlin.math.roundToInt

// Y offset of the sheet's top edge from the container top for each stable position.
private data class SheetAnchors(val expanded: Float, val half: Float, val collapsed: Float)

@Composable
private fun SheetDragHandle() {
    Box(
        modifier = Modifier
            .padding(vertical = 10.dp)
            .size(width = 36.dp, height = 4.dp)
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.6f), CircleShape),
    )
}

@Composable
private fun DraggableBottomSheet(
    sheetOffset: Animatable<Float, *>,
    anchors: SheetAnchors,
    sheetHeight: Dp,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val velocityThresholdPx = with(LocalDensity.current) { 300.dp.toPx() }
    val sheetShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(sheetHeight)
            .offset {
                val relativeOffset = (sheetOffset.value - anchors.expanded).coerceAtLeast(0f)
                IntOffset(0, relativeOffset.roundToInt())
            }
            .shadow(8.dp, sheetShape)
            .background(MaterialTheme.colorScheme.surfaceContainer, sheetShape),
    ) {
        Column(Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .draggable(
                        orientation = Orientation.Vertical,
                        state = rememberDraggableState { delta ->
                            coroutineScope.launch {
                                sheetOffset.snapTo(
                                    (sheetOffset.value + delta).coerceIn(
                                        anchors.expanded,
                                        anchors.collapsed
                                    ),
                                )
                            }
                        },
                        onDragStopped = { velocity ->
                            coroutineScope.launch {
                                val target = when {
                                    velocity > velocityThresholdPx -> anchors.collapsed
                                    velocity < -velocityThresholdPx -> anchors.expanded
                                    else -> listOf(
                                        anchors.expanded,
                                        anchors.half,
                                        anchors.collapsed
                                    )
                                        .minByOrNull { abs(it - sheetOffset.value) }!!
                                }
                                sheetOffset.animateTo(
                                    target,
                                    spring(stiffness = 400f, dampingRatio = 0.85f)
                                )
                            }
                        },
                    ),
                contentAlignment = Alignment.Center,
            ) {
                SheetDragHandle()
            }
            content()
        }
    }
}

@Composable
private fun RecenterFab(onClick: () -> Unit, modifier: Modifier = Modifier) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.primary,
        modifier = modifier,
    ) {
        Icon(
            painter = painterResource(Res.drawable.my_location),
            contentDescription = stringResource(Res.string.recenter_on_me),
        )
    }
}

@Composable
fun MapScreen(navController: NavController) {
    val viewModel: MapViewModel = viewModel { MapViewModel() }
    val uiState by viewModel.uiState.collectAsState()
    val bestPicks by viewModel.bestPicks.collectAsState()
    val showBestPicks by viewModel.showBestPicks.collectAsState()
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
    onDismissBestPicks: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
) {
    val density = LocalDensity.current
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
        val containerHeightPx = constraints.maxHeight.toFloat()
        val peekHeightPx = with(density) { 160.dp.toPx() }
        val anchors = remember(containerHeightPx, peekHeightPx) {
            SheetAnchors(
                expanded = containerHeightPx * 0.1f,
                half = containerHeightPx * 0.5f,
                collapsed = containerHeightPx - peekHeightPx,
            )
        }
        val sheetOffset = remember { Animatable(anchors.half) }

        LaunchedEffect(containerHeightPx) { sheetOffset.snapTo(anchors.half) }
        LaunchedEffect(isCardMode) {
            if (!isCardMode) sheetOffset.animateTo(
                anchors.half,
                spring(stiffness = 400f, dampingRatio = 0.85f)
            )
        }

        val sheetVisibleHeightDp = with(density) { (containerHeightPx - sheetOffset.value).toDp() }
        val mapBottomPadding = if (isCardMode) detailCardHeightDp else sheetVisibleHeightDp
        val fabBottomPadding =
            if (isCardMode) detailCardHeightDp + 16.dp else sheetVisibleHeightDp + 16.dp
        val isSheetExpanded by remember { derivedStateOf { sheetOffset.value < anchors.half - 1f } }
        val sheetHeight = maxHeight * 0.9f

        Box(Modifier.fillMaxSize()) {
            MapView(
                restaurants = state.restaurants,
                selectedRestaurantId = state.selectedRestaurant?.id,
                userLocation = userLocation,
                isLocationEnabled = hasLocationPermission,
                recenterTrigger = recenterTrigger,
                onRestaurantSelected = { onEvent(MapEvent.SelectRestaurant(it)) },
                onMapTap = { onEvent(MapEvent.ClearSelection) },
                onMapIdle = { lat, lng, radius -> onEvent(MapEvent.MapIdle(lat, lng, radius)) },
                modifier = Modifier.fillMaxSize(),
                bottomPadding = mapBottomPadding,
            )

            AnimatedVisibility(
                visible = !isCardMode,
                enter = slideInVertically { it },
                exit = slideOutVertically { it },
                modifier = Modifier.align(Alignment.BottomCenter),
            ) {
                DraggableBottomSheet(sheetOffset, anchors, sheetHeight = sheetHeight) {
                    RestaurantListSheet(
                        restaurants = state.restaurants,
                        selectedRestaurantId = state.selectedRestaurant?.id,
                        filterState = state.filterState,
                        totalCount = state.allRestaurants.size,
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
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            ) {
                MapSearchBar(
                    filterState = state.filterState,
                    onFilterChange = { onEvent(MapEvent.FilterChanged(it)) },
                    onFilterClick = { filterPanelVisible = true },
                )
            }

            AnimatedVisibility(
                visible = hasLocationPermission && !isCardMode && !isSheetExpanded,
                enter = slideInVertically { it },
                exit = slideOutVertically { it },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = fabBottomPadding),
            ) {
                RecenterFab(onClick = { onEvent(MapEvent.RecenterRequested) })
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
