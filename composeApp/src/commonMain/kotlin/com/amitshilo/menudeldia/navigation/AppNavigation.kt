package com.amitshilo.menudeldia.navigation

sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object Map : Screen("map")
    data object RestaurantDetail : Screen("detail/{id}") {
        fun createRoute(id: String) = "detail/$id"
    }
    data object Account : Screen("account")
}
