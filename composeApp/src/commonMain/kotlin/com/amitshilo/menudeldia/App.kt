package com.amitshilo.menudeldia

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.amitshilo.menudeldia.domain.auth.model.AuthState
import com.amitshilo.menudeldia.navigation.Screen
import com.amitshilo.menudeldia.ui.account.AccountScreen
import com.amitshilo.menudeldia.ui.auth.LoginScreen
import com.amitshilo.menudeldia.ui.detail.RestaurantDetailScreen
import com.amitshilo.menudeldia.ui.map.MapScreen
import com.amitshilo.menudeldia.ui.root.RootViewModel
import com.amitshilo.menudeldia.ui.theme.MenuTheme

@Composable
fun App() {
    MenuTheme {
        val rootVm: RootViewModel = viewModel { RootViewModel() }
        val authState by rootVm.authState.collectAsState()
        val navController = rememberNavController()

        LaunchedEffect(authState) {
            when (authState) {
                AuthState.NeedsAuth -> navController.navigate(Screen.Login.route) {
                    popUpTo(0) { inclusive = true }
                }

                is AuthState.Authenticated, AuthState.Guest -> {
                    val dest = navController.currentDestination?.route
                    if (dest == null || dest == Screen.Login.route) {
                        navController.navigate(Screen.Map.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }

                AuthState.Loading -> {}
            }
        }

        NavHost(navController = navController, startDestination = Screen.Login.route) {
            composable(Screen.Login.route) {
                LoginScreen()
            }
            composable(Screen.Map.route) {
                MapScreen(navController = navController)
            }
            composable(
                route = Screen.RestaurantDetail.route,
                arguments = listOf(navArgument("id") { type = NavType.StringType }),
            ) { backStackEntry ->
                val id = backStackEntry.savedStateHandle.get<String>("id") ?: return@composable
                RestaurantDetailScreen(restaurantId = id, navController = navController)
            }
            composable(Screen.Account.route) {
                AccountScreen(navController = navController)
            }
        }
    }
}
