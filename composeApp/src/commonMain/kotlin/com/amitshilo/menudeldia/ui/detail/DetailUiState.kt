package com.amitshilo.menudeldia.ui.detail

import com.amitshilo.menudeldia.domain.model.Menu
import com.amitshilo.menudeldia.domain.model.Restaurant

sealed class DetailUiState {
    data object Loading : DetailUiState()
    data class Success(
        val restaurant: Restaurant,
        val menu: Menu?,
    ) : DetailUiState()
    data class Error(val message: String) : DetailUiState()
}
