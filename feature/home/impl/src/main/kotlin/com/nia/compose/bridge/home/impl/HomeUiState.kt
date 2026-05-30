package com.nia.compose.bridge.home.impl

import com.nia.compose.bridge.common.UiEffect
import com.nia.compose.bridge.common.UiState
import com.nia.compose.bridge.model.DemoItem

data class HomeData(
    val items: List<DemoItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)

sealed interface HomeUiState : UiState {
    data object Loading : HomeUiState
    data object Empty : HomeUiState
    data class Error(val message: String?) : HomeUiState
    data class Success(val data: HomeData) : HomeUiState
}

sealed interface HomeEffect : UiEffect {
    data class ShowToast(val message: String) : HomeEffect
    data object NavigateToSettings : HomeEffect
    data object NavigateToSearch : HomeEffect
    data object NavigateToBookmarks : HomeEffect
    data object NavigateToInterests : HomeEffect
}
