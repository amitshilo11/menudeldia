package com.amitshilo.menudeldia.ui.map

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.amitshilo.menudeldia.APP_VERSION_CODE
import com.amitshilo.menudeldia.APP_VERSION_NAME
import com.amitshilo.menudeldia.location.rememberLocationState
import com.amitshilo.menudeldia.navigation.Screen
import com.amitshilo.menudeldia.ui.account.AccountViewModel
import com.amitshilo.menudeldia.ui.drawer.AppDrawerContent
import com.amitshilo.menudeldia.ui.map.components.ErrorState
import com.amitshilo.menudeldia.util.AppLinks
import com.amitshilo.menudeldia.util.rememberUriLauncher
import kotlinx.coroutines.launch


@Composable
fun MapScreen(navController: NavController) {
    val viewModel: MapViewModel = viewModel { MapViewModel() }
    // The account VM is the auth-facing one; the drawer needs the same session state and
    // sign-out path the Account screen uses, so it's reused rather than duplicated here.
    val accountViewModel: AccountViewModel = viewModel { AccountViewModel() }
    val uiState by viewModel.uiState.collectAsState()
    val bestPicks by viewModel.bestPicks.collectAsState()
    val showBestPicks by viewModel.showBestPicks.collectAsState()
    val fetchGeneration by viewModel.fetchGeneration.collectAsState()
    val authState by accountViewModel.authState.collectAsState()
    val locationState = rememberLocationState()

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val drawerScope = rememberCoroutineScope()
    val uriLauncher = rememberUriLauncher()

    // Every row closes the drawer before it acts — letting a navigation start while the sheet
    // is still sliding leaves the scrim painted over the destination on iOS.
    fun closeDrawerThen(action: () -> Unit) {
        drawerScope.launch {
            drawerState.close()
            action()
        }
    }

    LaunchedEffect(locationState.location) {
        viewModel.onEvent(MapEvent.LocationChanged(locationState.location))
    }

    // Skip the very first ON_RESUME — it fires as soon as this effect is registered (the
    // screen is already resumed on cold start) and the ViewModel already loads on init.
    var isFirstResume by remember { mutableStateOf(true) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        if (isFirstResume) {
            isFirstResume = false
        } else {
            viewModel.onEvent(MapEvent.Refresh)
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        // Edge swipes belong to the map, so the drawer only opens from the search-bar button.
        // Once open, the swipe is free to close it again.
        gesturesEnabled = drawerState.isOpen,
        drawerContent = {
            AppDrawerContent(
                authState = authState,
                onProfile = { closeDrawerThen { navController.navigate(Screen.Account.route) } },
                onTodaysPicks = { closeDrawerThen(viewModel::showBestPicks) },
                onHowItWorks = {
                    closeDrawerThen { navController.navigate(Screen.HowItWorks.route) }
                },
                onHelpSupport = { closeDrawerThen { uriLauncher.open(AppLinks.SUPPORT) } },
                onPrivacyPolicy = { closeDrawerThen { uriLauncher.open(AppLinks.PRIVACY_POLICY) } },
                onTerms = { closeDrawerThen { uriLauncher.open(AppLinks.TERMS) } },
                onSendFeedback = {
                    closeDrawerThen {
                        uriLauncher.open(
                            AppLinks.feedbackMailto(
                                "Menudiz feedback (v$APP_VERSION_NAME build $APP_VERSION_CODE)"
                            )
                        )
                    }
                },
                onLogOut = { closeDrawerThen(accountViewModel::signOut) },
                onSignIn = { closeDrawerThen { navController.navigate(Screen.Login.route) } },
            )
        },
    ) {
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
                isDrawerOpen = drawerState.isOpen,
                onOpenDrawer = { drawerScope.launch { drawerState.open() } },
                onCloseDrawer = { drawerScope.launch { drawerState.close() } },
                onDismissBestPicks = viewModel::dismissBestPicks,
                onNavigateToDetail = {
                    navController.navigate(Screen.RestaurantDetail.createRoute(it))
                },
            )
        }
    }
}
