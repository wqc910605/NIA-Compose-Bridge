package com.nia.compose.bridge.feature.search.impl

import com.nia.compose.bridge.core.mvi.UiEffect
import com.nia.compose.bridge.core.mvi.UiState
import com.nia.compose.bridge.core.model.DemoItem

data class SearchData(
    val query: String = "",
    val results: List<DemoItem> = emptyList(),
    val isSearching: Boolean = false,
)

sealed interface SearchUiState : UiState {
    data object Idle : SearchUiState
    data object Searching : SearchUiState
    data class Empty(val query: String) : SearchUiState
    data class Success(val data: SearchData) : SearchUiState
    data class Error(val message: String?) : SearchUiState
}

sealed interface SearchEffect : UiEffect {
    data class ShowToast(val message: String) : SearchEffect
}
