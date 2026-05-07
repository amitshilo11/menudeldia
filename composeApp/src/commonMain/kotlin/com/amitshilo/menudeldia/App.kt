package com.amitshilo.menudeldia

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.amitshilo.menudeldia.navigation.Screen
import com.amitshilo.menudeldia.ui.detail.RestaurantDetailScreen
import com.amitshilo.menudeldia.ui.map.MapScreen
import com.amitshilo.menudeldia.ui.theme.MenuTheme

@Composable
fun App() {
    MenuTheme {
        val navController = rememberNavController()
        NavHost(navController = navController, startDestination = Screen.Map.route) {
            composable(Screen.Map.route) {
                MapScreen()
            }
            composable(
                route = Screen.RestaurantDetail.route,
                arguments = listOf(navArgument("id") { type = NavType.StringType }),
            ) { backStackEntry ->
                val id = backStackEntry.savedStateHandle.get<String>("id") ?: return@composable
                RestaurantDetailScreen(restaurantId = id, navController = navController)
            }
        }
    }
}
