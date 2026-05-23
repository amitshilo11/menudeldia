package com.amitshilo.menudeldia.ui.map

sealed class MapEffect {
    data object RecenterOnUser : MapEffect()
}