package com.amitshilo.menudeldia.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amitshilo.menudeldia.di.AppGraphProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DetailViewModel(private val restaurantId: String) : ViewModel() {

    private val useCase = AppGraphProvider.appGraph.getRestaurantDetailUseCase

    private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    init {
        loadDetail()
    }

    private fun loadDetail() {
        viewModelScope.launch {
            _uiState.value = try {
                val restaurant = useCase.getDetail(restaurantId)
                val menu = useCase.getTodayMenu(restaurantId)
                DetailUiState.Success(restaurant, menu)
            } catch (e: Exception) {
                DetailUiState.Error(e.message ?: "Failed to load restaurant")
            }
        }
    }
}
